package uni.gaben.iscat.controller.components.settings;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.stage.Stage;
import uni.gaben.iscat.controller.components.ConfirmationOverlayController;
import uni.gaben.iscat.controller.game.GameController;
import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.model.user.UserSettings;
import uni.gaben.iscat.utils.SessionManager;

/**
 * Controller per la gestione della schermata delle impostazioni video (Display Settings).
 * Coordina i controlli grafici per la visualizzazione dei fotogrammi per secondo (FPS),
 * l'attivazione della modalità schermo intero (Fullscreen) e l'abilitazione della
 * modalità di debug (Debug Mode) tramite un overlay di conferma.
 * Salva in modo asincrono le preferenze dell'utente nel database.
 */
public class DisplaySettingsController {

    /** CheckBox per abilitare o disabilitare il contatore FPS a schermo. */
    @FXML private CheckBox checkFps;

    /** CheckBox per attivare o disattivare la modalità a schermo intero. */
    @FXML private CheckBox FullscreenCheck;

    /** CheckBox per attivare o disattivare la modalità sviluppatore/debug. */
    @FXML private CheckBox DebugModeCheck;

    /** Controller per la gestione della finestra di overlay di conferma. */
    private ConfirmationOverlayController confirmOverlayController;

    /** Riferimento al controller principale del gioco per l'applicazione delle modifiche a runtime. */
    private GameController gameController;

    /**
     * Inizializza il componente leggendo le impostazioni correnti dell'utente
     * tramite il {@link SessionManager} e allineando lo stato di selezione dei CheckBox.
     */
    @FXML
    public void initialize() {
        UserSettings settings = SessionManager.getInstance().getCurrentSettings();
        if (settings != null) {
            checkFps.setSelected(settings.getShowFps() == 1);
            FullscreenCheck.setSelected(settings.getFullscreen() == 1);
            if (DebugModeCheck != null) {
                DebugModeCheck.setSelected(settings.getDebugMode() == 1);
            }
        }
    }

    /**
     * Assegna il riferimento al {@link GameController}.
     *
     * @param gameController Il controller di gioco principale.
     */
    public void setGameController(GameController gameController) {
        this.gameController = gameController;
    }

    /**
     * Assegna il riferimento al controller dell'overlay di conferma.
     * Utilizzato per richiedere l'approvazione dell'utente prima di attivare funzionalità critiche.
     *
     * @param controller Il controller dell'overlay di conferma.
     */
    public void setConfirmOverlayController(ConfirmationOverlayController controller) {
        this.confirmOverlayController = controller;
    }

    /**
     * Sincronizza lo stato dei CheckBox con le reali proprietà dello {@link Stage} dell'applicazione.
     * Registra un listener sulla proprietà fullscreen dello stage per intercettare i cambi di stato
     * repentini (es. pressione di tasti di scelta rapida di sistema) e salvare la preferenza nel database.
     *
     * @param stage Lo stage primario della finestra dell'applicazione.
     */
    public void bindDisplayProperties(Stage stage) {
        if (stage == null) return;

        UserSettings settings = SessionManager.getInstance().getCurrentSettings();

        if (settings != null) {
            if (checkFps != null) checkFps.setSelected(settings.getShowFps() == 1);
            if (FullscreenCheck != null) FullscreenCheck.setSelected(stage.isFullScreen());
            if (DebugModeCheck != null) DebugModeCheck.setSelected(settings.getDebugMode() == 1);
        } else if (FullscreenCheck != null) {
            FullscreenCheck.setSelected(stage.isFullScreen());
        }

        if (FullscreenCheck == null) return;

        stage.fullScreenProperty().addListener((obs, oldVal, newVal) -> {
            if (FullscreenCheck.isSelected() != newVal) {
                FullscreenCheck.setSelected(newVal);
            }

            UserSettings currentSettings = SessionManager.getInstance().getCurrentSettings();
            if (currentSettings != null && FullscreenCheck.getScene() != null && FullscreenCheck.getScene().getWindow() != null) {
                int fsValue = newVal ? 1 : 0;

                if (currentSettings.getFullscreen() != fsValue) {
                    currentSettings.setFullscreen(fsValue);

                    IscatDB.getInstance().executeAsync(() ->
                            IscatDB.getInstance().getSettingsDAO().updateDisplaySetting(currentSettings.getUserId(), "Fullscreen", fsValue)
                    );
                }
            }
        });
    }

