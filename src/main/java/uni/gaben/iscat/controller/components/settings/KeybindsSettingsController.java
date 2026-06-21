package uni.gaben.iscat.controller.components.settings;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.database.dao.SettingsDAO;
import uni.gaben.iscat.controller.components.ConfirmationOverlayController;
import uni.gaben.iscat.model.user.UserSettings;
import uni.gaben.iscat.utils.SessionManager;

/**
 * Controller per la gestione della schermata di configurazione dei comandi (Keybinds).
 * Permette la riassegnazione dinamica dei tasti di movimento, attacco, scatto (dash) e pausa.
 * Cattura gli input da tastiera e mouse a runtime e persiste le modifiche nel database in modo asincrono.
 */
public class KeybindsSettingsController {

    /** Contenitore principale dell'interfaccia delle impostazioni dei controlli. */
    @FXML private VBox mainContainer;

    /** Pulsanti dell'interfaccia associati ai singoli comandi di gioco configurabili. */
    @FXML private Button walkUp, walkDown, walkLeft, walkRight, dash, bulletTime, attack, esc;

    /** Controller dell'overlay di interfaccia per la gestione delle conferme e dell'ascolto tasti. */
    private ConfirmationOverlayController confirmOverlayController;

    /** Riferimento al pulsante attualmente selezionato per la riassegnazione. */
    private Button selectedButton = null;

    /** Nome della colonna del database o campo di impostazione associato al comando selezionato. */
    private String selectedColumn = null;

    /** Flag di stato che indica se il controller è attivamente in ascolto di un input (tasto o mouse). */
    private boolean isListening = false;

    /** Data Access Object per la persistenza delle modifiche ai controlli nel database. */
    private final SettingsDAO settingsDAO;

    /**
     * Costruttore della classe. Inizializza il DAO dedicato per la gestione delle query sui controlli.
     */
    public KeybindsSettingsController() {
        this.settingsDAO = IscatDB.getInstance().getSettingsDAO();
    }

