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
import uni.gaben.iscat.universe.entities.hardcoded.heart.HeartModel;
import uni.gaben.iscat.universe.entities.hardcoded.projectiles.ProjectileModel;
import uni.gaben.iscat.universe.spawn.UniverseSpawner;
import uni.gaben.iscat.universe.spawn.UniverseWaveController;
import uni.gaben.iscat.utils.AudioManager;
import uni.gaben.iscat.utils.SessionManager;
import uni.gaben.iscat.utils.SessionScoreTracker;
import uni.gaben.iscat.universe.entities.AbstractLivingEntityModel;
import uni.gaben.iscat.universe.entities.AbstractPhysicalEntityModel;
import uni.gaben.iscat.universe.entities.hardcoded.player.PlayerModel;

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

        setupUniverse();
    }

    private void setupUniverse() {
        String currentSkinKey = SessionManager.getPlayerSkinKey();

        var bundle = lifecycleManager.resetUniverse(this::onPlayerDeath, currentSkinKey);
        this.universeController = bundle.universeController();
        this.waveController = bundle.waveController();
        this.universeController.setEntityDeathListener(this::onEntityDied);
        this.universeController.getPlayerController().setGameModel(gameModel);
        this.waveController.setOnBossDeadCallback(this::notifyBossDead);

        if (gameModel.getUniverseModel() != null) {
            double arenaDiameterMetres = 10.0;
            gameModel.getUniverseModel().setDimensions(arenaDiameterMetres, arenaDiameterMetres);
        }
    }

    private void tick(double dt) {
        if (inputs.consumePause()) togglePause();
        if (!gameModel.getGameState().isPaused()) {
            universeController.updatev(dt, inputs, getCameraModel());
            UniverseModel universe = gameModel.getUniverseModel();
            if (universe != null && universe.getPlayer() != null) {
                PlayerModel player = universe.getPlayer();
                org.dyn4j.geometry.Vector2 pos = player.getTransform().getTranslation();
                double radius = universe.getUniverseRadius();
                double dist = pos.getMagnitude();

                if (dist > radius) {
                    org.dyn4j.geometry.Vector2 normal = pos.getNormalized();
                    player.getTransform().setTranslation(normal.x * radius, normal.y * radius);

                    // Annulla la velocità proiettata verso l'esterno
                    org.dyn4j.geometry.Vector2 vel = player.getLinearVelocity();
                    double dot = vel.dot(normal);
                    if (dot > 0) {
                        vel.subtract(normal.product(dot));
                    }
                }
            }

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
        AudioManager.getInstance().playBGM("/uni/gaben/iscat/audio/BGM/SuperHero_original.wav", true);
        gameModel.setGameState(GameState.PLAYING);

        setupUniverse();

        if (onUniverseResetCallback != null) onUniverseResetCallback.run();
    }

    public void quitToMainMenu() {
        gameLoop.stop();
        statsManager.saveStats((int) gameModel.getTotalElapsedSeconds(), false);
        resetGame();
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
            statsManager.saveStats((int) gameModel.getTotalElapsedSeconds(), false);
        });
    }

    private void onEntityDied(AbstractPhysicalEntityModel entity, boolean killedByProjectile) {
        if (entity instanceof ProjectileModel || entity instanceof HeartModel) {
            return;
        }

        if (entity instanceof AbstractLivingEntityModel living) {
            // Incrementa kills solo per nemici reali (filtra proiettili/asteroidi via ThreatLevel)
            UniverseWaveController.incrementKills(entity);

            if (living.getXpReward() > 0) {
                String key = living.getEntityRecord() != null ? living.getEntityRecord().entityKey() : null;
                String cleanKey = key != null ? key.toLowerCase().trim() : "";
                boolean isSpecial = cleanKey.equals("iscat_healer") || cleanKey.equals("iscat_master");

                if (killedByProjectile || isSpecial) {
                    PlayerModel player = gameModel.getUniverseModel().getPlayer();
                    if (player != null) player.incrementExperience(living.getXpReward());

                    SessionScoreTracker tracker = SessionScoreTracker.getInstance();
                    tracker.addKill();
                    tracker.addScore((int) living.getXpReward() + 100);
                    tracker.addEnemyKill(cleanKey);
                }
            }
        }
    }

    public void notifyBossDead() {
        statsManager.saveStats((int) gameModel.getTotalElapsedSeconds(),true);
        Platform.runLater(() -> {
            AudioManager.getInstance().stopBGM();
            gameModel.setGameState(GameState.WIN);
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
    public UniverseWaveController getUniverseWaveController() { return waveController; }
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