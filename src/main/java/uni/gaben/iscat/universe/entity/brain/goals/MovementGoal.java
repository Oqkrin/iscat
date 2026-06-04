package uni.gaben.iscat.universe.entity.brain.goals;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.entity.AbstractEntityModel;
import uni.gaben.iscat.universe.entity.brain.Target;

import java.util.List;

@FunctionalInterface
public interface MovementGoal {

    Vector2 computeDesiredVelocity(AbstractEntityModel self, double maxVelocity, UniverseModel universe, double dt);

    static MovementGoal idle() {
        Vector2 idleVelocity = UU.vector2zero();
        return (_, _, _, _) -> idleVelocity;
    }

    static MovementGoal pursuit(Target target) {
        // Hoisted vectors (Zero GC per frame)
        Vector2 pursuitVelocity = UU.vector2zero();
        Vector2 pursuitPos = UU.vector2zero();

        return (self, maxVelocity, universe, dt) -> {
            List<AbstractEntityModel> targets = target.getEntities(universe);

            // Safe access: If no targets exist, stop moving.
            if (targets == null || targets.isEmpty()) {
                return pursuitVelocity.set(0, 0);
            }

            AbstractEntityModel entity = targets.get(0);

            // Ensure the target isn't queued for removal this frame
            if (entity.shouldRemove()) {
                return pursuitVelocity.set(0, 0);
            }

            Vector2 selfPos = self.getTransform().getTranslation();
            Vector2 targetPosition = entity.getTransform().getTranslation();
            Vector2 targetVelocity = entity.getLinearVelocity();

            double distance = targetPosition.distance(selfPos);

            double lookAheadTime = distance / maxVelocity;

            // Predict future position: targetPosition + (targetVelocity * lookAheadTime)
            pursuitPos.set(
                    targetPosition.x + (targetVelocity.x * lookAheadTime),
                    targetPosition.y + (targetVelocity.y * lookAheadTime)
            );

            // Calculate desired steering velocity
            pursuitVelocity.set(pursuitPos).subtract(selfPos);

            return pursuitVelocity;
        };
    }
}