package uni.gaben.iscat.universe;

import uni.gaben.iscat.universe.entity.hardcoded.heart.HeartModel;
import uni.gaben.iscat.universe.entity.hardcoded.blackhole.BlackHoleModel;
import uni.gaben.iscat.universe.entity.AbstractEntityModel;
import uni.gaben.iscat.universe.entity.hardcoded.asteroid.AsteroidModel;
import uni.gaben.iscat.universe.entity.hardcoded.player.PlayerModel;
import uni.gaben.iscat.universe.entity.hardcoded.projectiles.ProjectileModel;
import uni.gaben.iscat.universe.entity.hardcoded.worm.IscatWormSegment;

public enum UniverseSpawnable {
    PLAYER(PlayerModel.class),
    ASTEROID(AsteroidModel.class),
    BLACKHOLE(BlackHoleModel.class),
    HEART(HeartModel.class),
    WORM(IscatWormSegment.class),
    PROJECTILE(ProjectileModel.class);

    private final Class<? extends AbstractEntityModel> modelClass;

    UniverseSpawnable(Class<? extends AbstractEntityModel> modelClass) {
        this.modelClass = modelClass;
    }

    public Class<? extends AbstractEntityModel> getModelClass() {
        return modelClass;
    }

    /**
     * Tenta di mappare una stringa a un'entità fissa dell'enum.
     * @return L'enum corrispondente, o null se si tratta di un'entità custom/dinamica.
     */
    public static UniverseSpawnable fromString(String value) {
        if (value == null) return null;
        try {
            return UniverseSpawnable.valueOf(value.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            // Non fa parte dell'enum rigido: è un'entità custom a runtime!
            return null;
        }
    }

    public static UniverseSpawnable fromModelClass(Class<?> modelClass) {
        for (UniverseSpawnable type : values()) {
            if (type.modelClass == modelClass) return type;
        }
        return null;
    }
}