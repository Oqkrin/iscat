package uni.gaben.iscat.universe.enemies.healer;

import uni.gaben.iscat.universe.lib.implementations.AiBehaviours;
import uni.gaben.iscat.universe.lib.implementations.behaviors.movement.*;
import uni.gaben.iscat.universe.lib.implementations.behaviors.passive.*;
import uni.gaben.iscat.universe.UU;

import static uni.gaben.iscat.universe.enemies.healer.IscatHealerSettings.ISCATHEALER;

/**
 * BUG 7 FIXED: {@code HideBehindEntitiesBehavior} was constructed as:
 *   {@code new HideBehindEntitiesBehavior(ISCATHEALER.force, ISCATHEALER.maxVelocity, 3.0)}
 * But the new constructor signature is {@code (maxVelocity, hideDistance, priority)}.
 * This meant:
 *   • maxVelocity  = ISCATHEALER.force   (wrong speed)
 *   • hideDistance = ISCATHEALER.maxVelocity (wrong distance)
 *   • priority     = 3.0 (way too low — Healer would never hide)
 * FIX: pass (maxVelocity, 3.0 meters hideDistance, 40.0 priority).
 */
public class IscatHealerController extends AiBehaviours<IscatHealerModel> {

    public IscatHealerController(IscatHealerModel iscat) {
        super(iscat, ISCATHEALER.force, ISCATHEALER.maxVelocity, ISCATHEALER.rotationSpeed);

        // Passive
        addPassive(new SeparationBehavior(UU.pxToM(32.0), ISCATHEALER.force * 0.8));
        addPassive(new HealingAreaBehavior(
                IscatHealerSettings.HEAL_RADIUS_M,
                IscatHealerSettings.HEAL_AMOUNT,
                IscatHealerSettings.HEAL_COOLDOWN_S));

        // Movement
        // FIX BUG 7: correct arg order (maxVelocity, hideDistance, priority)
        addMovement(new HideBehindEntitiesBehavior(ISCATHEALER.maxVelocity, 3.0, 40.0));
        addMovement(new DodgeProjectileBehavior(ISCATHEALER.force * 1.5, ISCATHEALER.detectionRange, 2.0));
    }
}
