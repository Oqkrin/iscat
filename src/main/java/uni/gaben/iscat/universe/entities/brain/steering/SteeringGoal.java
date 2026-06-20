package uni.gaben.iscat.universe.entities.brain.steering;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.camera.CameraModel;
import uni.gaben.iscat.universe.entities.AbstractPhysicalEntityModel;
import uni.gaben.iscat.universe.entities.brain.target.Predictor;
import uni.gaben.iscat.universe.entities.brain.target.Target;

import java.util.List;

@FunctionalInterface
public interface SteeringGoal {

    Vector2 computeDesiredVelocity(AbstractPhysicalEntityModel self, UniverseModel universe, double dt);

    static SteeringGoal idle() {
        Vector2 idleVelocity = UU.vector2zero();
        return (_, _, _) -> idleVelocity;
    }

    static SteeringGoal pursuit(Target target, double maxPredictionTime) {
        Vector2 pursuitVelocity = UU.vector2zero();
        Vector2 pursuitPos = UU.vector2zero();

        return (self, universe, dt) -> {
            List<AbstractPhysicalEntityModel> targets = target.getEntities(universe);
            if (targets == null || targets.isEmpty()) return pursuitVelocity.set(0, 0);

            AbstractPhysicalEntityModel entity = targets.getFirst();
            if (entity.shouldRemove()) return pursuitVelocity.set(0, 0);

            Vector2 selfPos = self.getTransform().getTranslation();
            Vector2 targetPos = entity.getTransform().getTranslation();

            double lookAheadTime = Predictor.calculatePursuitTime(
                    selfPos, self.getTransform().getRotationAngle(),
                    targetPos, entity.getTransform().getRotationAngle(),
                    self.getLinearVelocity().getMagnitude(), self.getTerminalVelocity()
            );
            lookAheadTime = Math.min(lookAheadTime, maxPredictionTime);

            Predictor.extrapolate(target, universe, lookAheadTime, pursuitPos);

            pursuitVelocity.set(pursuitPos).subtract(selfPos);

            if (!pursuitVelocity.isZero()) {
                pursuitVelocity.setMagnitude(self.getTerminalVelocity()); // Desired Velocity
                pursuitVelocity.subtract(self.getLinearVelocity());  // Convert to Force

                if (!pursuitVelocity.isZero()) {
                    pursuitVelocity.setMagnitude(self.getAcceleration());
                }
            }

            return pursuitVelocity;
        };
    }

    static SteeringGoal evade(Target target, double maxPredictionTime) {
        Vector2 evadeVelocity = UU.vector2zero();
        Vector2 predictedPos = UU.vector2zero();

        return (self, universe, dt) -> {
            List<AbstractPhysicalEntityModel> targets = target.getEntities(universe);
            if (targets == null || targets.isEmpty()) return evadeVelocity.set(0, 0);

            AbstractPhysicalEntityModel entity = targets.getFirst();
            if (entity.shouldRemove()) return evadeVelocity.set(0, 0);

            Vector2 selfPos = self.getTransform().getTranslation();
            Vector2 targetPos = entity.getTransform().getTranslation();

            double lookAheadTime = Predictor.calculateEvadeTime(
                    selfPos, targetPos, self.getTerminalVelocity(), entity.getLinearVelocity().getMagnitude()
            );
            lookAheadTime = Math.min(lookAheadTime, maxPredictionTime);

            Predictor.extrapolate(target, universe, lookAheadTime, predictedPos);

            evadeVelocity.set(selfPos).subtract(predictedPos);

            if (!evadeVelocity.isZero()) {
                evadeVelocity.setMagnitude(self.getTerminalVelocity());
                evadeVelocity.subtract(self.getLinearVelocity());

                if (!evadeVelocity.isZero()) {
                    evadeVelocity.setMagnitude(self.getAcceleration());
                }
            }

            return evadeVelocity;
        };
    }

