package uni.gaben.iscat.universe;

import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Vector2;

import uni.gaben.iscat.controller.game.GameInputsHandler;
import uni.gaben.iscat.universe.entity.brain.Brain;
import uni.gaben.iscat.universe.camera.CameraModel;
import uni.gaben.iscat.universe.entity.special.worm.IscatWormSegment;
import uni.gaben.iscat.universe.entity.enviroment.asteroid.AsteroidModel;
import uni.gaben.iscat.universe.entity.AbstractEntityModel;
import uni.gaben.iscat.universe.entity.projectiles.AbstractProjectileModel;
import uni.gaben.iscat.universe.entity.LivingEntityModel;
import uni.gaben.iscat.universe.entity.brain.IEntityController;
import uni.gaben.iscat.universe.entity.HasTerminalVelocity;
import uni.gaben.iscat.universe.entity.player.PlayerController;
import uni.gaben.iscat.universe.entity.player.PlayerModel;
import uni.gaben.iscat.utils.Updatable;
import uni.gaben.iscat.utils.Updatable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
        PlayerModel player = universeModel.getPlayer();

        syncPlayerController(player);
        processPlayerInputs(player, inputs, camera, dt);
        updateProjectiles(camera, dt);
        updateEntities(dt);
        updateAI(dt);
        applyTerminalVelocityLimits();
        syncWormSegmentRotations();

        universeModel.stepPhysics(dt);

        processEntityCleanup(player);
        updateCamera(player, camera, dt);
    }

    // -------------------------------------------------------------------------
    // Private update steps
    // -------------------------------------------------------------------------

    private void syncPlayerController(PlayerModel player) {
        if (playerController.getPlayer() != player) {
            playerController.setPlayer(player);
        }
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
        for (IEntityController ctrl : entityControllers) ctrl.update(universeModel, dt);
    }

    /** Clamps every body's speed to its declared terminal velocity. */
    private void applyTerminalVelocityLimits() {
        for (Body body : universeModel.getBodies()) {
            if (body instanceof HasTerminalVelocity entity) {
                double maxSpeed = entity.getTerminalVelocity();
                Vector2 vel = body.getLinearVelocity();
                if (vel.getMagnitude() > maxSpeed)
                    body.setLinearVelocity(vel.getNormalized().setMagnitude(maxSpeed));
            }
        }
    }

    /**
     * Aligns non-head worm segments to face their movement direction.
     * The head is handled by its own AI brain.
     */
    private void syncWormSegmentRotations() {
        for (Body body : universeModel.getBodies()) {
            if (body instanceof IscatWormSegment seg && seg.getType() != IscatWormSegment.Type.HEAD) {
                Vector2 vel = body.getLinearVelocity();
                if (vel.getMagnitudeSquared() > 0.01)
                    body.getTransform().setRotation(vel.getDirection());
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

        for (AbstractProjectileModel p : universeModel.getProjectiles()) {
            if (p.shouldRemove()) continue;
            double px = UU.mToPx(p.getTransform().getTranslationX());
            double py = UU.mToPx(p.getTransform().getTranslationY());
            if (px < left || px > right || py < top || py > bottom) p.kill(true);
        }
    }

    /**
     * Identifies dead or marked entities, awards XP/score to the player,
     * increments kill stats in the DB, then removes them from the world.
     */
    private void processEntityCleanup(PlayerModel player) {
        List<AbstractEntityModel> toRemove = new ArrayList<>();

        for (AbstractEntityModel entity : universeModel.getEntities()) {
            if (entity == null) continue;

            boolean remove = entity.shouldRemove();
            if (!remove && entity instanceof LivingEntityModel living) {
                if (living.getLife() <= 0) {
                    if (!living.shouldRemove()) living.kill();
                    remove = living.shouldRemove();
                }
            }

            if (remove) {
                toRemove.add(entity);
                if (entity instanceof LivingEntityModel living && living != player && entityDeathListener != null) {
                    entityDeathListener.onEntityDied(entity, living.isKilledByProjectile());
                }
            }
        }

        for (AbstractEntityModel entity : toRemove) {
            universeModel.removeEntity(entity);
            entityControllers.removeIf(
                    ctrl -> ctrl instanceof Brain<?> b && b.getEntity() == entity);
            if (entity instanceof uni.gaben.iscat.universe.entity.projectiles.Projectile p) {
                uni.gaben.iscat.universe.entity.projectiles.ProjectilePool.release(p);
            }
        }
    }

    /**
     * Updates the camera spring targets using the player's position and velocity,
     * adding a small look-ahead in the movement direction.
     */
    private void updateCamera(PlayerModel player, CameraModel camera, double dt) {
        if (player != null) {
            double px  = UU.mToPx(player.getTransform().getTranslationX());
            double py  = UU.mToPx(player.getTransform().getTranslationY());
            double mag = player.getLinearVelocity().getMagnitude();
            double rot = player.getTransform().getRotationAngle();
            camera.getSpringX().setTarget(px + Math.sin(rot) * mag);
            camera.getSpringY().setTarget(py + Math.cos(rot) * mag);
        }
        camera.getSpringX().update(dt);
        camera.getSpringY().update(dt);
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    public void addEntityController(IEntityController controller) {
        entityControllers.add(controller);
    }

    public UniverseModel       getUniverseModel()     { return universeModel; }

}
