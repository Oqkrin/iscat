package uni.gaben.iscat.universe;

import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.controller.game.GameInputsHandler;
import uni.gaben.iscat.universe.camera.CameraController;
import uni.gaben.iscat.universe.entities.AbstractLivingEntityModel;
import uni.gaben.iscat.universe.entities.EntityDeathListener;
import uni.gaben.iscat.universe.entities.parsed.EntityModel;
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
import uni.gaben.iscat.utils.EntityAudioManager;
import uni.gaben.iscat.utils.SessionScoreTracker;
import uni.gaben.iscat.utils.Updatable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Controller principale dell'universo di gioco.
 * Gestisce l'aggiornamento di fisica, IA, proiettili, culling e camera in modalità zero-allocation.
 */
public class UniverseController {

    private final UniverseModel           universeModel;
    private final PlayerController        playerController;
    private final CameraController        cameraController  = new CameraController();
    private final List<IEntityController> entityControllers = new ArrayList<>();

    private EntityDeathListener entityDeathListener;

    // --- Buffer pre-allocati contro la Garbage Collection nel Game Loop ---
    private final List<AbstractPhysicalEntityModel> entitySnapshot    = new ArrayList<>(256);
    private final List<AbstractPhysicalEntityModel> toRemoveBuffer    = new ArrayList<>(64);
    private final Set<AbstractPhysicalEntityModel>  toRemoveSet       = new HashSet<>(64);
    private final List<IEntityController>           aiSnapshot        = new ArrayList<>(64);
    private final List<AbstractPhysicalProjectileModel> projSnapshot  = new ArrayList<>(512);

    private double lastPlayerHealth = -1.0;

    public UniverseController(UniverseModel universeModel) {
        this.universeModel    = universeModel;
        this.playerController = new PlayerController(universeModel.getPlayer());
    }

    public void setEntityDeathListener(EntityDeathListener listener) {
        this.entityDeathListener = listener;
    }

    /**
     * Esegue il tick di aggiornamento principale del mondo di gioco.
     */
    public void updatev(double dt, GameInputsHandler inputs, CameraModel camera) {
        PlayerModel player = universeModel.getPlayer();
        if (player == null || player.shouldRemove()) return;

        syncPlayerController(player);
        processPlayerInputs(player, inputs, camera, dt);
        processPhysicalBodies(dt);
        updateAI(dt);

        universeModel.stepPhysics(dt);
        universeModel.updateSparks(dt);
        updateProjectiles(camera);
        processEntityCleanup(player);
        updateCamera(player, camera, inputs, dt);
    }

    private void syncPlayerController(PlayerModel player) {
        if (playerController.getPlayer() != player) {
            playerController.setPlayer(player);
        }
    }

    private void processPlayerInputs(PlayerModel player, GameInputsHandler inputs, CameraModel camera, double dt) {
        if (player == null) return;
        playerController.processInput(inputs, camera, dt);
    }

    /**
     * Processa i corpi fisici: aggiornamento, velocità terminale e confinamento radiale dell'universo.
     */
    private void processPhysicalBodies(double dt) {
        double radius = universeModel.getUniverseRadius();
        double radiusSq = radius * radius;

        for (Body body : universeModel.getBodies()) {
            if (body instanceof Updatable u) {
                u.update(dt);
            }

            // Limite velocità terminale ottimizzato senza Math.sqrt()
            if (body instanceof Dynamic entity) {
                double maxSpeed = entity.getTerminalVelocity();
                Vector2 vel = body.getLinearVelocity();
                double speedSq = vel.x * vel.x + vel.y * vel.y;
                double maxSq   = maxSpeed * maxSpeed;
                if (speedSq > maxSq) {
                    double scale = maxSpeed / Math.sqrt(speedSq);
                    vel.x *= scale; vel.y *= scale;
                }
            }

            // Vincolo confini dell'universo circolare (esclusi proiettili ed asteroidi)
            if (radius > 0 && !(body instanceof AbstractPhysicalProjectileModel) && !(body instanceof AsteroidModel)) {
                Vector2 pos = body.getTransform().getTranslation();
                double distSq = pos.x * pos.x + pos.y * pos.y;

                if (distSq > radiusSq) {
                    double dist = Math.sqrt(distSq);
                    double nx = pos.x / dist; double ny = pos.y / dist;

                    body.getTransform().setTranslation(nx * radius, ny * radius);

                    Vector2 vel = body.getLinearVelocity();
                    double dot = vel.x * nx + vel.y * ny;
                    if (dot > 0) {
                        vel.x -= nx * dot; vel.y -= ny * dot;
                    }
                }
            }
        }
    }