    /**
     * Gestisce l'evento di interazione sul CheckBox dello schermo intero.
     * Modifica lo stato dello stage corrente in base alla selezione.
     *
     * @param event L'evento di azione generato dal CheckBox.
     */
    @FXML
    void toggleFullscreen(ActionEvent event) {
        if (FullscreenCheck.getScene() != null) {
            Stage stage = (Stage) FullscreenCheck.getScene().getWindow();
            stage.setFullScreen(FullscreenCheck.isSelected());
        }
    }

    /**
     * Gestisce l'evento di interazione sul CheckBox della visualizzazione FPS.
     * Aggiorna la memoria di sessione e persiste la modifica sul database asincronamente.
     *
     * @param event L'evento di azione generato dal CheckBox.
     */
    @FXML
    void toggleFPSVisible(ActionEvent event) {
        UserSettings settings = SessionManager.getInstance().getCurrentSettings();
        if (settings != null) {
            int fpsValue = checkFps.isSelected() ? 1 : 0;
            settings.setShowFps(fpsValue);
            IscatDB.getInstance().executeAsync(() ->
                    IscatDB.getInstance().getSettingsDAO().updateDisplaySetting(settings.getUserId(), "ShowFPS", fpsValue)
            );
        }
    }

    /**
     * Gestisce l'evento di interazione sul CheckBox della modalità di debug.
     * Se l'utente richiede l'attivazione della modalità, mostra un overlay di conferma per segnalare
     * che i punteggi e i progressi non verranno salvati. Se disattivata, ripristina lo stato originale immediatamente.
     *
     * @param event L'evento di azione generato dal CheckBox.
     */
    @FXML
    void toggleDebugMode(ActionEvent event) {
        UserSettings settings = SessionManager.getInstance().getCurrentSettings();
        if (settings == null || DebugModeCheck == null) return;

        boolean requestedState = DebugModeCheck.isSelected();

        if (requestedState) {
            DebugModeCheck.setSelected(false);

            if (confirmOverlayController != null) {
                confirmOverlayController.askWithButtons(
                        "Attivare Debug Mode?",
                        "Attivando la console di debug, i progressi di gioco e i punteggi non verranno salvati.",
                        "SÌ", "NO",
                        () -> {
                            DebugModeCheck.setSelected(true);
                            int debugValue = 1;
                            settings.setDebugMode(debugValue);

                            if (gameController != null) {
                                gameController.setShowDebugMode(true);
                            }

                            IscatDB.getInstance().executeAsync(() ->
                                    IscatDB.getInstance().getSettingsDAO().updateDisplaySetting(settings.getUserId(), "DebugMode", debugValue)
                            );
                        },
                        () -> {
                            DebugModeCheck.setSelected(false);
                        }
                );
            } else {
                DebugModeCheck.setSelected(true);
                if (gameController != null) gameController.setShowDebugMode(true);
            }
        } else {
            int debugValue = 0;
            settings.setDebugMode(debugValue);

            if (gameController != null) {
                gameController.setShowDebugMode(false);
            }

            IscatDB.getInstance().executeAsync(() ->
                    IscatDB.getInstance().getSettingsDAO().updateDisplaySetting(settings.getUserId(), "DebugMode", debugValue)
            );
        }
    }

    /** @return Il componente {@link CheckBox} relativo agli FPS. */
    public javafx.scene.control.CheckBox getCheckFps() { return checkFps; }

    /** @return Il componente {@link CheckBox} relativo alla modalità di debug. */
    public javafx.scene.control.CheckBox getDebugModeCheck() { return DebugModeCheck; }

    /** @return Il componente {@link CheckBox} relativo al fullscreen. */
    public javafx.scene.control.CheckBox getFullscreenCheck() { return FullscreenCheck; }
}