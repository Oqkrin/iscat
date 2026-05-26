package uni.gaben.iscat.iscat_game.universe.enemies.fake_iscat;

import uni.gaben.iscat.iscat_game.lib.implementations.AiBehaviours;
import uni.gaben.iscat.iscat_game.lib.implementations.behaviors.*;
import uni.gaben.iscat.iscat_game.universe.UniverseModel;
import uni.gaben.iscat.iscat_game.lib.implementations.attacks.MultiDirectionAttack;
import uni.gaben.iscat.iscat_game.lib.implementations.attacks.RepeaterAttack;
import uni.gaben.iscat.iscat_game.lib.implementations.attacks.SingleShotAttack;
import uni.gaben.iscat.iscat_game.lib.implementations.attacks.SpreadAttack;
import uni.gaben.iscat.iscat_game.universe.projectiles.ProjectileType;
import uni.gaben.iscat.iscat_game.utils.UU;

public class FakeIscatController extends AiBehaviours<FakeIscatModel> {

    private CheckLineOfSight checkLineOfSight;
    private ShooterBehaviour shooterBehaviour;
    private SeekLineOfSightBehavior seekLineOfSight;

    public FakeIscatController(FakeIscatModel iscat) {
        super(iscat);

        // Evita assembramenti
        this.addBehavior(new SeparationBehavior(UU.pxToM(64.0), FakeIscatSettings.FAKEISCAT.force * 0.8));

        // Wander nativo
        this.addBehavior(new WanderBehavior(
                FakeIscatSettings.FAKEISCAT.force,
                FakeIscatSettings.FAKEISCAT.rotationSpeed
        ));

        // Chase nativo
        this.addBehavior(new ChaseBehavior(
                FakeIscatSettings.FAKEISCAT.force,
                FakeIscatSettings.FAKEISCAT.maxVelocity,
                FakeIscatSettings.FAKEISCAT.detectionRange,
                50.0,
                FakeIscatSettings.FAKEISCAT.rotationSpeed
        ));

        this.shooterBehaviour = new ShooterBehaviour(
                80.0,
                FakeIscatSettings.FAKEISCAT.combatRange,
                FakeIscatSettings.FAKEISCAT.preferredRange,
                FakeIscatSettings.FAKEISCAT.force,
                FakeIscatSettings.FAKEISCAT.rotationSpeed,
                FakeIscatSettings.FAKEISCAT.fireCooldownS,
                ProjectileType.ENEMY_BULLET,

                new RepeaterAttack(5, new SingleShotAttack()),               // Spara 5 colpi singoli di fila
                new RepeaterAttack(2, new SpreadAttack(3, 30.0)),            // Spara solo 2 sventagliate di fila
                new MultiDirectionAttack(4, 0, new SingleShotAttack())       // Questo NON è dentro un repeater, quindi fa un colpo a croce singolo!
        );

        this.addBehavior(shooterBehaviour);

        // Dodge behavior
        this.addBehavior(new DodgeProjectileBehavior(FakeIscatSettings.FAKEISCAT.force * 1.5, 2.0));
    }

    @Override
    public void aiUpdate(UniverseModel universeModel, double dt) {
        super.aiUpdate(universeModel, dt);
        if (checkLineOfSight == null) {
            checkLineOfSight = new CheckLineOfSight(universeModel.getPlayer());
            seekLineOfSight = new SeekLineOfSightBehavior(FakeIscatSettings.FAKEISCAT.force, FakeIscatSettings.FAKEISCAT.maxVelocity);
            addBehavior(checkLineOfSight);
        } else {
            if (checkLineOfSight.hasLineOfSightWithTarget()) {
                addBehavior(shooterBehaviour);
                removeBehavior(seekLineOfSight);
            } else {
                removeBehavior(shooterBehaviour);
                addBehavior(seekLineOfSight);
            }
        }
    }
}