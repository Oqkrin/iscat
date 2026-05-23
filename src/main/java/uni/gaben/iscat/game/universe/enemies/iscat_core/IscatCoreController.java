package uni.gaben.iscat.game.universe.enemies.iscat_core;

import uni.gaben.iscat.game.lib.implementations.AiBehaviours;
import uni.gaben.iscat.game.lib.implementations.behaviors.*;
import uni.gaben.iscat.game.lib.utils.UU;
import uni.gaben.iscat.game.universe.attacks.MultiDirectionAttack;
import uni.gaben.iscat.game.universe.attacks.ParallelLineAttack;
import uni.gaben.iscat.game.universe.projectiles.ProjectileType;
/**
 * Controller di Intelligenza Artificiale per l'IscatCore (Boss Quadrato).
 * Gestisce il movimento e l'attacco rotatorio tramite il sistema a comportamenti compositi (AiBehavior).
 */
public class IscatCoreController extends AiBehaviours<IscatCoreModel> {

    public IscatCoreController(IscatCoreModel iscat) {
        super(iscat);

        // Evita assembramenti
        this.addBehavior(new SeparationBehavior(UU.pxToM(32.0), IscatCoreSettings.FORCE * 0.8));

        // Rotazione a 45°
        this.addBehavior(new RotationBehavior(
                IscatCoreSettings.ROTATION_INTERVAL,
                45.0,
                IscatCoreSettings.ROTATION_SPEED,
                10
        ));

        // Wander
        this.addBehavior(new WanderBehavior(
                IscatCoreSettings.FORCE,
                IscatCoreSettings.ROTATION_SPEED
        ));

        // Chase
        this.addBehavior(new ChaseBehavior(
                IscatCoreSettings.FORCE,
                IscatCoreSettings.MAX_VELOCITY,
                IscatCoreSettings.DETECTION_RANGE,
                50.0,
                IscatCoreSettings.ROTATION_SPEED
        ));

        // Attacco
        this.addBehavior(new ShooterBehaviour<IscatCoreModel>(
                80.0,
                IscatCoreSettings.COMBAT_RANGE,
                IscatCoreSettings.PREFERRED_RANGE,
                IscatCoreSettings.FORCE,
                IscatCoreSettings.ROTATION_SPEED,
                () -> {
                    if (iscat.shouldRemove() || iscat.getLife() <= 0) return Double.MAX_VALUE;
                    double healthPercent = iscat.getLife() / iscat.getMaxLife();
                    return IscatCoreSettings.FIRE_COOLDOWN_S * Math.max(0.1, healthPercent);
                },
                false,
                ProjectileType.ENEMY_BULLET,
                new MultiDirectionAttack<>(4, 0.0,
                        new ParallelLineAttack<>(3, IscatCoreSettings.BULLET_SPACING_M)
                )
        ));
    }
}