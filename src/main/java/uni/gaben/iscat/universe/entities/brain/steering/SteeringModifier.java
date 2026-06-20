package uni.gaben.iscat.universe.entities.brain.steering;

import javafx.beans.property.DoubleProperty;
import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entities.AbstractPhysicalEntityModel;
import uni.gaben.iscat.universe.entities.brain.target.Target;

import java.util.List;

@FunctionalInterface
public interface SteeringModifier {

    void computeSteer(AbstractPhysicalEntityModel self, UniverseModel world, double maxForce, double dt, Vector2 outForce);

    static SteeringModifier separation(Target neighborhood, double separationRadius, DoubleProperty weight) {
        Vector2 toNeighbor = UU.vector2zero();

        return (self, world, maxForce, dt, outForce) -> {
            outForce.set(0, 0);
            List<AbstractPhysicalEntityModel> neighbors = neighborhood.getEntities(world);
            if (neighbors == null || neighbors.isEmpty()) return;

            Vector2 selfPos = self.getTransform().getTranslation();


            for (int i = 0; i < neighbors.size(); i++) {
                AbstractPhysicalEntityModel neighbor = neighbors.get(i);
                if (neighbor == self || neighbor.shouldRemove()) continue;

                toNeighbor.set(selfPos).subtract(neighbor.getTransform().getTranslation());
                double distSq = toNeighbor.getMagnitudeSquared();

                if (distSq > 0.0001 && distSq < (separationRadius * separationRadius)) {
                    double dist = Math.sqrt(distSq);
                    double strength = 1.0 - (dist / separationRadius);
                    toNeighbor.divide(dist).multiply(strength*weight.get());
                    outForce.add(toNeighbor);

                }
            }
        };
    }

    static SteeringModifier alignment(Target neighborhood, DoubleProperty weight) {
        Vector2 avgVelocity = UU.vector2zero();

        return (self, world, maxForce, dt, outForce) -> {
            outForce.set(0, 0);
            avgVelocity.set(0, 0);
            List<AbstractPhysicalEntityModel> neighbors = neighborhood.getEntities(world);
            if (neighbors == null || neighbors.isEmpty()) return;

            int count = 0;
            for (int i = 0; i < neighbors.size(); i++) {
                AbstractPhysicalEntityModel neighbor = neighbors.get(i);
                if (neighbor == self || neighbor.shouldRemove()) continue;

                avgVelocity.add(neighbor.getLinearVelocity());
                count++;
            }

            if (count > 0) {
                avgVelocity.divide(count);
                outForce.set(avgVelocity).subtract(self.getLinearVelocity()).multiply(weight.get());
            }
        };
    }

    static SteeringModifier cohesion(Target neighborhood, DoubleProperty weight) {
        Vector2 centerOfMass = UU.vector2zero();

        return (self, world, maxForce, dt, outForce) -> {
            outForce.set(0, 0);
            centerOfMass.set(0, 0);
            List<AbstractPhysicalEntityModel> neighbors = neighborhood.getEntities(world);
            if (neighbors == null || neighbors.isEmpty()) return;

            Vector2 selfPos = self.getTransform().getTranslation();
            int count = 0;

            for (int i = 0; i < neighbors.size(); i++) {
                AbstractPhysicalEntityModel neighbor = neighbors.get(i);
                if (neighbor == self || neighbor.shouldRemove()) continue;

                centerOfMass.add(neighbor.getTransform().getTranslation());
                count++;
            }

            if (count > 0) {
                centerOfMass.divide(count);
                outForce.set(centerOfMass).subtract(selfPos).multiply(weight.get());
            }
        };
    }

    static SteeringModifier collisionAvoidance(Target threats, double maxPredictionTime, double avoidRadius, DoubleProperty weight) {
        Vector2 dp = UU.vector2zero();
        Vector2 dv = UU.vector2zero();
        Vector2 myFuture = UU.vector2zero();
        Vector2 threatFuture = UU.vector2zero();

        return (self, world, maxForce, dt, outForce) -> {
            outForce.set(0, 0);
            List<AbstractPhysicalEntityModel> entities = threats.getEntities(world);
            if (entities == null || entities.isEmpty()) return;

            double shortestTime = Double.MAX_VALUE;
            AbstractPhysicalEntityModel mostImminent = null;

            Vector2 selfPos = self.getTransform().getTranslation();
            Vector2 selfVel = self.getLinearVelocity();

            for (int i = 0; i < entities.size(); i++) {
                AbstractPhysicalEntityModel threat = entities.get(i);
                if (threat == self || threat.shouldRemove()) continue;

                dp.set(threat.getTransform().getTranslation()).subtract(selfPos);
                dv.set(threat.getLinearVelocity()).subtract(selfVel);

                double dvSq = dv.getMagnitudeSquared();
                if (dvSq < 0.0001) continue;

                double t = -dp.dot(dv) / dvSq;

                if (t > 0 && t < maxPredictionTime) {
                    double cx = dp.x + (dv.x * t);
                    double cy = dp.y + (dv.y * t);
                    if ((cx * cx) + (cy * cy) < (avoidRadius * avoidRadius)) {
                        if (t < shortestTime) {
                            shortestTime = t;
                            mostImminent = threat;
                        }
                    }
                }
            }

            if (mostImminent != null) {
                // Future positions at the predicted collision moment
                threatFuture.set(mostImminent.getTransform().getTranslation());
                threatFuture.add(mostImminent.getLinearVelocity().x * shortestTime,
                        mostImminent.getLinearVelocity().y * shortestTime);

                myFuture.set(selfPos);
                myFuture.add(selfVel.x * shortestTime, selfVel.y * shortestTime);

                // Direction away from threat
                outForce.set(myFuture).subtract(threatFuture);

                double urgency = 1.0 - (shortestTime / maxPredictionTime);  // 1 = immediate, 0 = far
                urgency = Math.max(urgency, 0.1);                           // always some push

                if (!outForce.isZero()) {
                    outForce.normalize();
                    outForce.multiply(maxForce * weight.get() * urgency);
                }
            }
        };
    }
}