package uni.gaben.iscat.universe.consumables.heart;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.lib.implementations.AiBehaviours;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.lib.implementations.MovementRequest;
import uni.gaben.iscat.universe.lib.implementations.behaviors.interfaces.MovementBehavior;
import uni.gaben.iscat.universe.lib.interfaces.controller.AiBehavior;
import uni.gaben.iscat.universe.player.PlayerModel;

public class HeartController extends AiBehaviours<HeartModel> {

    private final HeartModel hearth;
    private boolean collected = false; // Flag locale per evitare calcoli duplicati in un singolo frame

    public HeartController(HeartModel hearth) {
        super(hearth, hearth.getBaseAccelerationPerTick(), hearth.getTerminalVelocity(), 0.0) ;
        this.hearth = hearth;

        // COSA SUCCEDE SE COLLIDO? Lo decido io, qui nel controller.
        this.hearth.setOnCollision(otherEntity -> {
            if (otherEntity instanceof PlayerModel player && !collected) {
                collected = true;
                double halfMaxLife = player.getMaxLife() / 2.0;
                player.deltaToLife(halfMaxLife);
                hearth.setLife(0);
                hearth.kill();
                hearth.setShouldRemove(true);// Chiamata diretta a kill(): svanisce l'entità
            }
        });
        addMovement(new MovementBehavior() {
            @Override
            public double getPriority(AbstractEntityModel npc, UniverseModel universe) {
                return 10.0; // Sempre attivo
            }

            @Override
            public MovementRequest computeRequest(AbstractEntityModel npc, UniverseModel universe, double dt) {
                if (collected) return new MovementRequest(Vector2.create(0,0), 0.0, true);
                PlayerModel player = universe.getPlayer();
                if (player == null) return new MovementRequest(Vector2.create(0,0), 0.0, true);;

                Vector2 hPos = hearth.getTransform().getTranslation();
                Vector2 pPos = player.getTransform().getTranslation();
                Vector2 diff = pPos.copy().subtract(hPos);
                double dist = diff.getMagnitude();

                if (dist < HeartSettings.RANGE_ATTIVAZIONE) {
                    Vector2 desired = diff.getNormalized().multiply(HeartSettings.VELOCITA_INSEGUIMENTO);
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
                return new MovementRequest(Vector2.create(0,0), 0.0, true);
            }
        });
    }

    @Override
    public void aiUpdate(UniverseModel universeModel, double dt) {
        if (hearth == null || hearth.shouldRemove() || collected) return;
        super.aiUpdate(universeModel, dt);
    }
}