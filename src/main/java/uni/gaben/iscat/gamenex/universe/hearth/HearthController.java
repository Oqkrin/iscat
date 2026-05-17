package uni.gaben.iscat.gamenex.universe.hearth;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.gamenex.lib.implementations.AiBehaviours;
import uni.gaben.iscat.gamenex.universe.UniverseModel;
import uni.gaben.iscat.gamenex.universe.player.PlayerModel;

public class HearthController extends AiBehaviours<HearthModel> {

    private final HearthModel hearth;
    private boolean collected = false; // Flag locale per evitare calcoli duplicati in un singolo frame

    public HearthController(HearthModel hearth) {
        super(hearth);
        this.hearth = hearth;

        // COSA SUCCEDE SE COLLIDO? Lo decido io, qui nel controller.
        this.hearth.setOnCollision(otherEntity -> {
            if (otherEntity instanceof PlayerModel player && !collected) {
                collected = true;
                player.deltaToLife(HearthSettings.HP_BOOST); // Cura il giocatore
                hearth.setLife(0);
                hearth.kill();
                hearth.setShouldRemove(true);// Chiamata diretta a kill(): svanisce l'entità
            }
        });
    }

    @Override
    public void aiUpdate(UniverseModel universeModel, double dt) {
        if (hearth == null || hearth.shouldRemove() || collected) return;
        PlayerModel player = universeModel.getPlayer();
        if (player == null) return;

        Vector2 hPos = hearth.getTransform().getTranslation();
        Vector2 pPos = player.getTransform().getTranslation();
        Vector2 diff = pPos.copy().subtract(hPos);
        double dist = diff.getMagnitude();

        if (dist < HearthSettings.RANGE_ATTIVAZIONE) {
            Vector2 desired = diff.getNormalized().multiply(HearthSettings.VELOCITA_INSEGUIMENTO);
            Vector2 steering = desired.copy().subtract(hearth.getLinearVelocity());

            if (steering.getMagnitude() > 0.8) {
                steering = steering.getNormalized().multiply(0.8);
            }
            hearth.applyImpulse(steering);
        } else {
            Vector2 vel = hearth.getLinearVelocity();
            if (vel.getMagnitude() > 0.1) {
                hearth.setLinearVelocity(vel.multiply(Math.pow(0.85, dt * 60)));
            } else {
                hearth.setLinearVelocity(new Vector2(0, 0));
            }
        }
    }
}