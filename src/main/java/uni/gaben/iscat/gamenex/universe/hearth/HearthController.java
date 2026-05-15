package uni.gaben.iscat.gamenex.universe.hearth;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.gamenex.lib.implementations.AiBehaviours;
import uni.gaben.iscat.gamenex.universe.UniverseModel;
import uni.gaben.iscat.gamenex.universe.UniverseSettings;
import uni.gaben.iscat.gamenex.universe.player.PlayerModel;

public class HearthController extends AiBehaviours<HearthModel> {

    private final HearthModel hearth;

    public HearthController(HearthModel hearth) {
        super(hearth);
        this.hearth = hearth;
    }

    @Override
    public void aiUpdate(UniverseModel universeModel, double dt) {
        if (hearth == null) return;

        PlayerModel player = universeModel.getPlayer();
        if (player == null) return;

        Vector2 hPos = hearth.getTransform().getTranslation();
        Vector2 pPos = player.getTransform().getTranslation();

        // Calcoliamo vettore direzione e distanza
        Vector2 diff = pPos.copy().subtract(hPos);
        double dist = diff.getMagnitude();

        // 1. COLLISIONE (Appena è abbastanza vicino, cura e sparisce)
        double collisionThreshold = (HearthSettings.RAGGIO_COLLISIONE_PX / UniverseSettings.SCALE) + 0.2;
        if (dist < collisionThreshold) {
            applyHeal(player, universeModel);
            return;
        }

        // 2. INSEGUIMENTO Se il player è nel raggio, hearth va verso di lui
        if (dist < HearthSettings.RANGE_ATTIVAZIONE) {
            // Calcoliamo la velocità verso il player
            Vector2 velocitaDesiderata = diff.getNormalized().multiply(HearthSettings.VELOCITA_INSEGUIMENTO);

            // Applichiamo direttamente la velocità per una risposta immediata
            hearth.setLinearVelocity(velocitaDesiderata);

        } else {
            // Se il player è lontano, rallenta fino a fermarsi (inerzia)
            hearth.getLinearVelocity().multiply(0.9);
        }
    }

    private void applyHeal(PlayerModel player, UniverseModel universe) {
        // Cura il giocatore
        player.deltaToLife(HearthSettings.HP_BOOST);
        hearth.setLife(0);
    }
}