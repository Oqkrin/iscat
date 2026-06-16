package uni.gaben.iscat.universe.entity;

import uni.gaben.iscat.universe.entity.brain.*;
import uni.gaben.iscat.universe.entity.brain.abilities.Ability;
import uni.gaben.iscat.universe.entity.brain.abilities.HealAbility;
import uni.gaben.iscat.universe.entity.brain.abilities.shoot.*;
import uni.gaben.iscat.universe.entity.brain.rotation.RotationGoal;
import uni.gaben.iscat.universe.entity.brain.steering.SteeringGoal;
import uni.gaben.iscat.universe.entity.brain.steering.SteeringModifier;
import uni.gaben.iscat.universe.entity.brain.target.Target;
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
                        new SingleShotPattern(),
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
                        new RepeaterPattern(12,record.actionCooldownSec(), new SingleShotPattern()),
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
                        new MultiDirectionPattern(4, 0, new ParallelLinePattern(3, 32)),
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
                        new SummonPattern(3,"iscat_mob",100),
                        new SummonPattern(1,"fake_iscat",100),
                        new SummonPattern(2,"iscat_healer",100),
                        new RepeaterPattern(4,1, new SpreadPattern(5, 30))
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
                        new SpreadPattern(3, 30),
                        new MultiDirectionPattern(8, 0, new SingleShotPattern()),
                        new RepeaterPattern(3,0.25, new SingleShotPattern())
                ));
                break;

            case "fallen_star_golem":
                setRotationGoal(RotationGoal.continuesSpin(.1));
                addAction(new ShootAbility(
                        record.detectionRange(),
                        record.actionCooldownSec(),
                        ProjectileType.ENEMY_BULLET,
                        new RepeaterPattern(2, 0.5, new RingPattern(16)),
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
                        new RingPattern(16),
                        new RingPattern(8),
                        new RingPattern(24)
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
                        new RingPattern(32),
                        new SummonPattern(2,"eater",100),
                        new SummonPattern(1,"iscat_dasher",100),
                        new RepeaterPattern(3,0.3, new RingPattern(16)),
                        new RepeaterPattern(4,0.3, new SpreadPattern(8, 45))
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