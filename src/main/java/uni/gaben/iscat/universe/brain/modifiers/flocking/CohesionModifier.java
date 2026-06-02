package uni.gaben.iscat.universe.brain.modifiers.flocking;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.brain.Target;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;

public class CohesionModifier extends AbstractFlockingModifier {

    public CohesionModifier(Target flock, double range, double multiplier) {
        super(flock, range, multiplier);
    }

    @Override
    public Vector2 compute(AbstractEntityModel self, UniverseModel universe, double maxForce, double dt) {
        Vector2 centerOfMass = new Vector2();
        int flockSize = 0;
        Vector2 selfPos = self.getTransform().getTranslation();

        for (var body : flock.getEntities(universe)) {
            Vector2 bodyPos = body.getTransform().getTranslation();
            if (selfPos.distance(bodyPos) < range) {
                centerOfMass.add(bodyPos);
                flockSize++;
            }
        }

        if (flockSize > 0) {
            centerOfMass.divide(flockSize);

            Vector2 steer = centerOfMass.subtract(selfPos);

            if (steer.getMagnitudeSquared() > 0) {
                steer.normalize();
                steer.multiply(maxForce*multiplier);
            }

            return steer;
        }

        return UU.vector2zero();
    }
}