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
        Vector2 diff = pPos.copy().subtract(hPos);
        double dist = diff.getMagnitude();

        // COLLISIONE
        double collisionThreshold = (HearthSettings.RAGGIO_COLLISIONE_PX / UniverseSettings.SCALE) + 0.3;
        if (dist < collisionThreshold) {
            applyHeal(player, universeModel);
            hearth.setLinearVelocity(new Vector2(0, 0));
            return;
        }

        if (dist < HearthSettings.RANGE_ATTIVAZIONE) {
            // Velocità desiderata: direzione * velocità massima
            Vector2 desired = diff.getNormalized()
                    .multiply(HearthSettings.VELOCITA_INSEGUIMENTO);

            // Steering = desired - current (correzione progressiva)
            Vector2 steering = desired.copy()
                    .subtract(hearth.getLinearVelocity());

            // Clamp dello steering per evitare accelerazioni brusche
            if (steering.getMagnitude() > 0.8) {
                steering = steering.getNormalized()
                        .multiply(0.8);
            }

            hearth.applyImpulse(steering);

            // Clamp velocità massima
            Vector2 vel = hearth.getLinearVelocity();
            if (vel.getMagnitude() > HearthSettings.VELOCITA_INSEGUIMENTO) {
                hearth.setLinearVelocity(
                        vel.getNormalized().multiply(HearthSettings.VELOCITA_INSEGUIMENTO)
                );
            }
        } else {
            // Fuori range: damping graduale
            Vector2 vel = hearth.getLinearVelocity();
            double speed = vel.getMagnitude();
            if (speed > 0.1) {
                hearth.setLinearVelocity(vel.multiply(Math.pow(0.85, dt * 60)));
            } else {
                hearth.setLinearVelocity(new Vector2(0, 0));
            }
        }
    }

    private void applyHeal(PlayerModel player, UniverseModel universe) {
        // Cura il giocatore
        player.deltaToLife(HearthSettings.HP_BOOST);
        hearth.setLife(0); // This will trigger automatic removal in UniverseModel.update()
    }
}