package uni.gaben.iscat.universe.entity;

import javafx.beans.property.SimpleDoubleProperty;
import uni.gaben.iscat.universe.entity.brain.*;
import uni.gaben.iscat.universe.entity.brain.abilities.HealAbility;
import uni.gaben.iscat.universe.entity.brain.abilities.shoot.*;
import uni.gaben.iscat.universe.entity.player.PlayerModel;
import uni.gaben.iscat.universe.entity.projectiles.Projectile;
import uni.gaben.iscat.universe.entity.projectiles.ProjectileType;
import uni.gaben.iscat.universe.entity.projectiles.shooters.*;


public class GenericEntityBrain extends Brain<GenericEntityModel> {

    public GenericEntityBrain(GenericEntityModel entity) {
        super(entity);

        setRotationGoal(RotationGoal.idle());

        GenericEntitySettings settings = entity.getSettings();

        Target neighbour = Target.neighboursCached(entity, settings.detectionRange/2, body -> !(body instanceof PlayerModel || (body instanceof Projectile p && p.getType() == ProjectileType.ENEMY_BULLET)));

        addModifier(SteeringModifier.collisionAvoidance(neighbour, 10, settings.detectionRange/5, new SimpleDoubleProperty(10)));

        addModifier(SteeringModifier.alignment(neighbour.filtered(entityModel -> !(entityModel instanceof Projectile)), new SimpleDoubleProperty(1)));
        addModifier(SteeringModifier.cohesion(neighbour.filtered(entityModel -> !(entityModel instanceof Projectile)), new SimpleDoubleProperty(1)));
        addModifier(SteeringModifier.separation(neighbour.filtered(entityModel -> !(entityModel instanceof Projectile)), settings.detectionRange/4, new SimpleDoubleProperty(3)));

        loadBehaviorsFromSettings(settings);
    }


    private void loadBehaviorsFromSettings(GenericEntitySettings settings) {
        switch (settings.entityKey) {
            case "iscat_mob":
                setSteeringGoal(SteeringGoal.pursuitWithRange(Target.ofPlayer(), 2.5, settings.combatRange, settings.combatRange));
                setRotationGoal(RotationGoal.target(Target.ofPlayer()));
                addAction(new ShootAbility(
                        settings.detectionRange,
                        settings.actionCooldownMS/1000,
                        ProjectileType.ENEMY_BULLET,
                        new SingleShotPatternShooter(),
                        Target.ofPlayer(),
                        true,
                        2.6
                ));
                break;

            case "iscat_bomber":
                setSteeringGoal(SteeringGoal.pursuitWithRange(Target.ofPlayer(), 5, settings.combatRange, settings.combatRange));
                setRotationGoal(RotationGoal.target(Target.ofPlayer()));
                addAction(new ShootAbility(
                        settings.detectionRange,
                        settings.actionCooldownMS/1000,
                        ProjectileType.STUN_BULLET,
                        new RepeaterPatternShooter(12,settings.actionCooldownMS/10000, new SingleShotPatternShooter()),
                        Target.ofPlayer(),
                        true,
                        0
                ));
                break;

            case "iscat_core":
                setRotationGoal(RotationGoal.intervalSpin(8, settings.actionCooldownMS/100, settings.maxAngularVelocity ));
                addAction( "shoot" ,new ShootAbility(
                        settings.detectionRange,
                        settings.actionCooldownMS/1000,
                        ProjectileType.ENEMY_BULLET,
                        new MultiDirectionPatternShooter(4, 0, new ParallelLinePatternShooter(3, 32)),
                        Target.ofPlayer(),
                        false,
                        0
                ));
                entity.lifeProperty().addListener((_, _, newValue) -> {
                    AbstractShootAbility s = (AbstractShootAbility) getAction("shoot");
                    s.setCooldown((settings.actionCooldownMS/1000) * (newValue.doubleValue()/entity.getMaxLife()));
                });
                break;

            case "iscat_mother":
                setSteeringGoal(SteeringGoal.pursuitWithRange(Target.ofPlayer(), 3, settings.combatRange, settings.combatRange));
                addAction(RandomizedShootAbility.targetingPlayer(
                        settings.detectionRange,
                        settings.actionCooldownMS/1000,
                        ProjectileType.ENEMY_BULLET,
                        true,
                        3
                        ,
                        new SummonPatternShooter(3,"iscat_mob",100),
                        new SummonPatternShooter(1,"fake_iscat",100),
                        new SummonPatternShooter(2,"iscat_healer",100),
                        new RepeaterPatternShooter(4,1, new SpreadPatternShooter(5, 30))
                ));
                break;

            case "fake_iscat":
                setSteeringGoal(SteeringGoal.pursuitWithRange(Target.ofPlayer(), 2, settings.combatRange, settings.combatRange));
                addAction(RandomizedShootAbility.targetingPlayer(
                        settings.detectionRange,
                        settings.actionCooldownMS/1000,
                        ProjectileType.ENEMY_BULLET,
                        true,
                        1
                        ,
                        new SpreadPatternShooter(3, 30),
                        new MultiDirectionPatternShooter(8, 0, new SingleShotPatternShooter()),
                        new RepeaterPatternShooter(3,0.25, new SingleShotPatternShooter())
                ));
                break;

            case "fallen_star_golem":
                setRotationGoal(RotationGoal.continuesSpin(.1));
                addAction(new ShootAbility(
                        settings.detectionRange,
                        settings.actionCooldownMS/1000,
                        ProjectileType.ENEMY_BULLET,
                        new RepeaterPatternShooter(2, 0.5, new RingPatternShooter(16)),
                        Target.ofPlayer(),
                        false,
                        0
                ));
                break;

            case "eater":
                setSteeringGoal(SteeringGoal.pursuit(Target.ofPlayer(), 1));
            case "iscat_worm_head":
            case "iscat_worm_body_part":
                break;

            case "iscat_worm_tail":
                addAction(RandomizedShootAbility.targetingPlayer(
                        settings.detectionRange,
                        settings.actionCooldownMS/1000,
                        ProjectileType.ENEMY_BULLET,
                        true,
                        3,
                        new RingPatternShooter(16),
                        new RingPatternShooter(8),
                        new RingPatternShooter(24)
                ));
                break;

            case "iscat_master":
                addAction(RandomizedShootAbility.targetingPlayer(
                        settings.detectionRange,
                        settings.actionCooldownMS/1000,
                        ProjectileType.ENEMY_BULLET,
                        true,
                        0
                        ,
                        new RingPatternShooter(32),
                        new SummonPatternShooter(2,"eater",100),
                        new SummonPatternShooter(1,"iscat_dasher",100),
                        new RepeaterPatternShooter(3,0.3, new RingPatternShooter(16)),
                        new RepeaterPatternShooter(4,0.3, new SpreadPatternShooter(8, 45))
                ));
                break;

            case "iscat_dasher":
                break;

            case "iscat_healer":
                setSteeringGoal(SteeringGoal.evadeWithRange(Target.ofPlayer(), 3, settings.combatRange));
                addAction(new HealAbility(settings.actionCooldownMS, settings.combatRange/2, settings.initLife/200));
                break;

            default:
                break;
        }
    }
}