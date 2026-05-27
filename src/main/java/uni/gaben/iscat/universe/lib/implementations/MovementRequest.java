package uni.gaben.iscat.universe.lib.implementations.behaviors.core;

import org.dyn4j.geometry.Vector2;

/**
 * Represents what a {@link MovementBehavior} wants to do this frame.
 * <p>
 * Behaviors never call {@code applyForce} directly. Instead they return a
 * {@code MovementRequest}, and {@link SteeringController} translates it into
 * a single physics call per frame — eliminating force conflicts between
 * competing movement behaviors.
 * </p>
 *
 * <pre>
 *   desiredVelocity  ← where to go and how fast (null = stay still)
 *   rotationTarget   ← which way to face (NaN = don't change rotation)
 *   lockMovement     ← true suppresses all other movement blending (e.g. during a plunge)
 * </pre>
 */
public record MovementRequest(
        Vector2 desiredVelocity,
        double  rotationTarget,
        boolean lockMovement
) {
    /** No movement, no rotation change. */
    public static MovementRequest idle() {
        return new MovementRequest(null, Double.NaN, false);
    }

    /** Normal movement + rotation. */
    public static MovementRequest of(Vector2 desiredVelocity, double rotationTarget) {
        return new MovementRequest(desiredVelocity, rotationTarget, false);
    }

    /** Locks out every other movement behavior this frame (use for impulse attacks). */
    public static MovementRequest locked(Vector2 desiredVelocity, double rotationTarget) {
        return new MovementRequest(desiredVelocity, rotationTarget, true);
    }

    /** Movement only, no rotation override. */
    public static MovementRequest moveOnly(Vector2 desiredVelocity) {
        return new MovementRequest(desiredVelocity, Double.NaN, false);
    }
}
