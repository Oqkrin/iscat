package uni.gaben.iscat.universe.entity;

import uni.gaben.iscat.universe.entity.brain.Brain;
import uni.gaben.iscat.universe.entity.brain.SteeringGoal;
import uni.gaben.iscat.universe.entity.brain.Target;
import uni.gaben.iscat.universe.entity.brain.RotationGoal;
import uni.gaben.iscat.universe.entity.brain.actions.HealAction;
import uni.gaben.iscat.universe.entity.brain.actions.shoot.ShootAction;
import uni.gaben.iscat.universe.entity.projectiles.ProjectileType;
import uni.gaben.iscat.universe.entity.projectiles.Shooters.MultiDirectionPatternShooter;
import uni.gaben.iscat.universe.entity.projectiles.Shooters.ParallelLinePatternShooter;
import uni.gaben.iscat.universe.entity.projectiles.Shooters.SingleShotPatternShooter;

/**
 * Controller logico unificato per l'Intelligenza Artificiale delle entità genericamente configurate.
 * Guida le routine decisionali della CPU e i comportamenti cinematici sulla base dei parametri
 * dinamici estratti dal database SQLite tramite {@link GenericEntitySettings}.
 */
public class GenericEntityBrain extends Brain<GenericEntityModel> {

    public GenericEntityBrain(GenericEntityModel entity) {
        super(entity,
                SteeringGoal.idle(),
                entity.getSettings().maxForce,
                entity.getSettings().maxVelocity,
                entity.getSettings().maxAngularVelocity,
                entity.getSettings().mass
        );

        setRotationGoal(RotationGoal.target(Target.ofPlayer()));

        GenericEntitySettings settings = entity.getSettings();

        switch (settings.entityKey) {

            case "iscat_mob":
                setSteeringGoal(SteeringGoal.pursuit(Target.ofPlayer(), entity.getSettings().maxForce/entity.getSettings().maxVelocity));
                addAction(new ShootAction(
                        settings.detectionRange,
                        settings.actionCooldownMS/1000,
                        ProjectileType.ENEMY_BULLET,
                        new SingleShotPatternShooter(),
                        Target.ofPlayer(),
                        false//,
                        // Math.PI/4,
                        // entity
                ));
                break;

            case "iscat_bomber":
                break;

            case "iscat_core":
                addAction(new ShootAction(
                        settings.detectionRange,
                        settings.actionCooldownMS/1000,
                        ProjectileType.ENEMY_BULLET,
                        new MultiDirectionPatternShooter(4, 0, new ParallelLinePatternShooter(3, 30)),
                        Target.ofPlayer(),
                        false
                ));
                break;

            case "iscat_mother":
                setSteeringGoal(SteeringGoal.pursuit(Target.ofPlayer(), entity.getSettings().maxForce/entity.getSettings().maxVelocity));
                addAction(new ShootAction(
                        settings.detectionRange,
                        settings.actionCooldownMS / 1000,
                        ProjectileType.ENEMY_BULLET,
                        new SingleShotPatternShooter(),
                        Target.ofPlayer(),
                        false
                ));
                break;

            case "fake_iscat":
                setSteeringGoal(SteeringGoal.pursuit(Target.ofPlayer(), entity.getSettings().maxForce/entity.getSettings().maxVelocity));

                addAction(new ShootAction(
                        settings.detectionRange,
                        settings.actionCooldownMS/1000,
                        ProjectileType.ENEMY_BULLET,
                        new SingleShotPatternShooter(),
                        //new RandomizedShootAction(),
                        Target.ofPlayer(),
                        false
                ));
                break;

            case "fallen_star_golem":
                addAction(new ShootAction(
                        settings.detectionRange,
                        settings.actionCooldownMS / 1000,
                        ProjectileType.ENEMY_BULLET,
                        new SingleShotPatternShooter(),
                        Target.ofPlayer(),
                        false
                ));
                break;

            case "eater":
                setSteeringGoal(SteeringGoal.pursuit(Target.ofPlayer(), entity.getSettings().maxForce/entity.getSettings().maxVelocity));
                break;

            case "iscat_worm_head":
                setSteeringGoal(SteeringGoal.pursuit(Target.ofPlayer(), entity.getSettings().maxForce/entity.getSettings().maxVelocity));
                break;

            case "iscat_worm_body_part":
                break;

            case "iscat_worm_tail":
                break;

            case "iscat_master":
                setSteeringGoal(SteeringGoal.pursuit(Target.ofPlayer(), entity.getSettings().maxForce/entity.getSettings().maxVelocity));
                addAction(new ShootAction(
                        settings.detectionRange,
                        settings.actionCooldownMS / 1000,
                        ProjectileType.ENEMY_BULLET,
                        new SingleShotPatternShooter(),
                        Target.ofPlayer(),
                        false
                ));
                break;

            case "iscat_dasher":
                setSteeringGoal(SteeringGoal.pursuit(Target.ofPlayer(), entity.getSettings().maxForce/entity.getSettings().maxVelocity));
                break;

            case "iscat_healer":
                setSteeringGoal(SteeringGoal.evade(Target.ofPlayer(), entity.getSettings().maxForce/entity.getSettings().maxVelocity));
                addAction(new HealAction(
                        settings.actionCooldownMS/1000,
                        settings.combatRange,
                        settings.xpReward
                ));
                break;

            default:
                break;
        }

    }
}