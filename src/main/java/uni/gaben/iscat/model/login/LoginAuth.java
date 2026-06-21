package uni.gaben.iscat.model.login;

import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.database.dao.UserDAO;
import uni.gaben.iscat.model.user.SessionUser;
import uni.gaben.iscat.model.user.User;
import uni.gaben.iscat.utils.PasswordHasher;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Modello e servizio per la gestione dell'autenticazione, della registrazione e della sicurezza degli account.
 * Interfaccia i flussi logici di login con la persistenza dei dati tramite il {@link UserDAO},
 * eseguendo i controlli crittografici di verifica hash ({@link PasswordHasher}) e le query di scrittura
 * in modalità asincrona mediante i thread di background di {@link IscatDB}.
 */
public class LoginAuth {

    /** Data Access Object (DAO) per l'interfacciamento e la manipolazione dei record degli utenti. */
    private final UserDAO users;

    /**
     * Costruisce il modulo di autenticazione legandolo al DAO specifico degli utenti.
     *
     * @param users Il DAO degli utenti di riferimento.
     */
    public LoginAuth(UserDAO users) {
        this.users = users;
    }

    /**
     * Verifica asincronamente se un determinato nome utente è già registrato nel database.
     *
     * @param username Il nome utente da controllare.
     * @return Un {@link CompletableFuture} che conterrà {@code true} se lo username esiste, {@code false} altrimenti.
     */
    public CompletableFuture<Boolean> exists(String username) {
        return IscatDB.getInstance().queryAsync(() -> users.exists(username));
    }

    /**
     * Esegue asincronamente la registrazione di un nuovo account e, in caso di successo, effettua
     * automaticamente il login interno restituendo l'utente di sessione.
     *
     * @param username Il nome utente scelto per il nuovo account.
     * @param password La password in chiaro, che verrà cifrata prima della persistenza.
     * @return Un {@link CompletableFuture} contenente un {@link Optional} con il {@link SessionUser} allocato,
     * oppure un optional vuoto in caso di fallimento.
     */
    public CompletableFuture<Optional<SessionUser>> register(String username, String password) {
        return IscatDB.getInstance().queryAsync(() -> {
            users.create(username, password);
            return loginInternal(username, password);
        });
    }

    /**
     * Avvia il processo asincrono di login per verificare le credenziali fornite dall'utente.
     *
     * @param username Il nome utente inserito.
     * @param password La password in chiaro da sottoporre a verifica.
     * @return Un {@link CompletableFuture} contenente un {@link Optional} con il {@link SessionUser} se le credenziali
     * sono corrette, oppure {@link Optional#empty()} se l'utente non esiste o la password è errata.
     */
    public CompletableFuture<Optional<SessionUser>> login(String username, String password) {
        return IscatDB.getInstance().queryAsync(() -> loginInternal(username, password));
    }

    /**
     * Routine interna di autenticazione eseguita nel pool di thread di background del database.
     * Recupera il profilo utente, convalida la corrispondenza dell'impronta crittografica della password
     * e aggiorna il timestamp dell'ultimo accesso in caso di esito positivo.
     *
     * @param username Il nome utente da autenticare.
     * @param password La password in chiaro da verificare.
     * @return Un {@link Optional} popolato con il record {@link SessionUser} se l'autenticazione ha successo,
     * altrimenti un {@link Optional#empty()}.
     */
    private Optional<SessionUser> loginInternal(String username, String password) {
        Optional<User> userOpt = users.findByUsername(username);

        if (userOpt.isEmpty() || !PasswordHasher.verify(password, userOpt.get().passwordHash())) {
            return Optional.empty();
        }

        User user = userOpt.get();
        users.updateLastLogin(user.id(), LocalDateTime.now());

        return Optional.of(new SessionUser(
                user.id(),
                user.username(),
                user.dateOfCreation(),
                LocalDateTime.now()
        ));
    }
}