    private void updateAI(double dt) {
        aiSnapshot.clear();
        aiSnapshot.addAll(entityControllers);
        for (IEntityController ctrl : aiSnapshot) {
            if (ctrl instanceof Brain<?> b && b.getEntity().shouldRemove()) continue;
            ctrl.update(universeModel, dt);
        }
    }

    /**
     * Esegue il culling dei proiettili fuori dal raggio della telecamera (Viewport).
     */
    private void updateProjectiles(CameraModel camera) {
        double zoom   = camera.getZoom();
        double left   = camera.getViewportLeftX()  - 200.0;
        double right  = left + (camera.getScreenWidth()  / zoom) + 400.0;
        double top    = camera.getViewportTopY()   - 200.0;
        double bottom = top  + (camera.getScreenHeight() / zoom) + 400.0;

        projSnapshot.clear();
        projSnapshot.addAll(universeModel.getProjectiles());
        for (AbstractPhysicalProjectileModel p : projSnapshot) {
            if (p.shouldRemove()) continue;
            double px = UU.mToPx(p.getTransform().getTranslationX());
            double py = UU.mToPx(p.getTransform().getTranslationY());
            if (px < left || px > right || py < top || py > bottom) {
                p.extinguish(true);
            }
        }
    }

    /**
     * Pulisce le entità rimosse e i rispettivi controller in un'unica passata $O(N)$.
     */
    private void processEntityCleanup(PlayerModel player) {
        entitySnapshot.clear();
        entitySnapshot.addAll(universeModel.getEntities());
        toRemoveBuffer.clear();
        toRemoveSet.clear();

        for (AbstractPhysicalEntityModel entity : entitySnapshot) {
            if (entity == null) continue;

            boolean remove = entity.shouldRemove();
            if (!remove && entity instanceof AbstractLivingEntityModel living) {
                if (living.getEndurance() <= 0) {
                    if (!living.shouldRemove()) living.extinguish();
                    remove = living.shouldRemove();
                }
            }

            if (remove) {
                toRemoveBuffer.add(entity);
                toRemoveSet.add(entity);
                if (entity instanceof AbstractLivingEntityModel living && living != player) {
                    if (entityDeathListener != null) {
                        entityDeathListener.onEntityDied(entity, living.isKilledByProjectile());
                    }
                    handleEntityDeathEffects(living);
                }
            }
        }

        if (!toRemoveBuffer.isEmpty()) {
            for (AbstractPhysicalEntityModel entity : toRemoveBuffer) {
                universeModel.removeEntity(entity);
                if (entity instanceof ProjectileModel p) {
                    ProjectilePool.release(p);
                }
            }
            entityControllers.removeIf(ctrl -> ctrl instanceof Brain<?> b && toRemoveSet.contains(b.getEntity()));
        }
    }

    /**
     * Gestisce gli effetti sonori e i drop (es. BlackHole o Cuori) alla morte delle entità.
     */
    private void handleEntityDeathEffects(AbstractLivingEntityModel entity) {
        if (entity instanceof EntityModel enemy) {
            EntityAudioManager.playEventAudio(enemy, "death");
        }

        // Spawn stocastico BlackHole alla distruzione del sole
        if ("iscat_sun".equals(entity.getEntityRecord().entityKey()) && Math.random() < 0.5) {
            UniverseSpawner.getInstance().spawn(UniverseSpawnable.BLACKHOLE,
                    entity.getTransform().getTranslationX(),
                    entity.getTransform().getTranslationY());
        }

        // Calcolo e spawn stocastico del drop dei cuori (25% di probabilità)
        if (!(entity instanceof PlayerModel)
                && !(entity instanceof HeartModel)
                && !(entity instanceof ProjectileModel)
                && (entity.isKilledByProjectile() || entity.isKilledByMeele())) {

            SessionScoreTracker.getInstance().addDeaths(1);
            if (Math.random() < 0.25) {
                double px = UU.mToPx(entity.getTransform().getTranslationX());
                double py = UU.mToPx(entity.getTransform().getTranslationY());
                UniverseSpawner.getInstance().spawn("HEART", px, py);
            }
        }
    }

    /**
     * Aggiorna lo stato, gli shock visivi e il tracking della telecamera sul giocatore.
     */
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

    public void addEntityController(IEntityController controller) { entityControllers.add(controller); }
    public UniverseModel    getUniverseModel()    { return universeModel; }
    public PlayerController getPlayerController() { return playerController; }
}