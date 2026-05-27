package uni.gaben.iscat.universe.lib.behaviurs.modifiers;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.projectiles.Projectile;
import uni.gaben.iscat.universe.projectiles.ProjectileType;

public class ProjectileAvoidanceModifier implements AvoidanceModifier {
    private final double detectionRadius = 5.0;
    private final double sidestepStrength = 1.5;

    @Override
    public Vector2 modify(Vector2 desired, AbstractEntityModel entity, UniverseModel world, double dt) {
        // Find closest player projectile heading toward entity
        Vector2 pos = entity.getTransform().getTranslation();
        Vector2 bestDodgeDir = null;
        double bestDot = -1;
        for (AbstractEntityModel e : world.getEntitiesOfType(Projectile.class)) {
            Projectile p = (Projectile) e;
            if (p.getType() != ProjectileType.PLAYER_BULLET) continue;
            Vector2 toProj = p.getTransform().getTranslation().copy().subtract(pos);
            double dist = toProj.getMagnitude();
            if (dist > detectionRadius) continue;
            Vector2 projVel = p.getLinearVelocity();
            if (projVel.getMagnitude() < 0.1) continue;
            Vector2 projDir = projVel.getNormalized();
            // Predict impact in next 0.3 seconds
            Vector2 predicted = p.getTransform().getTranslation().copy().add(projVel.multiply(0.3));
            Vector2 toPredicted = predicted.copy().subtract(pos);
            if (toPredicted.getMagnitude() > dist + 1.0) continue; // going away
            // Desired sidestep: perpendicular to projDir, weighted by closeness
            Vector2 left = new Vector2(-projDir.y, projDir.x);
            Vector2 right = new Vector2(projDir.y, -projDir.x);
            // Choose direction that keeps us moving roughly toward original goal
            double dotLeft = desired.getNormalized().dot(left);
            double dotRight = desired.getNormalized().dot(right);
            Vector2 sidestep = (dotLeft > dotRight) ? left : right;
            double strength = Math.max(0, (detectionRadius - dist) / detectionRadius) * sidestepStrength;
            return desired.copy().add(sidestep.multiply(strength));
        }
        return desired;
    }
}