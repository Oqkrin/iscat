package uni.gaben.iscat.menus.login_menu.model;

import javafx.beans.property.*;

/** Stato osservabile della schermata di login. JavaBean + JavaFX properties. */
public class LoginModel {

    private final BooleanProperty isLoggedIn = new SimpleBooleanProperty(false);

    private final StringProperty  username      = new SimpleStringProperty("");
    private final StringProperty  password      = new SimpleStringProperty("");
    private final StringProperty  status        = new SimpleStringProperty("");

    /** False when typing username True when Typing Password */
    private final BooleanProperty loginState   = new SimpleBooleanProperty(false);

    private final BooleanProperty userExists = new SimpleBooleanProperty(false);
    private final BooleanProperty wrongCredentials = new SimpleBooleanProperty(false);

    public LoginModel() {/*gestiti da controller*/}

    // --- JavaFX property accessors ---
    public StringProperty  usernameProperty()     { return username; }
    public StringProperty  passwordProperty()     { return password; }
    public StringProperty  statusProperty()       { return status; }
    public BooleanProperty loginStateProperty()  { return loginState; }

    // --- JavaBeans getters ---
    public String  getUsername()      { return username.get(); }
    public String  getPassword()      { return password.get(); }
    public String  getStatus()        { return status.get(); }
    public boolean getLoginState()   { return loginState.get(); }
    public boolean getUserExists() { return userExists.get(); }
    public boolean getWrongCredentials() { return wrongCredentials.get(); }

    // --- JavaBeans setters ---
    public void setUsername(String v)      { username.set(v); }
    public void setPassword(String v)      { password.set(v); }
    public void setStatus(String v)        { status.set(v); }
    public void setLoginState(boolean v)  { loginState.set(v); }
    public void setUserExists(boolean v) { userExists.set(v); }
    public void setWrongCredentials(boolean v) { wrongCredentials.set(v); }

    public BooleanProperty userExistsProperty() {
        return userExists;
    }
    public BooleanProperty wrongCredentialsProperty() {
        return wrongCredentials;
    }
    public void triggerError() {
        wrongCredentials.set(true);
        wrongCredentials.set(false); // Reset immediato per permettere trigger successivi
    }

    // Metodi per l'animazione del login menu
    public BooleanProperty isLoggedInProperty() { return isLoggedIn; }
    public boolean isLoggedIn() { return isLoggedIn.get(); }
    public void setLoggedIn(boolean v) { isLoggedIn.set(v); }
}
