package uni.gaben.iscat.gamenex.universe.iscat_eater;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.gamenex.lib.implementations.AiBehaviours;
import uni.gaben.iscat.gamenex.universe.UniverseModel;
import uni.gaben.iscat.gamenex.universe.player.PlayerModel;

public class IscatEaterController extends AiBehaviours<IscatEaterModel> {

    private final IscatEaterModel eater;
    private Vector2 target = null;

    public IscatEaterController(IscatEaterModel eater) {
        super(eater);
        this.eater = eater;

        // COSA SUCCEDE SE COLLIDO? Definito in modo indipendente.
        this.eater.setOnCollision(otherEntity -> {
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
        if (eater == null || eater.shouldRemove()) return;

        PlayerModel player = universeModel.getPlayer();
        if (player == null) return;

        Vector2 eaterPos = eater.getTransform().getTranslation();
        Vector2 playerPos = player.getTransform().getTranslation();

        if (target == null || eaterPos.distanceSquared(target) < 10) {
            target = playerPos.copy();
        }

        Vector2 direction = target.copy().subtract(eaterPos);
        double distance = direction.getMagnitude();

        if (distance > 0.5) {
            if (eater.getLinearVelocity().getMagnitude() <= IscatEaterSettings.MAX_VELOCITY_MS) {
                Vector2 force = direction.getNormalized().multiply(IscatEaterSettings.FORCE);
                eater.applyForce(force);
            } else {
                Vector2 vel = eater.getLinearVelocity();
                eater.setLinearVelocity(vel.getNormalized().multiply(IscatEaterSettings.MAX_VELOCITY_MS));
            }
        }
    }
}