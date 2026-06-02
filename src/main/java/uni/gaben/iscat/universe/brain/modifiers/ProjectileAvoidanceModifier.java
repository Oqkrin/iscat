package uni.gaben.iscat.universe.brain.modifiers;

import org.dyn4j.geometry.AABB;
import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.player.PlayerModel;
import uni.gaben.iscat.universe.projectiles.Projectile;
import uni.gaben.iscat.universe.projectiles.ProjectileType;
import uni.gaben.iscat.utils.Interpolator;

/**
 * Movement modifier that smoothly evades incoming hostile projectiles.
 * Ottimizzato tramite query AABB (Broad-Phase) per analizzare solo le minacce vicine.
 */
public class ProjectileAvoidanceModifier implements MovementModifier {

    private final double detectionRadius;
    private final double sidestepStrength;
    private final double lookAheadTime;
    private final double smoothingFactor;

    private Vector2 currentDodgeVector = new Vector2();

    public ProjectileAvoidanceModifier(double detectionRadius, double sidestepStrength) {
        this.detectionRadius = detectionRadius;
        this.sidestepStrength = sidestepStrength;
        this.lookAheadTime = 1.5;
        this.smoothingFactor = 0.15;
    }

    @Override
    public Vector2 compute(AbstractEntityModel self, UniverseModel world, double maxForce, double dt) {
        Vector2 pos = self.getTransform().getTranslation();
        double hitRadius = self.getWidthMeters() / 2.0;
        double evasionRadius = hitRadius * 2.5;

        Vector2 targetDodge = new Vector2();
        boolean inDanger = false;

        // 1. OTTIMIZZAZIONE BROAD-PHASE: Creiamo un AABB di rilevamento
        AABB selfAabb = self.createAABB();
        AABB detectionBox = new AABB(
                selfAabb.getMinX() - detectionRadius,
                selfAabb.getMinY() - detectionRadius,
                selfAabb.getMaxX() + detectionRadius,
                selfAabb.getMaxY() + detectionRadius
        );

        // 2. Chiediamo all'universo solo i corpi dentro questa zona (ipotizzando null filter per prendere tutto, o un filtro specifico)
        // Usa il tuo metodo detect() di UniverseModel
        var nearbyResults = world.detect(detectionBox, null);

        for (var result : nearbyResults) {
            AbstractEntityModel entity = (AbstractEntityModel) result.getBody();

            // Filtriamo solo i proiettili
            if (!(entity instanceof Projectile p)) continue;

            Vector2 projPos = p.getTransform().getTranslation();
            Vector2 projVel = p.getLinearVelocity();

            // --- Quick rejects ---
            if (!isHostile(p, self)) continue;

            double distToBullet = projPos.distance(pos);
            if (distToBullet > detectionRadius) continue;

            double projSpeedSq = projVel.getMagnitudeSquared();
            if (projSpeedSq < 0.01) continue;

            Vector2 projDir = projVel.getNormalized();
            Vector2 toEntity = pos.copy().subtract(projPos);

            // --- 1. Is the bullet heading toward us? ---
            double distanceAlongPath = toEntity.dot(projDir);
            if (distanceAlongPath <= 0) continue;

            // --- 2. How close are we to the bullet's infinite line? ---
            double perpDistance = Math.abs(projVel.cross(toEntity)) / projVel.getMagnitude();
            if (perpDistance > evasionRadius) continue;

            // --- 3. Time-to-impact ---
            double t = distanceAlongPath / projVel.getMagnitude();
            if (t > lookAheadTime) continue;

            inDanger = true;

            // --- 4. Compute the ESCAPE VECTOR ---
            Vector2 closestPoint = projPos.copy().add(projDir.multiply(distanceAlongPath));
            Vector2 escapeDir = pos.copy().subtract(closestPoint);

            if (escapeDir.getMagnitudeSquared() < 0.001) {
                escapeDir = projDir.getRightHandOrthogonalVector();
            }
            escapeDir.normalize();

            // --- 5. Threat weights ---
            double spatialWeight = 1.0 - (perpDistance / evasionRadius);
            double temporalWeight = 1.0 - (t / lookAheadTime);
            double proximityWeight = 1.0 - (distToBullet / detectionRadius);

            double threat = spatialWeight * temporalWeight * proximityWeight;
            Vector2 singleDodgeForce = escapeDir.multiply(threat);

            // --- TRAPPOLA RILEVATA (Isteresi e Scivolamento) ---
            if (currentDodgeVector.getMagnitudeSquared() > 0.01) {
                Vector2 currentDir = currentDodgeVector.getNormalized();
                Vector2 forceDir = singleDodgeForce.getNormalized();
                double alignment = forceDir.dot(currentDir);

                if (alignment < -0.3) {
                    Vector2 slideRail = projDir.copy();
                    if (slideRail.dot(currentDodgeVector) < 0) slideRail.multiply(-1);
                    singleDodgeForce = slideRail.multiply(threat * 1.5);
                } else if (alignment > 0) {
                    singleDodgeForce.multiply(1.2);
                }
            }

            targetDodge.add(singleDodgeForce);
        }

        // --- 6. Smooth the raw dodge direction ---
        if (inDanger && targetDodge.getMagnitudeSquared() > 0.001) {
            currentDodgeVector = Interpolator.lerp(currentDodgeVector, targetDodge, smoothingFactor);
        } else {
            currentDodgeVector = Interpolator.lerp(currentDodgeVector, new Vector2(), smoothingFactor * 0.5);
        }

        // --- 7. Restituisce la Forza Pura ---
        if (currentDodgeVector.getMagnitudeSquared() > 0.01) {
            double magnitude = currentDodgeVector.getMagnitude();
            double cappedThreat = Math.min(magnitude, 1.5);
            return currentDodgeVector.getNormalized().multiply(cappedThreat * sidestepStrength * maxForce);
        }

        return new Vector2();
    }

    private boolean isHostile(Projectile p, AbstractEntityModel self) {
        if (self instanceof PlayerModel) {
            return p.getType() == ProjectileType.ENEMY_BULLET;
        }
        return p.getType() == ProjectileType.PLAYER_BULLET;
    }
}