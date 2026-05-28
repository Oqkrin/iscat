package uni.gaben.iscat.screens.login.model;

import java.time.LocalDateTime;

public record SessionUser(
        int id,
        String username,
        LocalDateTime dateOfCreation,
        LocalDateTime lastLogin
) {
}
