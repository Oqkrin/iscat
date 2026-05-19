package uni.gaben.iscat.game.controller;

import javafx.animation.AnimationTimer;
import uni.gaben.iscat.game.universe.asteroid.AsteroidModel;
import uni.gaben.iscat.game.view.camera.CameraModel;
import uni.gaben.iscat.game.model.GameModel;
import uni.gaben.iscat.game.universe.UniverseController;
import uni.gaben.iscat.game.universe.UniverseModel;
import uni.gaben.iscat.game.universe.UniverseSpawner;

import java.util.Random;

public class GameController {
    private GameModel gameModel;
    private UniverseController universeController;
    private AnimationTimer gameLoop;
    private Runnable drawCall;
    private GameInputs gameInputs = new GameInputs();

    public GameInputs getInputManager() {
        return gameInputs;
    }

    public void setDrawCall(Runnable drawCall) {
        this.drawCall = drawCall;
    }

    public GameController(GameModel gameModel, UniverseController universeController) {
        this.gameModel = gameModel;
        this.universeController = universeController;

        // Inizializzazione dello Spawner e generazione del mondo iniziale
        UniverseSpawner.getInstance().init(getUniverseModel(), universeController);

        double midX = getUniverseModel().getWidth() / 2.0;
        double midY = getUniverseModel().getHeight() / 2.0;

        UniverseSpawner.getInstance().spawnPlayer(midX, midY);

        // Stabilize spring hooks immediately to prevent camera lens pan jitter during launch
        getCameraModel().getSpringX().setPosition(midX);
        getCameraModel().getSpringY().setPosition(midY);

        setupTimer(gameModel);
    }

    private void setupTimer(GameModel gameModel) {
        this.gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (gameModel.getLastUpdate() == 0) {
                    gameModel.setLastUpdate(now);
                    return;
                }

                gameModel.setNow(now);
                double dt = gameModel.getDt();

                if (dt > GameModel.ACCUMULATORUNIT) {
                    dt = GameModel.ACCUMULATORUNIT;
                }

                tick(dt);

                if (drawCall != null) {
                    drawCall.run();
                }

                gameModel.setLastUpdate(now);
            }
        };
    }

    private void tick(double dt) {
        if (gameModel.isPaused()) {
            universeController.updatev(dt, gameInputs, getCameraModel());
        }
    }

    public void togglePause() {
        gameModel.setPaused(gameModel.isPaused());
    }

    public void startGameLoop() {
        gameLoop.start();
    }

    public void stopGameLoop() {
        gameLoop.stop();
    }

    public void debugSpawn(String spawnableId) {
        // THE FIX: Drops models symmetrically balanced over the actual camera center position
        double spawnWorldX = getCameraModel().getX() + ((Math.random() - 0.5) * 400);
        double spawnWorldY = getCameraModel().getY() + ((Math.random() - 0.5) * 400);

        UniverseSpawner.getInstance().spawn(spawnableId, spawnWorldX, spawnWorldY);
    }

    private boolean showFps = false;

    public void setShowFps(boolean show) {
        this.showFps = show;
    }

    public boolean isFpsOn() {
        return showFps;
    }

    public UniverseModel getUniverseModel() {
        return gameModel.getUniverseModel();
    }

    public UniverseController getUniverseController() {
        return universeController;
    }

    public CameraModel getCameraModel() {
        return gameModel.getCameraModel();
    }

    public void resetUniverse() {
        UniverseModel newUniverse = new UniverseModel();
        gameModel.setUniverseModel(newUniverse);
        universeController = new UniverseController(newUniverse);

        UniverseSpawner.getInstance().init(newUniverse, universeController);
        universeController.getStarfieldController().regenerate(
                newUniverse.getStarfieldModel(),
                newUniverse.getWidth(),
                newUniverse.getHeight()
        );
        double midX = newUniverse.getWidth() / 2.0;
        double midY = newUniverse.getHeight() / 2.0;

        UniverseSpawner.getInstance().spawnPlayer(midX, midY);

        // Spawn 6 initial clumps of asteroids all around the world (mostly off-camera)
        for (int clump = 0; clump < 6; clump++) {
            double angle = (clump * (Math.PI * 2.0 / 6.0)) + (Math.random() * 0.5);
            double dist = 600.0 + Math.random() * 1200.0;

            double cx = midX + Math.cos(angle) * dist;
            double cy = midY + Math.sin(angle) * dist;

            int count = 3 + new Random().nextInt(3);
            for (int i = 0; i < count; i++) {
                double offsetAngle = Math.random() * Math.PI * 2.0;
                double offsetDist = Math.random() * 180.0;

                double ax = cx + Math.cos(offsetAngle) * offsetDist;
                double ay = cy + Math.sin(offsetAngle) * offsetDist;

                // Larger radii (diameter up to 180px)
                double radius = 20.0 + Math.random() * 70.0;

                AsteroidModel ast = new AsteroidModel(ax, ay, radius);

                double driftAngle = Math.random() * Math.PI * 2.0;
                double speed = 0.5 + Math.random() * 2.0;
                ast.setLinearVelocity(new org.dyn4j.geometry.Vector2(
                        Math.cos(driftAngle) * speed,
                        Math.sin(driftAngle) * speed
                ));

                UniverseSpawner.getInstance().spawnEntity(ast);
            }
        }

        getCameraModel().getSpringX().setPosition(midX);
        getCameraModel().getSpringY().setPosition(midY);
    }
}