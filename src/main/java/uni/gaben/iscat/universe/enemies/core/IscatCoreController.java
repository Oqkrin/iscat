package uni.gaben.iscat.universe.enemies.core;

import uni.gaben.iscat.universe.lib.implementations.AiBehaviours;
import uni.gaben.iscat.universe.lib.implementations.behaviors.attack.*;
import uni.gaben.iscat.universe.lib.implementations.behaviors.movement.*;
import uni.gaben.iscat.universe.lib.implementations.behaviors.passive.*;
import uni.gaben.iscat.universe.lib.implementations.behaviors.passive.CheckLineOfSight;
import uni.gaben.iscat.universe.lib.implementations.attacks.ParallelLineAttack;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.projectiles.ProjectileType;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.lib.implementations.attacks.MultiDirectionAttack;

import static uni.gaben.iscat.universe.enemies.core.IscatCoreSettings.ISCATCORE;

/**
 * BUG 1 (inherited): CheckLineOfSight now implements PassiveBehavior —
 *   addPassive(checkLineOfSight) correctly registers it.
 *
 * BUG 8 FIXED: SeekLineOfSightBehavior constructed as (force, maxVelocity).
 *   New constructor: (maxVelocity, priority).
 *   Was treating maxVelocity as the priority value (e.g. priority = 5.0 or similar tiny number).
 *   FIX: pass (maxVelocity, 45.0).
 *
 * BUG 9 FIXED: WanderBehavior constructed with (maxVelocity, 50.0, detectionRange, combatRange).
 *   New constructor: (maxVelocity, priority, minRadius, maxRadius).
 *   detectionRange is typically > combatRange → minRadius > maxRadius → negative random radius.
 *   FIX: use small fixed wander radii (1.0, 3.0 world units).
 */
public class IscatCoreController extends AiBehaviours<IscatCoreModel> {

    private CheckLineOfSight       checkLineOfSight;
    private ShooterBehaviour       shooterBehaviour;
    private SeekLineOfSightBehavior seekLineOfSight;

    public IscatCoreController(IscatCoreModel iscat) {
        super(iscat, ISCATCORE.force, ISCATCORE.maxVelocity, ISCATCORE.rotationSpeed);

        // Passive
        addPassive(new SeparationBehavior(UU.pxToM(32.0), ISCATCORE.force * 0.8));
        addPassive(new RotationBehavior(
                IscatCoreSettings.ROTATION_INTERVAL, 45.0, ISCATCORE.rotationSpeed, 10));

        // Movement
        // FIX BUG 9: use fixed radii instead of detectionRange/combatRange
        addMovement(new WanderBehavior(ISCATCORE.maxVelocity, 10.0, 1.0, 3.0));
        addMovement(new ChaseBehavior(ISCATCORE.maxVelocity, ISCATCORE.detectionRange, 50.0));
        addMovement(new DodgeProjectileBehavior(ISCATCORE.force * 1.5, ISCATCORE.combatRange, 2.0));

        // Attack
        shooterBehaviour = new ShooterBehaviour(
                80.0,
                ISCATCORE.combatRange,
                () -> {
                    if (iscat.shouldRemove() || iscat.getLife() <= 0) return Double.MAX_VALUE;
                    double healthPercent = iscat.getLife() / iscat.getMaxLife();
                    return ISCATCORE.fireCooldownS * Math.max(0.1, healthPercent);
                },
                ProjectileType.ENEMY_BULLET,
                new MultiDirectionAttack(4, 0.0, new ParallelLineAttack(3, 30)));
        addAttack(shooterBehaviour);
        addAttack(new DirectionalSlamBehavior(5.0, ISCATCORE.force * 4.0, 3.0, 1.2));
    }

    @Override
    public void aiUpdate(UniverseModel universeModel, double dt) {
        if (!aiEntity.isAlive()) return;
        super.aiUpdate(universeModel, dt);

        if (checkLineOfSight == null) {
            checkLineOfSight = new CheckLineOfSight(universeModel.getPlayer());
            // FIX BUG 8: correct args (maxVelocity, priority)
            seekLineOfSight  = new SeekLineOfSightBehavior(ISCATCORE.maxVelocity, 45.0);
            // FIX BUG 1: CheckLineOfSight now implements PassiveBehavior — this works
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
