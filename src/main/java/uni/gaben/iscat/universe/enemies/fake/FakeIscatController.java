package uni.gaben.iscat.universe.enemies.fake;

import uni.gaben.iscat.universe.lib.implementations.AiBehaviours;
import uni.gaben.iscat.universe.lib.implementations.behaviors.attack.*;
import uni.gaben.iscat.universe.lib.implementations.behaviors.movement.*;
import uni.gaben.iscat.universe.lib.implementations.behaviors.passive.*;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.lib.implementations.attacks.MultiDirectionAttack;
import uni.gaben.iscat.universe.lib.implementations.attacks.RepeaterAttack;
import uni.gaben.iscat.universe.lib.implementations.attacks.SingleShotAttack;
import uni.gaben.iscat.universe.lib.implementations.attacks.SpreadAttack;
import uni.gaben.iscat.universe.projectiles.ProjectileType;
import uni.gaben.iscat.universe.UU;

import static uni.gaben.iscat.universe.enemies.fake.FakeIscatSettings.FAKEISCAT;

public class FakeIscatController extends AiBehaviours<FakeIscatModel> {

    private CheckLineOfSight checkLineOfSight;
    private ShooterBehaviour shooterBehaviour;
    private SeekLineOfSightBehavior seekLineOfSight;

    public FakeIscatController(FakeIscatModel iscat) {
        super(iscat, FAKEISCAT.force, FAKEISCAT.maxVelocity, FAKEISCAT.rotationSpeed);

        // Evita assembramenti (Passive track)
        this.addPassive(new SeparationBehavior(UU.pxToM(64.0), FAKEISCAT.force * 0.8));

        // Wander nativo (Movement track)
        this.addMovement(new WanderBehavior(
                FAKEISCAT.maxVelocity,
                50.0,
                FAKEISCAT.detectionRange,
                FAKEISCAT.combatRange
        ));

        // Chase nativo (Movement track)
        this.addMovement(new ChaseBehavior(
                FAKEISCAT.maxVelocity,
                FAKEISCAT.detectionRange,
                50.0
        ));

        this.shooterBehaviour = new ShooterBehaviour(
                80.0,
                FAKEISCAT.combatRange,
                FAKEISCAT.fireCooldownS,
                ProjectileType.ENEMY_BULLET,
                new RepeaterAttack(5, new SingleShotAttack()),
                new RepeaterAttack(2, new SpreadAttack(3, 30.0)),
                new MultiDirectionAttack(4, 0, new SingleShotAttack())
        );
        this.addAttack(shooterBehaviour);

        // Dodge behavior (Movement track)
        this.addMovement(new DodgeProjectileBehavior(FAKEISCAT.force * 1.5, FAKEISCAT.combatRange,2.0));
    }

    @Override
    public void aiUpdate(UniverseModel universeModel, double dt) {
        super.aiUpdate(universeModel, dt);
        if (checkLineOfSight == null) {
            checkLineOfSight = new CheckLineOfSight(universeModel.getPlayer());
            seekLineOfSight = new SeekLineOfSightBehavior(FAKEISCAT.force, FAKEISCAT.maxVelocity);
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