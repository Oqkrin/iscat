package uni.gaben.iscat.universe.lib.implementations.behaviors.passive;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.lib.implementations.behaviors.interfaces.PassiveBehavior;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.utils.Spring;

/**
 * Smoothly rotates the entity to face the player via a spring system.
 *
 * <p>Runs as a {@link PassiveBehavior}, meaning it layeres on top of whatever
 * movement behavior is active. Note: movement behaviors may also set rotation
 * via their {@code MovementRequest}. If you want {@code LookAtBehavior} to
 * fully control rotation, configure movement behaviors with
 * {@link uni.gaben.iscat.universe.lib.implementations.behaviors.core.MovementRequest#moveOnly}
 * (pass {@code Double.NaN} as rotation target).</p>
 *
 * <h3>Organic imprecision</h3>
 * <p>When {@code accuracy < 1.0}, a jitter offset drifts slowly toward a new
 * random target every {@value #JITTER_INTERVAL_S}s, producing a natural weapon
 * wobble instead of per-frame noise flickering.</p>
 */
public class LookAtBehavior implements PassiveBehavior {

    private static final double JITTER_INTERVAL_S = 0.4;
    private static final double JITTER_SMOOTHING  = 5.0;
    private static final double JITTER_MAX_RAD    = Math.PI * 0.15;

    private final Spring rotationSpring;
    private final double accuracy;

    private double currentAngle  = 0.0;
    private double jitterOffset  = 0.0;
    private double jitterTarget  = 0.0;
    private double jitterTimer   = 0.0;

    /**
     * @param stiffness Spring stiffness (higher = snappier tracking).
     * @param damping   Damping (critical ≈ 2√stiffness avoids oscillation).
     * @param accuracy  1.0 = perfect aim; lower values add organic wobble.
     */
    public LookAtBehavior(double stiffness, double damping, double accuracy) {
        this.rotationSpring = Spring.critico(0, stiffness, damping);
        this.accuracy       = Math.max(0.0, Math.min(1.0, accuracy));
    }

    @Override
    public void execute(AbstractEntityModel npc, UniverseModel universe, double dt) {
        Vector2 target = universe.getPlayer().getTransform().getTranslation();
        Vector2 myPos  = npc.getTransform().getTranslation();

        double targetAngle = Math.atan2(target.y - myPos.y, target.x - myPos.x);
        if (accuracy < 1.0) targetAngle += updateJitter(dt);

        double diff = targetAngle - currentAngle;
        while (diff < -Math.PI) diff += 2.0 * Math.PI;
        while (diff >  Math.PI) diff -= 2.0 * Math.PI;

        rotationSpring.setTarget(currentAngle + diff);
        rotationSpring.update(dt);

        currentAngle = rotationSpring.getPosition();
        npc.getTransform().setRotation(currentAngle);
    }

    private double updateJitter(double dt) {
        jitterTimer -= dt;
        if (jitterTimer <= 0.0) {
            double amplitude = (1.0 - accuracy) * JITTER_MAX_RAD;
            jitterTarget = (Math.random() - 0.5) * 2.0 * amplitude;
            jitterTimer  = JITTER_INTERVAL_S;
        }
        jitterOffset += (jitterTarget - jitterOffset) * dt * JITTER_SMOOTHING;
        return jitterOffset;
    }
}
