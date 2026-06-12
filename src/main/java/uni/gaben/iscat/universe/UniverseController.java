package uni.gaben.iscat.universe;

import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Vector2;

import uni.gaben.iscat.controller.game.GameInputsHandler;
import uni.gaben.iscat.universe.camera.CameraController;
import uni.gaben.iscat.universe.camera.CameraModel;
import uni.gaben.iscat.universe.camera.CameraSettings;
import uni.gaben.iscat.universe.entity.GameEntity;
import uni.gaben.iscat.universe.entity.brain.Brain;
import uni.gaben.iscat.universe.entity.brain.IEntityController;
import uni.gaben.iscat.universe.entity.interfaces.Dynamic;
import uni.gaben.iscat.universe.entity.modules.EnduranceModule;
import uni.gaben.iscat.universe.entity.player.PlayerController;
import uni.gaben.iscat.universe.entity.projectiles.ProjectilePool;
import uni.gaben.iscat.utils.Updatable;

import java.util.ArrayList;
import java.util.List;

/**
 * Master controller for the game universe.
 *
 * <p>Coordinates every tick: player input, AI, physics step, asteroid spawning,
 * entity cleanup, and camera tracking. The external {@link UniverseWaveController}
 * is managed by {@link uni.gaben.iscat.controller.game.GameController} and
 * is not duplicated here.</p>
 */
public class UniverseController {

    private final UniverseModel       universeModel;
    private final PlayerController    playerController;
    private final CameraController cameraController = new CameraController();
    private final List<IEntityController> entityControllers = new ArrayList<>();
    
    private EntityDeathListener entityDeathListener;

    public UniverseController(UniverseModel universeModel) {
        this.universeModel   = universeModel;
        this.playerController = new PlayerController(universeModel.getPlayer());
    }

    public void setEntityDeathListener(EntityDeathListener listener) {
        this.entityDeathListener = listener;
    }

    // -------------------------------------------------------------------------
    // Main tick
    // -------------------------------------------------------------------------

    /**
     * Full logical update for one game frame.
     *
     * @param dt     seconds since the last frame
     * @param inputs current input snapshot
     * @param camera camera model (for viewport-relative calculations)
     */
    public void updatev(double dt, GameInputsHandler inputs, CameraModel camera) {
        GameEntity player = universeModel.getPlayer();
        if(player == null || player.shouldRemove()) return;

        syncPlayerController(player);
        processPlayerInputs(player, inputs, camera, dt);
        updateProjectiles(camera, dt);
        updateEntities(dt);
        updateAI(dt);
        applyTerminalVelocityLimits();

        universeModel.stepPhysics(dt);

        processEntityCleanup(player);
        updateCamera(player, camera, inputs, dt);
    }

    // -------------------------------------------------------------------------
    // Private update steps
    // -------------------------------------------------------------------------

    private void syncPlayerController(GameEntity player) {
        if (playerController.getPlayer() != player) {
            playerController.setPlayer(player);
        }
    }

