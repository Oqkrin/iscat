package uni.gaben.iscat.screens.options;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import uni.gaben.iscat.IscatNavigator;
import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.database.dao.ScoreDAO;
import uni.gaben.iscat.database.dao.SettingsDAO;
import uni.gaben.iscat.model.IscatViews;
import uni.gaben.iscat.screens.confirmation_overlay.ConfirmationOverlayController;
import uni.gaben.iscat.screens.login.model.UserSettings;
import uni.gaben.iscat.utils.AudioManager;
import uni.gaben.iscat.utils.SessionManager;

public class OptionAccountController {

    private ConfirmationOverlayController confirmOverlayController;
    private final ScoreDAO scoreDAO;
    private final SettingsDAO settingsDAO;

    // Inizializzazione corretta tramite costruttore
    public OptionAccountController() {
        this.scoreDAO = IscatDB.getInstance().getScoreDAO();
        this.settingsDAO = IscatDB.getInstance().getSettingsDAO();
    }

    public void setConfirmOverlayController(ConfirmationOverlayController controller) {
        this.confirmOverlayController = controller;
    }

    @FXML
    void resetAccount(ActionEvent event) {
        if (confirmOverlayController != null) {
            confirmOverlayController.ask("Resettare Account?", "I progressi locali verranno azzerati.", () -> {
                UserSettings settings = SessionManager.getInstance().getCurrentSettings();
                if (settings != null) {
                    int userId = settings.getUserId();

                    // Esecuzione asincrona per evitare blocchi della UI
                    IscatDB.getInstance().executeAsync(() -> {
                        scoreDAO.reset(userId);
                        var updatedSave = scoreDAO.load(userId);

                        Platform.runLater(() -> {
                            updatedSave.ifPresent(saveData ->
                                    SessionManager.getInstance().setCurrentSaveData(saveData)
                            );
                            AudioManager.getInstance().playSFX("laugh");
                        });
                    });
                }
            });
        }
    }

    @FXML
    void deleteAccount(ActionEvent event) {
        if (confirmOverlayController != null) {
            confirmOverlayController.ask("Eliminare Account?", "L'azione è irreversibile.", () -> {
                UserSettings currentSettings = SessionManager.getInstance().getCurrentSettings();
                if (currentSettings == null) return;

                int userId = currentSettings.getUserId();

                // Esecuzione asincrona
                IscatDB.getInstance().executeAsync(() -> {
                    settingsDAO.delete(userId);

                    Platform.runLater(() -> {
                        SessionManager.getInstance().setCurrentUser(null);
                        SessionManager.getInstance().setCurrentSettings(null);
                        SessionManager.getInstance().setCurrentSaveData(null);
                        AudioManager.getInstance().playSFX("laugh");
                        IscatNavigator.getInstance().navigateWithFade(IscatViews.LOGIN_MENU);
                    });
                });
            });
        }
    }

    @FXML void changeUsername(ActionEvent event) { /* TODO */ }
    @FXML void changePassword(ActionEvent event) { /* TODO */ }
}