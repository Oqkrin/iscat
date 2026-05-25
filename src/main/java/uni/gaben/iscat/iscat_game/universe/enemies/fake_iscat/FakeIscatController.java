package uni.gaben.iscat.iscat_game.universe.enemies.fake_iscat;

import uni.gaben.iscat.iscat_game.lib.implementations.AiBehaviours;
import uni.gaben.iscat.iscat_game.lib.implementations.behaviors.*;
import uni.gaben.iscat.iscat_game.universe.UniverseModel;
import uni.gaben.iscat.iscat_game.utils.UU;
import uni.gaben.iscat.iscat_game.universe.attacks.BurstArcAttack;
import uni.gaben.iscat.iscat_game.universe.attacks.RadialNovaAttack;
import uni.gaben.iscat.iscat_game.universe.attacks.SingleBurstAttack;
import uni.gaben.iscat.iscat_game.universe.projectiles.ProjectileType;

public class FakeIscatController extends AiBehaviours<FakeIscatModel> {

    private CheckLineOfSight checkLineOfSight;
    private ShooterBehaviour<FakeIscatModel> shooterBehaviur;
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

        shooterBehaviur = new ShooterBehaviour<FakeIscatModel>(
                80.0,
                FakeIscatSettings.FAKEISCAT.combatRange,
                FakeIscatSettings.FAKEISCAT.preferredRange,
                FakeIscatSettings.FAKEISCAT.force,
                FakeIscatSettings.FAKEISCAT.rotationSpeed,
                FakeIscatSettings.FAKEISCAT.fireCooldownS,
                false,
                ProjectileType.ENEMY_BULLET,
                new SingleBurstAttack<>(3, 0.12),
                new RadialNovaAttack<>(15),
                new BurstArcAttack<>(3, 0.18, 15)
        );

        // ShooterBehaviour configurato con gli attacchi specifici di FakeIscat!
        this.addBehavior(shooterBehaviur);

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
                addBehavior(shooterBehaviur);
                removeBehavior(seekLineOfSight);
            } else {
                removeBehavior(shooterBehaviur);
                addBehavior(seekLineOfSight);
            }
        }
    }
}