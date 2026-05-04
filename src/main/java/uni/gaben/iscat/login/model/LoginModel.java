package uni.gaben.iscat.login.model;

import javafx.beans.property.*;

import java.io.Serial;
import java.io.Serializable;

/** Stato osservabile della schermata di login. JavaBean + JavaFX properties. */
public class LoginModel implements Serializable {
    @Serial
    private static final long serialVersionUID = 0;

    private final StringProperty  username      = new SimpleStringProperty("");
    private final StringProperty  password      = new SimpleStringProperty("");
    private final StringProperty  status        = new SimpleStringProperty("");

    /** False when typing username True when Typing Password */
    private final BooleanProperty loginState   = new SimpleBooleanProperty(false);

    public LoginModel() {}

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

    // --- JavaBeans setters ---
    public void setUsername(String v)      { username.set(v); }
    public void setPassword(String v)      { password.set(v); }
    public void setStatus(String v)        { status.set(v); }
    public void setLoginState(boolean v)  { loginState.set(v); }
}
