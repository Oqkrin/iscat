package uni.gaben.iscat.universe.entity.modules;

import uni.gaben.iscat.universe.entity.GameEntity;

public interface EntityModule {
    /**
     * Called when the module is attached to an entity.
     */
    void attach(GameEntity entity);

    /**
     * Called every tick to update the module's logic.
     * @param dt delta time
     */
    default void update(double dt) {}
    
    /**
     * Called when the entity is being removed from the universe.
     */
    default void onRemove() {}
}
