package uni.gaben.iscat.universe.enemies.healer;

import uni.gaben.iscat.universe.lib.implementations.AiBehaviours;
import uni.gaben.iscat.universe.lib.implementations.behaviors.*;
import uni.gaben.iscat.universe.UU;

import static uni.gaben.iscat.universe.enemies.healer.IscatHealerSettings.ISCATHEALER;

public class IscatHealerController extends AiBehaviours<IscatHealerModel> {

    public IscatHealerController(IscatHealerModel iscat) {
        super(iscat);

        // Separation
        this.addBehavior(new SeparationBehavior(UU.pxToM(32.0), ISCATHEALER.force * 0.8));

        // Hide Behind Allies
        this.addBehavior(new HideBehindEntitiesBehavior(ISCATHEALER.force, ISCATHEALER.maxVelocity, 3.0));

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
