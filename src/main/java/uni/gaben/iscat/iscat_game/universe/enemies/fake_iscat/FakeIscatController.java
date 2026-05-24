package uni.gaben.iscat.iscat_game.universe.enemies.fake_iscat;

import uni.gaben.iscat.iscat_game.lib.implementations.AiBehaviours;
import uni.gaben.iscat.iscat_game.lib.implementations.behaviors.SeparationBehavior;
import uni.gaben.iscat.iscat_game.lib.implementations.behaviors.WanderBehavior;
import uni.gaben.iscat.iscat_game.lib.implementations.behaviors.ChaseBehavior;
import uni.gaben.iscat.iscat_game.lib.implementations.behaviors.ShooterBehaviour;
import uni.gaben.iscat.iscat_game.utils.UU;
import uni.gaben.iscat.iscat_game.universe.attacks.BurstArcAttack;
import uni.gaben.iscat.iscat_game.universe.attacks.RadialNovaAttack;
import uni.gaben.iscat.iscat_game.universe.attacks.SingleBurstAttack;
import uni.gaben.iscat.iscat_game.universe.projectiles.ProjectileType;

public class FakeIscatController extends AiBehaviours<FakeIscatModel> {

    public FakeIscatController(FakeIscatModel iscat) {
        super(iscat);

        // Evita assembramenti
        this.addBehavior(new SeparationBehavior(UU.pxToM(32.0), FakeIscatSettings.FORCE * 0.8));

        // Wander nativo
        this.addBehavior(new WanderBehavior(
                FakeIscatSettings.FORCE,
                FakeIscatSettings.ROTATION_SPEED
        ));

        // Chase nativo
        this.addBehavior(new ChaseBehavior(
                FakeIscatSettings.FORCE,
                FakeIscatSettings.MAX_VELOCITY,
                FakeIscatSettings.DETECTION_RANGE,
                50.0,
                FakeIscatSettings.ROTATION_SPEED
        ));

        // ShooterBehaviour configurato con gli attacchi specifici di FakeIscat!
        this.addBehavior(new ShooterBehaviour<FakeIscatModel>(
                80.0,
                FakeIscatSettings.COMBAT_RANGE,
                FakeIscatSettings.PREFERRED_RANGE,
                FakeIscatSettings.FORCE,
                FakeIscatSettings.ROTATION_SPEED,
                FakeIscatSettings.FIRE_COOLDOWN_S,
                false,
                ProjectileType.ENEMY_BULLET,
                new SingleBurstAttack<>(3, 0.12),
                new RadialNovaAttack<>(15),
                new BurstArcAttack<>(3, 0.18, 15)
        ));
    }
}