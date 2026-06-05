package uni.gaben.iscat.universe.entity.brain;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entity.AbstractEntityModel;
import uni.gaben.iscat.utils.Cooldown;

@FunctionalInterface
public interface RotationGoal {
    /**
     * @return desired angle in radians, or Double.NaN if no rotation should occur.
     */
    double compute(AbstractEntityModel self, UniverseModel world, double dt);

    static RotationGoal movement() {
        return (self, world, dt) -> {
            Vector2 vel = self.getLinearVelocity();
            return vel.getMagnitudeSquared() > 0.01 ? vel.getDirection() : Double.NaN;
        };
    }

    static RotationGoal target(Target target) {
        return (self, world, dt) -> {
            Vector2 pos = target.getPosition(world);
            if (pos == null) return Double.NaN;
            Vector2 diff = pos.copy().subtract(self.getTransform().getTranslation());
            return diff.getMagnitudeSquared() > 0.01 ? diff.getDirection() : Double.NaN;
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

    static RotationGoal intervalSpin(int spinSteps, double stepPauseSec, double stepSpeedRadiansPerTick) {
        return new RotationGoal() {
            private double currentAngle = Double.NaN;
            private final Cooldown stepPause = new Cooldown(stepPauseSec);
            private double targetAngle = Double.NaN;
            @Override
            public double compute(AbstractEntityModel self, UniverseModel world, double dt) {
                if (stepPause.isCoolingDown()) stepPause.update(dt);
                if (Double.isNaN(currentAngle)) {
                    currentAngle = self.getTransform().getRotationAngle();
                    targetAngle = currentAngle + (Math.TAU / spinSteps);
                }
                if (currentAngle >= targetAngle) {
                    targetAngle += (Math.TAU / spinSteps);
                    stepPause.start();
                }
                if (stepPause.isReady()) {
                    currentAngle += spinSteps * stepSpeedRadiansPerTick * dt;
                }
                return currentAngle;
            }
        };
    }
}