package uni.gaben.iscat.universe;

import uni.gaben.iscat.universe.entity.GameEntity;

public enum UniverseSpawnable {
    PLAYER("player1"),
    ASTEROID("asteroid"),
    BLACKHOLE("blackhole"),
    HEART("heart"),
    WORM("iscat_worm_head"),
    PROJECTILE("projectile");

    private final String defaultKey;

    UniverseSpawnable(String defaultKey) {
        this.defaultKey = defaultKey;
    }

    public String getDefaultKey() {
        return defaultKey;
    }

    public static UniverseSpawnable fromString(String value) {
        if (value == null) return null;
        try {
            return UniverseSpawnable.valueOf(value.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
