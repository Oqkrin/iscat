package uni.gaben.iscat.universe.brain.modifiers;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;

@FunctionalInterface
public interface MovementModifier {
    /**
     * Returns a steering force (in Newtons) that this modifier wants to apply.
     * The force will be added to other modifier forces and clamped to maxForce.
     */
    Vector2 computeForce(AbstractEntityModel self, UniverseModel world, double maxForce, double dt);
}