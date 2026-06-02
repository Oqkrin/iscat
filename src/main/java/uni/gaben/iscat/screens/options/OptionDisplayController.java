package uni.gaben.iscat.screens.options;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.stage.Stage;
import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.screens.login.model.UserSettings;
import uni.gaben.iscat.utils.SessionManager;

public class OptionDisplayController {

    @FXML private CheckBox checkFps;
    @FXML private CheckBox FullscreenCheck;
    @FXML private CheckBox DebugModeCheck;

    @FXML
    public void initialize() {
        UserSettings settings = SessionManager.getInstance().getCurrentSettings();
        if (settings != null) {
            checkFps.setSelected(settings.getShowFps() == 1);
            FullscreenCheck.setSelected(settings.getFullscreen() == 1);
        }
    }

    /**
     * Sincronizza in modo sicuro ed efficiente lo stato del Fullscreen tra Finestra e Database.
     */
    public void bindFullscreenProperty(Stage stage) {
        if (stage == null || FullscreenCheck == null) return;

        UserSettings settings = SessionManager.getInstance().getCurrentSettings();

        if (settings != null) {
            stage.setFullScreen(settings.getFullscreen() == 1);
        }

        stage.fullScreenProperty().addListener((obs, oldVal, newVal) -> {
            FullscreenCheck.setSelected(newVal);
            if (settings != null) {
                int fsValue = newVal ? 1 : 0;
                settings.setFullscreen(fsValue);
                IscatDB.getInstance().executeAsync(() ->
                        IscatDB.getInstance().getSettingsDAO().updateDisplaySetting(settings.getUserId(), "Fullscreen", fsValue)
                );
            }
        });
    }

    @FXML
    void toggleFullscreen(ActionEvent event) {
        if (FullscreenCheck.getScene() != null) {
            Stage stage = (Stage) FullscreenCheck.getScene().getWindow();
            stage.setFullScreen(FullscreenCheck.isSelected());
        }
    }

    @FXML
    void toggleFPSVisible(ActionEvent event) {
        UserSettings settings = SessionManager.getInstance().getCurrentSettings();
        if (settings != null) {
            int fpsValue = checkFps.isSelected() ? 1 : 0;
            settings.setShowFps(fpsValue);

            // TODO: connettere al game

            IscatDB.getInstance().executeAsync(() ->
                    IscatDB.getInstance().getSettingsDAO().updateDisplaySetting(settings.getUserId(), "ShowFPS", fpsValue)
            );
        }
    }

    @FXML
    void toggleDebugMode(ActionEvent event) {
    }

    public javafx.scene.control.CheckBox getCheckFps() {
        return checkFps;
    }

    public javafx.scene.control.CheckBox getDebugModeCheck() {
        return DebugModeCheck;
    }
}