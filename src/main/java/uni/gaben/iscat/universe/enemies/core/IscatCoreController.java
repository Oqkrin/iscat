package uni.gaben.iscat.universe.enemies.core;

import uni.gaben.iscat.universe.lib.implementations.AiBehaviours;
import uni.gaben.iscat.universe.lib.implementations.behaviors.attack.*;
import uni.gaben.iscat.universe.lib.implementations.behaviors.movement.*;
import uni.gaben.iscat.universe.lib.implementations.behaviors.passive.*;
import uni.gaben.iscat.universe.lib.implementations.attacks.ParallelLineAttack;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.projectiles.ProjectileType;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.lib.implementations.attacks.MultiDirectionAttack;

import static uni.gaben.iscat.universe.enemies.core.IscatCoreSettings.ISCATCORE;

public class IscatCoreController extends AiBehaviours<IscatCoreModel> {

    private CheckLineOfSight checkLineOfSight;
    private ShooterBehaviour shooterBehaviour;
    private SeekLineOfSightBehavior seekLineOfSight;

    public IscatCoreController(IscatCoreModel iscat) {
        super(iscat, ISCATCORE.force, ISCATCORE.maxVelocity, ISCATCORE.rotationSpeed);

        // Evita assembramenti (Passive track)
        this.addPassive(new SeparationBehavior(UU.pxToM(32.0), ISCATCORE.force * 0.8));

        // Rotazione a 45° (Passive track)
        this.addPassive(new RotationBehavior(
                IscatCoreSettings.ROTATION_INTERVAL,
                45.0,
                ISCATCORE.rotationSpeed,
                10
        ));

        // Wander (Movement track)
        this.addMovement(new WanderBehavior(
                ISCATCORE.maxVelocity,
                50.0,
                ISCATCORE.detectionRange,
                ISCATCORE.combatRange
        ));

        // Chase (Movement track)
        this.addMovement(new ChaseBehavior(
                ISCATCORE.maxVelocity,
                ISCATCORE.detectionRange,
                50.0
        ));

        shooterBehaviour = new ShooterBehaviour(
                80.0,
                ISCATCORE.combatRange,
                () -> {
                    if (iscat.shouldRemove() || iscat.getLife() <= 0) return Double.MAX_VALUE;
                    double healthPercent = iscat.getLife() / iscat.getMaxLife();
                    return ISCATCORE.fireCooldownS * Math.max(0.1, healthPercent);
                },
                ProjectileType.ENEMY_BULLET,
                new MultiDirectionAttack(4, 0.0, new ParallelLineAttack(3,30))
        );
        this.addAttack(shooterBehaviour);

        // Dodge e Slam
        this.addMovement(new DodgeProjectileBehavior(ISCATCORE.force * 1.5, ISCATCORE.combatRange, 2.0));
        this.addAttack(new DirectionalSlamBehavior(5.0, ISCATCORE.force * 4.0, 3.0, 1.2));
    }

    @Override
    public void aiUpdate(UniverseModel universeModel, double dt) {
        if (!aiEntity.isAlive()) return;
        super.aiUpdate(universeModel, dt);

        if (checkLineOfSight == null) {
            checkLineOfSight = new CheckLineOfSight(universeModel.getPlayer());
            seekLineOfSight = new SeekLineOfSightBehavior(ISCATCORE.force, ISCATCORE.maxVelocity);
            addPassive(checkLineOfSight);
        } else {
            if (checkLineOfSight.hasLineOfSightWithTarget()) {
                addAttack(shooterBehaviour);
                removeMovement(seekLineOfSight);
            } else {
                removeAttack(shooterBehaviour);
                addMovement(seekLineOfSight);
            }
        }
    }
}