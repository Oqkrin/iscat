package uni.gaben.iscat.universe.brain.modifiers;

import org.dyn4j.geometry.AABB;
import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.player.PlayerModel;
import uni.gaben.iscat.universe.projectiles.Projectile;
import uni.gaben.iscat.universe.projectiles.ProjectileType;
import uni.gaben.iscat.utils.Interpolator;

public class ProjectileAvoidanceModifier implements MovementModifier {
    private final double detectionRadius;
    private final double sidestepStrength;
    private final double lookAheadTime;
    private final double smoothingFactor;

    // Persistent state
    private final Vector2 currentDodgeVector = new Vector2();

    // Zero-allocation workspaces
    private final Vector2 targetDodgeWorkspace = new Vector2();
    private final Vector2 projDirWorkspace = new Vector2();
    private final Vector2 toEntityWorkspace = new Vector2();
    private final Vector2 closestPointWorkspace = new Vector2();
    private final Vector2 escapeDirWorkspace = new Vector2();
    private final Vector2 singleDodgeForceWorkspace = new Vector2();
    private final Vector2 slideRailWorkspace = new Vector2();
    private final Vector2 currentDirWorkspace = new Vector2();
    private final Vector2 forceDirWorkspace = new Vector2();
    private AABB detectionBox = new AABB(0,0,0,0); // Reusable AABB

    public ProjectileAvoidanceModifier(double detectionRadius, double sidestepStrength) {
        this(detectionRadius, sidestepStrength, 1.5, 0.15);
    }

    public ProjectileAvoidanceModifier(double detectionRadius, double sidestepStrength,
                                       double lookAheadTime, double smoothingFactor) {
        this.detectionRadius = detectionRadius;
        this.sidestepStrength = sidestepStrength;
        this.lookAheadTime = lookAheadTime;
        this.smoothingFactor = smoothingFactor;
    }

