package uni.gaben.iscat.model.login;

import javafx.beans.property.*;

/**
 * Modello contenitore dello stato logico e reattivo associato alla schermata di autenticazione (Login).
 * Implementa il pattern misto JavaBean + JavaFX Properties per consentire il data-binding bidirezionale
 * dei campi di testo dell'interfaccia utente (username, password), il tracciamento degli stati di errore
 * e l'attivazione dei trigger per le animazioni di transizione dei menu.
 */
public class LoginModel {

    /** Proprietà osservabile che traccia l'avvenuta autenticazione con successo dell'utente nel sistema. */
    private final BooleanProperty isLoggedIn = new SimpleBooleanProperty(false);

    /** Proprietà di testo collegate ai campi di input e all'etichetta di stato informativo della UI. */
    private final StringProperty  username      = new SimpleStringProperty("");
    private final StringProperty  password      = new SimpleStringProperty("");
    private final StringProperty  status        = new SimpleStringProperty("");

    /**
     * Stato della fase di immissione dati.
     * Valutato come {@code false} durante la digitazione dello username, passa a {@code true}
     * quando il focus o il flusso si sposta sulla digitazione della password.
     */
    private final BooleanProperty loginState   = new SimpleBooleanProperty(false);

    /** Proprietà reattiva che indica se lo username digitato corrisponde a un utente registrato nel database. */
    private final BooleanProperty userExists = new SimpleBooleanProperty(false);

    /** Proprietà trigger utilizzata per segnalare e intercettare l'inserimento di credenziali errate. */
    private final BooleanProperty wrongCredentials = new SimpleBooleanProperty(false);

    /**
     * Costruisce il modello di login inizializzando le proprietà ai valori di default.
     * Le logiche di validazione, binding logico e manipolazione dei flag sono delegate al rispettivo controller.
     */
    public LoginModel() {}

    /** @return La proprietà {@link StringProperty} legata al campo username. */
    public StringProperty  usernameProperty()     { return username; }

    /** @return La proprietà {@link StringProperty} legata al campo password. */
    public StringProperty  passwordProperty()     { return password; }

    /** @return La proprietà {@link StringProperty} per i messaggi di stato o errore testuali della UI. */
    public StringProperty  statusProperty()       { return status; }

    /** @return La proprietà {@link BooleanProperty} sul focus della fase di login. */
    public BooleanProperty loginStateProperty()  { return loginState; }

    /** @return La proprietà {@link BooleanProperty} legata all'esistenza dell'account. */
    public BooleanProperty userExistsProperty() { return userExists; }

    /** @return La proprietà {@link BooleanProperty} associata alla notifica di errore credenziali. */
    public BooleanProperty wrongCredentialsProperty() { return wrongCredentials; }

    /** @return La proprietà {@link BooleanProperty} legata allo stato complessivo di avvenuto login. */
    public BooleanProperty isLoggedInProperty() { return isLoggedIn; }

    public String  getUsername()         { return username.get(); }
    public String  getPassword()         { return password.get(); }
    public String  getStatus()           { return status.get(); }
    public boolean getLoginState()       { return loginState.get(); }
    public boolean getUserExists()       { return userExists.get(); }
    public boolean getWrongCredentials() { return wrongCredentials.get(); }
    public boolean isLoggedIn()          { return isLoggedIn.get(); }

    public void setUsername(String v)          { username.set(v); }
    public void setPassword(String v)          { password.set(v); }
    public void setStatus(String v)            { status.set(v); }
    public void setLoginState(boolean v)       { loginState.set(v); }
    public void setUserExists(boolean v)       { userExists.set(v); }
    public void setWrongCredentials(boolean v) { wrongCredentials.set(v); }
    public void setLoggedIn(boolean v)         { isLoggedIn.set(v); }

    /**
     * Innesca un evento "pulse" di errore per le credenziali errate.
     * Altera temporaneamente la proprietà su {@code true} e la azzera immediatamente su {@code false},
     * permettendo ai listener grafici (es. animazioni di scuotimento o flash rossi) di riattivarsi
     * anche in caso di errori consecutivi identici.
     */
    public void triggerError() {
        wrongCredentials.set(true);
        wrongCredentials.set(false);
    }

    /**
     * Ripristina integralmente lo stato del modello di login ai valori iniziali svuotando i campi
     * di testo e azzerando i flag reattivi di errore, validazione e tracciamento sessione.
     */
    public void reset() {
        username.set("");
        password.set("");
        status.set("");
        loginState.set(false);
        userExists.set(false);
        wrongCredentials.set(false);
        isLoggedIn.set(false);
    }
}