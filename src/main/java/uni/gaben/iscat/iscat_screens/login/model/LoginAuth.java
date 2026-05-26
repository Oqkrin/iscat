package uni.gaben.iscat.iscat_screens.login.model;

import uni.gaben.iscat.database.interfaces.UsersQueriesInterface;
import uni.gaben.iscat.utils.PasswordHasher;

import java.time.LocalDateTime;
import java.util.Optional;

public class LoginAuth {
    private final UsersQueriesInterface users;

    public LoginAuth(UsersQueriesInterface users) {
        this.users = users;
    }

    /**
     * Verifica l'esistenza di un utente nel database.
     */
    public boolean exists(String username) {
        return users.exists(username);
    }

    /**
     * Registra un nuovo utente e ne avvia immediatamente la sessione.
     */
    public Optional<SessionUser> register(String username, String password) {
        // Crea l'utente (la query si occuperà di hashare la password internamente)
        users.create(username, password);
        // Effettua subito il login per popolare l'oggetto SessionUser
        return login(username, password);
    }

    public Optional<SessionUser> login(
            String username,
            String password
    ) {
        Optional<User> userOpt =
                users.findByUsername(username);

        if(userOpt.isEmpty()) {
            return Optional.empty();
        }

        User user = userOpt.get();

        if(!PasswordHasher.verify(
                password,
                user.passwordHash()
        )) {
            return Optional.empty();
        }

        users.updateLastLogin(
                user.id(),
                LocalDateTime.now()
        );

        return Optional.of(
                new SessionUser(
                        user.id(),
                        user.username(),
                        user.dateOfCreation(),
                        LocalDateTime.now()
                )
        );
    }
}