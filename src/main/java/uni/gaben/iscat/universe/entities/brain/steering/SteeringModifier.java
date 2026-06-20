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

                    // CORRETTO: Separato normalize() da multiply()
                    toNeighbor.normalize();
                    toNeighbor.multiply(maxForce * strength);
                    outForce.add(toNeighbor);
                }
            }

            if (!outForce.isZero()) {
                // CORRETTO: Separato normalize() da multiply()
                outForce.normalize();
                outForce.multiply(maxForce * weight.get());
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
                if (!avgVelocity.isZero()) {
                    // CORRETTO: Separato normalize() da multiply()
                    avgVelocity.normalize();
                    avgVelocity.multiply(maxForce);
                }
                outForce.set(avgVelocity).subtract(self.getLinearVelocity());
                if (!outForce.isZero()) {
                    // CORRETTO: Separato normalize() da multiply()
                    outForce.normalize();
                    outForce.multiply(maxForce * weight.get());
                }
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
                Vector2 desired = centerOfMass.subtract(selfPos);
                if (!desired.isZero()) {
                    // CORRETTO: Separato normalize() da multiply()
                    desired.normalize();
                    desired.multiply(maxForce);

                    outForce.set(desired).subtract(self.getLinearVelocity());
                    if (!outForce.isZero()) {
                        // CORRETTO: Separato normalize() da multiply()
                        outForce.normalize();
                        outForce.multiply(maxForce * weight.get());
                    }
                }
            }
        };
    }

    static SteeringModifier collisionAvoidance(Target threats, double maxPredictionTime, double avoidRadius, DoubleProperty weight) {
        Vector2 dp = UU.vector2zero();
        Vector2 dv = UU.vector2zero();

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
                Vector2 threatVel = mostImminent.getLinearVelocity();

                if (!threatVel.isZero()) {
                    // 1. Otteniamo la direzione del proiettile/minaccia
                    Vector2 bulletDir = threatVel.copy();
                    bulletDir.normalize();

                    // 2. Calcoliamo il vettore perpendicolare (Schivata Laterale a 90 gradi)
                    Vector2 lateralEvasion = new Vector2(-bulletDir.y, bulletDir.x);

                    // 3. Scegliamo il lato (+ o -) che ci allontana dal proiettile, senza indietreggiare
                    Vector2 toSelf = selfPos.copy().subtract(mostImminent.getTransform().getTranslation());
                    if (lateralEvasion.dot(toSelf) < 0) {
                        lateralEvasion.multiply(-1);
                    }

                    outForce.set(lateralEvasion);
                } else {
                    // Fallback nel caso in cui la minaccia sia ferma (es. un ostacolo statico)
                    outForce.set(selfPos).subtract(mostImminent.getTransform().getTranslation());
                    if (!outForce.isZero()) outForce.normalize();
                }

                double urgency = 1.0 - (shortestTime / maxPredictionTime);
                urgency = Math.clamp(urgency, 0.1, 1.0);

                if (!outForce.isZero()) {
                    // Applica la forza laterale scalata sul peso
                    outForce.multiply(maxForce * weight.get() * urgency);
                }
            }
        };
    }
}