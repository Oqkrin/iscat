package uni.gaben.iscat.universe.entity.brain;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entity.AbstractEntityModel;

import java.util.List;

@FunctionalInterface
public interface SteeringModifier {
    /**
     * Computes an independent steering force (in Newtons) and writes it into outForce.
     * The force will be added to the total steering force accumulator and clamped to maxForce.
     */
    void computeSteer(AbstractEntityModel self, UniverseModel world, double maxForce, double dt, Vector2 outForce);

    /**
     * Separation: Steers the entity away from nearby neighbors to prevent crowding.
     * Uses an inverse-distance (1/r) weighting as recommended by Reynolds.
     */
    public static SteeringModifier separation(Target neighborhood, double separationRadius, double weight) {
        Vector2 toNeighbor = UU.vector2zero();

        return (self, world, maxForce, dt, outForce) -> {
            outForce.set(0, 0);
            List<AbstractEntityModel> neighbors = neighborhood.getEntities(world);
            if (neighbors == null || neighbors.isEmpty()) return;

            Vector2 selfPos = self.getTransform().getTranslation();
            int count = 0;

            for (int i = 0; i < neighbors.size(); i++) {
                AbstractEntityModel neighbor = neighbors.get(i);
                if (neighbor == self || neighbor.shouldRemove()) continue;

                toNeighbor.set(selfPos).subtract(neighbor.getTransform().getTranslation());
                double distSq = toNeighbor.getMagnitudeSquared();

                if (distSq > 0.0001 && distSq < (separationRadius * separationRadius)) {
                    double dist = Math.sqrt(distSq);
                    toNeighbor.divide(dist);             // Normalize vector
                    toNeighbor.multiply(1.0 / dist);     // Apply 1/r weighting
                    outForce.add(toNeighbor);
                    count++;
                }
            }

            if (count > 0) {
                outForce.multiply(weight);
                if (outForce.getMagnitudeSquared() > maxForce * maxForce) {
                    outForce.setMagnitude(maxForce);
                }
            }
        };
    }

    /**
     * Alignment: Steers the entity to match the average velocity (heading/speed) of its neighbors.
     */
    public static SteeringModifier alignment(Target neighborhood, double weight) {
        Vector2 avgVelocity = UU.vector2zero();

        return (self, world, maxForce, dt, outForce) -> {
            outForce.set(0, 0);
            avgVelocity.set(0, 0);
            List<AbstractEntityModel> neighbors = neighborhood.getEntities(world);
            if (neighbors == null || neighbors.isEmpty()) return;

            int count = 0;

            for (int i = 0; i < neighbors.size(); i++) {
                AbstractEntityModel neighbor = neighbors.get(i);
                if (neighbor == self || neighbor.shouldRemove()) continue;

                avgVelocity.add(neighbor.getLinearVelocity());
                count++;
            }

            if (count > 0) {
                avgVelocity.divide(count); // Get average neighbor velocity

                // Reynolds standard steering formula: Steering = Desired - Current
                outForce.set(avgVelocity).subtract(self.getLinearVelocity());
                outForce.multiply(weight);

                if (outForce.getMagnitudeSquared() > maxForce * maxForce) {
                    outForce.setMagnitude(maxForce);
                }
            }
        };
    }

