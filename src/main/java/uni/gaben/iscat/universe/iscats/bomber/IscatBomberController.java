package uni.gaben.iscat.universe.iscats.bomber;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.lib.implementations.AiBehaviours;
import uni.gaben.iscat.universe.lib.implementations.behaviors.DodgeProjectileBehavior;
import uni.gaben.iscat.universe.lib.implementations.behaviors.SeparationBehavior;
import uni.gaben.iscat.universe.lib.interfaces.controller.AiBehavior;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.player.PlayerModel;
import uni.gaben.iscat.utils.Interpolator;

import java.util.LinkedList;

import static uni.gaben.iscat.universe.iscats.bomber.IscatBomberSettings.ISCATBOMBER;

/**
 * Controller AI per IscatBomber.
 *
 * Segue il giocatore mirando a posizioni passate (trail ritardato),
 * simulando un inseguimento pesante e lento a reagire.
 * Quando è stordito (colpito dal player), si ferma finché il cooldown scade.
 *
 * Registra il callback di collisione per lo stun direttamente qui
 * (è responsabilità del controller, non del model).
 */
public class IscatBomberController extends AiBehaviours<IscatBomberModel> {

    private final LinkedList<Vector2> playerTrail = new LinkedList<>();

    public IscatBomberController(IscatBomberModel iscat) {
        super(iscat);
        // Il controller è responsabile di registrare la logica di collisione.
        // Il modello espone applyStun() come interfaccia pulita.
        aiEntity.setOnCollision(other -> {
            if (other instanceof PlayerModel) {
                aiEntity.applyStun();
            }
        });

        this.addBehavior(new SeparationBehavior(
                UU.pxToM(48.0), ISCATBOMBER.force * 0.8));

        this.addBehavior(new DodgeProjectileBehavior(ISCATBOMBER.force * 1.5, 2.0));

        addBehavior(new AiBehavior() {
            @Override
            public double getPriority(AbstractEntityModel npc, UniverseModel universe) {
                // Se stordito priorità 0, altrimenti 50
                return aiEntity.isStunned() || universe.getPlayer() == null ? 0.0 : 50.0;
            }

            @Override
            public void execute(uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel npc, UniverseModel universe, double dt) {
                PlayerModel player = universe.getPlayer();
                if (player == null) return;

                // ── REGISTRAZIONE TRAIL ──────────────────────────────────────────────
                Vector2 playerPos = player.getTransform().getTranslation().copy();
                playerTrail.addLast(playerPos);
                if (playerTrail.size() > IscatBomberSettings.LUNGHEZZA_TRAIL) {
                    playerTrail.removeFirst();
                }

                // ── INSEGUIMENTO CON RITARDO ─────────────────────────────────────────
                if (playerTrail.size() > IscatBomberSettings.RITARDO_TRAIL) {
                    int targetIndex = Math.max(0, playerTrail.size() - IscatBomberSettings.RITARDO_TRAIL - 1);
                    Vector2 delayedTarget = playerTrail.get(targetIndex);

                    Vector2 pos    = aiEntity.getTransform().getTranslation();
                    Vector2 toTarget = delayedTarget.copy().subtract(pos);
                    double dist    = toTarget.getMagnitude();

                    double minDistMeters = UU.pxToM(IscatBomberSettings.DISTANZA_MIN_INSEGUIMENTO);
                    if (dist > minDistMeters) {
                        toTarget.normalize();
                        aiEntity.applyForce(toTarget.multiply(ISCATBOMBER.force * aiEntity.getMass().getMass()));
                    }
                }

                // ── ROTAZIONE VERSO IL PLAYER REALE ─────────────────────────────────
                rotateTo(playerPos, dt);
            }
        });
    }

    @Override
    public void aiUpdate(UniverseModel universeModel, double dt) {
        if (aiEntity == null || aiEntity.shouldRemove()) return;

        // Tick del cooldown interno al modello
        aiEntity.update(dt);

        super.aiUpdate(universeModel, dt);
    }

    /**
     * Lerp fluido verso l'angolo che punta al player, con wrap-around ±PI.
     */
    private void rotateTo(Vector2 playerPos, double dt) {
        aiEntity.setAngularVelocity(0.0); // annulla inerzia angolare da collisioni

        Vector2 pos = aiEntity.getTransform().getTranslation();
        double targetAngle  = playerPos.copy().subtract(pos).getDirection();
        double currentAngle = aiEntity.getTransform().getRotationAngle();

        double diff = targetAngle - currentAngle;
        while (diff < -Math.PI) diff += Math.PI * 2;
        while (diff >  Math.PI) diff -= Math.PI * 2;

        double next = Interpolator.lerp(
                currentAngle,
                currentAngle + diff,
                Math.min(IscatBomberSettings.SMOOTHING_ROTAZIONE * dt * 60, 1.0)
        );
        aiEntity.getTransform().setRotation(next);
    }
}
