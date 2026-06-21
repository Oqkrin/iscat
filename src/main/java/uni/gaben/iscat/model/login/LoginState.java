package uni.gaben.iscat.model.login;

/**
 * Enum rappresentante le fasi sequenziali del flusso di input nella schermata di login.
 * Viene utilizzata dal controller e dal modello logico per determinare quale campo di testo
 * (username o password) sia attualmente attivo, guidando la logica di validazione e le
 * animazioni di transizione dell'interfaccia utente.
 */
public enum LoginState {

    /** Fase di inserimento o verifica del nome utente. */
    USERNAME,

    /** Fase di inserimento della password associata all'account. */
    PASSWORD
}