package uni.gaben.iscat.universe.entity.brain;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entity.AbstractEntityModel;

import java.util.List;

@FunctionalInterface
public interface SteeringGoal {

    Vector2 computeDesiredVelocity(AbstractEntityModel self, UniverseModel universe, double dt);

    static SteeringGoal idle() {
        Vector2 idleVelocity = UU.vector2zero();
        return (_, _, _) -> idleVelocity;
    }

    /**
     * Classic Craig Reynolds Pursuit using generalized matrix heuristics.
     */
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
                    self.getLinearVelocity().getMagnitude(), self.getMaxVelocity()
            );
            lookAheadTime = Math.min(lookAheadTime, maxPredictionTime);

            // Generalization usage: Zero allocations extrapolate
            Predictor.extrapolate(target, universe, lookAheadTime, pursuitPos);

            pursuitVelocity.set(pursuitPos).subtract(selfPos);
            if (!pursuitVelocity.isZero()) {
                pursuitVelocity.setMagnitude(self.getMaxVelocity());
            }

            return pursuitVelocity;
        };
    }

    /**
     * Classic OpenSteer Evade using capability closing limits.
     */
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
                    selfPos, targetPos, self.getMaxVelocity(), entity.getLinearVelocity().getMagnitude()
            );
            lookAheadTime = Math.min(lookAheadTime, maxPredictionTime);

            // Generalization usage: Zero allocations extrapolate
            Predictor.extrapolate(target, universe, lookAheadTime, predictedPos);

            evadeVelocity.set(selfPos).subtract(predictedPos);
            if (!evadeVelocity.isZero()) {
                evadeVelocity.setMagnitude(self.getMaxVelocity());
            }

            return evadeVelocity;
        };
    }

    /**
     * Craig Reynolds Pursuit with an integrated comfort zone interval [minDistance, maxDistance].
     * - Outside maxDistance: Pursues the predicted position at max velocity.
     * - Inside minDistance: Backs away (Evades) from the predicted position at max velocity.
     * - Within the interval: Smoothly matches the target's velocity to maintain distance.
     */
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
                    self.getLinearVelocity().getMagnitude(), self.getMaxVelocity()
            );
            lookAheadTime = Math.min(lookAheadTime, maxPredictionTime);

            // Zero allocations extrapolate
            Predictor.extrapolate(target, universe, lookAheadTime, predictedPos);

            // Calculate vector pointing from self to the predicted location
            desiredVelocity.set(predictedPos).subtract(selfPos);
            double distance = desiredVelocity.getMagnitude();

            if (distance > maxDistance) {
                // State A: Too far away -> Close the gap at maximum velocity
                if (distance > 0) {
                    desiredVelocity.setMagnitude(self.getMaxVelocity());
                }
            } else if (distance < minDistance) {
                // State B: Too close -> Invert direction and back off (Evade)
                if (distance > 0) {
                    desiredVelocity.multiply(-1.0);
                    desiredVelocity.setMagnitude(self.getMaxVelocity());
                } else {
                    // Fallback to prevent NaN if perfectly overlapping
                    desiredVelocity.set(1, 0).setMagnitude(self.getMaxVelocity());
                }
            } else {
                // State C: Comfort Zone -> Shadow the target smoothly by matching its velocity
                // Alternatively, use desiredVelocity.set(0, 0) if you want it to halt completely
                desiredVelocity.set(entity.getLinearVelocity());
            }

            return desiredVelocity;
        };
    }

    /**
     * OpenSteer Evade with a safety range parameter.
     * - Inside safetyDistance: Actively evades the predicted threat position.
     * - Outside safetyDistance: Threat is neutralized/ignored; sets desired velocity to zero.
     */
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
                    selfPos, targetPos, self.getMaxVelocity(), entity.getLinearVelocity().getMagnitude()
            );
            lookAheadTime = Math.min(lookAheadTime, maxPredictionTime);

            // Zero allocations extrapolate
            Predictor.extrapolate(target, universe, lookAheadTime, predictedPos);

            // Calculate vector pointing from predicted threat to self
            desiredVelocity.set(selfPos).subtract(predictedPos);
            double distance = desiredVelocity.getMagnitude();

            if (distance < safetyDistance) {
                // Threat is inside the safety bubble -> Flee at max capability
                if (distance > 0) {
                    desiredVelocity.setMagnitude(self.getMaxVelocity());
                } else {
                    desiredVelocity.set(1, 0).setMagnitude(self.getMaxVelocity());
                }
            } else {
                // Threat is safely out of range -> Stand down
                desiredVelocity.set(0, 0);
            }

            return desiredVelocity;
        };
    }

}