    /**
     * Inizializza il componente aggiornando le etichette dei pulsanti con i valori attuali.
     * Registra listener di visibilità e scena per forzare il refresh visivo dei testi
     * non appena il pannello torna visibile a schermo.
     */
    @FXML
    public void initialize() {
        refreshButtonLabels();

        mainContainer.visibleProperty().addListener((obs, wasVisible, isNowVisible) -> {
            if (isNowVisible) {
                Platform.runLater(this::refreshButtonLabels);
            }
        });

        mainContainer.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null && mainContainer.isVisible()) {
                Platform.runLater(this::refreshButtonLabels);
            }
        });
    }

    /**
     * Assegna il riferimento al controller dell'overlay di conferma.
     *
     * @param controller Il controller dell'overlay per dialoghi di associazione.
     */
    public void setConfirmOverlayController(ConfirmationOverlayController controller) {
        this.confirmOverlayController = controller;
    }

    /**
     * Sincronizza i testi visualizzati su ciascun pulsante con le stringhe dei comandi
     * attualmente salvate nella sessione utente corrente.
     */
    public void refreshButtonLabels() {
        UserSettings settings = SessionManager.getInstance().getCurrentSettings();
        if (settings == null) return;

        if (walkUp != null) walkUp.setText(settings.getWalkUp());
        if (walkDown != null) walkDown.setText(settings.getWalkDown());
        if (walkLeft != null) walkLeft.setText(settings.getWalkLeft());
        if (walkRight != null) walkRight.setText(settings.getWalkRight());
        if (attack != null) attack.setText(settings.getAttack());
        if (dash != null) dash.setText(settings.getDash());
        if (bulletTime != null) bulletTime.setText(settings.getBulletTime());
        if (esc != null) esc.setText(settings.getPauseGame());
    }

    /**
     * Gestisce la pressione di un pulsante di comando per avviarne la modifica.
     * Identifica il comando selezionato, mappa la colonna database corrispondente
     * e apre l'overlay di richiesta input impostandosi in modalità di ascolto.
     *
     * @param event L'evento di azione generato dal pulsante cliccato.
     */
    @FXML
    void changeControl(ActionEvent event) {
        isListening = false;
        selectedButton = null;
        selectedColumn = null;

        selectedButton = (Button) event.getSource();

        if (selectedButton == walkUp) selectedColumn = "WalkUp";
        else if (selectedButton == walkDown) selectedColumn = "WalkDown";
        else if (selectedButton == walkLeft) selectedColumn = "WalkLeft";
        else if (selectedButton == walkRight) selectedColumn = "WalkRight";
        else if (selectedButton == attack) selectedColumn = "Attack";
        else if (selectedButton == dash) selectedColumn = "Dash";
        else if (selectedButton == bulletTime) selectedColumn = "BulletTime";
        else if (selectedButton == esc) selectedColumn = "PauseGame";

        if (confirmOverlayController != null) {
            confirmOverlayController.askForKeybind(
                    "Mappatura Comando",
                    "Clicca il pulsante centrale per registrare il comando.",
                    () -> {
                        isListening = true;
                        confirmOverlayController.setKeybindBtnText("... IN ASCOLTO ...");
                    },
                    this::clearSelection
            );
        }
    }

    /**
     * Intercetta ed elabora gli input da tastiera quando il controller è in modalità di ascolto.
     * Se viene premuto il tasto ESCAPE annulla la selezione, altrimenti cattura il codice del tasto
     * e richiede la conferma di assegnazione.
     *
     * @param event L'evento di pressione del tasto.
     * @return {@code true} se l'input è stato gestito e consumato, {@code false} altrimenti.
     */
    public boolean handleKeyPress(KeyEvent event) {
        if (!isListening || selectedButton == null || selectedColumn == null) return false;

        if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
            clearSelection();
            if (confirmOverlayController != null) confirmOverlayController.handleBack();
            return true;
        }

        String pressedKey = event.getCode().toString().toUpperCase();
        askForConfirmation(pressedKey);
        return true;
    }

    /**
     * Intercetta ed elabora gli input del mouse (pressione dei tasti) quando il controller è in ascolto.
     * Converte l'identificativo del tasto cliccato nel formato testuale standard e richiede la conferma.
     *
     * @param event L'evento di pressione del mouse.
     * @return {@code true} se l'input è stato gestito e consumato, {@code false} altrimenti.
     */
    public boolean handleMousePress(MouseEvent event) {
        if (!isListening || selectedButton == null || selectedColumn == null) return false;

        String pressedMouse = "MOUSE" + event.getButton().name().toUpperCase();
        askForConfirmation(pressedMouse);
        return true;
    }

    /**
     * Sospende la modalità di ascolto e richiede all'utente una conferma esplicita
     * tramite popup prima di salvare definitivamente la nuova mappatura dei controlli.
     *
     * @param pendingValue La stringa che rappresenta l'input catturato e in attesa di conferma.
     */
    private void askForConfirmation(String pendingValue) {
        isListening = false;

        if (confirmOverlayController != null) {
            confirmOverlayController.askWithButtons(
                    "Conferma Comando",
                    "Vuoi assegnare il tasto '" + pendingValue + "' a questo comando?",
                    "OK",
                    "ANNULLA",
                    () -> saveBinding(pendingValue),
                    this::clearSelection
            );
        } else {
            saveBinding(pendingValue);
        }
    }

    /**
     * Aggiorna il modello dei dati in sessione, persiste asincronamente la nuova configurazione
     * sul database ed esce dall'overlay di inserimento, pulendo lo stato di selezione.
     *
     * @param value Il valore testuale del nuovo comando da memorizzare.
     */
    private void saveBinding(String value) {
        UserSettings settings = SessionManager.getInstance().getCurrentSettings();

        if (settings != null) {
            switch (selectedColumn) {
                case "WalkUp"    -> settings.setWalkUp(value);
                case "WalkDown"  -> settings.setWalkDown(value);
                case "WalkLeft"  -> settings.setWalkLeft(value);
                case "WalkRight" -> settings.setWalkRight(value);
                case "Attack"    -> settings.setAttack(value);
                case "Dash"      -> settings.setDash(value);
                case "BulletTime"     -> settings.setBulletTime(value);
                case "PauseGame" -> settings.setPauseGame(value);
            }

            IscatDB.getInstance().executeAsync(() ->
                    settingsDAO.updateControl(settings.getUserId(), selectedColumn, value)
            );
        }

        selectedButton.setText(value);

        if (confirmOverlayController != null) {
            confirmOverlayController.handleBack();
        }

        selectedButton = null;
        selectedColumn = null;
        isListening = false;
    }

    /**
     * Ripristina tutti i comandi di gioco ai valori predefiniti (layout standard WASD + Mouse).
     * Sincronizza i testi dei pulsanti e invia un aggiornamento cumulativo e asincrono al database.
     *
     * @param event L'evento di azione attivato dal pulsante di reset.
     */
    @FXML
    void resetControls(ActionEvent event) {
        UserSettings settings = SessionManager.getInstance().getCurrentSettings();
        if (settings == null) return;
        int uid = settings.getUserId();

        settings.setWalkUp("W");
        settings.setWalkDown("S");
        settings.setWalkLeft("A");
        settings.setWalkRight("D");
        settings.setAttack("MOUSEPRIMARY");
        settings.setDash("SPACE");
        settings.setBulletTime("MOUSESECONDARY");
        settings.setPauseGame("P");

        refreshButtonLabels();

        IscatDB.getInstance().executeAsync(() -> {
            settingsDAO.updateControl(uid, "WalkUp", "W");
            settingsDAO.updateControl(uid, "WalkDown", "S");
            settingsDAO.updateControl(uid, "WalkLeft", "A");
            settingsDAO.updateControl(uid, "WalkRight", "D");
            settingsDAO.updateControl(uid, "Attack", "MOUSEPRIMARY");
            settingsDAO.updateControl(uid, "Dash1", "SPACE");
            settingsDAO.updateControl(uid, "BulletTime", "MOUSESECONDARY");
            settingsDAO.updateControl(uid, "PauseGame", "P");
        });
    }

    /**
     * Verifica se vi è un pulsante attualmente selezionato e in fase di configurazione.
     *
     * @return {@code true} se c'è una selezione attiva, {@code false} altrimenti.
     */
    public boolean hasActiveSelection() {
        return selectedButton != null;
    }

    /**
     * Annulla lo stato corrente di selezione e di ascolto input,
     * ripristinando la visualizzazione originale delle etichette dei controlli.
     */
    public void clearSelection() {
        selectedButton = null;
        selectedColumn = null;
        isListening = false;
        refreshButtonLabels();
    }

    /**
     * Indica se il sistema è attualmente in attesa di ricevere un input dall'utente.
     *
     * @return {@code true} se il flag di ascolto è attivo, {@code false} altrimenti.
     */
    public boolean isListening() {
        return isListening;
    }
}