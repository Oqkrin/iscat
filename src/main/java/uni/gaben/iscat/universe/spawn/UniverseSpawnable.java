package uni.gaben.iscat.universe.spawn;

import uni.gaben.iscat.universe.entities.EntityModel;
import uni.gaben.iscat.universe.entities.hardcoded.heart.HeartModel;
import uni.gaben.iscat.universe.entities.hardcoded.blackhole.BlackHoleModel;
import uni.gaben.iscat.universe.entities.AbstractPhysicalEntityModel;
import uni.gaben.iscat.universe.entities.hardcoded.asteroid.AsteroidModel;
import uni.gaben.iscat.universe.entities.player.PlayerModel;
import uni.gaben.iscat.universe.entities.hardcoded.projectiles.ProjectileModel;

/**
 * Mappatura forte tra costanti di spawn e metadati di riflessione delle classi fisiche.
 */
public enum UniverseSpawnable {

    /** Entità della navicella del giocatore. */
    PLAYER(PlayerModel.class),

    /** Corpi celesti rigidi/asteroidi. */
    ASTEROID(AsteroidModel.class),

    /** Singolarità e buchi neri gravitazionali. */
    BLACKHOLE(BlackHoleModel.class),

    /** Consumabile per il ripristino dell'endurance. */
    HEART(HeartModel.class),

    /** Entità biologica articolata (Worm). */
    WORM(EntityModel.class),

    /** Elementi balistici e proiettili. */
    PROJECTILE(ProjectileModel.class);

    private final Class<? extends AbstractPhysicalEntityModel> modelClass;

    UniverseSpawnable(Class<? extends AbstractPhysicalEntityModel> modelClass) {
        this.modelClass = modelClass;
    }

    public Class<? extends AbstractPhysicalEntityModel> getModelClass() { return modelClass; }

    /** * Mappa una stringa all'enum corrispondente; ritorna null se l'entità è dinamica/custom JSON.
     */
    public static UniverseSpawnable fromString(String value) {
        if (value == null) return null;
        try {
            return UniverseSpawnable.valueOf(value.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
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