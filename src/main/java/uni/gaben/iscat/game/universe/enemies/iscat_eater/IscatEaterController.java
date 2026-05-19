package uni.gaben.iscat.game.universe.enemies.iscat_eater;

import uni.gaben.iscat.game.lib.implementations.AiBehaviours;
import uni.gaben.iscat.game.lib.implementations.behaviors.ChaseBehavior;
import uni.gaben.iscat.game.lib.implementations.behaviors.SeparationBehavior;
import uni.gaben.iscat.game.lib.utils.UU;
import uni.gaben.iscat.game.universe.UniverseModel;
import uni.gaben.iscat.game.universe.player.PlayerModel;

public class IscatEaterController extends AiBehaviours<IscatEaterModel> {

    public IscatEaterController(IscatEaterModel eater) {
        super(eater);

        // Add modular behaviors
        this.addBehavior(new ChaseBehavior(IscatEaterSettings.FORCE, IscatEaterSettings.MAX_VELOCITY_MS));
        this.addBehavior(new SeparationBehavior(
                UU.pxToM(24.0), IscatEaterSettings.FORCE * 0.8));

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

    @Override
    public void aiUpdate(UniverseModel universeModel, double dt) {
        // Execute modular behaviors added in constructor
        super.aiUpdate(universeModel, dt);
    }
}