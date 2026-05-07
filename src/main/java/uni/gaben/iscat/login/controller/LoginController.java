package uni.gaben.iscat.login.controller;

import javafx.scene.input.KeyEvent;
import uni.gaben.iscat.IscatScenes;
import uni.gaben.iscat.IscatNavigator;
import uni.gaben.iscat.login.model.LoginData;
import uni.gaben.iscat.login.model.LoginModel;
import uni.gaben.iscat.login.model.LoginState;

public class LoginController {

    private final LoginModel model;
    private final LoginData loginData;
    private final StringBuilder usernameBuffer = new StringBuilder();
    private final StringBuilder passwordBuffer = new StringBuilder();

    private LoginState currentLoginState = LoginState.USERNAME;

    public LoginController(LoginModel model, LoginData loginData) {
        this.model = model;
        this.loginData = loginData;

        // Sincronizziamo lo stato interno se l'utente clicca sulla Scene
        model.loginStateProperty().addListener((obs, old, isTypingPass) ->
                this.currentLoginState = Boolean.TRUE.equals(isTypingPass) ? LoginState.PASSWORD : LoginState.USERNAME);
    }

    public void onKeyPressed(KeyEvent e) {
        switch (e.getCode()) {
            case BACK_SPACE -> onBackspace();
            case ENTER      -> onEnter();
            default         -> {/*non fare nulla*/}
        }
    }

    public void onKeyTyped(KeyEvent e) {
        String ch = e.getCharacter();
        // Filtriamo caratteri non stampabili (es. Enter/Backspace che gestiamo sopra)
        if (ch.isEmpty() || ch.charAt(0) < 32 || e.isControlDown()) return;

        char character = ch.charAt(0);

        if (currentLoginState == LoginState.USERNAME) {
            usernameBuffer.append(character);
        } else {
            passwordBuffer.append(character);
        }

        updateDisplay();
        checkUserExistence(); // Verifica real-time per il colore dell'icona
    }

    private void onBackspace() {
        if (currentLoginState == LoginState.USERNAME) {
            if (!usernameBuffer.isEmpty()) usernameBuffer.deleteCharAt(usernameBuffer.length() - 1);
        } else {
            if (!passwordBuffer.isEmpty()) {
                passwordBuffer.deleteCharAt(passwordBuffer.length() - 1);
            } else {
                // Se la password è vuota e premo backspace, torno allo username
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
            if (u.isEmpty()) model.setStatus("");
            else model.setStatus(exists ? "giocatore esistente (accedi) " : "nuovo giocatore (registrati)");
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
        model.setStatus(model.userExistsProperty().get() ? "inserisci password" : "imposta una nuova password");
    }

    private void submitLogin() {
        String u = usernameBuffer.toString().trim();
        String p = passwordBuffer.toString();

        if (p.isEmpty()) return;

        if (loginData.exists(u)) {
            if (loginData.checkPassword(u, p)) {
                model.setStatus("ACCESSO IN CORSO...");
                IscatNavigator.getInstance().navigateTo(IscatScenes.MENU);
            } else {
                handleError("password errata");
            }
        } else {
            // Registrazione automatica
            loginData.register(u, p);
            model.setStatus("registrazione completata!");
            IscatNavigator.getInstance().navigateTo(IscatScenes.MENU);
        }
    }

    private void handleError(String message) {
        model.setStatus(message);
        model.triggerError(); // Fa partire il blink rosso nella Scene
        passwordBuffer.setLength(0); // Pulisce la password per riprovare
        updateDisplay();
    }

    private void updateDisplay() {
        model.setUsername(usernameBuffer.toString());
        model.setPassword("*".repeat(passwordBuffer.length()));
    }
}