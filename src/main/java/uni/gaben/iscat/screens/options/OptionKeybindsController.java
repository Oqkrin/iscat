package uni.gaben.iscat.screens.options;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.input.KeyEvent;
import uni.gaben.iscat.database.dao.SettingsDAO;
import uni.gaben.iscat.screens.login.model.UserSettings;
import uni.gaben.iscat.utils.SessionManager;

public class OptionKeybindsController {
    @FXML private Button walkUp, walkDown, walkLeft, walkRight, dash1, dash2, esc;

    private Button selectedButton = null;
    private String selectedColumn = null;
    private SettingsDAO settingsDAO;

    @FXML
    public void initialize() {
        refreshButtonLabels();
    }

    public void refreshButtonLabels() {
        UserSettings settings = SessionManager.getInstance().getCurrentSettings();
        if (settings == null) return;
        if (walkUp != null) walkUp.setText(settings.getWalkUp());
        if (walkDown != null) walkDown.setText(settings.getWalkDown());
        if (walkLeft != null) walkLeft.setText(settings.getWalkLeft());
        if (walkRight != null) walkRight.setText(settings.getWalkRight());
        if (dash1 != null) dash1.setText(settings.getDash1());
        if (dash2 != null) dash2.setText(settings.getDash2());
        if (esc != null) esc.setText(settings.getPauseGame());
    }

    @FXML
    void changeControl(ActionEvent event) {
        if (selectedButton != null) refreshButtonLabels();
        selectedButton = (Button) event.getSource();
        selectedButton.setText("[ PREMI UN TASTO ]");
        if (selectedButton == walkUp) selectedColumn = "WalkUp";
        else if (selectedButton == walkDown) selectedColumn = "WalkDown";
        else if (selectedButton == walkLeft) selectedColumn = "WalkLeft";
        else if (selectedButton == walkRight) selectedColumn = "WalkRight";
        else if (selectedButton == dash1) selectedColumn = "Dash1";
        else if (selectedButton == dash2) selectedColumn = "Dash2";
        else if (selectedButton == esc) selectedColumn = "PauseGame";
    }

    public boolean handleKeyPress(KeyEvent event) {
        if (selectedButton == null || selectedColumn == null) return false;
        String pressedKey = event.getCode().toString();
        UserSettings settings = SessionManager.getInstance().getCurrentSettings();
        if (settings != null) {
            switch (selectedColumn) {
                case "WalkUp"    -> settings.setWalkUp(pressedKey);
                case "WalkDown"  -> settings.setWalkDown(pressedKey);
                case "WalkLeft"  -> settings.setWalkLeft(pressedKey);
                case "WalkRight" -> settings.setWalkRight(pressedKey);
                case "Dash1"     -> settings.setDash1(pressedKey);
                case "Dash2"     -> settings.setDash2(pressedKey);
            }
            if (settingsDAO != null) {
                settingsDAO.updateControl(settings.getUserId(), selectedColumn, pressedKey);
            } else {
                System.err.println("SettingsDAO non iniettato in OptionKeybindsController");
            }
        }
        selectedButton.setText(pressedKey);
        selectedButton = null;
        selectedColumn = null;
        return true;
    }

    @FXML
    void resetControls(ActionEvent event) {
        UserSettings settings = SessionManager.getInstance().getCurrentSettings();
        if (settings == null) return;
        int uid = settings.getUserId();
        if (settingsDAO != null) {
            settingsDAO.updateControl(uid, "WalkUp", "W");
            settingsDAO.updateControl(uid, "WalkDown", "S");
            settingsDAO.updateControl(uid, "WalkLeft", "A");
            settingsDAO.updateControl(uid, "WalkRight", "D");
            settingsDAO.updateControl(uid, "Space", "Space");
            settingsDAO.updateControl(uid, "Dash2", "Middle Mouse");
            settingsDAO.updateControl(uid, "PauseGame", "ESC");
            refreshButtonLabels();
        }
        refreshButtonLabels();
    }

    public boolean hasActiveSelection() { return selectedButton != null; }
    public void clearSelection() { selectedButton = null; selectedColumn = null; refreshButtonLabels(); }
}