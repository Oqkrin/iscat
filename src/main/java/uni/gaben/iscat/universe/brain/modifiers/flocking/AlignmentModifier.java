package uni.gaben.iscat.universe.brain.modifiers.flocking;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.brain.Target;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.lib.implementations.LivingEntityModel;

public class AlignmentModifier extends AbstractFlockingModifier {
    // Workspace vector (reused per frame, zero allocation)
    private final Vector2 averageVelocity = new Vector2();
    
    public AlignmentModifier(Target flock, double multiplier) {
        super(flock, multiplier);
    }

    @Override
    public Vector2 computeForce(AbstractEntityModel self, UniverseModel universe, double maxForce, double dt) {
        // Reset workspace to (0,0) primitive by primitive
        averageVelocity.x = 0;
        averageVelocity.y = 0;
        int flockSize = 0;

        // Accumulate velocities from all flock members
        for (var body : flock.getEntities(universe)) {
            if (body == self) continue;
            Vector2 bodyVel = body.getLinearVelocity();
            
            // Safety check: skip entities with invalid velocities
            if (bodyVel == null || Double.isNaN(bodyVel.x) || Double.isNaN(bodyVel.y)) {
                continue;
            }
            
            // add() mutates the workspace directly
            averageVelocity.add(bodyVel);
            flockSize++;
        }

        // If alone, return zero velocity
        if (flockSize == 0) return averageVelocity;

        // Calculate the average velocity of the flock
        double avgScale = 1.0 / flockSize;
        averageVelocity.x *= avgScale;
        averageVelocity.y *= avgScale;
        
        // Safety check: validate average velocity
        double avgMagSq = averageVelocity.getMagnitudeSquared();
        if (Double.isNaN(avgMagSq) || avgMagSq <= 0.0001) {
            averageVelocity.x = 0;
            averageVelocity.y = 0;
            return averageVelocity; // No flock movement, no alignment velocity
        }

        // Return average velocity as the desired velocity contribution
        // Normalize and scale by maxForce * multiplier

        averageVelocity.x *= multiplier;
        averageVelocity.y *= multiplier;
        
        // Final safety check
        if (Double.isNaN(averageVelocity.x) || Double.isNaN(averageVelocity.y)) {
            averageVelocity.x = 0;
            averageVelocity.y = 0;
        }
        
        return averageVelocity;
    }
}