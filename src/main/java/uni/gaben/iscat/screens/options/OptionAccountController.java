package uni.gaben.iscat.screens.options;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import uni.gaben.iscat.IscatNavigator;
import uni.gaben.iscat.database.dao.ScoreDAO;
import uni.gaben.iscat.database.sqlite.SettingsDAO;
import uni.gaben.iscat.model.IscatViews;
import uni.gaben.iscat.screens.confirmation_overlay.ConfirmationOverlayController;
import uni.gaben.iscat.screens.login.model.UserSettings;
import uni.gaben.iscat.utils.AudioManager;
import uni.gaben.iscat.utils.SessionManager;

public class OptionAccountController {

    private ConfirmationOverlayController confirmOverlayController;
    private ScoreDAO scoreDAO;

    public void setConfirmOverlayController(ConfirmationOverlayController controller) {
        this.confirmOverlayController = controller;
    }

    public void setScoreDAO(ScoreDAO scoreDAO) {
        this.scoreDAO = scoreDAO;
    }

    @FXML
    void resetAccount(ActionEvent event) {
        if (confirmOverlayController != null) {
            confirmOverlayController.ask("Resettare Account?", "I progressi locali verranno azzerati.", () -> {
                UserSettings settings = SessionManager.getInstance().getCurrentSettings();
                if (settings != null) {
                    scoreDAO.reset(settings.getUserId());
                    scoreDAO.load(settings.getUserId()).ifPresent(saveData ->
                            SessionManager.getInstance().setCurrentSaveData(saveData)
                    );

                    AudioManager.getInstance().playSFX("laugh");
                }
            });
        }
    }

    @FXML
    void deleteAccount(ActionEvent event) {
        if (confirmOverlayController != null) {
            confirmOverlayController.ask("Eliminare Account?", "L'azione è irreversibile.", () -> {
                int userId = SessionManager.getInstance().getCurrentSettings().getUserId();
                if (userId != -1) {
                    SettingsDAO.delete(userId);
                    SessionManager.getInstance().setCurrentUser(null);
                    SessionManager.getInstance().setCurrentSettings(null);
                    SessionManager.getInstance().setCurrentSaveData(null);
                    AudioManager.getInstance().playSFX("laugh");
                    IscatNavigator.getInstance().navigateWithFade(IscatViews.LOGIN_MENU);
                }
            });
        }
    }
}