package uni.gaben.iscat.universe.enemies.eater;

import uni.gaben.iscat.universe.lib.implementations.AiBehaviours;
import uni.gaben.iscat.universe.lib.implementations.behaviors.movement.ChaseBehavior;
import uni.gaben.iscat.universe.lib.implementations.behaviors.movement.DodgeProjectileBehavior;
import uni.gaben.iscat.universe.lib.implementations.behaviors.passive.SeparationBehavior;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.player.PlayerModel;

import static uni.gaben.iscat.universe.enemies.eater.IscatEaterSettings.ISCATEATER;

public class IscatEaterController extends AiBehaviours<IscatEaterModel> {

    public IscatEaterController(IscatEaterModel eater) {
        super(eater, ISCATEATER.force, ISCATEATER.maxVelocity, ISCATEATER.rotationSpeed);

        // Add modular behaviors via respective tracks
        this.addMovement(new ChaseBehavior(ISCATEATER.maxVelocity, ISCATEATER.detectionRange, 50.0));
        this.addPassive(new SeparationBehavior(UU.pxToM(24.0), ISCATEATER.force * 0.8));
        this.addMovement(new DodgeProjectileBehavior(ISCATEATER.force * 1.5, ISCATEATER.combatRange,2.0));

        // COSA SUCCEDE SE COLLIDO? Definito in modo indipendente.
        eater.setOnCollision(otherEntity -> {
            if (otherEntity instanceof PlayerModel player && !eater.shouldRemove()) {
                player.deltaToLife(-IscatEaterSettings.ATTACK_POWER);

                eater.setLife(0);
                eater.kill();
                eater.setShouldRemove(true);
            }
        });
    }
}