package uni.gaben.iscat.controller.game;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import uni.gaben.iscat.controller.interfaces.IscatFxmlController;
import uni.gaben.iscat.controller.components.ConfirmationOverlayController;
import uni.gaben.iscat.utils.ComponentsUtils;
import uni.gaben.iscat.view.game.GameView;
import uni.gaben.iscat.controller.components.settings.AudioSettingsController;
import uni.gaben.iscat.controller.components.settings.DisplaySettingsController;

/**
 * Controller FXML delegato alla gestione del menu di pausa durante le sessioni di gameplay.
 * Coordina la ripresa del gioco, il rientro al menu principale, la chiusura dell'applicazione
 * e l'integrazione con i sotto-pannelli di configurazione audio ({@link AudioSettingsController})
 * e video ({@link DisplaySettingsController}). Include l'aggancio a un overlay di conferma
 * interattivo per la convalida delle azioni distruttive di uscita.
 */
public class GamePauseMenuController implements IscatFxmlController {

    /** Pulsanti di controllo per la gestione del flusso di pausa e la navigazione tra i menu. */
    @FXML private Button resumeBtn, menuBtn, quitBtn, settingsBtn;

    /** Sotto-controller iniettato per la gestione delle configurazioni video e delle modalità grafiche. */
    @FXML private DisplaySettingsController subDisplayController;

    /** Sotto-controller iniettato per la gestione dei volumi e dei canali audio. */
    @FXML private AudioSettingsController subAudioController;

    /** Contenitore a sovrapposizione (overlay) per le richieste di conferma a schermo. */
    @FXML private StackPane confirmOverlay;

    /** Controller dedicato alla logica di interazione e callback dell'overlay di conferma. */
    @FXML private ConfirmationOverlayController confirmOverlayController;

    /** Riferimento al controller principale del ciclo di gioco per la manipolazione degli stati logici. */
    private GameController gameController;

    /** Riferimento alla vista di gioco principale per le transizioni grafiche e l'apertura delle impostazioni. */
    private GameView gameView;

    /**
     * Inizializza i componenti grafici iniettati tramite FXML.
     * Applica le icone vettoriali ai pulsanti del menu e configura le interpolazioni di animazione
     * (tweening) per gli effetti visivi al passaggio del mouse (hover).
     */
    @FXML
    public void initialize() {
        ComponentsUtils.applyIconButton(resumeBtn, "fas-play");
        ComponentsUtils.applyIconButton(menuBtn,   "fas-home");
        ComponentsUtils.applyIconButton(quitBtn,   "fas-power-off");
        ComponentsUtils.applyIconButton(settingsBtn, "fas-sliders-h");

        ComponentsUtils.setupButtonHoverTween(resumeBtn);
        ComponentsUtils.setupButtonHoverTween(menuBtn);
        ComponentsUtils.setupButtonHoverTween(quitBtn);
        ComponentsUtils.setupButtonHoverTween(settingsBtn);
    }

    /**
     * Inietta i puntatori ai moduli core di gioco, configurando reattivamente le proprietà
     * del sotto-pannello video. Sincronizza i flag dei fotogrammi (FPS) e della modalità sviluppatore,
     * registrando listener bidirezionali per mantenere allineata l'interfaccia con lo stato del motore di gioco.
     *
     * @param controller Il controller logico della sessione di gioco.
     * @param view       La vista grafica associata al gameplay.
     */
    public void initData(GameController controller, GameView view) {
        this.gameController = controller;
        this.gameView       = view;

        if (subDisplayController != null) {
            subDisplayController.setConfirmOverlayController(this.confirmOverlayController);

            subDisplayController.getCheckFps().setSelected(gameController.isFpsOn());
            subDisplayController.getDebugModeCheck().setSelected(gameController.isDebugModeOn());

            subDisplayController.getCheckFps().selectedProperty().addListener((obs, oldV, newV) ->
                    gameController.setShowFps(newV));

            gameController.debugModeProperty().addListener((obs, oldV, newV) -> {
                Platform.runLater(() -> {
                    if (subDisplayController.getDebugModeCheck().isSelected() != newV) {
                        subDisplayController.getDebugModeCheck().setSelected(newV);
                    }
                });
            });

            subDisplayController.setGameController(gameController);
        }
    }

    /**
     * Sincronizza lo stato visivo dei componenti grafici (Checkbox FPS, Debug e Fullscreen)
     * leggendo direttamente i valori correnti memorizzati nel controller e nella finestra di stage JavaFX.
     */
    public void syncVisualState() {
        if (subDisplayController != null && gameController != null) {
            subDisplayController.getCheckFps().setSelected(gameController.isFpsOn());
            subDisplayController.getDebugModeCheck().setSelected(gameController.isDebugModeOn());

            if (resumeBtn != null && resumeBtn.getScene() != null) {
                javafx.stage.Stage stage = (javafx.stage.Stage) resumeBtn.getScene().getWindow();
                if (stage != null && subDisplayController.getFullscreenCheck() != null) {
                    subDisplayController.getFullscreenCheck().setSelected(stage.isFullScreen());
                }
            }
        }
    }

    /**
     * Gestisce la ripresa del gameplay ordinando alla vista di eseguire la transizione
     * verso lo stato logico successivo (annullamento della pausa tramite tasto Escape).
     */
    @FXML
    private void handleResume() {
        if (gameView != null) gameView.transitionTo(gameController.getGameModel().getGameState().onEscape());
    }

    /**
     * Gestisce la richiesta di rientro al menu principale dell'applicazione.
     * Se disponibile, attiva l'overlay di conferma informando l'utente del salvataggio dello score,
     * altrimenti procede al reindirizzamento immediato.
     */
    @FXML
    private void handleQuitToMenu() {
        if (confirmOverlayController != null) {
            confirmOverlayController.ask(
                    "Uscire dal Gioco?",
                    "Lo score della partita corrente verrà calcolato.",
                    () -> { if (gameController != null) gameController.quitToMainMenu(); }
            );
        } else {
            if (gameController != null) gameController.quitToMainMenu();
        }
    }

    /**
     * Gestisce la richiesta di chiusura forzata dell'applicazione.
     * Se disponibile, attiva l'overlay di conferma avvisando l'utente della perdita dei progressi correnti,
     * altrimenti termina direttamente il processo.
     */
    @FXML
    private void handleQuitGame() {
        if (confirmOverlayController != null) {
            confirmOverlayController.ask(
                    "Uscire dal Gioco?",
                    "I progressi della partita corrente andranno persi.",
                    () -> { if (gameController != null) gameController.quitGame(); }
            );
        } else {
            if (gameController != null) gameController.quitGame();
        }
    }

    /**
     * Innesca l'apertura del sottomenu grafico dedicato alle impostazioni generali avanzate.
     */
    @FXML
    private void openSettingsMenu() {
        if (gameView != null) gameView.openSettings();
    }

    /**
     * Metodo di interfaccia implementato da {@link IscatFxmlController}. In questo specifico
     * sotto-componente di overlay non richiede l'ancoraggio di nodi radice esterni.
     */
    @Override
    public void setPointerToView(StackPane pointer) {}
}