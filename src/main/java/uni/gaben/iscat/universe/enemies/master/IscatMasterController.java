package uni.gaben.iscat.universe.enemies.master;

import uni.gaben.iscat.universe.lib.implementations.AiBehaviours;
import uni.gaben.iscat.universe.lib.implementations.behaviors.attack.*;
import uni.gaben.iscat.universe.lib.implementations.behaviors.movement.*;
import uni.gaben.iscat.universe.lib.implementations.behaviors.passive.*;
import uni.gaben.iscat.universe.lib.implementations.behaviors.CheckLineOfSight;
import uni.gaben.iscat.universe.UniverseSpawnable;
import uni.gaben.iscat.universe.lib.implementations.attacks.*;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.projectiles.ProjectileType;
import uni.gaben.iscat.universe.UniverseModel;

import java.util.Random;

import static uni.gaben.iscat.universe.enemies.master.IscatMasterSettings.ISCATMASTER;

/**
 * BUG 1 (inherited): CheckLineOfSight now implements PassiveBehavior.
 * BUG 8 FIXED: SeekLineOfSightBehavior args corrected to (maxVelocity, 45.0).
 * BUG 9 FIXED: WanderBehavior uses fixed radii (1.0, 3.0).
 */
public class IscatMasterController extends AiBehaviours<IscatMasterModel> {

    private CheckLineOfSight        checkLineOfSight;
    private ShooterBehaviour        shooterBehaviour;
    private SeekLineOfSightBehavior seekLineOfSight;

    private final Random rand = new Random();

    public IscatMasterController(IscatMasterModel iscat) {
        super(iscat, ISCATMASTER.force, ISCATMASTER.maxVelocity, ISCATMASTER.rotationSpeed);

        // Passive
        addPassive(new SeparationBehavior(UU.pxToM(32.0), ISCATMASTER.force * 0.8));

        // Movement
        // FIX BUG 9: fixed radii
        addMovement(new WanderBehavior(ISCATMASTER.maxVelocity, 10.0, 1.0, 3.0));
        addMovement(new OrbitPlayerBehavior(
                ISCATMASTER.maxVelocity,
                (ISCATMASTER.preferredRange + ISCATMASTER.preferredRange) / 2.0,
                45.0, false));
        addMovement(new DodgeProjectileBehavior(ISCATMASTER.force * 1.2, ISCATMASTER.combatRange, 1.5));

        // Attack
        shooterBehaviour = new ShooterBehaviour(
                80.0,
                ISCATMASTER.combatRange,
                ISCATMASTER.fireCooldownS,
                ProjectileType.ENEMY_BULLET,
                new RepeaterAttack(3, new SummonAttack(1, UniverseSpawnable.ISCAT_DASHER, 0)),
                new RepeaterAttack(3, new SummonAttack(1, UniverseSpawnable.ISCAT_HEALER, 0)),
                new RepeaterAttack(3, new SummonAttack(1, UniverseSpawnable.ISCAT_CORE, 0)),
                new RepeaterAttack(5, new MultiDirectionAttack(3, rand.nextInt(90),
                        new SpreadAttack(rand.nextInt((int) ISCATMASTER.combatRange) / 3, rand.nextInt(180)))),
                new RepeaterAttack(3, new FigureAttack(3, FigureAttack.FigureType.STAR)));
        addAttack(shooterBehaviour);
    }

    @Override
    public void aiUpdate(UniverseModel universeModel, double dt) {
        aiEntity.shockwave().update(dt);
        if (aiEntity.getAnimationState() == IscatMasterModel.AnimationState.DEATH) return;
        super.aiUpdate(universeModel, dt);

        if (checkLineOfSight == null) {
            checkLineOfSight = new CheckLineOfSight(universeModel.getPlayer());
            // FIX BUG 8
            seekLineOfSight  = new SeekLineOfSightBehavior(ISCATMASTER.maxVelocity, 45.0);
            // FIX BUG 1: now works
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
