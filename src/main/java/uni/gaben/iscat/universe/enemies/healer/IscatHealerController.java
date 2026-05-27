package uni.gaben.iscat.universe.enemies.healer;

import uni.gaben.iscat.universe.lib.implementations.AiBehaviours;
import uni.gaben.iscat.universe.lib.implementations.behaviors.movement.*;
import uni.gaben.iscat.universe.lib.implementations.behaviors.passive.*;
import uni.gaben.iscat.universe.UU;

import static uni.gaben.iscat.universe.enemies.healer.IscatHealerSettings.ISCATHEALER;

public class IscatHealerController extends AiBehaviours<IscatHealerModel> {

    public IscatHealerController(IscatHealerModel iscat) {
        super(iscat, ISCATHEALER.force, ISCATHEALER.maxVelocity, ISCATHEALER.rotationSpeed);

        // Separation (Passive track)
        this.addPassive(new SeparationBehavior(UU.pxToM(32.0), ISCATHEALER.force * 0.8));

        // Hide Behind Allies (Movement track)
        this.addMovement(new HideBehindEntitiesBehavior(ISCATHEALER.force, ISCATHEALER.maxVelocity, 3.0));

        // Dodge (Movement track)
        this.addMovement(new DodgeProjectileBehavior(ISCATHEALER.force * 1.5, ISCATHEALER.detectionRange,2.0));

        // Healing Area (Passive track)
        this.addPassive(new HealingAreaBehavior(
                IscatHealerSettings.HEAL_RADIUS_M,
                IscatHealerSettings.HEAL_AMOUNT,
                IscatHealerSettings.HEAL_COOLDOWN_S
        ));
    }
}