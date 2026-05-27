package uni.gaben.iscat.universe.lib.behaviurs;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.UniverseModel;

@FunctionalInterface
public interface MovementStrategy {
    Vector2 computeDesiredVelocity(AbstractEntityModel entity, UniverseModel world, double dt);
}