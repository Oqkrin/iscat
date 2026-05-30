package uni.gaben.iscat.universe.enemies.mob;

import uni.gaben.iscat.universe.brain.Brain;
import uni.gaben.iscat.universe.brain.Target;
import uni.gaben.iscat.universe.brain.actions.shoot.LineOfSightShootAction;
import uni.gaben.iscat.universe.brain.goals.MovementGoal;
import uni.gaben.iscat.universe.brain.goals.RotationGoal;
import uni.gaben.iscat.universe.brain.modifiers.flocking.AlignmentModifier;
import uni.gaben.iscat.universe.brain.modifiers.flocking.CohesionModifier;
import uni.gaben.iscat.universe.brain.modifiers.flocking.SeparationModifier;
import uni.gaben.iscat.universe.lib.implementations.attacks.SingleShotAttack;
import uni.gaben.iscat.universe.projectiles.ProjectileType;


import java.util.stream.Collectors;

import static uni.gaben.iscat.universe.enemies.mob.IscatMobSettings.ISCATMOB;



public class IscatMobBrain extends Brain<IscatMobModel> {
    public IscatMobBrain(IscatMobModel entity) {

        super(entity, MovementGoal.idle(), ISCATMOB.force, ISCATMOB.maxVelocity, ISCATMOB.rotationSpeed);


        setMovementGoal(MovementGoal.wanderAroundTarget(ISCATMOB.force,ISCATMOB.combatRange,ISCATMOB.detectionRange));

        // 2. Dynamic Flocking Target (Finds all nearby mobs, excluding itself)
        Target flock = Target.ofEntities(world ->
                world.getEntities().stream()
                        .filter(IscatMobModel.class::isInstance)
                        .filter(e -> e != entity) // Crucial: Do not flock with yourself
                        .collect(Collectors.toList())
        );

        addAction(new LineOfSightShootAction(
                ISCATMOB.combatRange,
                ISCATMOB.fireCooldownS,
                ProjectileType.ENEMY_BULLET,
                new SingleShotAttack(),
                Target.ofPlayer(),
                false,
                Math.toRadians(ISCATMOB.detectionRange)
        ));

        setRotationGoal(RotationGoal.target(Target.ofPlayer()));


        addModifier(new CohesionModifier(flock, ISCATMOB.detectionRange, 1.3));
        addModifier(new AlignmentModifier(flock, ISCATMOB.detectionRange, Math.random()));
        addModifier(new SeparationModifier(flock, ISCATMOB.detectionRange/2, 1.5));

    }
}