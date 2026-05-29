package uni.gaben.iscat.universe.brain.goals;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;

@FunctionalInterface
public interface MovementGoal {
    Vector2 compute(AbstractEntityModel self, UniverseModel world, double dt);
}