package uni.gaben.iscat.universe.entity.brain;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entity.AbstractEntityModel;
import uni.gaben.iscat.universe.entity.EntityRecord;

import java.util.List;

@FunctionalInterface
public interface SteeringGoal {

    Vector2 computeDesiredVelocity(AbstractEntityModel self, UniverseModel universe, double dt);

    static SteeringGoal createSteeringGoal(EntityRecord.SteeringRecord steeringData) {
        Target target = Target.ofPlayer(); // could be extended to support other targets
        return switch (steeringData.type()) {
            case PURSUIT -> SteeringGoal.pursuit(target, steeringData.maxPredictionTime());
            case EVADE -> SteeringGoal.evade(target, steeringData.maxPredictionTime());
            case PURSUIT_WITH_RANGE ->
                    SteeringGoal.pursuitWithRange(target, steeringData.maxPredictionTime(), steeringData.minDistance(), steeringData.maxDistance());
            case EVADE_WITH_RANGE -> SteeringGoal.evadeWithRange(target, steeringData.maxPredictionTime(), steeringData.safetyDistance());
            default -> SteeringGoal.idle(); // IDLE and ORBIT both fall through to idle
        };
    }

    static SteeringGoal idle() {
        Vector2 idleVelocity = UU.vector2zero();
        return (_, _, _) -> idleVelocity;
    }

    static SteeringGoal pursuit(Target target, double maxPredictionTime) {
        Vector2 pursuitVelocity = UU.vector2zero();
        Vector2 pursuitPos = UU.vector2zero();

        return (self, universe, dt) -> {
            List<AbstractEntityModel> targets = target.getEntities(universe);
            if (targets == null || targets.isEmpty()) return pursuitVelocity.set(0, 0);

            AbstractEntityModel entity = targets.getFirst();
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
            List<AbstractEntityModel> targets = target.getEntities(universe);
            if (targets == null || targets.isEmpty()) return evadeVelocity.set(0, 0);

            AbstractEntityModel entity = targets.getFirst();
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

    static SteeringGoal pursuitWithRange(Target target, double maxPredictionTime, double minDistance, double maxDistance) {
        Vector2 desiredVelocity = UU.vector2zero();
        Vector2 predictedPos = UU.vector2zero();

        return (self, universe, dt) -> {
            List<AbstractEntityModel> targets = target.getEntities(universe);
            if (targets == null || targets.isEmpty()) return desiredVelocity.set(0, 0);

            AbstractEntityModel entity = targets.getFirst();
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

            desiredVelocity.set(predictedPos).subtract(selfPos);
            double distance = desiredVelocity.getMagnitude();

            if (distance > maxDistance) {
                if (distance > 0) desiredVelocity.setMagnitude(self.getTerminalVelocity());
            } else if (distance < minDistance) {
                if (distance > 0) {
                    desiredVelocity.multiply(-1.0);
                    desiredVelocity.setMagnitude(self.getTerminalVelocity());
                } else {
                    desiredVelocity.set(1, 0).setMagnitude(self.getTerminalVelocity());
                }
            } else {
                desiredVelocity.set(entity.getLinearVelocity());
            }

            desiredVelocity.subtract(self.getLinearVelocity());

            if (!desiredVelocity.isZero()) {
                desiredVelocity.setMagnitude(self.getAcceleration());
            }

            return desiredVelocity;
        };
    }

    static SteeringGoal evadeWithRange(Target target, double maxPredictionTime, double safetyDistance) {
        Vector2 desiredVelocity = UU.vector2zero();
        Vector2 predictedPos = UU.vector2zero();

        return (self, universe, dt) -> {
            List<AbstractEntityModel> targets = target.getEntities(universe);
            if (targets == null || targets.isEmpty()) return desiredVelocity.set(0, 0);

            AbstractEntityModel entity = targets.getFirst();
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