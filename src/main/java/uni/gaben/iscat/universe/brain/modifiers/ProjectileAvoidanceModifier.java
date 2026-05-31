package uni.gaben.iscat.universe.brain.modifiers;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.player.PlayerModel;
import uni.gaben.iscat.universe.projectiles.Projectile;
import uni.gaben.iscat.universe.projectiles.ProjectileType;
import uni.gaben.iscat.utils.Interpolator;

/**
 * Movement modifier that smoothly evades incoming hostile projectiles.
 * <p>
 * Instead of a simple perpendicular strafe, this uses <b>escape vectors</b> that push
 * the entity directly away from the bullet's path. The dodge direction is smoothed over
 * time to prevent oscillation, and multiple threats are blended by their urgency.
 * </p>
 *
 * <p><b>Configuration:</b></p>
 * <ul>
 *   <li>{@code detectionRadius} – how far (meters) the entity looks for threats.</li>
 *   <li>{@code sidestepStrength} – base multiplier for the dodge force.</li>
 *   <li>{@code lookAheadTime} – seconds into the future to predict collisions.</li>
 *   <li>{@code smoothingFactor} – how quickly the dodge direction changes (0=instant, 1=no smoothing).</li>
 * </ul>
 *
 * <p><b>Important:</b> Each entity must have its own instance of this modifier,
 * because it stores the current dodge vector as state.</p>
 */
public class ProjectileAvoidanceModifier implements MovementModifier {

    private final double detectionRadius;
    private final double sidestepStrength;
    private final double lookAheadTime;
    private final double smoothingFactor;

    /** Persistent dodge direction (smoothed across frames). */
    private Vector2 currentDodgeVector = new Vector2();

    /**
     * @param detectionRadius  how far the entity "sees" projectiles (world meters).
     * @param sidestepStrength base strength of the dodge force (will be scaled by threat).
     */
    public ProjectileAvoidanceModifier(double detectionRadius, double sidestepStrength) {
        this.detectionRadius = detectionRadius;
        this.sidestepStrength = sidestepStrength;
        this.lookAheadTime = 1.5;          // sensible default
        this.smoothingFactor = 0.15;       // 15% blend per frame – smooth but responsive
    }

    /**
     * Full constructor for fine‑tuning.
     */
    public ProjectileAvoidanceModifier(double detectionRadius, double sidestepStrength,
                                       double lookAheadTime, double smoothingFactor) {
        this.detectionRadius = detectionRadius;
        this.sidestepStrength = sidestepStrength;
        this.lookAheadTime = lookAheadTime;
        this.smoothingFactor = smoothingFactor;
    }

    @Override
    public Vector2 modify(Vector2 desired, AbstractEntityModel self, UniverseModel world,
                          double maxForce, double dt) {
        Vector2 pos = self.getTransform().getTranslation();
        double hitRadius = self.getWidthMeters() / 2.0;
        double evasionRadius = hitRadius * 2.5;   // start dodging before physical contact

        Vector2 targetDodge = new Vector2();
        boolean inDanger = false;

        for (Projectile p : world.getEntitiesOfType(Projectile.class)) {
            Vector2 projPos = p.getTransform().getTranslation();
            Vector2 projVel = p.getLinearVelocity();

            // --- Quick rejects ---
            if (!isHostile(p, self)) continue;
            double distToBullet = projPos.distance(pos);
            if (distToBullet > detectionRadius) continue;
            double projSpeedSq = projVel.getMagnitudeSquared();
            if (projSpeedSq < 0.01) continue;          // stationary projectile – ignore

            Vector2 projDir = projVel.getNormalized();
            Vector2 toEntity = pos.copy().subtract(projPos);

            // --- 1. Is the bullet heading toward us? ---
            double distanceAlongPath = toEntity.dot(projDir);
            if (distanceAlongPath <= 0) continue;      // bullet is moving away

            // --- 2. How close are we to the bullet's infinite line? ---
            double perpDistance = Math.abs(projVel.cross(toEntity)) / projVel.getMagnitude();
            if (perpDistance > evasionRadius) continue; // we are safely outside the corridor

            // --- 3. Time‑to‑impact (using only bullet speed, ignoring own velocity) ---
            double t = distanceAlongPath / projVel.getMagnitude();
            if (t > lookAheadTime) continue;            // not an immediate threat

            inDanger = true;

            // --- 4. Compute the ESCAPE VECTOR ---
            // The point on the bullet's path closest to the entity.
            Vector2 closestPoint = projPos.copy().add(projDir.multiply(distanceAlongPath));
            Vector2 escapeDir = pos.copy().subtract(closestPoint);

            // Fallback: if we are exactly on the line, dodge perpendicular.
            if (escapeDir.getMagnitudeSquared() < 0.001) {
                escapeDir = projDir.getRightHandOrthogonalVector();
            }
            escapeDir.normalize();

            // --- 5. Threat weights ---
            // Spatial: 1.0 when dead‑center, 0.0 at the edge of the evasion zone.
            double spatialWeight = 1.0 - (perpDistance / evasionRadius);

            // Temporal: 1.0 when impact is immediate, fading to 0.0 at lookAheadTime.
            double temporalWeight = 1.0 - (t / lookAheadTime);

            // Proximity: 1.0 when the bullet is right on top of us, 0.0 at detectionRadius.
            double proximityWeight = 1.0 - (distToBullet / detectionRadius);

            // Combine weights – you can tune the exponents to change the urgency curve.
            double threat = spatialWeight * temporalWeight * proximityWeight;

            // Accumulate this bullet's contribution into the raw dodge target.
            targetDodge.add(escapeDir.multiply(threat));
        }

        // --- 6. Smooth the raw dodge direction ---
        if (inDanger && targetDodge.getMagnitudeSquared() > 0.001) {
            // Blend toward the new target direction using linear interpolation.
            currentDodgeVector = Interpolator.lerp(currentDodgeVector, targetDodge, smoothingFactor);
        } else {
            // Decay the dodge force when safe, so the entity recovers smoothly.
            currentDodgeVector = Interpolator.lerp(currentDodgeVector, new Vector2(), smoothingFactor * 0.5);
        }

        // --- 7. Apply the dodge force to the desired velocity ---
        if (currentDodgeVector.getMagnitudeSquared() > 0.01) {
            // Scale the force by sidestepStrength, but cap it so physics don't explode.
            double magnitude = currentDodgeVector.getMagnitude();
            double cappedMagnitude = Math.min(magnitude, 3.0);  // panic cap
            Vector2 dodgeForce = currentDodgeVector.getNormalized()
                    .multiply(cappedMagnitude * sidestepStrength);

            desired.add(dodgeForce);
        }

        return desired;
    }

    private boolean isHostile(Projectile p, AbstractEntityModel self) {
        if (self instanceof PlayerModel) {
            return p.getType() == ProjectileType.ENEMY_BULLET;
        }
        return p.getType() == ProjectileType.PLAYER_BULLET;
    }
}