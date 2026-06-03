package uni.gaben.iscat.model.user;

import java.time.LocalDateTime;

public record SessionUser(
        int id,
        String username,
        LocalDateTime dateOfCreation,
        LocalDateTime lastLogin
) {
}
