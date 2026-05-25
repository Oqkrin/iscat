package uni.gaben.iscat.iscat_game.universe.enemies.iscat_healer;

import uni.gaben.iscat.iscat_game.lib.implementations.AiBehaviours;
import uni.gaben.iscat.iscat_game.lib.implementations.behaviors.WanderBehavior;
import uni.gaben.iscat.iscat_game.lib.implementations.behaviors.SeparationBehavior;
import uni.gaben.iscat.iscat_game.lib.implementations.behaviors.DodgeProjectileBehavior;
import uni.gaben.iscat.iscat_game.lib.implementations.behaviors.HealingAreaBehavior;
import uni.gaben.iscat.iscat_game.utils.UU;

import static uni.gaben.iscat.iscat_game.universe.enemies.iscat_healer.IscatHealerSettings.ISCATHEALER;

public class IscatHealerController extends AiBehaviours<IscatHealerModel> {

    public IscatHealerController(IscatHealerModel iscat) {
        super(iscat);

        // Separation
        this.addBehavior(new SeparationBehavior(UU.pxToM(32.0), ISCATHEALER.force * 0.8));

        // Hide Behind Allies
        this.addBehavior(new uni.gaben.iscat.iscat_game.lib.implementations.behaviors.HideBehindEntitiesBehavior(ISCATHEALER.force, ISCATHEALER.maxVelocity, 3.0));

        // Dodge
        this.addBehavior(new DodgeProjectileBehavior(ISCATHEALER.force * 1.5, 2.0));

        // Healing Area
        this.addBehavior(new HealingAreaBehavior(
            IscatHealerSettings.HEAL_RADIUS_M, 
            IscatHealerSettings.HEAL_AMOUNT, 
            IscatHealerSettings.HEAL_COOLDOWN_S
        ));
    }
}
