package uni.gaben.iscat.universe.entity;

import uni.gaben.iscat.universe.entity.brain.Brain;
import uni.gaben.iscat.universe.entity.brain.SteeringGoal;
import uni.gaben.iscat.universe.entity.brain.Target;
import uni.gaben.iscat.universe.entity.brain.RotationGoal;
import uni.gaben.iscat.universe.entity.brain.actions.HealAction;
import uni.gaben.iscat.universe.entity.brain.actions.shoot.RandomizedShootAction;
import uni.gaben.iscat.universe.entity.brain.actions.shoot.ShootAction;
import uni.gaben.iscat.universe.entity.projectiles.ProjectileType;
import uni.gaben.iscat.universe.entity.projectiles.Shooters.*;

/**
 * Controller logico unificato per l'Intelligenza Artificiale delle entità genericamente configurate.
 * Guida le routine decisionali della CPU e i comportamenti cinematici sulla base dei parametri
 * dinamici estratti dal database SQLite tramite {@link GenericEntitySettings}.
 */
public class GenericEntityBrain extends Brain<GenericEntityModel> {

    public GenericEntityBrain(GenericEntityModel entity) {
        super(entity,
                SteeringGoal.pursuit(Target.ofPlayer(), entity.getMaxForce() / entity.getMaxVelocity())
        );

        setRotationGoal(RotationGoal.target(Target.ofPlayer()));

        GenericEntitySettings settings = entity.getSettings();

        loadBehaviorsFromSettings(settings);
    }

    /**
     * STUB: Temporary hardcoded mapping of EntityKeys to AI Actions.
     * In the future, this will be replaced by an ActionBuilder/BehaviorProvider
     * that parses the JSON/String definitions from GenericEntitySettings.
     */
    private void loadBehaviorsFromSettings(GenericEntitySettings settings) {
        switch (settings.entityKey) {
            case "iscat_mob":
                addAction(new ShootAction(
                        settings.detectionRange,
                        settings.actionCooldownMS/1000,
                        ProjectileType.ENEMY_BULLET,
                        new SingleShotPatternShooter(),
                        Target.ofPlayer(),
                        false
                ));
                break;

            case "iscat_bomber":
                addAction(new ShootAction(
                        settings.detectionRange,
                        settings.actionCooldownMS/1000,
                        ProjectileType.ENEMY_BULLET,
                        new SummonPatternShooter(3, "BLACKHOLE", settings.detectionRange),
                        Target.ofPlayer(),
                        true
                ));
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
                addAction(RandomizedShootAction.targetingPlayer(
                        settings.detectionRange,
                        settings.actionCooldownMS/1000,
                        ProjectileType.ENEMY_BULLET,
                        true,
                        new SummonPatternShooter(3,"iscat_mob",100),
                        new SummonPatternShooter(1,"fake_iscat",100),
                        new SummonPatternShooter(2,"iscat_healer",100),
                        new RepeaterPatternShooter(4,1, new SpreadPatternShooter(5, 30))
                ));
                break;

            case "fake_iscat":
                addAction(RandomizedShootAction.targetingPlayer(
                        settings.detectionRange,
                        settings.actionCooldownMS/1000,
                        ProjectileType.ENEMY_BULLET,
                        true,
                        new SpreadPatternShooter(3, 30),
                        new MultiDirectionPatternShooter(8, 0, new SingleShotPatternShooter()),
                        new RepeaterPatternShooter(3,0.25, new SingleShotPatternShooter())
                ));
                break;

            case "fallen_star_golem":
                addAction(new ShootAction(
                        settings.detectionRange,
                        settings.actionCooldownMS/1000,
                        ProjectileType.ENEMY_BULLET,
                        new RepeaterPatternShooter(2, 0.5, new RingPatternShooter(16)),
                        Target.ofPlayer(),
                        false
                ));
                break;

            case "eater":
            case "iscat_worm_head":
            case "iscat_worm_body_part":
                break;

            case "iscat_worm_tail":
                addAction(RandomizedShootAction.targetingPlayer(
                        settings.detectionRange,
                        settings.actionCooldownMS/1000,
                        ProjectileType.ENEMY_BULLET,
                        true,
                        new RingPatternShooter(16),
                        new RingPatternShooter(8),
                        new RingPatternShooter(24)
                ));
                break;

            case "iscat_master":
                addAction(RandomizedShootAction.targetingPlayer(
                        settings.detectionRange,
                        settings.actionCooldownMS/1000,
                        ProjectileType.ENEMY_BULLET,
                        true,
                        new RingPatternShooter(24),
                        new SummonPatternShooter(3,"eater",100),
                        new SummonPatternShooter(2,"iscat_dasher",100),
                        new RepeaterPatternShooter(3,0.25, new RingPatternShooter(24)),
                        new RepeaterPatternShooter(2,0.25, new SpreadPatternShooter(24, 60))
                ));
                break;

            case "iscat_dasher":
                break;

            case "iscat_healer":
                addAction(new HealAction(settings.actionCooldownMS, settings.combatRange, settings.initLife/10));
                break;

            default:
                addAction(new ShootAction(
                        settings.detectionRange,
                        settings.actionCooldownMS/1000,
                        ProjectileType.ENEMY_BULLET,
                        new SingleShotPatternShooter(),
                        Target.ofPlayer(),
                        true
                ));
                break;
        }
    }
}