package uni.gaben.iscat.universe.brain.modifiers.flocking;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.brain.modifiers.MovementModifier;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;

public class AlignmentModifier implements MovementModifier {
    @Override
    public Vector2 modify(Vector2 currentDesired, AbstractEntityModel self, UniverseModel world, double dt) {
        return null;
    }
}
