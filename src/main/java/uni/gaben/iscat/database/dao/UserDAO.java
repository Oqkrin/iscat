package uni.gaben.iscat.database.dao;

import uni.gaben.iscat.model.user.User;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserDAO {
    Optional<User> findByUsername(String username);

    Optional<User> findById(int id);

    boolean exists(String username);

    User create(String username, String rawPassword);

    void updateLastLogin(int userId, LocalDateTime loginTime);

    void updateUsername(int userId, String newUsername);

    void updatePassword(int userId, String newPasswordHash);
}