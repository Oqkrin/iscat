package uni.gaben.iscat.universe.brain.modifiers.flocking;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.brain.Target;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;

public class CohesionModifier extends AbstractFlockingModifier {

    // Workspace vector (reused per frame, zero allocation)
    private final Vector2 centerOfMass = new Vector2();

    public CohesionModifier(Target flock, double multiplier) {
        super(flock, multiplier);
    }

    @Override
    public Vector2 computeForce(AbstractEntityModel self, UniverseModel universe, double maxForce, double dt) {
        // Reset workspace to (0,0) primitive by primitive
        centerOfMass.x = 0;
        centerOfMass.y = 0;
        int flockSize = 0;

        Vector2 selfPos = self.getTransform().getTranslation();

        for (var body : flock.getEntities(universe)) {
            if (body == self) continue;
            
            // add() mutates the workspace directly
            centerOfMass.add(body.getTransform().getTranslation());
            flockSize++;
        }

        // If alone, return zero velocity (workspace is already at 0,0)
        if (flockSize == 0) return centerOfMass;

        // Calculate the center of mass
        double avgScale = 1.0 / flockSize;
        centerOfMass.x *= avgScale;
        centerOfMass.y *= avgScale;

        // Calculate direction toward center (steer toward it)
        centerOfMass.x -= selfPos.x;
        centerOfMass.y -= selfPos.y;

        // Normalize and scale by maxForce * multiplier
        double magSq = centerOfMass.x * centerOfMass.x + centerOfMass.y * centerOfMass.y;
        if (magSq > 0.0001) {
            centerOfMass.x *= multiplier;
            centerOfMass.y *= multiplier;
        } else {
            // Zero magnitude, reset to zero
            centerOfMass.x = 0;
            centerOfMass.y = 0;
        }

        return centerOfMass;
    }
}