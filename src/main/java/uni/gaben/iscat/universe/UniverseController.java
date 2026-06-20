package uni.gaben.iscat.universe;

import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Vector2;

import uni.gaben.iscat.controller.game.GameInputsHandler;
import uni.gaben.iscat.universe.camera.CameraController;
import uni.gaben.iscat.universe.camera.CameraSettings;
import uni.gaben.iscat.universe.entities.AbstractLivingEntityModel;
import uni.gaben.iscat.universe.entities.EntityDeathListener;
import uni.gaben.iscat.universe.entities.EntityModel;
import uni.gaben.iscat.universe.entities.brain.Brain;
import uni.gaben.iscat.universe.camera.CameraModel;
import uni.gaben.iscat.universe.entities.hardcoded.asteroid.AsteroidModel;
import uni.gaben.iscat.universe.entities.hardcoded.heart.HeartModel;
import uni.gaben.iscat.universe.entities.hardcoded.projectiles.AbstractPhysicalProjectileModel;
import uni.gaben.iscat.universe.entities.hardcoded.projectiles.ProjectileModel;
import uni.gaben.iscat.universe.entities.hardcoded.projectiles.ProjectilePool;
import uni.gaben.iscat.universe.entities.AbstractPhysicalEntityModel;
import uni.gaben.iscat.universe.entities.brain.IEntityController;
import uni.gaben.iscat.universe.entities.interfaces.Dynamic;
import uni.gaben.iscat.universe.entities.player.PlayerController;
import uni.gaben.iscat.universe.entities.player.PlayerModel;
import uni.gaben.iscat.universe.spawn.UniverseSpawnable;
import uni.gaben.iscat.universe.spawn.UniverseSpawner;
import uni.gaben.iscat.universe.spawn.waves.UniverseWaveController;
import uni.gaben.iscat.utils.EntityAudioManager;
import uni.gaben.iscat.utils.SessionScoreTracker;
import uni.gaben.iscat.utils.Updatable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Master controller for the game universe.
 */
public class UniverseController {

    private final UniverseModel           universeModel;
    private final PlayerController        playerController;
    private final CameraController        cameraController  = new CameraController();
    private final List<IEntityController> entityControllers = new ArrayList<>();

    private EntityDeathListener entityDeathListener;

    public UniverseController(UniverseModel universeModel) {
        this.universeModel    = universeModel;
        this.playerController = new PlayerController(universeModel.getPlayer());
    }

    public void setEntityDeathListener(EntityDeathListener listener) {
        this.entityDeathListener = listener;
    }

    // -------------------------------------------------------------------------
    // Main tick
    // -------------------------------------------------------------------------

    public void updatev(double dt, GameInputsHandler inputs, CameraModel camera) {
        PlayerModel player = universeModel.getPlayer();
        if (player == null || player.shouldRemove()) return;

        syncPlayerController(player);
        processPlayerInputs(player, inputs, camera, dt);
        updateEntities(dt);
        updateAI(dt);

        // Applica i limiti fisici prima di avanzare con lo step del motore di dyn4j
        applyTerminalVelocityLimits();
        applyCircularBoundaries();

        universeModel.stepPhysics(dt);
        universeModel.updateSparks(dt);
        updateProjectiles(camera, dt);
        processEntityCleanup(player, dt);
        updateCamera(player, camera, inputs, dt);
    }

    // -------------------------------------------------------------------------
    // Private update steps
    // -------------------------------------------------------------------------

    private void syncPlayerController(PlayerModel player) {
        if (playerController.getPlayer() != player) playerController.setPlayer(player);
    }

    private void processPlayerInputs(PlayerModel player, GameInputsHandler inputs,
                                     CameraModel camera, double dt) {
        if (player == null) return;
        playerController.processInput(inputs, camera, dt);
    }

    private void updateEntities(double dt) {
        for (Body body : universeModel.getBodies()) {
            if (body instanceof Updatable u) u.update(dt);
        }
    }

