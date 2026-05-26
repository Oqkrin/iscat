package uni.gaben.iscat.database.interfaces;

import uni.gaben.iscat.iscat_screens.login.model.User;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UsersQueriesInterface {
    Optional<User> findByUsername(String username);

    Optional<User> findById(int id);

    boolean exists(String username);

    User create(String username, String rawPassword);

    void updateLastLogin(int userId, LocalDateTime loginTime);
}
