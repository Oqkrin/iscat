package uni.gaben.iscat.controller.components.options;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.database.dao.SettingsDAO;
import uni.gaben.iscat.controller.components.ConfirmationOverlayController;
import uni.gaben.iscat.model.user.UserSettings;
import uni.gaben.iscat.utils.SessionManager;

/**
 * Controller per la schermata di configurazione dei comandi (keybinds).
 * Gestisce la mappatura dei tasti per i controlli di gioco.
 */
public class OptionKeybindsController {

    @FXML private Button walkUp, walkDown, walkLeft, walkRight, dash1, dash2, attack, esc;

    private ConfirmationOverlayController confirmOverlayController;
    private Button selectedButton = null;
    private String selectedColumn = null;
    private boolean isListening = false;  // Stato di ascolto per la registrazione tasti

    private final SettingsDAO settingsDAO;

    public OptionKeybindsController() {
        this.settingsDAO = IscatDB.getInstance().getSettingsDAO();
    }

    @FXML
    public void initialize() {
        refreshButtonLabels();
    }

    /**
     * Imposta il controller dell'overlay di conferma.
     */
    public void setConfirmOverlayController(ConfirmationOverlayController controller) {
        this.confirmOverlayController = controller;
    }

    /**
     * Aggiorna le etichette dei pulsanti con i valori correnti delle impostazioni.
     */
    public void refreshButtonLabels() {
        UserSettings settings = SessionManager.getInstance().getCurrentSettings();
        if (settings == null) return;

        if (walkUp != null) walkUp.setText(settings.getWalkUp());
        if (walkDown != null) walkDown.setText(settings.getWalkDown());
        if (walkLeft != null) walkLeft.setText(settings.getWalkLeft());
        if (walkRight != null) walkRight.setText(settings.getWalkRight());
        if (attack != null) attack.setText(settings.getAttack());
        if (dash1 != null) dash1.setText(settings.getDash1());
        if (dash2 != null) dash2.setText(settings.getDash2());
        if (esc != null) esc.setText(settings.getPauseGame());
    }

    /**
     * Avvia la procedura di cambio comando per il pulsante cliccato.
     */
    @FXML
    void changeControl(ActionEvent event) {
        isListening = false;
        selectedButton = null;
        selectedColumn = null;

        selectedButton = (Button) event.getSource();

        // Identifica la colonna corrispondente al pulsante selezionato
        if (selectedButton == walkUp) selectedColumn = "WalkUp";
        else if (selectedButton == walkDown) selectedColumn = "WalkDown";
        else if (selectedButton == walkLeft) selectedColumn = "WalkLeft";
        else if (selectedButton == walkRight) selectedColumn = "WalkRight";
        else if (selectedButton == attack) selectedColumn = "Attack";
        else if (selectedButton == dash1) selectedColumn = "Dash1";
        else if (selectedButton == dash2) selectedColumn = "Dash2";
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
     * Gestisce la pressione dei tasti durante la modalità di ascolto.
     * @return true se il tasto è stato processato
     */
    public boolean handleKeyPress(KeyEvent event) {
        if (!isListening || selectedButton == null || selectedColumn == null) return false;

        // ESCAPE annulla la mappatura
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
     * Gestisce la pressione del mouse durante la modalità di ascolto.
     * @return true se il click è stato processato
     */
    public boolean handleMousePress(MouseEvent event) {
        if (!isListening || selectedButton == null || selectedColumn == null) return false;

        String pressedMouse = "MOUSE" + event.getButton().name().toUpperCase();
        askForConfirmation(pressedMouse);
        return true;
    }

    /**
     * Mostra un overlay di conferma per il nuovo comando.
     * @param pendingValue Il tasto/valore da assegnare
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
     * Salva il nuovo comando nel database e aggiorna l'interfaccia.
     * @param value Il tasto/valore da salvare
     */
    private void saveBinding(String value) {
        UserSettings settings = SessionManager.getInstance().getCurrentSettings();

        if (settings != null) {
            // Aggiorna l'oggetto settings in memoria
            switch (selectedColumn) {
                case "WalkUp"    -> settings.setWalkUp(value);
                case "WalkDown"  -> settings.setWalkDown(value);
                case "WalkLeft"  -> settings.setWalkLeft(value);
                case "WalkRight" -> settings.setWalkRight(value);
                case "Attack"    -> settings.setAttack(value);
                case "Dash1"     -> settings.setDash1(value);
                case "Dash2"     -> settings.setDash2(value);
                case "PauseGame" -> settings.setPauseGame(value);
            }

            // Persiste la modifica nel database
            IscatDB.getInstance().executeAsync(() ->
                    settingsDAO.updateControl(settings.getUserId(), selectedColumn, value)
            );
        }

        // Aggiorna il testo del pulsante
        selectedButton.setText(value);

        // Chiude l'overlay se presente
        if (confirmOverlayController != null) {
            confirmOverlayController.handleBack();
        }

        // Resetta lo stato
        selectedButton = null;
        selectedColumn = null;
        isListening = false;
    }

    /**
     * Resetta tutti i comandi ai valori predefiniti.
     */
    @FXML
    void resetControls(ActionEvent event) {
        UserSettings settings = SessionManager.getInstance().getCurrentSettings();
        if (settings == null) return;
        int uid = settings.getUserId();

        // Valori predefiniti
        settings.setWalkUp("W");
        settings.setWalkDown("S");
        settings.setWalkLeft("A");
        settings.setWalkRight("D");
        settings.setAttack("MOUSEPRIMARY");
        settings.setDash1("Q");
        settings.setDash2("E");
        settings.setPauseGame("P");

        refreshButtonLabels();

        // Persiste i valori predefiniti nel database
        IscatDB.getInstance().executeAsync(() -> {
            settingsDAO.updateControl(uid, "WalkUp", "W");
            settingsDAO.updateControl(uid, "WalkDown", "S");
            settingsDAO.updateControl(uid, "WalkLeft", "A");
            settingsDAO.updateControl(uid, "WalkRight", "D");
            settingsDAO.updateControl(uid, "Attack", "MOUSEPRIMARY");
            settingsDAO.updateControl(uid, "Dash1", "Q");
            settingsDAO.updateControl(uid, "Dash2", "E");
            settingsDAO.updateControl(uid, "PauseGame", "P");
        });
    }

    /**
     * Verifica se c'è una selezione attiva.
     */
    public boolean hasActiveSelection() {
        return selectedButton != null;
    }

    /**
     * Annulla la selezione corrente e resetta l'interfaccia.
     */
    public void clearSelection() {
        selectedButton = null;
        selectedColumn = null;
        isListening = false;
        refreshButtonLabels();
    }

    /**
     * Verifica se è in ascolto per la registrazione di un tasto.
     */
    public boolean isListening() {
        return isListening;
    }
}