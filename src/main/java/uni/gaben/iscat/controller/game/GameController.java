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
import uni.gaben.iscat.universe.entities.EntityFactory;
import uni.gaben.iscat.universe.entities.hardcoded.heart.HeartModel;
import uni.gaben.iscat.universe.entities.hardcoded.projectiles.ProjectileModel;
import uni.gaben.iscat.universe.entities.player.PlayerModel;
import uni.gaben.iscat.universe.spawn.waves.UniverseWaveController;
import uni.gaben.iscat.utils.AudioManager;
import uni.gaben.iscat.utils.SessionManager;
import uni.gaben.iscat.utils.SessionScoreTracker;
import uni.gaben.iscat.universe.entities.AbstractLivingEntityModel;
import uni.gaben.iscat.universe.entities.AbstractPhysicalEntityModel;

/**
 * Controller principale del ciclo di vita della partita, addetto alla gestione
 * degli input fisici, del ticking del mondo e del coordinamento dei moduli interni.
 */
public class GameController {

    private final GameModel gameModel;
    private final GameInputsHandler inputs = new GameInputsHandler();
    private final GameStatsManager statsManager = new GameStatsManager();
    private final DebugCheatManager cheatManager;

    private final GameLoopTimer gameLoop;
    private final GameLifecycleManager lifecycleManager;

    private UniverseController universeController;
    private UniverseWaveController waveController;
    private Runnable onUniverseResetCallback;

    private boolean showFps = false;
    private boolean debugUsedInThisSession = false;
    private final BooleanProperty showDebugMode = new SimpleBooleanProperty(false);

    /**
     * Inizializza il core del controller di gioco configurando i timer di loop e i listener di sessione.
     *
     * @param gameModel Il modello dati contenente lo stato del gioco attuale
     */
    public GameController(GameModel gameModel) {
        Platform.runLater(EntityFactory::ensureCacheLoaded);
        this.gameModel = gameModel;
        this.cheatManager = new DebugCheatManager(this);
        this.gameLoop = new GameLoopTimer(gameModel, this::tick);
        this.lifecycleManager = new GameLifecycleManager(gameModel, inputs, gameLoop);

        this.showDebugMode.addListener((obs, oldV, newV) -> {
            if (newV) {
                this.debugUsedInThisSession = true;
                System.out.println("[SECURITY] Debug attivato in partita. Salvataggio statistiche disabilitato per questa sessione.");
            }
        });

        setupUniverse();
    }

    public boolean isDebugUsedInThisSession() {
        return debugUsedInThisSession;
    }

    private void setupUniverse() {
        String currentSkinKey = SessionManager.getPlayerSkinKey();
        this.cheatManager.reset();
        this.debugUsedInThisSession = isDebugModeOn();

        var bundle = lifecycleManager.resetUniverse(this::onPlayerDeath, currentSkinKey);
        this.universeController = bundle.universeController();
        this.waveController = bundle.waveController();

        this.waveController.loadWavesFromResource("/uni/gaben/iscat/json/config/waves.json");

        assert this.universeController != null;
        this.universeController.setEntityDeathListener(this::onEntityDied);
        this.universeController.getPlayerController().setGameModel(gameModel);
        this.waveController.setOnBossDeadCallback(this::notifyBossDead);

        if (gameModel.getUniverseModel() != null) {
            double arenaDiameterMetres = 250.0;
            gameModel.getUniverseModel().setDimensions(arenaDiameterMetres, arenaDiameterMetres);
        }

        universeController.getUniverseModel().setCamera(getCameraModel());
    }

    private void tick(double dt) {
        if (inputs.consumePause()) togglePause();
        if (!gameModel.getGameState().isPaused()) {

            PlayerModel player = gameModel.getUniverseModel() != null ? gameModel.getUniverseModel().getPlayer() : null;

            if (cheatManager.isGodModeOn() && player != null) {
                player.setEndurance(player.getMaxEndurance());
            }

            universeController.updatev(dt, inputs, getCameraModel());

            if (player != null) {
                org.dyn4j.geometry.Vector2 pos = player.getTransform().getTranslation();
                double radius = gameModel.getUniverseModel().getUniverseRadius();
                double dist = pos.getMagnitude();

                if (dist > radius) {
                    org.dyn4j.geometry.Vector2 normal = pos.getNormalized();
                    player.getTransform().setTranslation(normal.x * radius, normal.y * radius);

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

    public void debugHeal(double amount) { cheatManager.debugHeal(amount); }
    public void debugDamage(double amount) { cheatManager.debugDamage(amount); }
    public void debugToggleGodMode() { cheatManager.debugToggleGodMode(); }
    public void debugLevelUp() { cheatManager.debugLevelUp(); }
    public void debugLevelDown() { cheatManager.debugLevelDown(); }
    public void debugSpawn(String id) { cheatManager.debugSpawn(id); }

    public PlayerModel getPlayer() {
        return gameModel.getUniverseModel() != null ? gameModel.getUniverseModel().getPlayer() : null;
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
        statsManager.saveStats((int) gameModel.getTotalElapsedSeconds(), false, isDebugUsedInThisSession());
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
            statsManager.saveStats((int) gameModel.getTotalElapsedSeconds(), false, isDebugUsedInThisSession());
        });
    }

    private void onEntityDied(AbstractPhysicalEntityModel entity, boolean killedByProjectile) {
        if (entity instanceof ProjectileModel || entity instanceof HeartModel) {
            return;
        }

        if (entity instanceof AbstractLivingEntityModel living) {
            UniverseWaveController.incrementKills(entity);

            if (living.getXpReward() > 0) {
                String key = living.getEntityRecord() != null ? living.getEntityRecord().entityKey() : null;
                String cleanKey = key != null ? key.toLowerCase().trim() : "";
                boolean isSpecial = cleanKey.equals("iscat_healer") || cleanKey.equals("iscat_master");

                if (killedByProjectile || isSpecial) {
                    PlayerModel player = getPlayer();
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
        statsManager.saveStats((int) gameModel.getTotalElapsedSeconds(), true, isDebugUsedInThisSession());
        Platform.runLater(() -> {
            AudioManager.getInstance().stopBGM();
            gameModel.setGameState(GameState.WIN);
        });
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
    public BooleanProperty godModeProperty() { return cheatManager.godModeProperty(); }

    public void stopGameLoop() { gameLoop.stop(); }
    public void startGameLoop() { gameLoop.start(); }
}