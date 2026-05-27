package uni.gaben.iscat.universe.lib.implementations.behaviors.movement;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Ray;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.result.RaycastResult;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.lib.implementations.behaviors.core.MovementRequest;
import uni.gaben.iscat.universe.lib.implementations.behaviors.interfaces.MovementBehavior;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.projectiles.Projectile;
import uni.gaben.iscat.universe.projectiles.ProjectileType;
import uni.gaben.iscat.utils.Cooldown;

import java.util.List;

/**
 * Detects incoming player projectiles via raycasting and takes evasive action.
 *
 * <h2>Priority model</h2>
 * <p>This is now a {@link MovementBehavior}, not a passive. When a projectile
 * is detected inside {@code detectionRadius} it returns a high priority
 * ({@value #PRIORITY_THREAT}), overriding chase/orbit/wander. When there is
 * no immediate threat it returns 0 and normal movement runs unimpeded.</p>
 *
 * <h2>Dodge strategy</h2>
 * <ul>
 *   <li><b>Post-cooldown + close threat:</b> explosive lateral dash impulse.</li>
 *   <li><b>Waiting for cooldown:</b> continuous low-speed lateral walk to
 *       pre-position, weighted away from ally crowd centre.</li>
 * </ul>
 */
public class DodgeProjectileBehavior implements MovementBehavior {

    private static final double PRIORITY_THREAT   = 80.0;
    private static final double DASH_TRIGGER_DIST = 6.0;

    private final double   dodgeVelocity;
    private final double   detectionRadius;
    private final Cooldown dodgeCooldown = new Cooldown();

    // Cached threat direction this frame
    private Vector2 cachedDodgeDir = null;
    private boolean threatThisFrame = false;

    public DodgeProjectileBehavior(double dodgeVelocity, double detectionRadius,
                                    double cooldownSeconds) {
        this.dodgeVelocity    = dodgeVelocity;
        this.detectionRadius  = detectionRadius;
        this.dodgeCooldown.start(cooldownSeconds);
    }

    @Override
    public void tick(AbstractEntityModel npc, UniverseModel universe, double dt) {
        dodgeCooldown.update(dt);
        // Re-scan every tick so getPriority is accurate
        threatThisFrame = false;
        cachedDodgeDir  = null;

        Vector2 npcPos     = npc.getTransform().getTranslation();
        Vector2 npcForward = new Vector2(npc.getTransform().getRotationAngle());

        Ray ray = new Ray(npcPos, npcForward);
        List<RaycastResult<Body, BodyFixture>> results = universe.raycast(ray, detectionRadius, null);
        if (results == null) return;

        Projectile closest     = null;
        double     closestDist = Double.MAX_VALUE;

        for (RaycastResult<Body, BodyFixture> r : results) {
            Body b = r.getBody();
            if (b instanceof Projectile proj && proj.getType() == ProjectileType.PLAYER_BULLET) {
                double d = proj.getTransform().getTranslation().distance(npcPos);
                if (d < closestDist) {
                    closestDist = d;
                    closest     = proj;
                }
            }
        }

        if (closest != null) {
            threatThisFrame = true;
            cachedDodgeDir  = computeDodgeDir(npc, npcForward, universe);
        }
    }

    @Override
    public double getPriority(AbstractEntityModel npc, UniverseModel universe) {
        return threatThisFrame ? PRIORITY_THREAT : 0.0;
    }

    @Override
    public MovementRequest computeRequest(AbstractEntityModel npc, UniverseModel universe, double dt) {
        if (!threatThisFrame || cachedDodgeDir == null) return MovementRequest.idle();

        // During cooldown: slow lateral walk to pre-position
        // When cooldown is over and threat is close: lock movement for an explosive dash
        if (!dodgeCooldown.isCoolingDown()) {
            // Dash: apply impulse, then start cooldown
            npc.applyImpulse(cachedDodgeDir.multiply(dodgeVelocity * 3.0));
            dodgeCooldown.start(2.0);
            return MovementRequest.locked(cachedDodgeDir.multiply(dodgeVelocity), Double.NaN);
        } else {
            // Slow pre-positioning walk (doesn't lock movement, lets steering blend it)
            return MovementRequest.of(cachedDodgeDir.multiply(dodgeVelocity * 0.4), Double.NaN);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────

    private Vector2 computeDodgeDir(AbstractEntityModel npc, Vector2 npcForward, UniverseModel universe) {
        Vector2 dir1 = new Vector2(-npcForward.y,  npcForward.x);
        Vector2 dir2 = new Vector2( npcForward.y, -npcForward.x);

        // Prefer direction that moves away from ally crowd centre
        Vector2 npcPos      = npc.getTransform().getTranslation();
        Vector2 crowdCentre = new Vector2();
        int     count       = 0;

        for (AbstractEntityModel ally : universe.getEntitiesOfType(npc.getClass())) {
            if (ally == npc) continue;
            if (ally.getTransform().getTranslation().distance(npcPos) < 10.0) {
                crowdCentre.add(ally.getTransform().getTranslation());
                count++;
            }
        }

        if (count > 0) {
            crowdCentre.divide(count);
            Vector2 toCrowd = crowdCentre.subtract(npcPos).getNormalized();
            // Choose the direction that points AWAY from the crowd
            return dir1.dot(toCrowd) < dir2.dot(toCrowd) ? dir1 : dir2;
        }

        return Math.random() > 0.5 ? dir1 : dir2;
    }
}
