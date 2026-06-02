package uni.gaben.iscat.universe.brain.modifiers.flocking;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.brain.Target;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;

public class SeparationModifier extends AbstractFlockingModifier {

    public SeparationModifier(Target flock, double distance, double multiplier) {
        super(flock, distance, multiplier);
    }

    @Override
    public Vector2 compute(AbstractEntityModel self, UniverseModel universe, double maxForce, double dt) {
        Vector2 sum = UU.vector2zero();
        int flockSize = 0;

        Vector2 selfPos = self.getTransform().getTranslation();

        for (var body : flock.getEntities(universe)) {

            Vector2 bodyPos = body.getTransform().getTranslation();
            double distance = selfPos.distance(bodyPos);

            // Distance must be > 0 to prevent division by zero (NaN)
            if (distance > 0 && distance < range) {
                // 1. Calculate vector pointing AWAY from the neighbor
                Vector2 diff = selfPos.copy().subtract(bodyPos);

                // 2. Normalize to get pure direction, then weight by distance
                // (closer neighbors push much harder than distant ones)
                diff.normalize();
                diff.divide(distance);

                // 3. Accumulate the forces
                sum.add(diff);
                flockSize++;
            }
        }

        if (flockSize > 0) {
            // Average out the accumulated separation vectors
            sum.divide(flockSize);

            // Normalize so we can predictably mix it with the other steering behaviors
            if (sum.getMagnitudeSquared() > 0) {
                sum.normalize();
                sum.multiply(maxForce*multiplier);
            }
        }

        return sum;
    }
}