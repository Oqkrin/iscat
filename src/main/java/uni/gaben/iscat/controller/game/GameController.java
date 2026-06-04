package uni.gaben.iscat.controller.game;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
    
    private final GameLoopTimer gameLoop;
    private final GameLifecycleManager lifecycleManager;

    private UniverseController universeController;
    private UniverseWaveController waveController;
    private Runnable onUniverseResetCallback;
    
    private boolean showFps = false;
    private final BooleanProperty showDebugMode = new SimpleBooleanProperty(false);
    private final BooleanProperty optionsMenuOpen = new SimpleBooleanProperty(false);

    public GameController(GameModel gameModel) {
        this.gameModel = gameModel;

        this.gameLoop = new GameLoopTimer(gameModel, this::tick);
        this.lifecycleManager = new GameLifecycleManager(gameModel, inputs, gameLoop);

        var bundle = lifecycleManager.resetUniverse(this::onPlayerDeath);
        this.universeController = bundle.universeController();
        this.waveController = bundle.waveController();
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

        var bundle = lifecycleManager.resetUniverse(this::onPlayerDeath);
        this.universeController = bundle.universeController();
        this.waveController = bundle.waveController();

        if (onUniverseResetCallback != null) onUniverseResetCallback.run();
    }

    public void quitToMainMenu() {
        gameLoop.stop();
        statsManager.saveStats((int) gameModel.getTotalElapsedSeconds());
        resetGame(); // clean state for next play
        AudioManager.getInstance().stopBGM();
        showDebugMode.set(false);
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

    public void setDrawCall(Runnable drawCall) { this.gameLoop.setDrawCall(drawCall); }
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