    private void processPlayerInputs(GameEntity player, GameInputsHandler inputs,
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

    /** Clamps every body's speed to its declared terminal velocity. */
    private void applyTerminalVelocityLimits() {
        for (Body body : universeModel.getBodies()) {
            if (body instanceof Dynamic entity) {
                double maxSpeed = entity.getTerminalVelocity();
                Vector2 vel = body.getLinearVelocity();
                if (vel.getMagnitude() > maxSpeed)
                    body.getLinearVelocity().setMagnitude(maxSpeed);
            }
        }
    }

    /**
     * Advances projectile lifetimes and immediately destroys any that have
     * left the visible viewport (plus a margin).
     */
    private void updateProjectiles(CameraModel camera, double dt) {
        double zoom   = camera.getZoom();
        double left   = camera.getViewportLeftX()  - 200.0;
        double right  = left + (camera.getScreenWidth()  / zoom) + 400.0;
        double top    = camera.getViewportTopY()   - 200.0;
        double bottom = top  + (camera.getScreenHeight() / zoom) + 400.0;

        for (GameEntity p : universeModel.getProjectiles()) {
            if (p.shouldRemove()) continue;
            double px = UU.mToPx(p.getTransform().getTranslationX());
            double py = UU.mToPx(p.getTransform().getTranslationY());
            if (px < left || px > right || py < top || py > bottom) {
                p.extinguish(true);
            }
        }
    }

    /**
     * Identifies dead or marked entities, awards XP/score to the player,
     * increments kill stats in the DB, then removes them from the world.
     */
    private void processEntityCleanup(GameEntity player) {
        List<GameEntity> toRemove = new ArrayList<>();

        for (GameEntity entity : universeModel.getEntities()) {
            if (entity == null) continue;

            boolean remove = entity.shouldRemove();
            if (!remove && entity.hasModule(EnduranceModule.class)) {
                EnduranceModule em = entity.getModule(EnduranceModule.class);
                if (em.getEndurance() <= 0) {
                    if (!entity.shouldRemove()) em.completeDeath();
                    remove = entity.shouldRemove();
                }
            }

            if (remove) {
                toRemove.add(entity);
                if (entity != player && entityDeathListener != null) {
                    entityDeathListener.onEntityDied(entity, true); //#TODO actual check
                }
            }
        }

        // Also clean up projectiles
        List<GameEntity> projToRemove = new ArrayList<>();
        for (GameEntity p : universeModel.getProjectiles()) {
            if (p.shouldRemove()) {
                projToRemove.add(p);
            }
        }

        for (GameEntity entity : toRemove) {
            universeModel.removeEntity(entity);
            entityControllers.removeIf(
                    ctrl -> ctrl instanceof Brain<?> b && b.getEntity() == entity);
        }
        for (GameEntity p : projToRemove) {
            universeModel.removeEntity(p);
            ProjectilePool.release(p);
        }
    }

    // 1. Add this field at the top of UniverseController.java to track damage changes
    private double lastPlayerHealth = -1.0;

    // 2. Refactor the camera processing loop
    private void updateCamera(GameEntity player, CameraModel camera, GameInputsHandler inputs, double dt) {
        if (player != null) {
            // =====================================================================
            // AUTOMATED DAMAGE DETECTION HOOK
            // =====================================================================
            double currentHealth = player.hasModule(EnduranceModule.class) ? player.getModule(EnduranceModule.class).getEndurance() : 100.0;

            if (lastPlayerHealth < 0) {
                lastPlayerHealth = currentHealth; // Initialize frame cache
            } else if (currentHealth < lastPlayerHealth) {
                // Player's health dropped since last frame -> trigger the hurt indicator effects!
                camera.triggerHurtEffects(1.0);
                lastPlayerHealth = currentHealth;
            } else {
                lastPlayerHealth = currentHealth;
            }

            // Tick internal camera shake and visual overlay decay loops
            camera.updateEffects(dt);

            // =====================================================================
            // SNAPPY VELOCITY-BASED DYNAMIC ZOOM
            // =====================================================================
            double newZoom = getDynamicZoom(player, camera, dt);
            camera.setActualZoom(newZoom);

            // =====================================================================
            // COORDINATE CALCULATION & DELEGATION
            // =====================================================================
            double px = UU.mToPx(player.getTransform().getTranslationX());
            double py = UU.mToPx(player.getTransform().getTranslationY());

            double screenCenterX = camera.getScreenWidth() / 2.0;
            double screenCenterY = camera.getScreenHeight() / 2.0;
            double updatedZoom = camera.getZoom();

            double mouseWorldX = camera.getX() + (inputs.mouseX - screenCenterX) / updatedZoom;
            double mouseWorldY = camera.getY() + (inputs.mouseY - screenCenterY) / updatedZoom;

            cameraController.update(
                    camera, px, py,
                    camera.getScreenWidth(), camera.getScreenHeight(),
                    mouseWorldX, mouseWorldY, dt
            );
        } else {
            camera.getSpringX().update(dt);
            camera.getSpringY().update(dt);
            camera.updateEffects(dt);
        }
    }

    private static double getDynamicZoom(GameEntity player, CameraModel camera, double dt) {
        double speed = player.getLinearVelocity().getMagnitude();
        double maxVel = UniverseVelocitySettings.PLAYER_MAX_VELOCITY*2;

        // Use a quadratic ratio for "high-speed contrast"
        double speedRatio = Math.clamp(speed / maxVel, 0.0, 1.0);
        double dynamicModifier = speedRatio * CameraSettings.MAX_ZOOM_OUT_MODIFIER;

        double targetZoom = camera.getBaseZoom() - dynamicModifier;

        double currentZoom = camera.getZoom();
        return currentZoom + (targetZoom - currentZoom) * (1.0 - Math.exp(-CameraSettings.ZOOM_SMOOTHING_SPEED * dt));
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    public void addEntityController(IEntityController controller) {
        entityControllers.add(controller);
    }

    public UniverseModel       getUniverseModel()     { return universeModel; }

    public PlayerController getPlayerController() {
        return playerController;
    }
}
