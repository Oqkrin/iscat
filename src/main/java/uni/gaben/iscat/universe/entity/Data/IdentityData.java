package uni.gaben.iscat.universe.entity.Data;

public record IdentityData(
        String entityKey,
        String name,
        String description,
        boolean isBoss,
        EntityType type,
        String ownerId
) {
    /** Convenience methods */
    public boolean isEnemy() {
        return type == EntityType.ENEMY;
    }
    public boolean isPlayer() {
        return type == EntityType.PLAYER;
    }
    public boolean isProjectile() {
        return type == EntityType.PROJECTILE;
    }
}
