package uni.gaben.iscat.gamenex.universe;

import uni.gaben.iscat.gamenex.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.gamenex.universe.asteroid.AsteroidModel;
import uni.gaben.iscat.gamenex.universe.hearth.HearthModel;
import uni.gaben.iscat.gamenex.universe.iscat_eater.IscatEaterModel;
import uni.gaben.iscat.gamenex.universe.iscat_mob.IscatMobModel;
import uni.gaben.iscat.gamenex.universe.iscat_mother.IscatMotherModel;
import uni.gaben.iscat.gamenex.universe.iscat_worm.iscat_worm_body_part.IscatWormBodyPartModel;
import uni.gaben.iscat.gamenex.universe.iscat_worm.iscat_worm_head.IscatWormHeadModel;
import uni.gaben.iscat.gamenex.universe.iscat_worm.iscat_worm_tail.IscatWormTailModel;
import uni.gaben.iscat.gamenex.universe.player.PlayerModel;
import uni.gaben.iscat.gamenex.universe.projectiles.Projectile;

public enum UniverseSpawnable {
    PLAYER(PlayerModel.class),
    ASTEROID(AsteroidModel.class),
    ISCAT_MOB(IscatMobModel.class),
    //ISCAT_MOTHER(IscatMotherModel.class),
    HEARTH(HearthModel.class),
    EATER(IscatEaterModel.class),

    WORM(null),
    WORM_HEAD(IscatWormHeadModel.class),
    WORM_BODY(IscatWormBodyPartModel.class),
    WORM_TAIL(IscatWormTailModel.class),

    PROJECTILE(Projectile.class);

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
}