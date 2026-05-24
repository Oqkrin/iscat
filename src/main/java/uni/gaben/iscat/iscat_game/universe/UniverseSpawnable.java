package uni.gaben.iscat.iscat_game.universe;

import uni.gaben.iscat.iscat_game.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.iscat_game.universe.asteroid.AsteroidModel;
import uni.gaben.iscat.iscat_game.universe.enemies.fake_iscat.FakeIscatModel;
import uni.gaben.iscat.iscat_game.universe.enemies.fallen_star_golem.FallenStarGolemModel;
import uni.gaben.iscat.iscat_game.universe.enemies.iscat_core.IscatCoreModel;
import uni.gaben.iscat.iscat_game.universe.enemies.iscat_master.IscatMasterModel;
import uni.gaben.iscat.iscat_game.universe.enemies.iscat_mother.IscatMotherModel;
import uni.gaben.iscat.iscat_game.universe.heart.HeartModel;
import uni.gaben.iscat.iscat_game.universe.enemies.iscat_eater.IscatEaterModel;
import uni.gaben.iscat.iscat_game.universe.enemies.iscat_mob.IscatMobModel;
import uni.gaben.iscat.iscat_game.universe.enemies.iscat_bomber.IscatBomberModel;
import uni.gaben.iscat.iscat_game.universe.player.PlayerModel;
import uni.gaben.iscat.iscat_game.universe.projectiles.Projectile;
import uni.gaben.iscat.iscat_game.universe.enemies.iscat_worm.IscatWormSegment;

public enum UniverseSpawnable {
    PLAYER(PlayerModel.class),
    ASTEROID(AsteroidModel.class),
    ISCAT_MOB(IscatMobModel.class),
    ISCAT_MOTHER(IscatMotherModel.class),
    HEART(HeartModel.class),
    EATER(IscatEaterModel.class),
    ISCAT_CORE(IscatCoreModel.class),
    FALLEN_STAR_GOLEM(FallenStarGolemModel.class),
    FAKE_ISCAT(FakeIscatModel.class),
    ISCAT_BOMBER(IscatBomberModel.class),
    ISCAT_MASTER(IscatMasterModel.class),

    WORM(IscatWormSegment.class),

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