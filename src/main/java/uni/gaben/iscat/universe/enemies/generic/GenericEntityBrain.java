package uni.gaben.iscat.universe.enemies.generic;

import uni.gaben.iscat.universe.brain.Brain;
import uni.gaben.iscat.universe.brain.goals.MovementGoal;
import uni.gaben.iscat.universe.brain.goals.RotationGoal;
import uni.gaben.iscat.universe.brain.Target;
import uni.gaben.iscat.universe.brain.modifiers.flocking.AlignmentModifier;
import uni.gaben.iscat.universe.brain.modifiers.flocking.CohesionModifier;
import uni.gaben.iscat.universe.brain.modifiers.flocking.SeparationModifier;

/**
 * A single Brain class that replaces all per-enemy Brain/Controller classes.
 * Behavior is driven entirely by GenericEntitySettings.behaviorType:
 *   WANDER_SHOOT → wanders around player, flocking with same-type enemies
 *   RAM          → charges directly at player
 *   IDLE         → stands still, no goal, no rotation (worm segments etc.)
 * No shoot actions are wired — combat can be added later per behavior type
 * without touching any other class.
 */
public class GenericEntityBrain extends Brain<GenericEntityModel> {

    public GenericEntityBrain(GenericEntityModel entity) {
        super(entity,
                MovementGoal.idle(),
                entity.getSettings().force,
                entity.getSettings().maxVelocity,
                entity.getSettings().rotationSpeed);

        GenericEntitySettings s = entity.getSettings();

        switch (s.behaviorType) {

            case WANDER_SHOOT -> {
                setMovementGoal(MovementGoal.wanderAroundTarget(
                        s.force,
                        s.combatRange,
                        s.detectionRange));

                setRotationGoal(RotationGoal.target(Target.ofPlayer()));

                var flock = Target.ofEntities(world ->
                        world.getEntities().stream()
                                .filter(GenericEntityModel.class::isInstance)
                                .map(GenericEntityModel.class::cast)
                                .filter(e -> e != entity)
                                .filter(e -> e.getSettings().entityKey.equals(s.entityKey))
                                .collect(java.util.stream.Collectors.toList()));

                addModifier(new CohesionModifier(flock,  s.detectionRange,       1.3));
                addModifier(new AlignmentModifier(flock, s.detectionRange,       Math.random()));
                addModifier(new SeparationModifier(flock, s.detectionRange / 2,  1.5));
            }

            case RAM -> {
                setMovementGoal(MovementGoal.wanderAroundTarget(
                        s.force,
                        0.0,           // combatRange = 0 → always closing in
                        s.detectionRange));

                setRotationGoal(RotationGoal.target(Target.ofPlayer()));
            }

            case IDLE -> {
                // MovementGoal.idle() è in super()
            }
        }
    }
}