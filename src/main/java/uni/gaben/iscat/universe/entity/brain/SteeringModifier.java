package uni.gaben.iscat.universe.entity.brain;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entity.EntityFilters;
import uni.gaben.iscat.universe.entity.Data.BrainData;
import uni.gaben.iscat.universe.entity.GameEntity;

import java.util.List;

@FunctionalInterface
public interface SteeringModifier {

    void computeSteer(GameEntity self, UniverseModel world, double maxForce, double dt, Vector2 outForce);

    static SteeringModifier createModifier(BrainData.ModifierRecord mc, GameEntity entity) {
        DoubleProperty weight = new SimpleDoubleProperty(mc.weight());
        Target neighbors = Target.neighboursCached(entity, mc.radius(), EntityFilters.isNot(entity));
        return switch (mc.type()) {
            case "separation" -> SteeringModifier.separation(neighbors, mc.radius(), weight);
            case "alignment" -> SteeringModifier.alignment(neighbors, weight);
            case "cohesion" -> SteeringModifier.cohesion(neighbors, weight);
            case "collisionAvoidance" ->
                    SteeringModifier.collisionAvoidance(neighbors, mc.maxPredictionTime(), mc.avoidRadius(), weight);
            default -> null;
        };
    }

    static SteeringModifier separation(Target neighborhood, double separationRadius, DoubleProperty weight) {
        Vector2 toNeighbor = UU.vector2zero();

        return (self, world, maxForce, dt, outForce) -> {
            outForce.set(0, 0);
            List<GameEntity> neighbors = neighborhood.getEntities(world);
            if (neighbors == null || neighbors.isEmpty()) return;

            Vector2 selfPos = self.getTransform().getTranslation();
            int count = 0;

            for (int i = 0; i < neighbors.size(); i++) {
                GameEntity neighbor = neighbors.get(i);
                if (neighbor == self || neighbor.shouldRemove()) continue;

                toNeighbor.set(selfPos).subtract(neighbor.getTransform().getTranslation());
                double distSq = toNeighbor.getMagnitudeSquared();

                if (distSq > 0.0001 && distSq < (separationRadius * separationRadius)) {
                    double dist = Math.sqrt(distSq);
                    double strength = 1.0 - (dist / separationRadius);
                    toNeighbor.divide(dist).multiply(strength*weight.get());
                    outForce.add(toNeighbor);
                    count++;
                }
            }
        };
    }

    static SteeringModifier alignment(Target neighborhood, DoubleProperty weight) {
        Vector2 avgVelocity = UU.vector2zero();

        return (self, world, maxForce, dt, outForce) -> {
            outForce.set(0, 0);
            avgVelocity.set(0, 0);
            List<GameEntity> neighbors = neighborhood.getEntities(world);
            if (neighbors == null || neighbors.isEmpty()) return;

            int count = 0;
            for (int i = 0; i < neighbors.size(); i++) {
                GameEntity neighbor = neighbors.get(i);
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
            List<GameEntity> neighbors = neighborhood.getEntities(world);
            if (neighbors == null || neighbors.isEmpty()) return;

            Vector2 selfPos = self.getTransform().getTranslation();
            int count = 0;

            for (int i = 0; i < neighbors.size(); i++) {
                GameEntity neighbor = neighbors.get(i);
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
            List<GameEntity> entities = threats.getEntities(world);
            if (entities == null || entities.isEmpty()) return;

            double shortestTime = Double.MAX_VALUE;
            GameEntity mostImminent = null;

            Vector2 selfPos = self.getTransform().getTranslation();
            Vector2 selfVel = self.getLinearVelocity();

            for (int i = 0; i < entities.size(); i++) {
                GameEntity threat = entities.get(i);
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
                threatFuture.set(mostImminent.getTransform().getTranslation());
                threatFuture.add(mostImminent.getLinearVelocity().x * shortestTime, mostImminent.getLinearVelocity().y * shortestTime);

                myFuture.set(selfPos);
                myFuture.add(selfVel.x * shortestTime, selfVel.y * shortestTime);

                outForce.set(myFuture).subtract(threatFuture).multiply(weight.get());
            }
        };
    }
}
