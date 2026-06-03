package uni.gaben.iscat.universe;

import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Vector2;

import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.database.dao.EnemyDAO;
import uni.gaben.iscat.database.dao.ScoreDAO;
import uni.gaben.iscat.controller.game.GameInputsHandler;
import uni.gaben.iscat.model.user.SessionUser;
import uni.gaben.iscat.universe.entity.brain.Brain;
import uni.gaben.iscat.universe.camera.CameraModel;
import uni.gaben.iscat.universe.entity.enemies.worm.IscatWormSegment;
import uni.gaben.iscat.universe.entity.enviroment.asteroid.AsteroidModel;
import uni.gaben.iscat.universe.entity.AbstractEntityModel;
import uni.gaben.iscat.universe.entity.projectiles.AbstractProjectileModel;
import uni.gaben.iscat.universe.entity.LivingEntityModel;
import uni.gaben.iscat.universe.entity.brain.IEntityController;
import uni.gaben.iscat.universe.interfaces.HasTerminalVelocity;
import uni.gaben.iscat.universe.entity.player.PlayerController;
import uni.gaben.iscat.universe.entity.player.PlayerModel;
import uni.gaben.iscat.utils.Cooldown;
import uni.gaben.iscat.utils.SessionManager;
import uni.gaben.iscat.utils.SessionScoreTracker;
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

    private static final double ASTEROID_SPAWN_INTERVAL = 3.0;
    private static final int    MAX_ACTIVE_ASTEROIDS    = 30;

    private final UniverseModel       universeModel;
    private final PlayerController    playerController;
    private final List<IEntityController> entityControllers = new ArrayList<>();
    private final Cooldown asteroidCooldown = new Cooldown();
    private final Random   random = new Random();

    private final ScoreDAO scoreDAO;
    private final EnemyDAO enemyDAO;

    public UniverseController(UniverseModel universeModel) {
        this.universeModel   = universeModel;
        this.scoreDAO        = IscatDB.getInstance().getScoreDAO();
        this.enemyDAO        = IscatDB.getInstance().getEnemyDAO();
        this.playerController = new PlayerController(universeModel.getPlayer());
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
        spawnAsteroids(dt, player);
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
            p.deltaToLife(-dt);
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
                if (entity instanceof LivingEntityModel living
                        && living != player
                        && living.getXpReward() > 0
                        && player != null) {

                    String key      = living.getEntityKey();
                    String cleanKey = key != null ? key.toLowerCase().trim() : "";
                    // Healer and Master bypass the projectile-kill requirement
                    boolean isSpecial = cleanKey.equals("iscat_healer") || cleanKey.equals("iscat_master");

                    if (living.isKilledByProjectile() || isSpecial) {
                        player.addXp(living.getXpReward());
                        SessionScoreTracker.getInstance().addScore((int) living.getXpReward());

                        SessionUser user = SessionManager.getInstance().getCurrentUser();
                        if (user != null) {
                            IscatDB.getInstance().executeAsync(
                                    () -> scoreDAO.increment(user.id(), "Deaths", 1));
                            if (!cleanKey.isEmpty()) {
                                IscatDB.getInstance().executeAsync(
                                        () -> enemyDAO.incrementKill(user.id(), cleanKey));
                            }
                        }
                    }
                }
            }
        }

        for (AbstractEntityModel entity : toRemove) {
            universeModel.removeEntity(entity);
            entityControllers.removeIf(
                    ctrl -> ctrl instanceof Brain<?> b && b.getEntity() == entity);
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

    /**
     * Periodically spawns asteroid clusters in a ring around the player,
     * capped at {@link #MAX_ACTIVE_ASTEROIDS}.
     */
    private void spawnAsteroids(double dt, PlayerModel player) {
        asteroidCooldown.update(dt);
        if (!asteroidCooldown.isReady()) return;
        asteroidCooldown.start(ASTEROID_SPAWN_INTERVAL);

        if (universeModel.getEntitiesOfType(AsteroidModel.class).size() >= MAX_ACTIVE_ASTEROIDS
                || player == null) return;

        double px    = UU.mToPx(player.getTransform().getTranslationX());
        double py    = UU.mToPx(player.getTransform().getTranslationY());
        double angle = random.nextDouble() * Math.PI * 2.0;
        double dist  = 900.0 + random.nextDouble() * 600.0;
        double cx    = px + Math.cos(angle) * dist;
        double cy    = py + Math.sin(angle) * dist;
        int    count = 3 + random.nextInt(9);

        for (int i = 0; i < count; i++) {
            double off  = random.nextDouble() * Math.PI * 2.0;
            double r    = random.nextDouble() * 150.0;
            double ax   = cx + Math.cos(off) * r;
            double ay   = cy + Math.sin(off) * r;
            double size = 15.0 + random.nextDouble() * 180.0;
            AsteroidModel ast = new AsteroidModel(ax, ay, size);
            double da    = random.nextDouble() * Math.PI * 2.0;
            double speed = UniverseVelocitySettings.ASTEROID_SPAWN_SPEED_MIN
                    + random.nextDouble() * (UniverseVelocitySettings.ASTEROID_SPAWN_SPEED_MAX
                    - UniverseVelocitySettings.ASTEROID_SPAWN_SPEED_MIN);
            ast.setLinearVelocity(new Vector2(Math.cos(da) * speed, Math.sin(da) * speed));
            UniverseSpawner.getInstance().spawnEntity(ast);
        }
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    public void addEntityController(IEntityController controller) {
        entityControllers.add(controller);
    }

    public UniverseModel       getUniverseModel()     { return universeModel; }
}
