package uni.gaben.iscat.model.login;

import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.database.dao.UserDAO;
import uni.gaben.iscat.model.user.SessionUser;
import uni.gaben.iscat.model.user.User;
import uni.gaben.iscat.utils.PasswordHasher;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class LoginAuth {
    private final UserDAO users;

    public LoginAuth(UserDAO users) {
        this.users = users;
    }

    public CompletableFuture<Boolean> exists(String username) {
        return IscatDB.getInstance().queryAsync(() -> users.exists(username));
    }

    public CompletableFuture<Optional<SessionUser>> register(String username, String password) {
        return IscatDB.getInstance().queryAsync(() -> {
            users.create(username, password);
            return loginInternal(username, password);
        });
    }

    public CompletableFuture<Optional<SessionUser>> login(String username, String password) {
        return IscatDB.getInstance().queryAsync(() -> loginInternal(username, password));
    }

    // Helper method: runs inside the IscatDB background thread pool
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