package uni.gaben.iscat.universe.entity;

import uni.gaben.iscat.universe.entity.brain.*;
import uni.gaben.iscat.universe.entity.brain.abilities.Ability;
import uni.gaben.iscat.universe.entity.brain.abilities.HealAbility;
import uni.gaben.iscat.universe.entity.brain.abilities.shoot.*;
import uni.gaben.iscat.universe.entity.hardcoded.projectiles.ProjectileType;
import uni.gaben.iscat.universe.entity.shooters.*;


public class EntityBrain extends Brain<EntityModel> {

    public EntityBrain(EntityModel entity) {
        super(entity);

        /*
        setRotationGoal(RotationGoal.idle());

        EntityRecord settings = entity.getSettings();

        Target neighbour = Target.neighboursCached(entity, settings.detectionRange/2, body -> !(body instanceof PlayerModel || (body instanceof ProjectileModel p && p.getType() == ProjectileType.ENEMY_BULLET)));

        addModifier(SteeringModifier.collisionAvoidance(neighbour, 10, settings.detectionRange/5, new SimpleDoubleProperty(10)));

        addModifier(SteeringModifier.alignment(neighbour.filtered(entityModel -> !(entityModel instanceof ProjectileModel)), new SimpleDoubleProperty(1)));
        addModifier(SteeringModifier.cohesion(neighbour.filtered(entityModel -> !(entityModel instanceof ProjectileModel)), new SimpleDoubleProperty(1)));
        addModifier(SteeringModifier.separation(neighbour.filtered(entityModel -> !(entityModel instanceof ProjectileModel)), settings.detectionRange/4, new SimpleDoubleProperty(3)));

        loadBehaviorsFromSettings(settings);
         */
    }


    private void loadBehaviorsFromRecord(EntityRecord record) {
        switch (record.entityKey()) {
            case "iscat_mob":
                setSteeringGoal(SteeringGoal.pursuitWithRange(Target.ofPlayer(), 2.5, record.combatRange(), record.combatRange()));
                setRotationGoal(RotationGoal.target(Target.ofPlayer()));
                addAction(new ShootAbility(
                        record.detectionRange(),
                        record.actionCooldownSec(),
                        ProjectileType.ENEMY_BULLET,
                        new SingleShotPatternShooter(),
                        Target.ofPlayer(),
                        true,
                        2.6
                ));
                break;

            case "iscat_bomber":
                setSteeringGoal(SteeringGoal.pursuitWithRange(Target.ofPlayer(), 5, record.combatRange(), record.combatRange()));
                setRotationGoal(RotationGoal.target(Target.ofPlayer()));
                addAction(new ShootAbility(
                        record.detectionRange(),
                        record.actionCooldownSec(),
                        ProjectileType.STUN_BULLET,
                        new RepeaterPatternShooter(12,record.actionCooldownSec(), new SingleShotPatternShooter()),
                        Target.ofPlayer(),
                        true,
                        0
                ));
                break;

            case "iscat_core":
                setRotationGoal(RotationGoal.intervalSpin(8, record.actionCooldownSec(), record.maxAngularVelocity()));
                addAction( "shoot" ,new ShootAbility(
                        record.detectionRange(),
                        record.actionCooldownSec(),
                        ProjectileType.ENEMY_BULLET,
                        new MultiDirectionPatternShooter(4, 0, new ParallelLinePatternShooter(3, 32)),
                        Target.ofPlayer(),
                        false,
                        0
                ));
                entity.enduranceProperty().addListener((_, _, newValue) -> {
                    AbstractShootAbility s = (AbstractShootAbility) getAction("shoot");
                    s.setCooldown((record.actionCooldownSec()) * (newValue.doubleValue()/entity.getMaxEndurance()));
                });
                break;

            case "iscat_mother":
                setSteeringGoal(SteeringGoal.pursuitWithRange(Target.ofPlayer(), 3, record.combatRange(), record.combatRange()));
                addAction(RandomizedShootAbility.targetingPlayer(
                        record.detectionRange(),
                        record.actionCooldownSec(),
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
                setSteeringGoal(SteeringGoal.pursuitWithRange(Target.ofPlayer(), 2, record.combatRange(), record.combatRange()));
                addAction(RandomizedShootAbility.targetingPlayer(
                        record.detectionRange(),
                        record.actionCooldownSec(),
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
                        record.detectionRange(),
                        record.actionCooldownSec(),
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
                        record.detectionRange(),
                        record.actionCooldownSec(),
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
                        record.detectionRange(),
                        record.actionCooldownSec(),
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
                setSteeringGoal(SteeringGoal.evadeWithRange(Target.ofPlayer(), 3, record.combatRange()));
                addAction(new HealAbility(record.actionCooldownSec(), record.combatRange() /2, record.initLife() /200));
                break;

            default:
                break;
        }
    }


    public static EntityBrain fromRecord(EntityModel entity) {
        EntityBrain brain = new EntityBrain(entity);
        EntityRecord s = entity.getEntity();
        if (s.brain() == null) return brain;

        // Steering
        brain.setSteeringGoal(SteeringGoal.createSteeringGoal(s.brain().steering()));

        // Rotation
        brain.setRotationGoal(RotationGoal.createRotationGoal(s.brain().rotation()));

        // Abilities
        for (EntityRecord.AbilityRecord ac : s.brain().abilities()) {
            Ability ability = Ability.createAbility(ac, entity);
            if (ability != null) brain.addAction(ability);
        }

        // Modifiers
        for (EntityRecord.ModifierRecord mc : s.brain().modifiers()) {
            SteeringModifier mod = SteeringModifier.createModifier(mc, entity);
            if (mod != null) brain.addModifier(mod);
        }
        return brain;
    }


}