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

public class KeybindsSettingsController {

    @FXML private VBox mainContainer;
    @FXML private Button walkUp, walkDown, walkLeft, walkRight, dash1, dash2, attack, esc;

    private ConfirmationOverlayController confirmOverlayController;
    private Button selectedButton = null;
    private String selectedColumn = null;
    private boolean isListening = false;

    private final SettingsDAO settingsDAO;

    public KeybindsSettingsController() {
        this.settingsDAO = IscatDB.getInstance().getSettingsDAO();
    }

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

    public void setConfirmOverlayController(ConfirmationOverlayController controller) {
        this.confirmOverlayController = controller;
    }

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

    public boolean handleMousePress(MouseEvent event) {
        if (!isListening || selectedButton == null || selectedColumn == null) return false;

        String pressedMouse = "MOUSE" + event.getButton().name().toUpperCase();
        askForConfirmation(pressedMouse);
        return true;
    }

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

    private void saveBinding(String value) {
        UserSettings settings = SessionManager.getInstance().getCurrentSettings();

        if (settings != null) {
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
        settings.setDash1("Q");
        settings.setDash2("E");
        settings.setPauseGame("P");

        refreshButtonLabels();

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

    public boolean hasActiveSelection() {
        return selectedButton != null;
    }

    public void clearSelection() {
        selectedButton = null;
        selectedColumn = null;
        isListening = false;
        refreshButtonLabels();
    }

    public boolean isListening() {
        return isListening;
    }
}