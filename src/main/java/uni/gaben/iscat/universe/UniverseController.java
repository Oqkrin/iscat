package uni.gaben.iscat.universe;

import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Vector2;

import uni.gaben.iscat.universe.camera.CameraModel;
import uni.gaben.iscat.universe.enemies.master.IscatMasterModel;
import uni.gaben.iscat.universe.enemies.master.IscatMasterController; // Imported for type-safe cleanup
import uni.gaben.iscat.universe.enemies.worm.IscatWormController;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.lib.abstracts.AbstractProjectileModel;
import uni.gaben.iscat.universe.lib.behaviurs.AiController;
import uni.gaben.iscat.universe.lib.implementations.LivingEntityModel;
import uni.gaben.iscat.universe.lib.interfaces.model.HasTerminalVelocity;
import uni.gaben.iscat.utils.Updatable;

import uni.gaben.iscat.universe.enviroment.asteroid.AsteroidModel;
import uni.gaben.iscat.universe.enemies.worm.IscatWormSegment;
import uni.gaben.iscat.universe.player.PlayerController;
import uni.gaben.iscat.universe.player.PlayerModel;
import uni.gaben.iscat.universe.enviroment.starfield.StarfieldController;

import uni.gaben.iscat.screens.game.controller.GameInputs;

import uni.gaben.iscat.utils.Cooldown;
import uni.gaben.iscat.utils.SessionScoreTracker;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class UniverseController {

    private UniverseModel universeModel;
    private final PlayerController playerController;
    private final List<uni.gaben.iscat.universe.lib.behaviurs.AiController> aiControllers = new ArrayList<>();
    private final StarfieldController starfieldController = new StarfieldController();
    private final UniverseWaveController universeWaveController = new UniverseWaveController();
    private final Cooldown asteroidCooldown = new Cooldown();

    private static final double ASTEROID_SPAWN_INTERVAL = 3.0;
    private static final int MAX_ACTIVE_ASTEROIDS = 30;
    private final Random random = new Random();

    private final List<IscatWormController> wormControllers = new ArrayList<>();

    public void addWormController(IscatWormController wormController) {
        wormControllers.add(wormController);
    }

    public UniverseController(UniverseModel universeModel) {
        this.universeModel = universeModel;
        this.playerController = new PlayerController(universeModel.getPlayer());
    }

    public void updatev(double dt, GameInputs inputs, CameraModel cameraModel) {
        PlayerModel player = universeModel.getPlayer();

        syncPlayerController(player);
        spawnAsteroids(dt, player);
        processPlayerInputs(player, inputs, cameraModel, dt);
        updateProjectiles(cameraModel, dt);
        updateEntities(dt);
        updateAI(dt);
        applyTerminalVelocityLimits();
        syncWormSegments();

        universeModel.stepPhysics(dt);

        processEntityCleanup(player);
        updateCamera(player, cameraModel, dt);
    }

    private void syncPlayerController(PlayerModel player) {
        if (playerController.getPlayer() != player) {
            playerController.setPlayer(player);
        }
    }

    private void processPlayerInputs(PlayerModel player, GameInputs inputs, CameraModel cameraModel, double dt) {
        if (player == null) return;

        playerController.processInput(
                inputs,
                cameraModel.getViewportLeftX(),
                cameraModel.getViewportTopY(),
                dt
        );
    }

    private void updateEntities(double dt) {
        for (Body body : universeModel.getBodies()) {
            if (body instanceof Updatable updatable) {
                updatable.update(dt);
            }
        }
    }

    private void updateAI(double dt) {
        for (AiController ai : new ArrayList<>(aiControllers)) {
            ai.update(universeModel, dt);

            for (IscatWormController w : wormControllers) {
                w.update(universeModel, dt);
            }
        }
    }

    private void applyTerminalVelocityLimits() {
        for (Body body : universeModel.getBodies()) {
            if (body instanceof HasTerminalVelocity entity) {
                double maxSpeed = entity.getTerminalVelocity();
                Vector2 velocity = body.getLinearVelocity();

                if (velocity.getMagnitude() > maxSpeed) {
                    body.setLinearVelocity(
                            velocity.getNormalized().setMagnitude(maxSpeed)
                    );
                }
            }
        }
    }

    private void syncWormSegments() {
        for (Body body : universeModel.getBodies()) {
            if (body instanceof IscatWormSegment segment && segment.getType() != IscatWormSegment.Type.HEAD) {
                Vector2 velocity = body.getLinearVelocity();
                if (velocity.getMagnitudeSquared() > 0.01) {
                    body.getTransform().setRotation(velocity.getDirection());
                }
            }
        }
    }

    private void updateProjectiles(CameraModel cameraModel, double dt) {
        double left = cameraModel.getViewportLeftX() - 200.0;
        double right = cameraModel.getViewportLeftX() + cameraModel.getScreenWidth() + 200.0;
        double top = cameraModel.getViewportTopY() - 200.0;
        double bottom = cameraModel.getViewportTopY() + cameraModel.getScreenHeight() + 200.0;

        for (AbstractProjectileModel p : new ArrayList<>(universeModel.getProjectiles())) {
            p.deltaToLife(-dt);

            if (p.shouldRemove()) continue;

            double px = UU.mToPx(p.getTransform().getTranslationX());
            double py = UU.mToPx(p.getTransform().getTranslationY());

            if (px < left || px > right || py < top || py > bottom) {
                p.kill(true);
            }
        }
    }

    /**
     * CLEANED AND FIXED: Thread-safe, main-thread synchronized cleanup strategy.
     * Completely purges the erratic background virtual threads and frees memory leaks.
     */
    private void processEntityCleanup(PlayerModel player) {
        List<AbstractEntityModel> toRemove = new ArrayList<>();

        for (AbstractEntityModel entity : new ArrayList<>(universeModel.getEntities())) {
            if (entity == null) continue;

            boolean shouldRemove = false;

            if (entity.shouldRemove()) {
                shouldRemove = true;
            } else if (entity instanceof LivingEntityModel living) {
                if (living.getLife() <= 0) {
                    if (!living.shouldRemove()) {
                        living.kill(); // Sets the model animation state to DEATH
                    }
                    // Returns false while dying; evaluates to true once the sprite view triggers completeKill()
                    shouldRemove = living.shouldRemove();
                }
            }

            if (shouldRemove) {
                toRemove.add(entity);

                if (entity instanceof LivingEntityModel living &&
                        living != player &&
                        living.getXpReward() > 0 &&
                        player != null &&
                        living.isKilledByProjectile()) {

                    player.addXp(living.getXpReward());
                    SessionScoreTracker.getInstance().addScore((int) living.getXpReward());
                }
            }
        }

        // Process all removals safely synchronized on the engine main thread
        for (AbstractEntityModel entity : toRemove) {
            universeModel.removeEntity(entity);

            // FIX: Purge the AI execution pool when Master is killed to avoid memory leaks
            if (entity instanceof IscatMasterModel) {
                aiControllers.removeIf(ai -> ai instanceof IscatMasterController);
            }
        }
    }

    private void updateCamera(PlayerModel player, CameraModel cameraModel, double dt) {
        if (player != null) {
            double targetX = UU.mToPx(player.getTransform().getTranslationX());
            double targetY = UU.mToPx(player.getTransform().getTranslationY());
            double velocityMag = player.getLinearVelocity().getMagnitude();

            cameraModel.getSpringX().setTarget(
                    targetX + Math.sin(player.getTransform().getRotationAngle()) * velocityMag
            );

            cameraModel.getSpringY().setTarget(
                    targetY + Math.cos(player.getTransform().getRotationAngle()) * velocityMag
            );
        }

        cameraModel.getSpringX().update(dt);
        cameraModel.getSpringY().update(dt);
    }

    private void spawnAsteroids(double dt, PlayerModel player) {
        asteroidCooldown.update(dt);

        if (!asteroidCooldown.isReady()) return;

        asteroidCooldown.start(ASTEROID_SPAWN_INTERVAL);

        List<AsteroidModel> activeAsteroids = universeModel.getEntitiesOfType(AsteroidModel.class);

        if (activeAsteroids.size() >= MAX_ACTIVE_ASTEROIDS || player == null) return;

        double playerX = UU.mToPx(player.getTransform().getTranslationX());
        double playerY = UU.mToPx(player.getTransform().getTranslationY());

        double angle = Math.random() * Math.PI * 2.0;
        double dist = 900.0 + Math.random() * 600.0;

        double cx = playerX + Math.cos(angle) * dist;
        double cy = playerY + Math.sin(angle) * dist;

        int count = 3 + random.nextInt(3);

        for (int i = 0; i < count; i++) {
            double offsetAngle = Math.random() * Math.PI * 2.0;
            double offsetDist = Math.random() * 150.0;

            double ax = cx + Math.cos(offsetAngle) * offsetDist;
            double ay = cy + Math.sin(offsetAngle) * offsetDist;

            double radius = 15.0 + Math.random() * 75.0;

            AsteroidModel asteroid = new AsteroidModel(ax, ay, radius);

            double driftAngle = Math.random() * Math.PI * 2.0;
            double speed = UniverseVelocitySettings.ASTEROID_SPAWN_SPEED_MIN
                    + Math.random() * (UniverseVelocitySettings.ASTEROID_SPAWN_SPEED_MAX - UniverseVelocitySettings.ASTEROID_SPAWN_SPEED_MIN);

            asteroid.setLinearVelocity(new Vector2(Math.cos(driftAngle) * speed, Math.sin(driftAngle) * speed));
            UniverseSpawner.getInstance().spawnEntity(asteroid);
        }
    }

    public void addAiController(uni.gaben.iscat.universe.lib.behaviurs.AiController controller) {
        aiControllers.add(controller);
    }

    public UniverseModel getUniverseModel() {
        return universeModel;
    }

    public StarfieldController getStarfieldController() {
        return starfieldController;
    }
}