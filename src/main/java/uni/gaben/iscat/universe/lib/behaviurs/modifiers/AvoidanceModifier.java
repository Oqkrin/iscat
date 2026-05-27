package uni.gaben.iscat.universe.lib.behaviurs.modifiers;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.UniverseModel;

@FunctionalInterface
public interface AvoidanceModifier {
    Vector2 modify(Vector2 desiredVelocity, AbstractEntityModel entity, UniverseModel world, double dt);
}