package uni.gaben.iscat.model.user;

import java.time.LocalDateTime;

public record User(
        int id,
        String username,
        String passwordHash,
        LocalDateTime dateOfCreation,
        LocalDateTime lastLogin
) {
}