package uni.gaben.iscat.controller.game;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.dyn4j.collision.CategoryFilter;
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
import uni.gaben.iscat.universe.spawn.UniverseSpawner;
import uni.gaben.iscat.universe.spawn.waves.UniverseWaveController;
import uni.gaben.iscat.utils.AudioManager;
import uni.gaben.iscat.utils.SessionManager;
import uni.gaben.iscat.utils.SessionScoreTracker;
import uni.gaben.iscat.universe.entities.AbstractLivingEntityModel;
import uni.gaben.iscat.universe.entities.AbstractPhysicalEntityModel;

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
    private boolean debugUsedInThisSession = false;
    private final BooleanProperty showDebugMode = new SimpleBooleanProperty(false);

    private final BooleanProperty godMode = new SimpleBooleanProperty(false);
    private final BooleanProperty ghostMode = new SimpleBooleanProperty(false);

    public GameController(GameModel gameModel) {
        Platform.runLater(EntityFactory::ensureCacheLoaded);
        this.gameModel = gameModel;
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

        this.godMode.set(false);
        this.ghostMode.set(false);

        // Se la modalità debug è già attiva nelle impostazioni all'avvio del livello,
        // invalidiamo subito i record per la sessione corrente
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
    }

    private void tick(double dt) {
        if (inputs.consumePause()) togglePause();
        if (!gameModel.getGameState().isPaused()) {

            PlayerModel player = gameModel.getUniverseModel() != null ? gameModel.getUniverseModel().getPlayer() : null;

            if (godMode.get() && player != null) {
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

    public void debugHeal(double amount) {
        PlayerModel player = getPlayer();
        if (player != null) {
            player.alter(amount);
            System.out.println("[DEBUG CHEAT] Curato di: " + amount);
        }
    }

    public void debugDamage(double amount) {
        if (godMode.get()) return;
        PlayerModel player = getPlayer();
        if (player != null) {
            player.alter(-amount);
            System.out.println("[DEBUG CHEAT] Danno autoinflitto: " + amount);
        }
    }

    public void debugToggleGodMode() {
        this.godMode.set(!this.godMode.get());
        System.out.println("[DEBUG CHEAT] Godmode impostato a: " + godMode.get());
    }

    public void debugToggleGhostMode() {
        PlayerModel player = getPlayer();
        if (player == null) return;

        this.ghostMode.set(!this.ghostMode.get());

        CategoryFilter filter = ghostMode.get()
                ? new CategoryFilter(UniverseCollisionLayers.PLAYER, 0)
                : UniverseCollisionLayers.PLAYER_FILTER;

        player.getFixtures().forEach(fixture -> fixture.setFilter(filter));
        System.out.println("[DEBUG CHEAT] Ghost Mode (No Collision) impostato a: " + ghostMode.get());
    }

    public void debugLevelUp() {
        PlayerModel player = getPlayer();
        if (player != null) {
            player.levelUp();
            System.out.println("[DEBUG CHEAT] Livello aumentato! Livello attuale: " + player.getLevel());
        }
    }

    public void debugLevelDown() {
        PlayerModel player = getPlayer();
        if (player != null && player.getLevel() > 1) {
            player.levelProperty().set(player.getLevel() - 1);
            System.out.println("[DEBUG CHEAT] Livello diminuito! Livello attuale: " + player.getLevel());
        }
    }

    private PlayerModel getPlayer() {
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
        // AGGIORNATO: Passa lo stato di utilizzo del debug
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
            // AGGIORNATO: Passa lo stato di utilizzo del debug
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
        // AGGIORNATO: Passa lo stato di utilizzo del debug
        statsManager.saveStats((int) gameModel.getTotalElapsedSeconds(), true, isDebugUsedInThisSession());
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

    public BooleanProperty godModeProperty() { return godMode; }
    public BooleanProperty ghostModeProperty() { return ghostMode; }

    public void stopGameLoop() { gameLoop.stop(); }
    public void startGameLoop() { gameLoop.start(); }
}