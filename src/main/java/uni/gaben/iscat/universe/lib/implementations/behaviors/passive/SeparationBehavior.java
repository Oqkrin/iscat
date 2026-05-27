package uni.gaben.iscat.universe.lib.implementations.behaviors.passive;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.lib.implementations.behaviors.interfaces.PassiveBehavior;
import uni.gaben.iscat.universe.UniverseModel;

import java.util.List;

/**
 * Applies a repulsive force away from same-type entities that are too close,
 * preventing NPC clumping.
 *
 * <p>Runs as a {@link PassiveBehavior} — always active, additive, and
 * independent of whatever movement behavior is running. The force is kept
 * proportionally small so it doesn't meaningfully fight the steering controller.</p>
 */
public class SeparationBehavior implements PassiveBehavior {

    private final double radius;
    private final double maxForce;

    public SeparationBehavior(double radius, double maxForce) {
        this.radius   = radius;
        this.maxForce = maxForce;
    }

    @Override
    public void execute(AbstractEntityModel npc, UniverseModel universe, double dt) {
        List<? extends AbstractEntityModel> peers = universe.getEntitiesOfType(npc.getClass());
        if (peers.size() <= 1) return;

        Vector2 npcPos   = npc.getTransform().getTranslation();
        Vector2 steering = new Vector2();
        int     count    = 0;

        for (AbstractEntityModel other : peers) {
            if (other == npc) continue;
            Vector2 otherPos = other.getTransform().getTranslation();
            double  dist     = npcPos.distance(otherPos);

            if (dist > 0 && dist < radius) {
                // Repulsion scales inversely with distance
                Vector2 away = npcPos.copy().subtract(otherPos);
                away.normalize();
                away.divide(dist);
                steering.add(away);
                count++;
            }
        }

        if (count > 0) {
            steering.divide(count);
            if (steering.getMagnitudeSquared() > 0) {
                steering.normalize();
                steering.multiply(maxForce);
                npc.applyForce(steering);
            }
        }
    }
}
