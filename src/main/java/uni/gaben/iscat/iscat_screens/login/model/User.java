package uni.gaben.iscat.iscat_screens.login.model;

import java.time.LocalDateTime;

public record User(
        int id,
        String username,
        String passwordHash,
        LocalDateTime dateOfCreation,
        LocalDateTime lastLogin
) {
}