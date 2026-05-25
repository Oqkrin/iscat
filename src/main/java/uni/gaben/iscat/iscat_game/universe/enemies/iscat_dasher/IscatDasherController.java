package uni.gaben.iscat.iscat_game.universe.enemies.iscat_dasher;

import uni.gaben.iscat.iscat_game.lib.implementations.AiBehaviours;
import uni.gaben.iscat.iscat_game.lib.implementations.behaviors.ChaseBehavior;
import uni.gaben.iscat.iscat_game.lib.implementations.behaviors.SeparationBehavior;
import uni.gaben.iscat.iscat_game.lib.implementations.behaviors.DodgeProjectileBehavior;
import uni.gaben.iscat.iscat_game.lib.implementations.behaviors.PlungeAttackBehavior;
import uni.gaben.iscat.iscat_game.utils.UU;

import static uni.gaben.iscat.iscat_game.universe.enemies.iscat_dasher.IscatDasherSettings.ISCATDASHER;

public class IscatDasherController extends AiBehaviours<IscatDasherModel> {

    public IscatDasherController(IscatDasherModel iscat) {
        super(iscat);

        // Separation
        this.addBehavior(new SeparationBehavior(UU.pxToM(24.0), ISCATDASHER.force * 0.8));

        // Orbit (Instead of Chase)
        this.addBehavior(new uni.gaben.iscat.iscat_game.lib.implementations.behaviors.OrbitPlayerBehavior(ISCATDASHER.force, ISCATDASHER.maxVelocity, 4.0));

        // Fast Dodge (cooldown 1.0s, post-dodge cooldown 0.5s)
        this.addBehavior(new DodgeProjectileBehavior(ISCATDASHER.force * 2.5, 1.0, 0.5));

        // Plunge Attack (duration 0.8s)
        this.addBehavior(new PlungeAttackBehavior(5.0, ISCATDASHER.force * 3.0, 2.0, 0.8));
    }
}
