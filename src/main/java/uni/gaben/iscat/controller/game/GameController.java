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
import uni.gaben.iscat.utils.audio.AudioManager;
import uni.gaben.iscat.utils.SessionManager;
import uni.gaben.iscat.utils.SessionScoreTracker;
import uni.gaben.iscat.universe.entities.AbstractLivingEntityModel;
import uni.gaben.iscat.universe.entities.AbstractPhysicalEntityModel;

/**
 * Controller principale del ciclo di vita della partita (Gameplay Core).
 * Coordina il ticking fisico del mondo di gioco, l'elaborazione degli input utente ({@link GameInputsHandler}),
 * la progressione delle ondate di nemici ({@link UniverseWaveController}), l'applicazione dei cheat diagnostici
 * ({@link DebugCheatManager}) e il salvataggio delle statistiche di sessione ({@link GameStatsManager}).
 */
public class GameController {

    /** Il modello dati globale contenente lo stato corrente della sessione e dell'universo di gioco. */
    private final GameModel gameModel;

    /** Gestore dedicato alla cattura e al consumo degli input fisici ed eventi di controllo. */
    private final GameInputsHandler inputs = new GameInputsHandler();

    /** Manager addetto al tracciamento, formattazione e salvataggio persistente delle statistiche a fine partita. */
    private final GameStatsManager statsManager = new GameStatsManager();

    /** Sotto-componente diagnostico per l'applicazione di trucchi ed alterazioni di stato in modalità debug. */
    private final DebugCheatManager cheatManager;

    /** Timer ad alta precisione che scandisce la frequenza di aggiornamento logico (tick) del loop principale. */
    private final GameLoopTimer gameLoop;

    /** Manager deputato all'orchestrazione delle fasi di setup, allocazione e reset fisico del mondo di gioco. */
    private final GameLifecycleManager lifecycleManager;

    /** Sotto-controller delegato alla gestione dei movimenti fisici e delle interazioni tra entità. */
    private UniverseController universeController;

    /** Sotto-controller delegato al calcolo dei tempi, dei pattern di spawn e delle ondate di nemici. */
    private UniverseWaveController waveController;

    /** Callback di notifica eseguita non appena si conclude un ripristino completo dell'universo. */
    private Runnable onUniverseResetCallback;

    /** Flag per l'abilitazione e visualizzazione a schermo del contatore dei fotogrammi (FPS). */
    private boolean showFps = false;

    /** Flag di sicurezza che traccia se la console di debug o i cheat sono stati attivati almeno una volta nella sessione. */
    private boolean debugUsedInThisSession = false;

    /** Proprietà osservabile che determina se l'interfaccia o la modalità sviluppatore sono attive. */
    private final BooleanProperty showDebugMode = new SimpleBooleanProperty(false);

    /**
     * Inizializza il core del controller di gioco configurando il timer del loop, allocando
     * i sotto-moduli e registrando un listener di sicurezza per invalidare i salvataggi in caso di debug.
     * Garantisce inoltre il pre-caricamento asincrono della cache delle entità.
     *
     * @param gameModel Il modello dati contenente lo stato del gioco attuale.
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

    /**
     * Verifica se i comandi di debug o i cheat sono stati compromessi o utilizzati durante la partita corrente.
     *
     * @return {@code true} se la modalità debug è stata attivata, {@code false} altrimenti.
     */
    public boolean isDebugUsedInThisSession() {
        return debugUsedInThisSession;
    }

    /**
     * Configura da zero le istanze del mondo di gioco applicando la skin selezionata del giocatore,
     * caricando la struttura delle ondate da file JSON e impostando i confini dell'arena fisica.
     */
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

    /**
     * Esegue un singolo passo logico di aggiornamento (tick).
     * Gestisce la pausa, l'immunità del God Mode, l'aggiornamento posizionale della simulazione fisica
     * e applica una forza di contenimento elastica se il giocatore supera i confini dell'arena circolare.
     *
     * @param dt Il tempo delta trascorso dall'ultimo frame espresso in secondi.
     */
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

    /** @see DebugCheatManager#debugHeal(double) */
    public void debugHeal(double amount) { cheatManager.debugHeal(amount); }
    /** @see DebugCheatManager#debugDamage(double) */
    public void debugDamage(double amount) { cheatManager.debugDamage(amount); }
    /** @see DebugCheatManager#debugToggleGodMode() */
    public void debugToggleGodMode() { cheatManager.debugToggleGodMode(); }
    /** @see DebugCheatManager#debugLevelUp() */
    public void debugLevelUp() { cheatManager.debugLevelUp(); }
    /** @see DebugCheatManager#debugLevelDown() */
    public void debugLevelDown() { cheatManager.debugLevelDown(); }
    /** @see DebugCheatManager#debugSpawn(String) */
    public void debugSpawn(String id) { cheatManager.debugSpawn(id); }

