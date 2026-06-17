package uni.gaben.iscat.universe.spawn;

import uni.gaben.iscat.universe.entities.EntityModel;
import uni.gaben.iscat.universe.entities.hardcoded.heart.HeartModel;
import uni.gaben.iscat.universe.entities.hardcoded.blackhole.BlackHoleModel;
import uni.gaben.iscat.universe.entities.AbstractPhysicalEntityModel;
import uni.gaben.iscat.universe.entities.hardcoded.asteroid.AsteroidModel;
import uni.gaben.iscat.universe.entities.hardcoded.player.PlayerModel;
import uni.gaben.iscat.universe.entities.hardcoded.projectiles.ProjectileModel;

public enum UniverseSpawnable {
    PLAYER(PlayerModel.class),
    ASTEROID(AsteroidModel.class),
    BLACKHOLE(BlackHoleModel.class),
    HEART(HeartModel.class),
    WORM(EntityModel.class),
    PROJECTILE(ProjectileModel.class);

    private final Class<? extends AbstractPhysicalEntityModel> modelClass;

    UniverseSpawnable(Class<? extends AbstractPhysicalEntityModel> modelClass) {
        this.modelClass = modelClass;
    }

    public Class<? extends AbstractPhysicalEntityModel> getModelClass() {
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