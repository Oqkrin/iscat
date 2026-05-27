package uni.gaben.iscat.universe.enemies.fake;

import uni.gaben.iscat.universe.lib.implementations.AiBehaviours;
import uni.gaben.iscat.universe.lib.implementations.behaviors.attack.*;
import uni.gaben.iscat.universe.lib.implementations.behaviors.movement.*;
import uni.gaben.iscat.universe.lib.implementations.behaviors.passive.*;
import uni.gaben.iscat.universe.lib.implementations.behaviors.CheckLineOfSight;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.lib.implementations.attacks.MultiDirectionAttack;
import uni.gaben.iscat.universe.lib.implementations.attacks.RepeaterAttack;
import uni.gaben.iscat.universe.lib.implementations.attacks.SingleShotAttack;
import uni.gaben.iscat.universe.lib.implementations.attacks.SpreadAttack;
import uni.gaben.iscat.universe.projectiles.ProjectileType;
import uni.gaben.iscat.universe.UU;

import static uni.gaben.iscat.universe.enemies.fake.FakeIscatSettings.FAKEISCAT;

/**
 * BUG 1 (inherited): CheckLineOfSight now implements PassiveBehavior.
 * BUG 8 FIXED: SeekLineOfSightBehavior args corrected to (maxVelocity, 45.0).
 * BUG 9 FIXED: WanderBehavior uses fixed radii (1.0, 3.0) instead of
 *   detectionRange/combatRange (which produced minRadius > maxRadius and
 *   negative random wander targets).
 */
public class FakeIscatController extends AiBehaviours<FakeIscatModel> {

    private CheckLineOfSight        checkLineOfSight;
    private ShooterBehaviour        shooterBehaviour;
    private SeekLineOfSightBehavior seekLineOfSight;

    public FakeIscatController(FakeIscatModel iscat) {
        super(iscat, FAKEISCAT.force, FAKEISCAT.maxVelocity, FAKEISCAT.rotationSpeed);

        // Passive
        addPassive(new SeparationBehavior(UU.pxToM(64.0), FAKEISCAT.force * 0.8));

        // Movement
        // FIX BUG 9: fixed radii
        addMovement(new WanderBehavior(FAKEISCAT.maxVelocity, 10.0, 1.0, 3.0));
        addMovement(new ChaseBehavior(FAKEISCAT.maxVelocity, FAKEISCAT.detectionRange, 50.0));
        addMovement(new DodgeProjectileBehavior(FAKEISCAT.force * 1.5, FAKEISCAT.combatRange, 2.0));

        // Attack
        shooterBehaviour = new ShooterBehaviour(
                80.0,
                FAKEISCAT.combatRange,
                FAKEISCAT.fireCooldownS,
                ProjectileType.ENEMY_BULLET,
                new RepeaterAttack(5, new SingleShotAttack()),
                new RepeaterAttack(2, new SpreadAttack(3, 30.0)),
                new MultiDirectionAttack(4, 0, new SingleShotAttack()));
        addAttack(shooterBehaviour);
    }

    @Override
    public void aiUpdate(UniverseModel universeModel, double dt) {
        super.aiUpdate(universeModel, dt);

        if (checkLineOfSight == null) {
            checkLineOfSight = new CheckLineOfSight(universeModel.getPlayer());
            // FIX BUG 8: correct args
            seekLineOfSight  = new SeekLineOfSightBehavior(FAKEISCAT.maxVelocity, 45.0);
            // FIX BUG 1: now works because CheckLineOfSight implements PassiveBehavior
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