    private void updateAI(double dt) {
        List<IEntityController> copy = new ArrayList<>(entityControllers);
        for (IEntityController ctrl : copy) {
            if (ctrl instanceof Brain<?> b && b.getEntity().shouldRemove()) continue;
            ctrl.update(universeModel, dt);
        }
    }

    private void applyTerminalVelocityLimits() {
        for (Body body : universeModel.getBodies()) {
            if (body instanceof Dynamic entity) {
                double maxSpeed = entity.getTerminalVelocity();
                Vector2 vel = body.getLinearVelocity();
                if (vel.getMagnitude() > maxSpeed) body.getLinearVelocity().setMagnitude(maxSpeed);
            }
        }
    }

    private void applyCircularBoundaries() {
        double radius = universeModel.getUniverseRadius();
        if (radius <= 0) return;

        for (Body body : universeModel.getBodies()) {
            if (body instanceof AbstractPhysicalProjectileModel || body instanceof AsteroidModel) {
                continue;
            }

            Vector2 pos = body.getTransform().getTranslation();
            double distance = pos.getMagnitude();

            if (distance > radius) {
                // Calcoliamo la normale di rimbalzo/congelamento verso il centro
                Vector2 normal = pos.getNormalized();

                // Forza la posizione sul perimetro esatto del cerchio
                body.getTransform().setTranslation(normal.x * radius, normal.y * radius);

                // Annulla o devia il vettore velocità proiettato al di fuori del bordo
                Vector2 vel = body.getLinearVelocity();
                double dotProduct = vel.dot(normal);
                if (dotProduct > 0) {
                    vel.subtract(normal.product(dotProduct));
                }
            }
        }
    }

    private void updateProjectiles(CameraModel camera, double dt) {
        double zoom   = camera.getZoom();
        double left   = camera.getViewportLeftX()  - 200.0;
        double right  = left + (camera.getScreenWidth()  / zoom) + 400.0;
        double top    = camera.getViewportTopY()   - 200.0;
        double bottom = top  + (camera.getScreenHeight() / zoom) + 400.0;

        List<AbstractPhysicalProjectileModel> snapshot = new ArrayList<>(universeModel.getProjectiles());
        for (AbstractPhysicalProjectileModel p : snapshot) {
            if (p.shouldRemove()) continue;
            double px = UU.mToPx(p.getTransform().getTranslationX());
            double py = UU.mToPx(p.getTransform().getTranslationY());
            if (px < left || px > right || py < top || py > bottom) p.extinguish(true);
        }
    }

    /**
     * Identifica le entità morte, notifica il listener, incrementa le kill
     * solo per nemici reali (filtraggio in {@link UniverseWaveController#incrementKills}),
     * poi rimuove i corpi dal mondo fisico.
     */
    private void processEntityCleanup(PlayerModel player, double dt) {
        List<AbstractPhysicalEntityModel> toRemove = new ArrayList<>();

        // Snapshot to avoid ConcurrentModificationException
        List<AbstractPhysicalEntityModel> snapshot = new ArrayList<>(universeModel.getEntities());
        for (AbstractPhysicalEntityModel entity : snapshot) {
            if (entity == null) continue;

            boolean remove = entity.shouldRemove();
            if (!remove && entity instanceof AbstractLivingEntityModel living) {
                if (living.getEndurance() <= 0) {
                    if (!living.shouldRemove()) living.extinguish();
                    remove = living.shouldRemove();
                }
            }

            if (remove) {
                toRemove.add(entity);
                if (entity instanceof AbstractLivingEntityModel living && living != player) {
                    // Notify external listener (e.g., for wave controller)
                    if (entityDeathListener != null) {
                        entityDeathListener.onEntityDied(entity, living.isKilledByProjectile());
                    }

                    // Apply death side effects (audio, heart spawn, etc.)
                    handleEntityDeathEffects(living);
                }
            }
        }

        // Now safely remove all entities in toRemove
        for (AbstractPhysicalEntityModel entity : toRemove) {
            universeModel.removeEntity(entity);
            entityControllers.removeIf(ctrl -> ctrl instanceof Brain<?> b && b.getEntity() == entity);
            if (entity instanceof ProjectileModel p) ProjectilePool.release(p);
        }
    }

