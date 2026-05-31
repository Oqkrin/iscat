package uni.gaben.iscat.screens.login;

import javafx.scene.input.KeyEvent;
import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.database.dao.ScoreDAO;
import uni.gaben.iscat.screens.login.model.LoginAuth;
import uni.gaben.iscat.screens.login.model.LoginModel;
import uni.gaben.iscat.screens.login.model.LoginState;
import uni.gaben.iscat.screens.login.model.SessionUser;
import uni.gaben.iscat.screens.scores.SaveData;
import uni.gaben.iscat.utils.SessionManager;
import uni.gaben.iscat.database.dao.SettingsDAO;
import uni.gaben.iscat.screens.login.model.UserSettings;
import java.util.Optional;

/**
 * Controller per la schermata di login.
 * Gestisce i buffer di input da tastiera, coordina lo stato della vista
 * e delega l'autenticazione/registrazione a LoginAuth.
 */
public class LoginController {

    private final LoginModel model;
    private final LoginAuth loginAuth;
    private final ScoreDAO scoreDAO;
    private final SettingsDAO settingsDAO;

    private final StringBuilder usernameBuffer = new StringBuilder();
    private final StringBuilder passwordBuffer = new StringBuilder();
    private LoginState currentLoginState = LoginState.USERNAME;

    public LoginController(LoginModel model, LoginAuth loginAuth) {
        this.model = model;
        this.loginAuth = loginAuth;
        this.scoreDAO = IscatDB.getInstance().getScoreDAO();
        this.settingsDAO = IscatDB.getInstance().getSettingsDAO();

        model.loginStateProperty().addListener((obs, old, isTypingPass) ->
                this.currentLoginState = Boolean.TRUE.equals(isTypingPass) ? LoginState.PASSWORD : LoginState.USERNAME);
    }

    public void onKeyPressed(KeyEvent e) {
        switch (e.getCode()) {
            case BACK_SPACE -> onBackspace();
            case ENTER      -> onEnter();
            case ESCAPE     -> onEscape();
            default         -> {}
        }
    }

    public void onKeyTyped(KeyEvent e) {
        String character = e.getCharacter();
        // Salta caratteri non stampabili (incluso ESCAPE 27 e BACKSPACE 8/127 intercettati da onKeyPressed)
        if (character == null || character.isEmpty() || character.charAt(0) < 32 || character.charAt(0) == 127 || character.charAt(0) == 27) {
            return;
        }

        if (currentLoginState == LoginState.USERNAME) {
            usernameBuffer.append(character);
        } else {
            passwordBuffer.append(character);
        }
        updateDisplay();
        checkUserExistence();
    }

    private void onBackspace() {
        if (currentLoginState == LoginState.USERNAME) {
            if (!usernameBuffer.isEmpty()) {
                usernameBuffer.setLength(usernameBuffer.length() - 1);
            }
        } else {
            if (!passwordBuffer.isEmpty()) {
                passwordBuffer.setLength(passwordBuffer.length() - 1);
            } else {
                backToUsername();
                return;
            }
        }
        updateDisplay();
        checkUserExistence();
    }

    private void onEnter() {
        if (currentLoginState == LoginState.USERNAME) {
            String u = usernameBuffer.toString().trim();
            if (!u.isEmpty()) {
                model.setLoginState(true); // Sposta il cursore sul campo password
            }
        } else {
            submitLogin();
        }
    }

    /**
     * Azione alla pressione del tasto ESCAPE.
     */
    private void onEscape() {
        if (currentLoginState == LoginState.PASSWORD) {
            backToUsername();
        }
    }

    /**
     * Svuota la password e sposta lo stato logico e visivo nuovamente sullo username.
     */
    private void backToUsername() {
        passwordBuffer.setLength(0);
        model.setLoginState(false); // Riporta lo stato (e il focus) su username
        updateDisplay();
        checkUserExistence();
    }

    private void checkUserExistence() {
        String u = usernameBuffer.toString().trim();
        boolean exists = loginAuth.exists(u);
        model.setUserExists(exists);

        if (currentLoginState == LoginState.USERNAME) {
            if (u.isEmpty()) {
                model.setStatus("");
            } else {
                model.setStatus(exists ? "giocatore esistente (premi INVIO)" : "nuovo giocatore (premi INVIO)");
            }
        } else {
            model.setStatus(exists ? "inserisci password" : "crea nuova password");
        }
    }

    private void submitLogin() {
        String u = usernameBuffer.toString().trim();
        String p = passwordBuffer.toString();

        if (p.isEmpty()) return;
        if (model.isLoggedIn()) return; // Previene input multipli ad animazione avviata

        if (loginAuth.exists(u)) {
            // Esegui flusso di Login standard
            Optional<SessionUser> sessionOpt = loginAuth.login(u, p);
            if (sessionOpt.isPresent()) {
                model.setStatus("ACCESSO IN CORSO...");

                // Salva utente loggato
                SessionUser loggedUser = sessionOpt.get();
                SessionManager.getInstance().setCurrentUser(loggedUser);

                // Carica e salva impostazioni personalizzate dell'utente dal DB
                UserSettings settings = settingsDAO.loadSettings(loggedUser.id())
                        .orElseGet(() -> {
                            settingsDAO.createDefault(loggedUser.id());
                            return settingsDAO.loadSettings(loggedUser.id()).orElse(null);
                        });
                SessionManager.getInstance().setCurrentSettings(settings);

                scoreDAO.createIfNotExists(loggedUser.id());
                Optional<SaveData> saveDataOpt = scoreDAO.load(loggedUser.id());
                SaveData saveData = saveDataOpt.orElse(new SaveData(loggedUser.id(), 0, 0, 0, 0, 0));
                SessionManager.getInstance().setCurrentSaveData(saveData);

                model.setLoggedIn(true);
            } else {
                handleError("password errata");
            }
        } else {
            // Esegui flusso di Registrazione con Login automatico integrato
            Optional<SessionUser> sessionOpt = loginAuth.register(u, p);
            if (sessionOpt.isPresent()) {
                model.setStatus("registrazione completata!");

                // Salva utente appena registrato
                SessionUser newUser = sessionOpt.get();
                SessionManager.getInstance().setCurrentUser(newUser);

                // Carica le impostazioni (generate di default nel DB durante la registrazione)
                UserSettings settings = settingsDAO.loadSettings(newUser.id())
                        .orElseGet(() -> {
                            settingsDAO.createDefault(newUser.id());
                            return settingsDAO.loadSettings(newUser.id()).orElse(null);
                        });
                SessionManager.getInstance().setCurrentSettings(settings);

                scoreDAO.createIfNotExists(newUser.id());
                Optional<SaveData> saveDataOpt = scoreDAO.load(newUser.id());
                SaveData saveData = saveDataOpt.orElse(new SaveData(newUser.id(), 0, 0, 0, 0, 0));
                SessionManager.getInstance().setCurrentSaveData(saveData);

                model.setLoggedIn(true);
            } else {
                handleError("errore di registrazione");
            }
        }
    }

    private void handleError(String message) {
        model.setStatus(message);
        model.triggerError();
        passwordBuffer.setLength(0);
        updateDisplay();
    }

    private void updateDisplay() {
        model.setUsername(usernameBuffer.toString());
        model.setPassword("*".repeat(passwordBuffer.length()));
    }

    public void reset() {
        usernameBuffer.setLength(0);
        passwordBuffer.setLength(0);
        currentLoginState = LoginState.USERNAME;
        model.reset();
    }

    public LoginModel getLoginModel() {
        return this.model;
    }
}