    /**
     * Recupera il modello logico associato alla navicella del giocatore.
     *
     * @return L'istanza corrente di {@link PlayerModel}, oppure {@code null} se non allocata.
     */
    public PlayerModel getPlayer() {
        return gameModel.getUniverseModel() != null ? gameModel.getUniverseModel().getPlayer() : null;
    }

    /**
     * Alterna lo stato corrente di gioco passando dalla modalità attiva (PLAYING) alla pausa (IN_PAUSE) e viceversa.
     */
    public void togglePause() {
        if (gameModel.getGameState() == GameState.PLAYING) {
            gameModel.setGameState(GameState.IN_PAUSE);
        } else if (gameModel.getGameState() == GameState.IN_PAUSE) {
            gameModel.setGameState(GameState.PLAYING);
        }
    }

    /**
     * Interrompe la simulazione in esecuzione, azzera l'intero progresso della mappa e riavvia il loop.
     */
    public void retryGame() {
        gameLoop.stop();
        resetGame();
        gameLoop.start();
    }

    /**
     * Ripristina la traccia audio di sottofondo principale, reimposta lo stato di gioco su PLAYING
     * e rigenera l'universo attivando l'eventuale callback registrata.
     */
    private void resetGame() {
        AudioManager.getInstance().playBGM("/uni/gaben/iscat/audio/BGM/SuperHero_original.wav", true);
        gameModel.setGameState(GameState.PLAYING);
        setupUniverse();
        if (onUniverseResetCallback != null) onUniverseResetCallback.run();
    }

    /**
     * Interrompe l'esecuzione della partita, effettua il salvataggio finale parziale delle statistiche,
     * azzera il tracciamento e reindirizza la navigazione verso il menu principale dell'applicazione.
     */
    public void quitToMainMenu() {
        gameLoop.stop();
        statsManager.saveStats((int) gameModel.getTotalElapsedSeconds(), false, isDebugUsedInThisSession());
        resetGame();
        AudioManager.getInstance().stopBGM();
        showDebugMode.set(false);
        IscatNavigator.getInstance().navigateWithFade(IscatViews.MAIN_MENU);
    }

    /**
     * Arresta forzatamente l'applicazione e termina l'esecuzione di tutti i thread JavaFX.
     */
    public void quitGame() { Platform.exit(); }

    /**
     * Intercetta la morte del giocatore sul thread dell'interfaccia grafica.
     * Riproduce il brano di game over, imposta lo stato globale e persiste i risultati ottenuti.
     */
    private void onPlayerDeath() {
        Platform.runLater(() -> {
            AudioManager.getInstance().stopBGM();
            AudioManager.getInstance().playBGM("/uni/gaben/iscat/audio/BGM/gameover.wav", true);
            gameModel.setGameState(GameState.GAME_OVER);
            statsManager.saveStats((int) gameModel.getTotalElapsedSeconds(), false, isDebugUsedInThisSession());
        });
    }

    /**
     * Listener d'ascolto scatenato alla rimozione di una entità fisica dal mondo.
     * Incrementa il contatore delle uccisioni dell'ondata ed assegna l'esperienza e i punti score
     * al giocatore se l'entità abbattuta possiede un quantitativo valido di XP premio.
     *
     * @param entity             L'entità fisica rimossa o deceduta.
     * @param killedByProjectile Indica se il colpo di grazia è stato inferto da un proiettile alleato.
     */
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

    /**
     * Notifica la sconfitta del Boss finale della sessione.
     * Salva i dati di gioco impostando il flag di vittoria e varia lo stato della partita su WIN.
     */
    public void notifyBossDead() {
        statsManager.saveStats((int) gameModel.getTotalElapsedSeconds(), true, isDebugUsedInThisSession());
        Platform.runLater(() -> {
            AudioManager.getInstance().stopBGM();
            gameModel.setGameState(GameState.WIN);
        });
    }

    /** @param drawCall Interfaccia funzionale contenente le istruzioni di rendering grafico. */
    public void setDrawCall(Runnable drawCall) { this.gameLoop.setDrawCall(drawCall); }

    /** @param cb Callback da lanciare al termine del reset dell'universo. */
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

    /** @return La {@link BooleanProperty} relativa alla visualizzazione dello stato di debug. */
    public BooleanProperty debugModeProperty() { return showDebugMode; }

    /** @return La {@link BooleanProperty} legata all'attivazione dell'invulnerabilità (God Mode). */
    public BooleanProperty godModeProperty() { return cheatManager.godModeProperty(); }

    /** Interrompe l'esecuzione del timer del loop grafico. */
    public void stopGameLoop() { gameLoop.stop(); }

    /** Avvia o riprende l'esecuzione del timer del loop grafico. */
    public void startGameLoop() { gameLoop.start(); }
}