    /**
     * Cohesion: Steers the entity towards the center of mass (average position) of its neighbors.
     */
    public static SteeringModifier cohesion(Target neighborhood, double weight) {
        Vector2 centerOfMass = UU.vector2zero();

        return (self, world, maxForce, dt, outForce) -> {
            outForce.set(0, 0);
            centerOfMass.set(0, 0);
            List<AbstractEntityModel> neighbors = neighborhood.getEntities(world);
            if (neighbors == null || neighbors.isEmpty()) return;

            Vector2 selfPos = self.getTransform().getTranslation();
            int count = 0;

            for (int i = 0; i < neighbors.size(); i++) {
                AbstractEntityModel neighbor = neighbors.get(i);
                if (neighbor == self || neighbor.shouldRemove()) continue;

                centerOfMass.add(neighbor.getTransform().getTranslation());
                count++;
            }

            if (count > 0) {
                centerOfMass.divide(count); // Center of gravity

                // Seek behavior towards the center of mass
                outForce.set(centerOfMass).subtract(selfPos);

                if (!outForce.isZero()) {
                    outForce.setMagnitude(self.getMaxVelocity()); // Desired velocity
                    outForce.subtract(self.getLinearVelocity());  // Steering = Desired - Current
                    outForce.multiply(weight);

                    if (outForce.getMagnitudeSquared() > maxForce * maxForce) {
                        outForce.setMagnitude(maxForce);
                    }
                }
            }
        };
    }

    /**
     * Unaligned Collision Avoidance: Predicts future collisions across arbitrary headings.
     * Calculates the exact Time of Closest Approach (TCA) and steers laterally away from the most imminent threat.
     */
    public static SteeringModifier collisionAvoidance(Target threats, double maxPredictionTime, double avoidRadius, double weight) {
        Vector2 dp = UU.vector2zero();
        Vector2 dv = UU.vector2zero();
        Vector2 myFuture = UU.vector2zero();
        Vector2 threatFuture = UU.vector2zero();

        return (self, world, maxForce, dt, outForce) -> {
            outForce.set(0, 0);
            List<AbstractEntityModel> entities = threats.getEntities(world);
            if (entities == null || entities.isEmpty()) return;

            double shortestTime = Double.MAX_VALUE;
            AbstractEntityModel mostImminent = null;

            Vector2 selfPos = self.getTransform().getTranslation();
            Vector2 selfVel = self.getLinearVelocity();

            // 1. Find the most imminent collision
            for (int i = 0; i < entities.size(); i++) {
                AbstractEntityModel threat = entities.get(i);
                if (threat == self || threat.shouldRemove()) continue;

                dp.set(threat.getTransform().getTranslation()).subtract(selfPos);
                dv.set(threat.getLinearVelocity()).subtract(selfVel);

                double dvSq = dv.getMagnitudeSquared();
                if (dvSq < 0.0001) continue; // Moving perfectly parallel, no collision approach

                // Calculate Time of Closest Approach (TCA)
                // t = -(dp • dv) / ||dv||^2
                double t = -dp.dot(dv) / dvSq;

                if (t > 0 && t < maxPredictionTime) {
                    // Check the distance at time 't'
                    double cx = dp.x + (dv.x * t);
                    double cy = dp.y + (dv.y * t);
                    double distSqAtT = (cx * cx) + (cy * cy);

                    if (distSqAtT < (avoidRadius * avoidRadius)) {
                        if (t < shortestTime) {
                            shortestTime = t;
                            mostImminent = threat;
                        }
                    }
                }
            }

            // 2. Steer to avoid the imminent threat
            if (mostImminent != null) {
                // Calculate where both entities will be at the moment of nearest approach
                threatFuture.set(mostImminent.getTransform().getTranslation());
                threatFuture.add(mostImminent.getLinearVelocity().x * shortestTime, mostImminent.getLinearVelocity().y * shortestTime);

                myFuture.set(selfPos);
                myFuture.add(selfVel.x * shortestTime, selfVel.y * shortestTime);

                // Steer away from the threat's future position
                outForce.set(myFuture).subtract(threatFuture);

                if (!outForce.isZero()) {
                    outForce.setMagnitude(self.getMaxVelocity()); // Desired evasion velocity
                    outForce.subtract(selfVel);                   // Turn to force
                    outForce.multiply(weight);

                    if (outForce.getMagnitudeSquared() > maxForce * maxForce) {
                        outForce.setMagnitude(maxForce);
                    }
                }
            }
        };
    }

}