    static SteeringGoal pursuitWithRange(Target target, double maxPredictionTime,
                                         double minDistance, double maxDistance) {
        Vector2 desiredVelocity = UU.vector2zero();
        Vector2 predictedPos   = UU.vector2zero();

        return (self, universe, dt) -> {
            List<AbstractPhysicalEntityModel> targets = target.getEntities(universe);
            if (targets == null || targets.isEmpty()) return desiredVelocity.set(0, 0);

            AbstractPhysicalEntityModel entity = targets.getFirst();
            if (entity.shouldRemove()) return desiredVelocity.set(0, 0);

            Vector2 selfPos = self.getTransform().getTranslation();
            Vector2 targetPos = entity.getTransform().getTranslation();

            double lookAheadTime = Predictor.calculatePursuitTime(
                    selfPos, self.getTransform().getRotationAngle(),
                    targetPos, entity.getTransform().getRotationAngle(),
                    self.getLinearVelocity().getMagnitude(), self.getTerminalVelocity()
            );
            lookAheadTime = Math.min(lookAheadTime, maxPredictionTime);

            Predictor.extrapolate(target, universe, lookAheadTime, predictedPos);

            // Vector from self to predicted target
            desiredVelocity.set(predictedPos).subtract(selfPos);
            double distance = desiredVelocity.getMagnitude();
            double idealDistance = (minDistance + maxDistance) * 0.5;

            // Clamp ideal distance to camera view so they don't back away out of bounds
            CameraModel camera = universe.getCamera();
            if (camera != null) {
                double zoom = camera.getZoom();
                double halfVW = UU.pxToM((camera.getScreenWidth() / 2.0) / zoom);
                double halfVH = UU.pxToM((camera.getScreenHeight() / 2.0) / zoom);
                double minScreenDim = Math.min(halfVW, halfVH) - UU.pxToM(50.0); // 50px margin
                if (idealDistance > minScreenDim && minScreenDim > 0) {
                    idealDistance = minScreenDim;
                }
            }

            double error = distance - idealDistance;

            // Desired speed: proportional to error, clamped between ±terminalVelocity
            double gain = 3.0;   // stiffness – higher = tighter
            double rawSpeed = gain * error;
            double desiredSpeed;
            if (error > 0) {
                // moving towards – keep full authority
                desiredSpeed = Math.clamp(gain * error, 0, self.getTerminalVelocity());
            } else {
                // moving away – damp heavily
                double backSpeed = 0.4 * self.getTerminalVelocity();
                desiredSpeed = Math.clamp(gain * error, -backSpeed, 0);
            }
            // Direction: towards/away from predicted target
            if (distance > 0.0001) {
                desiredVelocity.normalize();
            } else {
                // If right on top, pick a safe direction (e.g., target's velocity or a random)
                desiredVelocity.set(entity.getLinearVelocity());
                if (desiredVelocity.isZero()) desiredVelocity.set(1, 0);
            }
            double deadZone = 0.5; // meters
            if (Math.abs(error) < deadZone) {
                return desiredVelocity.set(0, 0);
            }
            desiredVelocity.multiply(desiredSpeed);

            // Add the target's velocity to seamlessly follow when distance is ideal
            // (scale it so that when error=0 we fully match the target's movement)
            double followFactor = 1.0 - Math.abs(error) / (maxDistance - minDistance + 0.0001);
            followFactor = Math.clamp(followFactor, 0.0, 1.0);
            desiredVelocity.add(entity.getLinearVelocity().copy().multiply(followFactor));

            // Steering = desired velocity – current velocity
            desiredVelocity.subtract(self.getLinearVelocity());

            if (!desiredVelocity.isZero()) {
                desiredVelocity.setMagnitude(self.getAcceleration()); // Clamp acceleration
            }

            return desiredVelocity;
        };
    }

    static SteeringGoal evadeWithRange(Target target, double maxPredictionTime, double safetyDistance) {
        Vector2 desiredVelocity = UU.vector2zero();
        Vector2 predictedPos = UU.vector2zero();

        return (self, universe, dt) -> {
            List<AbstractPhysicalEntityModel> targets = target.getEntities(universe);
            if (targets == null || targets.isEmpty()) return desiredVelocity.set(0, 0);

            AbstractPhysicalEntityModel entity = targets.getFirst();
            if (entity.shouldRemove()) return desiredVelocity.set(0, 0);

            Vector2 selfPos = self.getTransform().getTranslation();
            Vector2 targetPos = entity.getTransform().getTranslation();

            double lookAheadTime = Predictor.calculateEvadeTime(
                    selfPos, targetPos, self.getTerminalVelocity(), entity.getLinearVelocity().getMagnitude()
            );
            lookAheadTime = Math.min(lookAheadTime, maxPredictionTime);

            Predictor.extrapolate(target, universe, lookAheadTime, predictedPos);

            desiredVelocity.set(selfPos).subtract(predictedPos);
            double distance = desiredVelocity.getMagnitude();

            if (distance < safetyDistance) {
                if (distance > 0) {
                    desiredVelocity.setMagnitude(self.getTerminalVelocity());
                } else {
                    desiredVelocity.set(1, 0).setMagnitude(self.getTerminalVelocity());
                }
            } else {
                desiredVelocity.set(entity.getLinearVelocity()); // Neutralize force
            }

            desiredVelocity.subtract(self.getLinearVelocity());

            if (!desiredVelocity.isZero()) {
                desiredVelocity.setMagnitude(self.getAcceleration());
            }

            return desiredVelocity;
        };
    }
}