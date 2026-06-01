package uni.gaben.iscat.screens.login;

import javafx.application.Platform;
import javafx.scene.input.KeyEvent;
import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.database.dao.ScoreDAO;
import uni.gaben.iscat.database.dao.SettingsDAO;
import uni.gaben.iscat.screens.login.model.LoginAuth;
import uni.gaben.iscat.screens.login.model.LoginModel;
import uni.gaben.iscat.screens.login.model.LoginState;
import uni.gaben.iscat.screens.login.model.SessionUser;
import uni.gaben.iscat.screens.scores.SaveData;
import uni.gaben.iscat.screens.login.model.UserSettings;
import uni.gaben.iscat.utils.SessionManager;

import java.util.Optional;

/**
 * Controller ottimizzato per la schermata di login.
 * Protegge il thread grafico di JavaFX delegando le operazioni sul database SQLite
 * al thread pool asincrono di IscatDB.
 */
public class LoginController {

    private static final int MAX_INPUT_LENGTH = 20; // Previene rotture di layout

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
        if (character == null || character.isEmpty() || character.charAt(0) < 32 || character.charAt(0) == 127 || character.charAt(0) == 27) {
            return;
        }

        // Controllo della lunghezza massima per salvaguardare la UI
        if (currentLoginState == LoginState.USERNAME) {
            if (usernameBuffer.length() < MAX_INPUT_LENGTH) {
                usernameBuffer.append(character);
            }
        } else {
            if (passwordBuffer.length() < MAX_INPUT_LENGTH) {
                passwordBuffer.append(character);
            }
        }
        updateDisplay();
        checkUserExistenceAsync();
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
        checkUserExistenceAsync();
    }

    private void onEnter() {
        if (currentLoginState == LoginState.USERNAME) {
            String u = usernameBuffer.toString().trim();
            if (!u.isEmpty()) {
                model.setLoginState(true);
                checkUserExistenceAsync(); // Forza aggiornamento stato scendendo sul campo pass
            }
        } else {
            submitLoginAsync();
        }
    }

    private void onEscape() {
        if (currentLoginState == LoginState.PASSWORD) {
            backToUsername();
        }
    }

    private void backToUsername() {
        passwordBuffer.setLength(0);
        model.setLoginState(false);
        updateDisplay();
        checkUserExistenceAsync();
    }

    /**
     * Controlla l'esistenza dell'utente in modo ASINCRONO.
     * Evita microscatti dell'interfaccia grafica durante la digitazione.
     */
    private void checkUserExistenceAsync() {
        String u = usernameBuffer.toString().trim();
        if (u.isEmpty()) {
            model.setUserExists(false);
            model.setStatus("");
            return;
        }

        // Interroghiamo il DB sul thread pool dedicato
        IscatDB.getInstance().queryAsync(() -> loginAuth.exists(u))
                .thenAccept(exists -> Platform.runLater(() -> {
                    // Ritorniamo sul thread di JavaFX per aggiornare il modello della UI
                    model.setUserExists(exists);
                    if (currentLoginState == LoginState.USERNAME) {
                        model.setStatus(exists ? "giocatore esistente (premi INVIO)" : "nuovo giocatore (premi INVIO)");
                    } else {
                        model.setStatus(exists ? "inserisci password" : "crea nuova password");
                    }
                }));
    }

    /**
     * Avvia il processo pesante di Login/Registrazione e caricamento dati in background.
     */
    private void submitLoginAsync() {
        String u = usernameBuffer.toString().trim();
        String p = passwordBuffer.toString();

        if (p.isEmpty() || model.isLoggedIn()) return;

        model.setStatus("ELABORAZIONE IN CORSO...");

        // Spostiamo l'intera catena di query I/O sul thread asincrono del database
        IscatDB.getInstance().queryAsync(() -> {
            boolean userExists = loginAuth.exists(u);
            Optional<SessionUser> sessionOpt = userExists ? loginAuth.login(u, p) : loginAuth.register(u, p);

            if (sessionOpt.isPresent()) {
                SessionUser user = sessionOpt.get();

                // Sincronizzazione atomica dei dati dell'utente
                UserSettings settings = settingsDAO.loadSettings(user.id()).orElseGet(() -> {
                    settingsDAO.createDefault(user.id());
                    return settingsDAO.loadSettings(user.id()).orElse(null);
                });

                scoreDAO.createIfNotExists(user.id());
                SaveData saveData = scoreDAO.load(user.id()).orElse(new SaveData(user.id(), 0, 0, 0, 0, 0));

                // Restituiamo un pacchetto completo contenente tutto il necessario per la sessione
                return new LoginResult(user, settings, saveData, userExists ? "ACCESSO EFFETTUATO!" : "REGISTRAZIONE COMPLETATA!");
            }
            return new LoginResult(userExists ? "password errata" : "errore di registrazione");
        }).thenAccept(result -> Platform.runLater(() -> {
            // Torniamo sul thread di JavaFX per applicare i risultati alla sessione e alla UI
            if (result.isSuccess()) {
                model.setStatus(result.message());

                // Iniettiamo i dati nel SessionManager in modo sicuro
                SessionManager.getInstance().setCurrentUser(result.user());
                SessionManager.getInstance().setCurrentSettings(result.settings());
                SessionManager.getInstance().setCurrentSaveData(result.saveData());

                model.setLoggedIn(true);
            } else {
                handleError(result.message());
            }
        })).exceptionally(ex -> {
            Platform.runLater(() -> handleError("Errore di connessione al database"));
            return null;
        });
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

    /** Record DTO interno per trasportare in sicurezza i dati letti dal thread DB al thread UI. */
    private record LoginResult(SessionUser user, UserSettings settings, SaveData saveData, String message, boolean isSuccess) {
        // Costruttore per esito positivo
        public LoginResult(SessionUser user, UserSettings settings, SaveData saveData, String message) {
            this(user, settings, saveData, message, true);
        }
        // Costruttore per esito negativo
        public LoginResult(String errorMessage) {
            this(null, null, null, errorMessage, false);
        }
    }
}