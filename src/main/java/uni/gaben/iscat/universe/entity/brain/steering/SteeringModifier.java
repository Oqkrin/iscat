package uni.gaben.iscat.universe.entity.brain.steering;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entity.AbstractEntityModel;
import uni.gaben.iscat.universe.entity.EntityFilters;
import uni.gaben.iscat.universe.entity.EntityModel;
import uni.gaben.iscat.universe.entity.EntityRecord;
import uni.gaben.iscat.universe.entity.brain.target.Target;
import uni.gaben.iscat.universe.entity.hardcoded.projectiles.AbstractProjectileModel;
import uni.gaben.iscat.universe.entity.hardcoded.projectiles.ProjectileModel;
import uni.gaben.iscat.universe.entity.hardcoded.projectiles.ProjectileType;

import java.util.List;

@FunctionalInterface
public interface SteeringModifier {

    void computeSteer(AbstractEntityModel self, UniverseModel world, double maxForce, double dt, Vector2 outForce);

    static SteeringModifier createModifier(EntityRecord.ModifierRecord mc, EntityModel entity) {
        if (mc.type() == null) return null;
        DoubleProperty weight = new SimpleDoubleProperty(mc.weight());
        Target neighbors = Target.neighboursCached(entity, mc.radius(), EntityFilters.isNot(entity));
        Target everythingButEnemyProjectiles = neighbors.filtered(entityModel -> !(entityModel instanceof ProjectileModel pm&&pm.getType()==ProjectileType.ENEMY_BULLET));
        Target everythingButProjectiles = neighbors.filtered(entityModel -> !(entityModel instanceof AbstractProjectileModel));
        return switch (mc.type()) {
            case SEPARATION -> SteeringModifier.separation(everythingButEnemyProjectiles, mc.radius(), weight);
            case ALIGNMENT -> SteeringModifier.alignment(everythingButProjectiles, weight);
            case COHESION -> SteeringModifier.cohesion(everythingButProjectiles, weight);
            case COLLISION_AVOIDANCE ->
                    SteeringModifier.collisionAvoidance(everythingButEnemyProjectiles, mc.maxPredictionTime(), mc.avoidRadius(), weight);
        };
    }

    static SteeringModifier separation(Target neighborhood, double separationRadius, DoubleProperty weight) {
        Vector2 toNeighbor = UU.vector2zero();

        return (self, world, maxForce, dt, outForce) -> {
            outForce.set(0, 0);
            List<AbstractEntityModel> neighbors = neighborhood.getEntities(world);
            if (neighbors == null || neighbors.isEmpty()) return;

            Vector2 selfPos = self.getTransform().getTranslation();


            for (int i = 0; i < neighbors.size(); i++) {
                AbstractEntityModel neighbor = neighbors.get(i);
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
            List<AbstractEntityModel> entities = threats.getEntities(world);
            if (entities == null || entities.isEmpty()) return;

            double shortestTime = Double.MAX_VALUE;
            AbstractEntityModel mostImminent = null;

            Vector2 selfPos = self.getTransform().getTranslation();
            Vector2 selfVel = self.getLinearVelocity();

            for (int i = 0; i < entities.size(); i++) {
                AbstractEntityModel threat = entities.get(i);
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