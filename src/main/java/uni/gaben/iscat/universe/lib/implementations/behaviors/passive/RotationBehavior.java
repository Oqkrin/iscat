package uni.gaben.iscat.universe.lib.implementations.behaviors.passive;

import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.lib.implementations.behaviors.interfaces.PassiveBehavior;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.utils.Interpolator;

/**
 * Rotates the entity in fixed angular steps at a set interval,
 * useful for spinning projectile emitters or idle spin animations.
 *
 * <p>Runs as a {@link PassiveBehavior} so it composites on top of any
 * movement behavior that is active. If the movement behavior also sets
 * rotation via its {@code MovementRequest}, the last write wins — position
 * this behavior after movement in your registration order if you want it
 * to override, or use movement behaviors that pass {@code Double.NaN} for
 * rotation target.</p>
 */
public class RotationBehavior implements PassiveBehavior {

    private final double interval;
    private final double stepRadians;
    private final double rotationSpeed;
    private final int    stepsBeforeReset;

    private double targetAngle = 0.0;
    private double timer       = 0.0;
    private int    stepCount   = 0;

    /**
     * @param intervalSeconds   Time between rotation steps.
     * @param stepDegrees       Angle advanced per step.
     * @param rotationSpeed     Lerp speed toward the target angle (higher = snappier).
     * @param stepsBeforeReset  After this many steps, reset the step counter (loops).
     */
    public RotationBehavior(double intervalSeconds, double stepDegrees,
                             double rotationSpeed, int stepsBeforeReset) {
        this.interval         = intervalSeconds;
        this.stepRadians      = Math.toRadians(stepDegrees);
        this.rotationSpeed    = rotationSpeed;
        this.stepsBeforeReset = stepsBeforeReset;
    }

    @Override
    public void execute(AbstractEntityModel npc, UniverseModel universe, double dt) {
        timer += dt;
        if (timer >= interval || stepCount >= stepsBeforeReset) {
            timer -= interval;
            if (stepCount >= stepsBeforeReset) stepCount = 0;
            targetAngle += stepRadians;
            if (targetAngle >= Math.PI * 2.0) targetAngle -= Math.PI * 2.0;
            stepCount++;
        }

        npc.setAngularVelocity(0.0);
        double current = npc.getTransform().getRotationAngle();
        double diff    = targetAngle - current;
        while (diff < -Math.PI) diff += Math.PI * 2;
        while (diff >  Math.PI) diff -= Math.PI * 2;

        double next = Interpolator.lerp(current, current + diff,
                                         Math.min(rotationSpeed * dt, 1.0));
        npc.getTransform().setRotation(next);
    }
}
