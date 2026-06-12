package uni.gaben.iscat.universe.entity.record;

public record IdentityData(
        String entityKey,
        String name,
        String description,
        boolean isBoss,
        boolean isEnemy
) {
    /** Convenience constructor when isEnemy is not specified (defaults to true if not player). */
    public IdentityData(String entityKey, String name, String description, boolean isBoss) {
        this(entityKey, name, description, isBoss,
                entityKey != null && !entityKey.contains("player") && !entityKey.contains("heart"));
    }
}
