package uni.gaben.iscat.iscat_screens.game.controller;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.layout.StackPane;
import uni.gaben.iscat.iscat_game.universe.EnemyWaveController;
import uni.gaben.iscat.IscatNavigator;
import uni.gaben.iscat.iscat_game.universe.asteroid.AsteroidModel;
import uni.gaben.iscat.iscat_game.camera.CameraModel;
import uni.gaben.iscat.iscat_model_vc.IscatViews;
import uni.gaben.iscat.iscat_screens.game.model.GameModel;
import uni.gaben.iscat.iscat_game.universe.UniverseController;
import uni.gaben.iscat.iscat_game.universe.UniverseModel;
import uni.gaben.iscat.iscat_game.universe.UniverseSpawner;
import uni.gaben.iscat.utils.AudioManager;

import java.util.Random;

public class GameController {

    private final GameModel gameModel;
    private final GameInputs gameInputs = new GameInputs();

    private UniverseController universeController;
    private AnimationTimer gameLoop;
    private Runnable drawCall;
    private StackPane contentRoot;
    private boolean showFps = false;
    private final BooleanProperty showDebugMode = new SimpleBooleanProperty(false);
    private EnemyWaveController waveController;

    public GameController(GameModel gameModel) {
        this.gameModel = gameModel;
        this.universeController = new UniverseController(gameModel.getUniverseModel());
        this.waveController = new EnemyWaveController();

        UniverseSpawner.getInstance().init(getUniverseModel(), universeController, waveController);

        double midX = getUniverseModel().getWidth() / 2.0;
        double midY = getUniverseModel().getHeight() / 2.0;

        UniverseSpawner.getInstance().spawnPlayer(midX, midY);
        getUniverseModel().getPlayer().setOnDeathCallback(this::onPlayerDeath);

        getCameraModel().getSpringX().setPosition(midX);
        getCameraModel().getSpringY().setPosition(midY);

        setupTimer(gameModel);
    }

    private void setupTimer(GameModel gameModel) {
        this.gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if(gameModel.getStart() == -1) {
                    gameModel.startProperty().set(now);
                }
                if (gameModel.getLastUpdate() == 0) {
                    gameModel.setLastUpdate(now);
                    return;
                }

                gameModel.setNow(now);

                double totalSeconds = (now - gameModel.getStart()) / GameModel.ONE_SECOND_IN_NANO_SECONDS;

                int hours = (int) (totalSeconds / 3600);
                int minutes = (int) ((totalSeconds % 3600) / 60);
                int seconds = (int) (totalSeconds % 60);

                int packedTime = (hours * 10000) + (minutes * 100) + seconds;
                gameModel.timerProperty().set(packedTime);

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
        if (!gameModel.isPaused()) {
            universeController.updatev(dt, gameInputs, getCameraModel());

            if (waveController != null) {
                waveController.update(dt, getCameraModel());
            }
        }
    }

    public void startGameLoop() {
        gameLoop.start();
    }

    public void stopGameLoop() {
        gameLoop.stop();
    }

    public void togglePause() {
        gameModel.setPaused(!gameModel.isPaused());
    }

    public void debugSpawn(String spawnableId) {
        double spawnWorldX = getCameraModel().getX() + ((Math.random() - 0.5) * 400);
        double spawnWorldY = getCameraModel().getY() + ((Math.random() - 0.5) * 400);
        UniverseSpawner.getInstance().spawn(spawnableId, spawnWorldX, spawnWorldY);
    }

    public void resetUniverse() {
        AudioManager.getInstance().playBGM("/uni/gaben/iscat/audio/BGM/OrbitalColossus.wav", true);
        gameModel.setGameOver(false);
        gameModel.setPaused(false);

        // Preserve layout limits across rebuild sequences
        double currentWidth = getUniverseModel().getWidth();
        double currentHeight = getUniverseModel().getHeight();

        UniverseModel newUniverse = new UniverseModel();

        gameModel.setUniverseModel(newUniverse);
        this.universeController = new UniverseController(newUniverse);
        this.waveController = new EnemyWaveController();

        UniverseSpawner.getInstance().init(newUniverse, universeController, waveController);

        universeController.getStarfieldController().regenerate(
                newUniverse.getStarfieldModel(),
                currentWidth,
                currentHeight
        );

        double midX = currentWidth / 2.0;
        double midY = currentHeight / 2.0;

        UniverseSpawner.getInstance().spawnPlayer(midX, midY);
        newUniverse.getPlayer().setOnDeathCallback(this::onPlayerDeath);

        spawnInitialAsteroidBelts(newUniverse, midX, midY);

        getCameraModel().getSpringX().setPosition(midX);
        getCameraModel().getSpringY().setPosition(midY);

        // Clear time tracking benchmarks completely
        gameModel.startProperty().set(-1);
        gameModel.setLastUpdate(0);
    }

    private void spawnInitialAsteroidBelts(UniverseModel universe, double centerX, double centerY) {
        Random random = new Random();
        for (int clump = 0; clump < 6; clump++) {
            double angle = (clump * (Math.PI * 2.0 / 6.0)) + (Math.random() * 0.5);
            double dist = 600.0 + Math.random() * 1200.0;

            double cx = centerX + Math.cos(angle) * dist;
            double cy = centerY + Math.sin(angle) * dist;

            int count = 3 + random.nextInt(3);
            for (int i = 0; i < count; i++) {
                double offsetAngle = Math.random() * Math.PI * 2.0;
                double offsetDist = Math.random() * 180.0;

                double ax = cx + Math.cos(offsetAngle) * offsetDist;
                double ay = cy + Math.sin(offsetAngle) * offsetDist;

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
    }

    public void quitToMainMenu() {
        setShowDebugMode(false);
        stopGameLoop();
        gameModel.setPaused(false);
        resetUniverse();
        AudioManager.getInstance().stopBGM();

        if (contentRoot != null) {
            IscatNavigator.getInstance().navigateWithFade(IscatViews.MAIN_MENU);
        }
    }

    public void quitGame() {
        Platform.exit();
    }

    public GameInputs getInputManager() {
        return gameInputs;
    }

    public void setDrawCall(Runnable drawCall) {
        this.drawCall = drawCall;
    }

    public void setContentRoot(StackPane contentRoot) {
        this.contentRoot = contentRoot;
    }

    public void setShowFps(boolean show) {
        this.showFps = show;
    }

    public boolean isFpsOn() {
        return showFps;
    }

    public void setShowDebugMode(boolean show) {
        this.showDebugMode.set(show);
    }

    public boolean isDebugModeOn() {
        return showDebugMode.get();
    }

    public BooleanProperty debugModeProperty() {
        return showDebugMode;
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

    public void retryGame() {
        resetUniverse();
    }

    private void onPlayerDeath() {
        Platform.runLater(() -> {
            AudioManager.getInstance().stopBGM();
            gameModel.setGameOver(true);
            gameModel.setPaused(true);
        });
    }

    public GameModel getGameModel() {
        return gameModel;
    }
}