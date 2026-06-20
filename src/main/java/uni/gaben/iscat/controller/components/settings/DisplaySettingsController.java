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

public class DisplaySettingsController {

    @FXML private CheckBox checkFps;
    @FXML private CheckBox FullscreenCheck;
    @FXML private CheckBox DebugModeCheck;

    private ConfirmationOverlayController confirmOverlayController;

    private GameController gameController;

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

    public void setGameController(GameController gameController) {
        this.gameController = gameController;
    }

    public void setConfirmOverlayController(ConfirmationOverlayController controller) {
        this.confirmOverlayController = controller;
    }

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
            IscatDB.getInstance().executeAsync(() ->
                    IscatDB.getInstance().getSettingsDAO().updateDisplaySetting(settings.getUserId(), "ShowFPS", fpsValue)
            );
        }
    }

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

    public javafx.scene.control.CheckBox getCheckFps() { return checkFps; }
    public javafx.scene.control.CheckBox getDebugModeCheck() { return DebugModeCheck; }
    public javafx.scene.control.CheckBox getFullscreenCheck() { return FullscreenCheck; }
}