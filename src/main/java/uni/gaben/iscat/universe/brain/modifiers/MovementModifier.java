package uni.gaben.iscat.universe.brain.modifiers;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;

@FunctionalInterface
public interface MovementModifier {
    /**
     * @param self           the entity being controlled
     * @param universe       the game universe
     * @param maxForce       acceleration
     * @param dt             timeunit
     * @return the new desired velocity after this modifier
     */
    Vector2 compute(AbstractEntityModel self, UniverseModel universe, double maxForce, double dt);
}