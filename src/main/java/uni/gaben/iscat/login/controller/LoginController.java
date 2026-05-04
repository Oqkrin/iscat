package uni.gaben.iscat.login.controller;

import javafx.scene.input.KeyEvent;
import uni.gaben.iscat.login.model.LoginData;
import uni.gaben.iscat.login.model.LoginModel;
import uni.gaben.iscat.login.model.LoginState;

public class LoginController {

    private final LoginModel model;
    private final LoginData login;
    private final StringBuilder usernameBuffer = new StringBuilder();
    private final StringBuilder passwordBuffer = new StringBuilder();
    private LoginState currentLoginState = LoginState.USERNAME;

    public LoginController(LoginModel model, LoginData login) {
        this.model = model;
        this.login = login;
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
        if (ch.isEmpty() || ch.charAt(0) < 32) return;

        switch (currentLoginState) {
            case USERNAME -> {
                usernameBuffer.append(ch.charAt(0));
                model.setStatus(login.exists(usernameBuffer.toString().trim()) ? "nome utente trovato" : "registrati" );
            }
            case PASSWORD -> passwordBuffer.append(ch.charAt(0));
        }
        updateDisplay();
    }

    private void onBackspace() {

        switch (currentLoginState) {
            case USERNAME -> { if(!usernameBuffer.isEmpty()) usernameBuffer.deleteCharAt(usernameBuffer.length()-1); }
            case PASSWORD -> {
                if (!passwordBuffer.isEmpty()) passwordBuffer.deleteCharAt(passwordBuffer.length()-1);
                else currentLoginState = LoginState.USERNAME;
            }
        }

        updateDisplay();
    }

    private void onEnter() {
        switch (currentLoginState) {
            case USERNAME -> {
                String u = usernameBuffer.toString().trim();
                if (!u.isEmpty()) enterPasswordPhase(u);
            }
            case PASSWORD -> submitPassword();
        }
    }

    private void enterPasswordPhase(String username) {
        currentLoginState = LoginState.PASSWORD;
        passwordBuffer.setLength(0);
        model.setLoginState(true);
        model.setStatus(login.exists(username) ? "type password" : "set password");
        updateDisplay();
    }

    private void submitPassword() {
        String username = usernameBuffer.toString().trim();
        String password = passwordBuffer.toString();
        if (password.isEmpty()) return;

        if (login.exists(username)) {
            if (login.checkPassword(username, password)) {
                model.setStatus("INIZIALIZZANDO ISCAT");
            } else {
                model.setStatus("password errata");
                passwordBuffer.setLength(0);
                updateDisplay();
            }
        } else {
            login.register(username, password);
            model.setStatus("benvenuto, " + username);
        }
    }

    private void updateDisplay() {
        switch (currentLoginState) {
            case USERNAME -> model.setUsername(usernameBuffer.toString());
            case PASSWORD -> {
                model.setUsername(usernameBuffer.toString());
                model.setPassword("*".repeat(passwordBuffer.length()));
            }
        }
    }
}
