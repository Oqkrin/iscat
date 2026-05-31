package uni.gaben.iscat.database.dao;

import uni.gaben.iscat.screens.login.model.User;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserDAO {
    Optional<User> findByUsername(String username);

    Optional<User> findById(int id);

    boolean exists(String username);

    User create(String username, String rawPassword);

    void updateLastLogin(int userId, LocalDateTime loginTime);
}