    /**
     * Handles all side effects when a living entity dies.
     * This includes audio, heart drops, and score/kill tracking.
     */
    private void handleEntityDeathEffects(AbstractLivingEntityModel entity) {
        if (entity instanceof EntityModel enemy) {
            EntityAudioManager.playEventAudio(enemy, "death");
        }
        if(entity.getEntityRecord().entityKey().equals("iscat_sun") && Math.random() < 0.5) {
                UniverseSpawner.getInstance().spawn(UniverseSpawnable.BLACKHOLE, entity.getTransform().getTranslationX(), entity.getTransform().getTranslationY());
        }


        if (!(entity instanceof PlayerModel) && !(entity instanceof HeartModel) && !(entity instanceof ProjectileModel) && (entity.isKilledByProjectile() || entity.isKilledByMeele())) {
            SessionScoreTracker.getInstance().addDeaths(1);
            if (Math.random() < 0.25) {
                double px = UU.mToPx(entity.getTransform().getTranslationX());
                double py = UU.mToPx(entity.getTransform().getTranslationY());
                UniverseSpawner.getInstance().spawn("HEART", px, py);
            }
        }
    }

    // ── Camera ────────────────────────────────────────────────────────────────

    private double lastPlayerHealth = -1.0;

    private void updateCamera(PlayerModel player, CameraModel camera, GameInputsHandler inputs, double dt) {
        if (player != null) {
            double currentHealth = player.getEndurance();
            if (lastPlayerHealth < 0) {
                lastPlayerHealth = currentHealth;
            } else if (currentHealth < lastPlayerHealth) {
                camera.triggerHurtEffects(1.0);
                lastPlayerHealth = currentHealth;
            } else {
                lastPlayerHealth = currentHealth;
            }

            camera.updateEffects(dt);
            camera.setActualZoom(camera.getBaseZoom());

            double px = UU.mToPx(player.getTransform().getTranslationX());
            double py = UU.mToPx(player.getTransform().getTranslationY());

            double screenCenterX = camera.getScreenWidth()  / 2.0;
            double screenCenterY = camera.getScreenHeight() / 2.0;
            double updatedZoom   = camera.getZoom();

            double mouseWorldX = camera.getX() + (inputs.mouseX - screenCenterX) / updatedZoom;
            double mouseWorldY = camera.getY() + (inputs.mouseY - screenCenterY) / updatedZoom;

            cameraController.update(camera, px, py,
                    camera.getScreenWidth(), camera.getScreenHeight(),
                    mouseWorldX, mouseWorldY, dt);
        } else {
            camera.getSpringX().update(dt);
            camera.getSpringY().update(dt);
            camera.updateEffects(dt);
        }
    }

    private static double getDynamicZoom(PlayerModel player, CameraModel camera, double dt) {
        double speed        = player.getLinearVelocity().getMagnitude();
        double maxVel       = UniverseVelocitySettings.PLAYER_MAX_VELOCITY * 2;
        double speedRatio   = Math.clamp(speed / maxVel, 0.0, 1.0);
        double dynamicMod   = speedRatio * CameraSettings.MAX_ZOOM_OUT_MODIFIER;
        double targetZoom   = camera.getBaseZoom() - dynamicMod;
        double currentZoom  = camera.getZoom();
        return currentZoom + (targetZoom - currentZoom) * (1.0 - Math.exp(-CameraSettings.ZOOM_SMOOTHING_SPEED * dt));
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    public void addEntityController(IEntityController controller) { entityControllers.add(controller); }
    public UniverseModel    getUniverseModel()    { return universeModel; }
    public PlayerController getPlayerController() { return playerController; }
}