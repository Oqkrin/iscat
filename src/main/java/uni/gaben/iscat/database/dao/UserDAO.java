package uni.gaben.iscat.database.dao;

import uni.gaben.iscat.model.user.User;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * DAO per la gestione, l'autenticazione e l'anagrafica degli utenti/giocatori.
 */
public interface UserDAO {

    /** Cerca un utente nel database tramite il suo username. */
    Optional<User> findByUsername(String username);

    /** Verifica se un username è già stato registrato nel sistema. */
    boolean exists(String username);

    /** Crea e salva un nuovo utente nel database con le credenziali fornite. */
    User create(String username, String rawPassword);

    /** Aggiorna la data e l'ora dell'ultimo accesso effettuato dall'utente. */
    void updateLastLogin(int userId, LocalDateTime loginTime);

    /** Modifica l'username di un utente esistente. */
    void updateUsername(int userId, String newUsername);

    /** Sovrascrive la password dell'utente salvando il nuovo hash di sicurezza. */
    void updatePassword(int userId, String newPasswordHash);
}