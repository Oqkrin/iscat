package uni.gaben.iscat.universe.brain.goals;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.brain.Target;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.utils.Cooldown;

@FunctionalInterface
public interface RotationGoal {
    /**
     * Computes the desired rotation angle.
     * @return the desired angle in radians, or null if the entity should not rotate.
     */
    Double compute(AbstractEntityModel self, UniverseModel world, double dt);

    // ── Face Movement Direction ──────────────────────────────────────────────
    static RotationGoal movement() {
        return (self, world, dt) -> {
            Vector2 vel = self.getLinearVelocity();
            if (vel.getMagnitudeSquared() > 0.01) {
                return vel.getDirection();
            }
            return null; // maintain current rotation if not moving significantly
        };
    }

    // ── Face a Specific Target ─────────────────────────────────────────────
    static RotationGoal target(Target target) {
        return (self, world, dt) -> {
            Vector2 pos = target.getPosition(world);
            if (pos == null) return null;
            Vector2 diff = pos.copy().subtract(self.getTransform().getTranslation());
            if (diff.getMagnitudeSquared() > 0.01) {
                return diff.getDirection();
            }
            return null;
        };
    }

    // ── Idle (Do not rotate) ───────────────────────────────────────────────
    static RotationGoal idle() {
        return (self, world, dt) -> null;
    }

    // ── Continuous Spin ────────────────────────────────────────────────────
    static RotationGoal continuesSpin(double spinSpeedRadiansPerTicks) {
        return new RotationGoal() {
            private double currentAngle = Double.NaN;

            @Override
            public Double compute(AbstractEntityModel self, UniverseModel world, double dt) {
                if (Double.isNaN(currentAngle)) {
                    currentAngle = self.getTransform().getRotationAngle();
                }
                currentAngle += spinSpeedRadiansPerTicks * dt;
                return currentAngle;
            }
        };
    }

    static RotationGoal intervalSpin(int spinSteps, double stepPauseSec, double stepSpeedRadiansPerTicks) {
        return new RotationGoal() {
            private double currentAngle = Double.NaN;
            private final Cooldown stepPause = new Cooldown(stepPauseSec);
            private double targetAngle = Double.NaN;
            @Override
            public Double compute(AbstractEntityModel self, UniverseModel world, double dt) {
                if(stepPause.isCoolingDown()) stepPause.update(dt);
                if (Double.isNaN(currentAngle)) {
                    currentAngle = self.getTransform().getRotationAngle();
                    targetAngle = currentAngle + (Math.TAU/spinSteps);
                }
                if (currentAngle >= targetAngle) {
                    targetAngle += (Math.TAU/spinSteps);
                    stepPause.start();
                }
                if (stepPause.isReady()) currentAngle += spinSteps * stepSpeedRadiansPerTicks * dt;
                return currentAngle;
            }
        };
    }
}
