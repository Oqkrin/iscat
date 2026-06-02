package uni.gaben.iscat.universe.brain.modifiers.flocking;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.brain.Target;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.lib.implementations.LivingEntityModel;

public class CohesionModifier extends AbstractFlockingModifier {

    public CohesionModifier(Target flock, double multiplier) {
        super(flock, multiplier);
    }

    @Override
    public Vector2 compute(AbstractEntityModel self, UniverseModel universe, double maxForce, double dt) {
        Vector2 centerOfMass = new Vector2();
        int flockSize = 0;
        Vector2 selfPos = self.getTransform().getTranslation();

        for (var body : flock.getEntities(universe)) {
            if(!(body instanceof LivingEntityModel) || body == self ) continue;
            centerOfMass.add(body.getTransform().getTranslation());
            flockSize++;
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