    @Override
    public Vector2 computeForce(AbstractEntityModel self, UniverseModel world, double maxForce, double dt) {
        Vector2 pos = self.getTransform().getTranslation();
        double hitRadius = self.getWidthMeters() / 2.0;
        double evasionRadius = hitRadius * 2.5;

        // 1. Configure the reusable Broad-phase AABB without instantiating a new one

        AABB selfAabb = self.createAABB();
        double expandedMinX = selfAabb.getMinX() - detectionRadius;
        double expandedMinY = selfAabb.getMinY() - detectionRadius;
        double expandedMaxX = selfAabb.getMaxX() + detectionRadius;
        double expandedMaxY = selfAabb.getMaxY() + detectionRadius;
        detectionBox = new AABB(expandedMinX, expandedMinY, expandedMaxX, expandedMaxY);

        // Reset accumulation workspace
        targetDodgeWorkspace.x = 0;
        targetDodgeWorkspace.y = 0;
        boolean inDanger = false;

        for (var result : world.detect(detectionBox, null)) {
            if (!(result.getBody() instanceof Projectile p)) continue;
            if (!isHostile(p, self)) continue;

            Vector2 projPos = p.getTransform().getTranslation();
            Vector2 projVel = p.getLinearVelocity();

            double distToBullet = projPos.distance(pos); // primitive math
            if (distToBullet > detectionRadius) continue;

            double projSpeedSq = projVel.getMagnitudeSquared();
            if (projSpeedSq < 0.01) continue;

            // 2. Compute Direction Vectors safely
            projDirWorkspace.x = projVel.x;
            projDirWorkspace.y = projVel.y;
            projDirWorkspace.normalize();

            toEntityWorkspace.x = pos.x - projPos.x;
            toEntityWorkspace.y = pos.y - projPos.y;

            double distanceAlongPath = toEntityWorkspace.dot(projDirWorkspace);
            if (distanceAlongPath <= 0) continue;

            double projMagnitude = Math.sqrt(projSpeedSq);
            double perpDistance = Math.abs(projVel.cross(toEntityWorkspace)) / projMagnitude;
            if (perpDistance > evasionRadius) continue;

            double t = distanceAlongPath / projMagnitude;
            if (t > lookAheadTime) continue;

            inDanger = true;

            // 3. Compute Escape Vector safely
            closestPointWorkspace.x = projPos.x + (projDirWorkspace.x * distanceAlongPath);
            closestPointWorkspace.y = projPos.y + (projDirWorkspace.y * distanceAlongPath);

            escapeDirWorkspace.x = pos.x - closestPointWorkspace.x;
            escapeDirWorkspace.y = pos.y - closestPointWorkspace.y;

            if (escapeDirWorkspace.getMagnitudeSquared() < 0.001) {
                // Fallback orthogonal
                escapeDirWorkspace.x = -projDirWorkspace.y;
                escapeDirWorkspace.y = projDirWorkspace.x;
            }
            escapeDirWorkspace.normalize();

            // Threat weights
            double spatialWeight = 1.0 - (perpDistance / evasionRadius);
            double temporalWeight = 1.0 - (t / lookAheadTime);
            double proximityWeight = 1.0 - (distToBullet / detectionRadius);
            double threat = spatialWeight * temporalWeight * proximityWeight;

            singleDodgeForceWorkspace.x = escapeDirWorkspace.x * threat;
            singleDodgeForceWorkspace.y = escapeDirWorkspace.y * threat;

            // 4. Hysteresis Check safely
            if (currentDodgeVector.getMagnitudeSquared() > 0.01) {
                currentDirWorkspace.x = currentDodgeVector.x;
                currentDirWorkspace.y = currentDodgeVector.y;
                currentDirWorkspace.normalize();

                forceDirWorkspace.x = singleDodgeForceWorkspace.x;
                forceDirWorkspace.y = singleDodgeForceWorkspace.y;
                if (forceDirWorkspace.getMagnitudeSquared() > 0) { // avoid NaN if singleDodgeForce is 0
                    forceDirWorkspace.normalize();
                }

                double alignment = forceDirWorkspace.dot(currentDirWorkspace);
                if (alignment < -0.3) {
                    slideRailWorkspace.x = projDirWorkspace.x;
                    slideRailWorkspace.y = projDirWorkspace.y;

                    if (slideRailWorkspace.dot(currentDodgeVector) < 0) {
                        slideRailWorkspace.multiply(-1);
                    }
                    singleDodgeForceWorkspace.x = slideRailWorkspace.x * (threat * 1.5);
                    singleDodgeForceWorkspace.y = slideRailWorkspace.y * (threat * 1.5);
                } else if (alignment > 0) {
                    singleDodgeForceWorkspace.multiply(1.2);
                }
            }

            targetDodgeWorkspace.add(singleDodgeForceWorkspace);
        }

        // 5. Smoothing
        if (inDanger && targetDodgeWorkspace.getMagnitudeSquared() > 0.001) {
            // Note: Ensure your Interpolator.lerp doesn't allocate new objects internally!
            // If Interpolator.lerp returns a new Vector2, you must write it out manually here.
            currentDodgeVector.x = Interpolator.lerp(currentDodgeVector.x, targetDodgeWorkspace.x, smoothingFactor);
            currentDodgeVector.y = Interpolator.lerp(currentDodgeVector.y, targetDodgeWorkspace.y, smoothingFactor);
        } else {
            currentDodgeVector.x = Interpolator.lerp(currentDodgeVector.x, 0, smoothingFactor * 0.5);
            currentDodgeVector.y = Interpolator.lerp(currentDodgeVector.y, 0, smoothingFactor * 0.5);
        }

        // 6. Return as velocity contribution (single-pass scaling, zero allocations)
        double dodgeMagSq = currentDodgeVector.getMagnitudeSquared();
        if (dodgeMagSq > 0.01) {
            double magnitude = Math.sqrt(dodgeMagSq);
            
            // Compute desired velocity contribution strength
            double velocityContribution = Math.min(magnitude * sidestepStrength, sidestepStrength * 2.0);
            
            // Single-pass scaling: avoid chained .normalize().multiply()
            // Scale factor = velocityContribution / magnitude (normalizes and scales in one step)
            double scale = velocityContribution / magnitude;
            currentDodgeVector.x *= scale;
            currentDodgeVector.y *= scale;
        } else {
            currentDodgeVector.x = 0;
            currentDodgeVector.y = 0;
        }
        return currentDodgeVector;
    }

    private boolean isHostile(Projectile p, AbstractEntityModel self) {
        if (self instanceof PlayerModel) {
            return p.getType() == ProjectileType.ENEMY_BULLET;
        }
        return p.getType() == ProjectileType.PLAYER_BULLET;
    }
}