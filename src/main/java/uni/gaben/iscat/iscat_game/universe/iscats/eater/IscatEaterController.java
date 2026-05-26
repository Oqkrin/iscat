package uni.gaben.iscat.iscat_game.universe.iscats.eater;

import uni.gaben.iscat.iscat_game.lib.implementations.AiBehaviours;
import uni.gaben.iscat.iscat_game.lib.implementations.behaviors.ChaseBehavior;
import uni.gaben.iscat.iscat_game.lib.implementations.behaviors.DodgeProjectileBehavior;
import uni.gaben.iscat.iscat_game.lib.implementations.behaviors.SeparationBehavior;
import uni.gaben.iscat.iscat_game.utils.UU;
import uni.gaben.iscat.iscat_game.universe.player.PlayerModel;

import static uni.gaben.iscat.iscat_game.universe.iscats.eater.IscatEaterSettings.ISCATEATER;

public class IscatEaterController extends AiBehaviours<IscatEaterModel> {

    public IscatEaterController(IscatEaterModel eater) {
        super(eater);

        // Add modular behaviors
        this.addBehavior(new ChaseBehavior(ISCATEATER.force, ISCATEATER.maxVelocity));
        this.addBehavior(new SeparationBehavior(
                UU.pxToM(24.0), ISCATEATER.force * 0.8));
        this.addBehavior(new DodgeProjectileBehavior(ISCATEATER.force * 1.5, 2.0));

        // COSA SUCCEDE SE COLLIDO? Definito in modo indipendente.
        eater.setOnCollision(otherEntity -> {
            if (otherEntity instanceof PlayerModel player && !eater.shouldRemove()) {
                player.deltaToLife(-IscatEaterSettings.ATTACK_POWER); // Danneggia il giocatore

                // Richiesta esplicita soddisfatta: imposta la vita a zero.
                // Il modello attiverà internamente la pipeline kill() di rimozione sicura.
                eater.setLife(0);
                eater.kill();
                eater.setShouldRemove(true);
            }
        });
    }
}