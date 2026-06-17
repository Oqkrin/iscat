package uni.gaben.iscat.universe.entities;

/**
 * Listener callback that allows the Game layer to react when an entity dies
 * inside the Universe physics engine without coupling the Universe to the Game logic.
 */
@FunctionalInterface
public interface EntityDeathListener {
    void onEntityDied(AbstractEntityModel entity, boolean killedByProjectile);
}
