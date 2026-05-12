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
        super.aiUpdate(universeModel, dt);

        if (hearth == null || hearth.isConsumed()) return;

        PlayerModel player = universeModel.getPlayer();
        if (player == null) return;

        Vector2 hearthPos = hearth.getTransform().getTranslation();
        Vector2 playerPos = player.getTransform().getTranslation();

        double distanceSquared = hearthPos.distanceSquared(playerPos);

        double collisionRadius = HearthSettings.RAGGIO_COLLISIONE_PX / UniverseSettings.SCALE * 1.15;

        if (distanceSquared < collisionRadius * collisionRadius) {
            applyHeal(player, universeModel);
        }
    }

    private void applyHeal(PlayerModel player, UniverseModel universe) {
        // Cura il giocatore
        player.heal(HearthSettings.HP_BOOST);

        // Rimuovi l'hearth
        hearth.consume();
        universe.removeEntity(hearth);
    }
}