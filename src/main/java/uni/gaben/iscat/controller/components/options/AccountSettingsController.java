package uni.gaben.iscat.controller.components.options;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import uni.gaben.iscat.IscatNavigator;
import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.database.dao.ScoreDAO;
import uni.gaben.iscat.database.dao.SettingsDAO;
import uni.gaben.iscat.database.dao.UserDAO;
import uni.gaben.iscat.model.IscatViews;
import uni.gaben.iscat.controller.components.ConfirmationOverlayController;
import uni.gaben.iscat.model.user.SessionUser;
import uni.gaben.iscat.model.user.UserSettings;
import uni.gaben.iscat.utils.AudioManager;
import uni.gaben.iscat.utils.PasswordHasher;
import uni.gaben.iscat.utils.SessionManager;

public class AccountSettingsController {

    private ConfirmationOverlayController confirmOverlayController;
    private final ScoreDAO scoreDAO;
    private final SettingsDAO settingsDAO;
    private final UserDAO userDAO;

    public AccountSettingsController() {
        this.scoreDAO = IscatDB.getInstance().getScoreDAO();
        this.settingsDAO = IscatDB.getInstance().getSettingsDAO();
        this.userDAO = IscatDB.getInstance().getUserDAO();
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

    @FXML
    void changeUsername(ActionEvent event) {
        if (confirmOverlayController == null) return;
        SessionUser currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) return;

        confirmOverlayController.askWithInput(
                "Cambia Username",
                "Inserisci il nuovo username.",
                ConfirmationOverlayController.InputType.NORMAL,
                newUsername -> {
                    String trimmed = newUsername == null ? "" : newUsername.trim();
                    if (trimmed.isEmpty() || trimmed.equalsIgnoreCase(currentUser.username())) return;

                    IscatDB.getInstance().executeAsync(() -> {
                        boolean alreadyTaken = userDAO.exists(trimmed);

                        if (alreadyTaken) {
                            Platform.runLater(() ->
                                    // MODIFICATO QUI: Appare solo OK
                                    confirmOverlayController.askWithButtons(
                                            "Username non disponibile",
                                            "'" + trimmed + "' è già in uso. Scegli un altro nome.",
                                            "OK", null,
                                            () -> {}
                                    )
                            );
                            return;
                        }

                        userDAO.updateUsername(currentUser.id(), trimmed);

                        SessionUser updated = new SessionUser(
                                currentUser.id(),
                                trimmed,
                                currentUser.dateOfCreation(),
                                currentUser.lastLogin()
                        );

                        Platform.runLater(() -> {
                            SessionManager.getInstance().setCurrentUser(updated);
                            AudioManager.getInstance().playSFX("laugh");
                        });
                    });
                }
        );
    }

    @FXML
    void changePassword(ActionEvent event) {
        if (confirmOverlayController == null) return;
        SessionUser currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) return;

        confirmOverlayController.askWithInput(
                "Cambia Password",
                "Inserisci la password attuale.",
                ConfirmationOverlayController.InputType.PASSWORD,
                currentPass -> {
                    if (currentPass == null || currentPass.isEmpty()) return;

                    IscatDB.getInstance().executeAsync(() -> {
                        var userOpt = userDAO.findByUsername(currentUser.username());
                        boolean valid = userOpt
                                .map(u -> PasswordHasher.verify(currentPass, u.passwordHash()))
                                .orElse(false);

                        if (!valid) {
                            Platform.runLater(() ->
                                    // MODIFICATO QUI: Se la password è errata mostra solo OK
                                    confirmOverlayController.askWithButtons(
                                            "Password errata",
                                            "La password attuale inserita non è corretta.",
                                            "OK", null,
                                            () -> {}
                                    )
                            );
                            return;
                        }

                        Platform.runLater(() ->
                                confirmOverlayController.askWithInput(
                                        "Nuova Password",
                                        "Inserisci la nuova password.",
                                        ConfirmationOverlayController.InputType.PASSWORD,
                                        newPass -> {
                                            if (newPass == null || newPass.isEmpty()) return;

                                            IscatDB.getInstance().executeAsync(() -> {
                                                String newHash = PasswordHasher.hash(newPass);
                                                userDAO.updatePassword(currentUser.id(), newHash);

                                                var check = userDAO.findByUsername(currentUser.username());
                                                check.ifPresent(u -> System.out.println("[DEBUG] hash nel DB dopo update: " + u.passwordHash()));

                                                Platform.runLater(() -> AudioManager.getInstance().playSFX("laugh"));
                                            });
                                        }
                                )
                        );
                    });
                }
        );
    }
}