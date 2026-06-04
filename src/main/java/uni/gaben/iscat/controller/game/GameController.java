package uni.gaben.iscat.controller.game;

import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.layout.StackPane;
import uni.gaben.iscat.IscatNavigator;
import uni.gaben.iscat.model.IscatViews;
import uni.gaben.iscat.model.game.GameModel;
import uni.gaben.iscat.model.game.GameState;
import uni.gaben.iscat.universe.*;
import uni.gaben.iscat.universe.camera.CameraModel;
import uni.gaben.iscat.utils.AudioManager;

public class GameController {

    private final GameModel gameModel;
    private final GameInputsHandler inputs = new GameInputsHandler();
    private final GameStatsManager statsManager = new GameStatsManager();
    private final GameResetManager resetManager;

    private UniverseController universeController;
    private UniverseWaveController waveController;
    private AnimationTimer gameLoop;
    private Runnable drawCall;
    private Runnable onUniverseResetCallback;
    private StackPane contentRoot;
    private boolean showFps = false;
    private final BooleanProperty showDebugMode = new SimpleBooleanProperty(false);
    private final BooleanProperty optionsMenuOpen = new SimpleBooleanProperty(false);

    public GameController(GameModel gameModel) {
        this.gameModel = gameModel;

        // Fix 1: Pass the whole GameModel to ResetManager
        this.resetManager = new GameResetManager(gameModel, statsManager);
        this.universeController = new UniverseController(gameModel.getUniverseModel());
        this.waveController = new UniverseWaveController();

        UniverseSpawner.getInstance().init(getUniverseModel(), universeController, waveController);

        double midX = UniverseModel.DEFAULT_SPAWN_CENTER;
        double midY = UniverseModel.DEFAULT_SPAWN_CENTER;
        UniverseSpawner.getInstance().spawnPlayer(midX, midY);
        getUniverseModel().getPlayer().setOnDeathCallback(this::onPlayerDeath);

        getCameraModel().getSpringX().setPosition(midX);
        getCameraModel().getSpringY().setPosition(midY);

        buildGameLoop();
    }

    // -------------------------------------------------------------------------
    // Game loop
    // -------------------------------------------------------------------------

    private void buildGameLoop() {
        this.gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (gameModel.getStart() == -1) gameModel.startProperty().set(now);
                if (gameModel.getLastUpdate() == 0) {
                    gameModel.setLastUpdate(now);
                    return;
                }

                gameModel.setNow(now);

                double totalSec = (now - gameModel.getStart()) / GameModel.ONE_SECOND_IN_NANOS;
                gameModel.setTotalElapsedSeconds(totalSec);

                int h = (int) (totalSec / 3600);
                int m = (int) ((totalSec % 3600) / 60);
                int s = (int) (totalSec % 60);
                gameModel.timerProperty().set(h * 10000 + m * 100 + s);

                double dt = Math.min(gameModel.getDt(), GameModel.ACCUMULATORUNIT);
                tick(dt);

                if (drawCall != null) drawCall.run();

                gameModel.setLastUpdate(now);
            }
        };
    }

    private void tick(double dt) {
        if (inputs.consumePause()) togglePause();
        if (!gameModel.getGameState().isPaused()) {
            universeController.updatev(dt, inputs, getCameraModel());
            if (waveController != null && gameModel.isWaveActive())
                waveController.update(dt, getCameraModel(), gameModel);
        }
    }

    public void togglePause() {
        if (gameModel.getGameState() == GameState.PLAYING) {
            gameModel.setGameState(GameState.IN_PAUSE);
        } else if (gameModel.getGameState() == GameState.IN_PAUSE) {
            gameModel.setGameState(GameState.PLAYING);
        }
    }

    public void retryGame() {
        gameLoop.stop();
        resetGame();
        gameLoop.start();
    }

    private void resetGame() {
        AudioManager.getInstance().playBGM("/uni/gaben/iscat/audio/BGM/OrbitalColossus.wav", true);
        gameModel.setGameState(GameState.PLAYING);

        double canvasW = getCameraModel().getScreenWidth();
        double canvasH = getCameraModel().getScreenHeight();
        if (canvasW <= 0 || canvasH <= 0) {
            canvasW = UniverseSettings.DEFAULT_WIDTH;
            canvasH = UniverseSettings.DEFAULT_HEIGHT;
        }

        // Resets UniverseModel entirely
        resetManager.reset(canvasW, canvasH);
        // Reset game loop timing and input flags
        inputs.resetInputs();
        gameModel.startProperty().set(-1);    // forces timer restart
        gameModel.setLastUpdate(0);           // avoids delta spike
        gameModel.setTotalElapsedSeconds(0.0);

        // Re-wire the universe controller and wave controller after reset
        this.universeController = new UniverseController(getUniverseModel());
        this.waveController = new UniverseWaveController();
        waveController.reset();
        UniverseSpawner.getInstance().init(getUniverseModel(), universeController, waveController);

        // Re-attach death callback
        getUniverseModel().getPlayer().setOnDeathCallback(this::onPlayerDeath);

        if (onUniverseResetCallback != null) onUniverseResetCallback.run();
    }

    public void quitToMainMenu() {
        gameLoop.stop();
        statsManager.saveStats((int) gameModel.getTotalElapsedSeconds());
        resetGame(); // clean state for next play
        AudioManager.getInstance().stopBGM();
        showDebugMode.set(false);
        if (contentRoot != null)
            IscatNavigator.getInstance().navigateWithFade(IscatViews.MAIN_MENU);
    }

    public void quitGame() { Platform.exit(); }

    private void onPlayerDeath() {
        Platform.runLater(() -> {
            AudioManager.getInstance().stopBGM();
            AudioManager.getInstance().playBGM("/uni/gaben/iscat/audio/BGM/gameover.wav", true);
            gameModel.setGameState(GameState.GAME_OVER);
            statsManager.saveStats((int) gameModel.getTotalElapsedSeconds());
        });
    }

    public void debugSpawn(String id) {
        double x = getCameraModel().getX() + (Math.random() - 0.5) * 400;
        double y = getCameraModel().getY() + (Math.random() - 0.5) * 400;
        UniverseSpawner.getInstance().spawn(id, x, y);
    }

    public void setDrawCall(Runnable drawCall) { this.drawCall = drawCall; }
    public void setContentRoot(StackPane root) { this.contentRoot = root; }
    public void setOnUniverseResetCallback(Runnable cb) { this.onUniverseResetCallback = cb; }
    public GameModel getGameModel() { return gameModel; }
    public GameInputsHandler getInputManager() { return inputs; }
    public UniverseModel getUniverseModel() { return gameModel.getUniverseModel(); }
    public UniverseController getUniverseController() { return universeController; }
    public CameraModel getCameraModel() { return gameModel.getCameraModel(); }
    public boolean isFpsOn() { return showFps; }
    public void setShowFps(boolean v) { this.showFps = v; }
    public boolean isDebugModeOn() { return showDebugMode.get(); }
    public void setShowDebugMode(boolean v) { showDebugMode.set(v); }
    public BooleanProperty debugModeProperty() { return showDebugMode; }
    public boolean isOptionsMenuOpen() { return optionsMenuOpen.get(); }
    public void setOptionsMenuOpen(boolean v) { optionsMenuOpen.set(v); }
    public BooleanProperty optionsMenuOpenProperty() { return optionsMenuOpen; }

    public void stopGameLoop() { gameLoop.stop(); }
    public void startGameLoop() { gameLoop.start(); }
}