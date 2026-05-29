package uni.gaben.iscat.universe.brain.modifiers;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;

@FunctionalInterface
public interface MovementModifier {
    /**
     * @param currentDesired the desired velocity from the previous step
     * @param self           the entity being controlled
     * @param world          the game world
     * @param dt             time step
     * @return               the new desired velocity after this modifier
     */
    Vector2 modify(Vector2 currentDesired, AbstractEntityModel self, UniverseModel world, double dt);
}