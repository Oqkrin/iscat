package uni.gaben.iscat.universe.entity;

import org.dyn4j.collision.Filter;
import uni.gaben.iscat.universe.entity.brain.*;
import uni.gaben.iscat.universe.entity.brain.actions.HealAction;
import uni.gaben.iscat.universe.entity.brain.actions.shoot.AbstractShootAction;
import uni.gaben.iscat.universe.entity.brain.actions.shoot.RandomizedShootAction;
import uni.gaben.iscat.universe.entity.brain.actions.shoot.ShootAction;
import uni.gaben.iscat.universe.entity.projectiles.ProjectileType;
import uni.gaben.iscat.universe.entity.projectiles.shooters.*;

/**
 * Controller logico unificato per l'Intelligenza Artificiale delle entità genericamente configurate.
 * Guida le routine decisionali della CPU e i comportamenti cinematici sulla base dei parametri
 * dinamici estratti dal database SQLite tramite {@link GenericEntitySettings}.
 */
public class GenericEntityBrain extends Brain<GenericEntityModel> {

    public GenericEntityBrain(GenericEntityModel entity) {
        super(entity);

        setRotationGoal(RotationGoal.idle());

        GenericEntitySettings settings = entity.getSettings();

        Target neighbour = Target.neighboursCached(entity, settings.detectionRange, true, true, Filter.DEFAULT_FILTER, universe -> {
            return true;
        }, body -> {
            return true;
        } );

        addModifier(SteeringModifier.collisionAvoidance(neighbour, 10, settings.combatRange/4, 2));

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
                setSteeringGoal(SteeringGoal.pursuitWithRange(Target.ofPlayer(), 2.5, settings.combatRange, settings.combatRange));
                setRotationGoal(RotationGoal.target(Target.ofPlayer()));
                addAction(new ShootAction(
                        settings.detectionRange,
                        settings.actionCooldownMS/1000,
                        ProjectileType.ENEMY_BULLET,
                        new SingleShotPatternShooter(),
                        Target.ofPlayer(),
                        true
                ));
                break;

            case "iscat_bomber":
                setSteeringGoal(SteeringGoal.pursuitWithRange(Target.ofPlayer(), 5, settings.combatRange, settings.combatRange));
                setRotationGoal(RotationGoal.target(Target.ofPlayer()));
                addAction(new ShootAction(
                        settings.detectionRange,
                        settings.actionCooldownMS/1000,
                        ProjectileType.STUN_BULLET,
                        new RepeaterPatternShooter(12,settings.actionCooldownMS/10000, new SingleShotPatternShooter()),
                        Target.ofPlayer(),
                        true
                ));
                break;

            case "iscat_core":
                setRotationGoal(RotationGoal.intervalSpin(8, settings.actionCooldownMS/100, settings.maxAngularVelocity ));
                addAction( "shoot" ,new ShootAction(
                        settings.detectionRange,
                        settings.actionCooldownMS/1000,
                        ProjectileType.ENEMY_BULLET,
                        new MultiDirectionPatternShooter(4, 0, new ParallelLinePatternShooter(3, 32)),
                        Target.ofPlayer(),
                        false
                ));
                entity.lifeProperty().addListener((_, _, newValue) -> {
                    AbstractShootAction s = (AbstractShootAction) getAction("shoot");
                    s.setCooldown((settings.actionCooldownMS/1000) * (newValue.doubleValue()/entity.getMaxLife()));
                });
                break;

            case "iscat_mother":
                setSteeringGoal(SteeringGoal.pursuitWithRange(Target.ofPlayer(), 3, settings.combatRange, settings.combatRange));
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
                setSteeringGoal(SteeringGoal.pursuitWithRange(Target.ofPlayer(), 2, settings.combatRange, settings.combatRange));
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
                setRotationGoal(RotationGoal.continuesSpin(.1));
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
                setSteeringGoal(SteeringGoal.pursuit(Target.ofPlayer(), 1));
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
                setSteeringGoal(SteeringGoal.evadeWithRange(Target.ofPlayer(), 3, settings.combatRange));
                addAction(new HealAction(settings.actionCooldownMS, settings.combatRange/2, settings.initLife/200));
                break;

            default:
                break;
        }
    }
}