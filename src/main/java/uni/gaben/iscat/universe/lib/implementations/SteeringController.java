package uni.gaben.iscat.universe.lib.implementations.behaviors.core;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.utils.Interpolator;

/**
 * Applies a {@link MovementRequest} to an entity as a single, consistent
 * physics call per frame.
 *
 * <h2>Why this exists</h2>
 * <p>Without a centralised steering layer, every behavior that called
 * {@code applyForce} or {@code setLinearVelocity} in the same frame would
 * conflict — producing jitter, speed oscillations, and unpredictable
 * trajectories. {@code SteeringController} is the <em>only</em> place that
 * touches physics for movement.</p>
 *
 * <h2>Steering model</h2>
 * <pre>
 *   steeringForce = (desiredVelocity - currentVelocity).normalize() × maxForce
 *   velocity clamped to maxVelocity after force application
 *   rotation lerped toward rotationTarget at rotationSpeed rad/s
 * </pre>
 */
public class SteeringController {

    private final double maxForce;
    private final double maxVelocity;
    private final double rotationSpeed; // rad/s

    public SteeringController(double maxForce, double maxVelocity, double rotationSpeed) {
        this.maxForce       = maxForce;
        this.maxVelocity    = maxVelocity;
        this.rotationSpeed  = rotationSpeed;
    }

    /**
     * Applies {@code request} to {@code entity}. Call once per frame after
     * the active {@link uni.gaben.iscat.universe.lib.implementations.behaviors.interfaces.MovementBehavior}
     * has been selected.
     *
     * @param entity  The entity to steer.
     * @param request The desired movement state, or {@code null} to do nothing.
     * @param dt      Delta time in seconds.
     */
    public void apply(AbstractEntityModel entity, MovementRequest request, double dt) {
        if (request == null) return;

        applyVelocity(entity, request.desiredVelocity(), dt);
        applyRotation(entity, request.rotationTarget(), dt);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ─────────────────────────────────────────────────────────────────────────

    private void applyVelocity(AbstractEntityModel entity, Vector2 desired, double dt) {
        if (desired == null || desired.getMagnitudeSquared() < 0.0001) return;

        entity.setAtRest(false);

        // Arrival: smoothly decelerate as we approach desired speed
        Vector2 currentVel = entity.getLinearVelocity();
        Vector2 steering   = desired.copy().subtract(currentVel);

        double mag = steering.getMagnitude();
        if (mag > maxForce) {
            steering.multiply(maxForce / mag);
        }
        entity.applyForce(steering);

        // Hard clamp so no behavior can accidentally exceed the speed limit
        if (entity.getLinearVelocity().getMagnitude() > maxVelocity) {
            entity.setLinearVelocity(
                    entity.getLinearVelocity().getNormalized().multiply(maxVelocity));
        }
    }

    private void applyRotation(AbstractEntityModel entity, double targetAngle, double dt) {
        if (Double.isNaN(targetAngle)) return;

        entity.setAngularVelocity(0.0);

        double current = entity.getTransform().getRotationAngle();
        double diff    = targetAngle - current;
        while (diff < -Math.PI) diff += Math.PI * 2;
        while (diff >  Math.PI) diff -= Math.PI * 2;

        double next = Interpolator.lerp(current, current + diff,
                                        Math.min(rotationSpeed * dt, 1.0));
        entity.getTransform().setRotation(next);
    }
}
