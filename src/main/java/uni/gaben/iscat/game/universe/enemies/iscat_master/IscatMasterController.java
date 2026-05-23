package uni.gaben.iscat.game.universe.enemies.iscat_master;

import uni.gaben.iscat.game.lib.implementations.AiBehaviours;
import uni.gaben.iscat.game.lib.implementations.behaviors.SeparationBehavior;
import uni.gaben.iscat.game.lib.implementations.behaviors.WanderBehavior;
import uni.gaben.iscat.game.lib.implementations.behaviors.ChaseBehavior;
import uni.gaben.iscat.game.lib.implementations.behaviors.ShooterBehaviour;
import uni.gaben.iscat.game.lib.utils.UU;
import uni.gaben.iscat.game.universe.UniverseSpawnable;
import uni.gaben.iscat.game.universe.attacks.BurstArcAttack;
import uni.gaben.iscat.game.universe.attacks.RadialNovaAttack;
import uni.gaben.iscat.game.universe.attacks.SingleBurstAttack;
import uni.gaben.iscat.game.universe.attacks.SummonAttack;
import uni.gaben.iscat.game.universe.projectiles.ProjectileType;
import uni.gaben.iscat.game.universe.UniverseModel;

public class IscatMasterController extends AiBehaviours<IscatMasterModel> {

    public IscatMasterController(IscatMasterModel iscat) {
        super(iscat);


        // Evita assembramenti (parallelo, sempre attivo)
        this.addBehavior(new SeparationBehavior(UU.pxToM(32.0), IscatMasterSettings.FORCE * 0.8));

        // Wander
        this.addBehavior(new WanderBehavior(
                IscatMasterSettings.FORCE,
                IscatMasterSettings.ROTATION_SPEED
        ));

        // Chase
        this.addBehavior(new ChaseBehavior(
                IscatMasterSettings.FORCE,
                IscatMasterSettings.MAX_VELOCITY,
                IscatMasterSettings.DETECTION_RANGE,
                50.0,
                IscatMasterSettings.ROTATION_SPEED
        ));

        // Attacchi
        this.addBehavior(new ShooterBehaviour<IscatMasterModel>(
                80.0,
                IscatMasterSettings.COMBAT_RANGE,
                IscatMasterSettings.PREFERRED_RANGE,
                IscatMasterSettings.FORCE,
                IscatMasterSettings.ROTATION_SPEED,
                IscatMasterSettings.FIRE_COOLDOWN_S,
                true,
                ProjectileType.ENEMY_BULLET,
                new SingleBurstAttack<>(5, 0.05),
                new RadialNovaAttack<>(20),
                new BurstArcAttack<>(10, 0.05, 15),
                new SummonAttack<>(5, UniverseSpawnable.EATER, 80.0)
        ));
    }

    /**
     * Blocca l'AI finché l'animazione di entrata non è terminata.
     */
    @Override
    public void aiUpdate(UniverseModel universeModel, double dt) {
        if (aiEntity.getAnimationState() == IscatMasterModel.AnimationState.DEATH) return;
        super.aiUpdate(universeModel, dt);
    }
}