package uni.gaben.iscat.universe.entity.brain;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entity.AbstractEntityModel;

import java.util.List;

@FunctionalInterface
public interface SteeringGoal {

    Vector2 computeDesiredVelocity(AbstractEntityModel self, double maxVelocity, UniverseModel universe, double dt);

    static SteeringGoal idle() {
        Vector2 idleVelocity = UU.vector2zero();
        return (_, _, _, _) -> idleVelocity;
    }

    /**
     * Classic Craig Reynolds Pursuit using the empirical 9-case matrix from OpenSteer.
     * @param maxPredictionTime Strict cap on how many seconds into the future the AI can predict.
     */
    static SteeringGoal pursuit(Target target, double maxPredictionTime) {
        Vector2 pursuitVelocity = UU.vector2zero();
        Vector2 pursuitPos = UU.vector2zero();
        Vector2 offset = UU.vector2zero();
        Vector2 unitOffset = UU.vector2zero();
        Vector2 selfForward = UU.vector2zero();
        Vector2 targetForward = UU.vector2zero();

        return (self, maxVelocity, universe, dt) -> {
            List<AbstractEntityModel> targets = target.getEntities(universe);
            if (targets == null || targets.isEmpty()) return pursuitVelocity.set(0, 0);

            AbstractEntityModel entity = targets.get(0);
            if (entity.shouldRemove()) return pursuitVelocity.set(0, 0);

            Vector2 selfPos = self.getTransform().getTranslation();
            Vector2 targetPosition = entity.getTransform().getTranslation();

            // 1. Calculate basic distance metrics
            offset.set(targetPosition).subtract(selfPos);
            double distance = offset.getMagnitude();
            if (distance < 0.001) return pursuitVelocity.set(0, 0);

            unitOffset.set(offset).divide(distance);

            // 2. Derive 2D heading vectors from entity rotation angles
            double selfAngle = self.getTransform().getRotationAngle();
            selfForward.set(Math.cos(selfAngle), Math.sin(selfAngle));

            double targetAngle = entity.getTransform().getRotationAngle();
            targetForward.set(Math.cos(targetAngle), Math.sin(targetAngle));

            // 3. Compute geometric dot products (Reynolds Heuristics)
            double parallelness = selfForward.dot(targetForward);
            double forwardness = selfForward.dot(unitOffset);

            // 4. Calculate direct baseline travel time (with a safe floor to avoid dividing by 0)
            double currentSpeed = self.getLinearVelocity().getMagnitude();
            double speedBaseline = currentSpeed > 0.1 ? currentSpeed : maxVelocity;
            double directTravelTime = distance / speedBaseline;

            // Map continuous dot-product spaces into discrete [-1, 0, 1] intervals (~45 degree cones)
            double et = getEt(forwardness, parallelness, directTravelTime);
            double lookAheadTime = Math.min(et, maxPredictionTime);

            Vector2 targetVelocity = entity.getLinearVelocity();
            pursuitPos.set(
                    targetPosition.x + (targetVelocity.x * lookAheadTime),
                    targetPosition.y + (targetVelocity.y * lookAheadTime)
            );

            // 7. Standard Reynolds Seek toward predicted point
            pursuitVelocity.set(pursuitPos).subtract(selfPos);
            if (!pursuitVelocity.isZero()) {
                pursuitVelocity.setMagnitude(maxVelocity);
            }

            return pursuitVelocity;
        };
    }

    private static double getEt(double forwardness, double parallelness, double directTravelTime) {
        int f = intervalComparison(forwardness, -0.707, 0.707);
        int p = intervalComparison(parallelness, -0.707, 0.707);

        // 5. Evaluate the OpenSteer 9-Case Matrix
        double timeFactor = 0.0;
        timeFactor = switch (f) {
            case 1 -> // Target is Ahead
                // Ahead, Anti-Parallel
                    switch (p) {
                        case 1 -> 4.0; // Ahead, Parallel
                        case 0 -> 1.8; // Ahead, Perpendicular
                        case -1 -> 0.85;
                        default -> timeFactor;
                    };
            case 0 -> // Target is Aside
                // Aside, Anti-Parallel
                    switch (p) {
                        case 1 -> 1.0; // Aside, Parallel
                        case 0 -> 0.8; // Aside, Perpendicular
                        case -1 -> 4.0;
                        default -> timeFactor;
                    };
            case -1 -> // Target is Behind
                // Behind, Anti-Parallel
                    switch (p) {
                        case 1 -> 0.5; // Behind, Parallel
                        case 0 -> 2.0; // Behind, Perpendicular
                        case -1 -> 2.0;
                        default -> timeFactor;
                    };
            default -> 0.0;
        };

        // 6. Predict future intersection target
        return directTravelTime * timeFactor;
    }

    /**
     * Classic OpenSteer Evade. Uses combined relative closing speeds
     * to determine look-ahead time, then flies directly away.
     */
    static SteeringGoal evade(Target target, double maxPredictionTime) {
        // Hoisted workspace objects
        Vector2 evadeVelocity = UU.vector2zero();
        Vector2 predictedPos = UU.vector2zero();

        return (self, maxVelocity, universe, dt) -> {
            List<AbstractEntityModel> targets = target.getEntities(universe);
            if (targets == null || targets.isEmpty()) return evadeVelocity.set(0, 0);

            AbstractEntityModel entity = targets.get(0);
            if (entity.shouldRemove()) return evadeVelocity.set(0, 0);

            Vector2 selfPos = self.getTransform().getTranslation();
            Vector2 targetPosition = entity.getTransform().getTranslation();
            Vector2 targetVelocity = entity.getLinearVelocity();

            double distance = targetPosition.distance(selfPos);
            double threatSpeed = targetVelocity.getMagnitude();

            // OpenSteer Evade Formula: Prediction time is proportional to distance
            // divided by the sum of our max capabilities and their current speed.
            double lookAheadTime = distance / (maxVelocity + threatSpeed);
            if (lookAheadTime > maxPredictionTime) {
                lookAheadTime = maxPredictionTime;
            }

            // Predict where the threat will be
            predictedPos.set(
                    targetPosition.x + (targetVelocity.x * lookAheadTime),
                    targetPosition.y + (targetVelocity.y * lookAheadTime)
            );

            // True Evade: Flee directly away from the future threat position
            evadeVelocity.set(selfPos).subtract(predictedPos);
            if (!evadeVelocity.isZero()) {
                evadeVelocity.setMagnitude(maxVelocity);
            }

            return evadeVelocity;
        };
    }

    /**
     * Helper mapping a value into continuous intervals [-1, 0, 1].
     * Matches OpenSteer's intervalComparison function utility.
     */
    private static int intervalComparison(double value, double lower, double upper) {
        if (value < lower) return -1;
        if (value > upper) return 1;
        return 0;
    }
}