package uni.gaben.iscat.iscat_game.universe.iscats.dasher;

import uni.gaben.iscat.iscat_game.lib.implementations.AiBehaviours;
import uni.gaben.iscat.iscat_game.lib.implementations.behaviors.*;
import uni.gaben.iscat.iscat_game.utils.UU;

import static uni.gaben.iscat.iscat_game.universe.iscats.dasher.IscatDasherSettings.ISCATDASHER;

public class IscatDasherController extends AiBehaviours<IscatDasherModel> {

    public IscatDasherController(IscatDasherModel iscat) {
        super(iscat);

        // Separation
        this.addBehavior(new SeparationBehavior(UU.pxToM(24.0), ISCATDASHER.force * 0.8));

        // Orbit (Instead of Chase)
        this.addBehavior(new OrbitPlayerBehavior(ISCATDASHER.force, ISCATDASHER.maxVelocity, 4.0, true));

        // Fast Dodge (cooldown 1.0s, post-dodge cooldown 0.5s)
        this.addBehavior(new DodgeProjectileBehavior(ISCATDASHER.force * 2.5, 0, 1));

        // Plunge Attack (duration 0.8s)
        this.addBehavior(new PlungeAttackBehavior(5.0, ISCATDASHER.force * 3.0, 1, 1));
    }
}
