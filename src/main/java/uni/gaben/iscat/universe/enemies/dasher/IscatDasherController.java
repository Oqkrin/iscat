package uni.gaben.iscat.universe.enemies.dasher;

import uni.gaben.iscat.universe.lib.implementations.AiBehaviours;
import uni.gaben.iscat.universe.lib.implementations.behaviors.attack.*;
import uni.gaben.iscat.universe.lib.implementations.behaviors.movement.*;
import uni.gaben.iscat.universe.lib.implementations.behaviors.passive.*;
import uni.gaben.iscat.universe.UU;

import static uni.gaben.iscat.universe.enemies.dasher.IscatDasherSettings.ISCATDASHER;

public class IscatDasherController extends AiBehaviours<IscatDasherModel> {

    public IscatDasherController(IscatDasherModel iscat) {
        super(iscat, ISCATDASHER.force, ISCATDASHER.maxVelocity, ISCATDASHER.rotationSpeed);

        // Separation (Passive)
        this.addPassive(new SeparationBehavior(UU.pxToM(24.0), ISCATDASHER.force * 0.8));

        // Orbit (Movement track)
        this.addMovement(new OrbitPlayerBehavior(ISCATDASHER.force, ISCATDASHER.maxVelocity, 4.0, true));

        // Fast Dodge (Movement track)
        this.addMovement(new DodgeProjectileBehavior(ISCATDASHER.force * 2.5, 0, 1));

        // Plunge Attack (Both tracks)
        this.add(new PlungeAttackBehavior(5.0, ISCATDASHER.force * 3.0, 1, 1));
    }
}