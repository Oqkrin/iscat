package uni.gaben.iscat.controller.components.settings;

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

/**
 * Controller per la gestione delle impostazioni dell'account utente.
 * Gestisce operazioni critiche quali il cambio di credenziali (username e password),
 * il reset dei progressi locali di gioco e l'eliminazione definitiva dell'account utente.
 * <p>
 * Tutte le interazioni con il database avvengono in modo asincrono per evitare
 * di bloccare il thread principale della UI (JavaFX Application Thread).
 */
public class AccountSettingsController {

    private ConfirmationOverlayController confirmOverlayController;
    private final ScoreDAO scoreDAO;
    private final SettingsDAO settingsDAO;
    private final UserDAO userDAO;

    /**
     * Costruisce il controller inizializzando i Data Access Object (DAO) necessari
     * al recupero e alla manipolazione dei dati persistenti.
     */
    public AccountSettingsController() {
        this.scoreDAO = IscatDB.getInstance().getScoreDAO();
        this.settingsDAO = IscatDB.getInstance().getSettingsDAO();
        this.userDAO = IscatDB.getInstance().getUserDAO();
    }

    /**
     * Associa il controller dell'overlay di conferma a questa vista per gestire
     * le finestre di dialogo interattive.
     *
     * @param controller Il controller dell'overlay di conferma.
     */
    public void setConfirmOverlayController(ConfirmationOverlayController controller) {
        this.confirmOverlayController = controller;
    }

    /**
     * Avvia la procedura per resettare i progressi di gioco dell'account corrente.
     * Richiede una conferma visiva all'utente prima di procedere in background.
     *
     * @param event L'evento di azione generato dalla UI.
     */
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

    /**
     * Avvia la procedura di eliminazione permanente dell'account utente corrente.
     * Rimuove a cascata impostazioni, punteggi e credenziali dal database, effettua
     * il logout dalla sessione attiva e reindirizza l'utente alla schermata di Login.
     *
     * @param event L'evento di azione generato dalla UI.
     */
    @FXML
    void deleteAccount(ActionEvent event) {
        if (confirmOverlayController != null) {
            confirmOverlayController.ask("Eliminare Account?", "L'azione è irreversibile.", () -> {
                UserSettings currentSettings = SessionManager.getInstance().getCurrentSettings();
                if (currentSettings == null) return;

                int userId = currentSettings.getUserId();

                IscatDB.getInstance().executeAsync(() -> {
                    // Pulizia a cascata dei dati sul DB per evitare record orfani
                    settingsDAO.delete(userId);
                    scoreDAO.reset(userId);
                    userDAO.delete(userId);

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

    /**
     * Avvia il flusso interattivo per modificare l'username dell'account corrente.
     * Include controlli di validazione asincroni sulla disponibilità del nuovo nome.
     *
     * @param event L'evento di azione generato dalla UI.
     */
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

    /**
     * Gestisce il flusso sequenziale per la modifica della password.
     * Richiede prima la password attuale per effettuare la verifica di sicurezza hash,
     * e successivamente richiede e aggiorna la nuova password nel database.
     *
     * @param event L'evento di azione generato dalla UI.
     */
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