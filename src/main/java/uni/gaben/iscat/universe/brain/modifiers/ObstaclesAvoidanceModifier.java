package uni.gaben.iscat.universe.brain.modifiers;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.brain.Target;
import uni.gaben.iscat.universe.enviroment.blackhole.BlackHoleModel;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;

/**
 * ObstaclesAvoidanceModifier steers entities away from static obstacles.
 * 
 * Uses inverse-distance weighting (1/r) so closer obstacles exert
 * stronger repulsive forces. Black holes exert even stronger forces
 * based on their radius.
 */
public class ObstaclesAvoidanceModifier implements MovementModifier {

    private final Target environment;

    // Workspace vectors (reused per frame, zero allocation)
    private final Vector2 avoidanceSum = new Vector2();
    private final Vector2 avoidance = new Vector2();

    public ObstaclesAvoidanceModifier(Target environment) {
        this.environment = environment;
    }

    @Override
    public Vector2 computeForce(AbstractEntityModel self, UniverseModel universe, double maxForce, double dt) {
        // Reset the sum workspace
        avoidanceSum.x = 0;
        avoidanceSum.y = 0;

        Vector2 selfPos = self.getTransform().getTranslation();
        double selfRadius = self.getWidthMeters() / 2.0;
        if (selfRadius <= 0) selfRadius = 0.5;

        for (var body : environment.getEntities(universe)) {
            if (body == self) continue;

            Vector2 bodyPos = body.getTransform().getTranslation();
            double bodyRadius = body.getWidthMeters() / 2.0;
            if (bodyRadius <= 0) bodyRadius = 0.5;
            
            double centerDistance = selfPos.distance(bodyPos);
            
            // Edge-to-edge distance (minimum 0.1 to prevent division by zero)
            double edgeDistance = Math.max(0.1, centerDistance - selfRadius - bodyRadius);

            // Compute avoidance direction (away from obstacle)
            avoidance.x = selfPos.x - bodyPos.x;
            avoidance.y = selfPos.y - bodyPos.y;
            
            double avoidanceMagSq = avoidance.getMagnitudeSquared();
            if (avoidanceMagSq < 0.0001) {
                // If positions are identical, push in random direction
                avoidance.x = (Math.random() - 0.5) * 2.0;
                avoidance.y = (Math.random() - 0.5) * 2.0;
                avoidanceMagSq = avoidance.getMagnitudeSquared();
            }
            
            double mag = Math.sqrt(avoidanceMagSq);
            
            // Obstacle danger strength
            double strength = maxForce;
            if (body instanceof BlackHoleModel bh) {
                double bhRadius = bh.getRadius().m().get();
                if (bhRadius > 0) {
                    strength = bhRadius * maxForce;
                }
            }

            // Apply inverse-distance weighting: (strength / edgeDistance)
            // Combined with normalization: scale = strength / (mag * edgeDistance)
            double scale = strength / (mag * edgeDistance);
            avoidance.x *= scale;
            avoidance.y *= scale;
            
            avoidanceSum.add(avoidance);
        }

        // Return as velocity contribution
        double magSq = avoidanceSum.getMagnitudeSquared();
        if (magSq > 0.0001) {
            double mag = Math.sqrt(magSq);
            // Scale as velocity contribution (stronger for closer obstacles)
            double velocityContribution = Math.min(mag * 0.5, maxForce * 2.0);
            double scale = velocityContribution / mag;
            avoidanceSum.x *= scale;
            avoidanceSum.y *= scale;
        }

        return avoidanceSum;
    }
}