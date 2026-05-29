package uni.gaben.iscat.universe.enemies.mob;

import uni.gaben.iscat.universe.brain.Brain;
import uni.gaben.iscat.universe.brain.Target;
import uni.gaben.iscat.universe.brain.actions.shoot.LineOfSightShootAction;
import uni.gaben.iscat.universe.brain.goals.MovementGoal;
import uni.gaben.iscat.universe.brain.goals.RotationGoal;
import uni.gaben.iscat.universe.lib.implementations.attacks.SingleShotAttack;
import uni.gaben.iscat.universe.player.PlayerModel;
import uni.gaben.iscat.universe.projectiles.ProjectileType;

import static uni.gaben.iscat.universe.enemies.mob.IscatMobSettings.ISCATMOB;

public class IscatMobBrain extends Brain<IscatMobModel> {

    public IscatMobBrain(IscatMobModel entity) {
        super(entity, MovementGoal.idle(), ISCATMOB.force, ISCATMOB.maxVelocity, ISCATMOB.rotationSpeed);

        Target playerTarget = Target.ofDynamic(world -> {
            PlayerModel player = world.getPlayer();
            return player != null ? player.getTransform().getTranslation() : null;
        });

        //setRotationGoal(RotationGoal.target(playerTarget));

        addAction(new LineOfSightShootAction(
                ISCATMOB.combatRange,
                ISCATMOB.fireCooldownS,
                ProjectileType.ENEMY_BULLET,
                new SingleShotAttack(),
                playerTarget,
                false,
                Math.toRadians(ISCATMOB.detectionRange)
        ));

    }
}