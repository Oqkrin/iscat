package uni.gaben.iscat.menus.login_menu;

import javafx.scene.input.KeyEvent;

/**
 * Controller per la schermata di login.
 * Gestisce input da tastiera, validazione e navigazione.
 */
public class LoginController {

    private final LoginModel model;
    private final LoginData loginData;
    private final StringBuilder usernameBuffer = new StringBuilder();
    private final StringBuilder passwordBuffer = new StringBuilder();

    private LoginState currentLoginState = LoginState.USERNAME;

    public LoginController(LoginModel model, LoginData loginData) {
        this.model = model;
        this.loginData = loginData;

        // Sincronizza stato interno con model
        model.loginStateProperty().addListener((obs, old, isTypingPass) ->
                this.currentLoginState = Boolean.TRUE.equals(isTypingPass) ? LoginState.PASSWORD : LoginState.USERNAME);
    }

    public void onKeyPressed(KeyEvent e) {
        switch (e.getCode()) {
            case BACK_SPACE -> onBackspace();
            case ENTER      -> onEnter();
            default         -> {}
        }
    }

    public void onKeyTyped(KeyEvent e) {
        String ch = e.getCharacter();
        if (ch.isEmpty() || ch.charAt(0) < 32 || e.isControlDown()) return;

        char character = ch.charAt(0);

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
                usernameBuffer.deleteCharAt(usernameBuffer.length() - 1);
            }
        } else {
            if (!passwordBuffer.isEmpty()) {
                passwordBuffer.deleteCharAt(passwordBuffer.length() - 1);
            } else {
                // Backspace su password vuota torna a username
                switchToUsername();
            }
        }
        updateDisplay();
        checkUserExistence();
    }

    private void onEnter() {
        if (currentLoginState == LoginState.USERNAME) {
            String u = usernameBuffer.toString().trim();
            if (!u.isEmpty()) switchToPassword();
        } else {
            submitLogin();
        }
    }

    private void checkUserExistence() {
        String u = usernameBuffer.toString().trim();
        boolean exists = loginData.exists(u);
        model.setUserExists(exists);

        if (currentLoginState == LoginState.USERNAME) {
            if (u.isEmpty()) {
                model.setStatus("");
            } else {
                model.setStatus(exists ? "giocatore esistente (accedi)" : "nuovo giocatore (registrati)");
            }
        }
    }

    private void switchToUsername() {
        currentLoginState = LoginState.USERNAME;
        model.setLoginState(false);
        checkUserExistence();
    }

    private void switchToPassword() {
        currentLoginState = LoginState.PASSWORD;
        model.setLoginState(true);
        model.setStatus(model.userExistsProperty().get() ? "inserisci password" : "crea nuova password");
    }

    private void submitLogin() {
        String u = usernameBuffer.toString().trim();
        String p = passwordBuffer.toString();

        if (p.isEmpty()) return;

        // Impediamo doppie sottomissioni durante l'animazione
        if (model.isLoggedIn()) return;

        if (loginData.exists(u)) {
            if (loginData.checkPassword(u, p)) {
                model.setStatus("ACCESSO IN CORSO...");
                // Scatena l'animazione nella Scene tramite il listener nel model
                model.setLoggedIn(true);
            } else {
                handleError("password errata");
            }
        } else {
            loginData.register(u, p);
            model.setStatus("registrazione completata!");
            model.setLoggedIn(true);
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
        return model;
    }
}
