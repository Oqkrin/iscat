package uni.gaben.iscat.universe.entity.brain;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entity.AbstractEntityModel;
import uni.gaben.iscat.universe.entity.EntityRecord;
import uni.gaben.iscat.utils.Cooldown;

import java.util.concurrent.atomic.AtomicReference;

@FunctionalInterface
public interface RotationGoal {
    /**
     * @return desired angle in radians, or Double.NaN if no rotation should occur.
     */
    double compute(AbstractEntityModel self, UniverseModel world, double dt);

    static RotationGoal createRotationGoal(EntityRecord.RotationRecord rotation) {
        if (rotation == null) return RotationGoal.idle();
        return switch (rotation.type()) {
            case MOVEMENT -> RotationGoal.movement();
            case TARGET -> RotationGoal.target(Target.ofPlayer());
            case CONTINUES_SPIN -> RotationGoal.continuesSpin(rotation.spinSpeedRadPerSec());
            case INTERVAL_SPIN -> RotationGoal.intervalSpin(rotation.spinSteps(), rotation.stepPauseSec(), rotation.spinSpeedRadPerSec());
            default -> RotationGoal.idle();
        };
    }

    static RotationGoal movement() {
        return (self, world, dt) -> {
            Vector2 vel = self.getLinearVelocity();
            return vel.getMagnitudeSquared() > 0.01 ? vel.getDirection() : Double.NaN;
        };
    }

    static RotationGoal target(Target target) {
        AtomicReference<Vector2> pos = new AtomicReference<>(UU.vector2zero());
        return (self, world, dt) -> {
            pos.set(target.getPosition(world));
            return pos.get().subtract(self.getTransform().getTranslation()).getDirection();
        };
    }

    static RotationGoal idle() {
        return (self, world, dt) -> Double.NaN;
    }

    static RotationGoal continuesSpin(double spinSpeedRadiansPerTick) {
        return new RotationGoal() {
            private double currentAngle = Double.NaN;
            @Override
            public double compute(AbstractEntityModel self, UniverseModel world, double dt) {
                if (Double.isNaN(currentAngle)) {
                    currentAngle = self.getTransform().getRotationAngle();
                }
                currentAngle += spinSpeedRadiansPerTick * dt;
                return currentAngle;
            }
        };
    }

    static RotationGoal intervalSpin(int spinSteps, double stepPauseSec, double speedRadPerSec) {
        return new RotationGoal() {
            private double currentAngle = Double.NaN;
            private double targetAngle = Double.NaN;
            private final Cooldown pauseTimer = new Cooldown(stepPauseSec);
            private final double stepDelta = Math.TAU / spinSteps;
            private boolean isPaused = true;

            @Override
            public double compute(AbstractEntityModel self, UniverseModel world, double dt) {
                // 1. Lazy Initialization on the first valid simulation frame
                if (Double.isNaN(currentAngle)) {
                    currentAngle = self.getTransform().getRotationAngle();
                    targetAngle = currentAngle;
                    pauseTimer.start(); // Start the sequence with an initial pause
                    isPaused = true;
                }

                if (isPaused) {
                    // --- PAUSED STATE ---
                    pauseTimer.update(dt);
                    if (pauseTimer.isReady()) {
                        isPaused = false;
                        // Support both positive and negative rotation directions naturally
                        double direction = Math.signum(speedRadPerSec) >= 0 ? 1.0 : -1.0;
                        targetAngle = currentAngle + (stepDelta * direction);
                    }
                } else {
                    // --- ROTATING STATE ---
                    double direction = Math.signum(speedRadPerSec) >= 0 ? 1.0 : -1.0;

                    // Move cleanly over time using delta time (dt)
                    currentAngle += speedRadPerSec * dt;

                    // Check if we have arrived at or overshot the precise step target
                    boolean overshot = (direction > 0 && currentAngle >= targetAngle) ||
                            (direction < 0 && currentAngle <= targetAngle);

                    if (overshot) {
                        currentAngle = targetAngle; // Hard clamp to target to eliminate floating-point drift
                        pauseTimer.start();         // Transition into cooling down phase
                        isPaused = true;
                    }
                }

                // Sync the mutated state with the world transform and return the angle
                self.getTransform().setRotation(currentAngle);
                return currentAngle;
            }
        };
    }
}