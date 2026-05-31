package uni.gaben.iscat.universe.brain.modifiers;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;

public class BoundaryAvoidanceModifier implements MovementModifier {
    private final double margin = 100.0; // world pixels

    @Override
    public Vector2 modify(Vector2 desired, AbstractEntityModel self, UniverseModel world, double maxForce, double dt) {
        double px = UU.mToPx(self.getTransform().getTranslationX());
        double py = UU.mToPx(self.getTransform().getTranslationY());
        double w = world.getWidth();
        double h = world.getHeight();

        Vector2 avoid = new Vector2();

        if (px < margin) avoid.x = (margin - px) / margin;
        else if (px > w - margin) avoid.x = (w - margin - px) / margin;

        if (py < margin) avoid.y = (margin - py) / margin;
        else if (py > h - margin) avoid.y = (h - margin - py) / margin;

        if (avoid.getMagnitudeSquared() > 0) {
            avoid.getNormalized().multiply(maxForce);
            return desired.add(avoid);
        }
        return desired;
    }
}
