package uni.gaben.iscat.universe.lib.implementations.behaviors.movement;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.lib.implementations.MovementRequest;
import uni.gaben.iscat.universe.lib.implementations.behaviors.interfaces.MovementBehavior;
import uni.gaben.iscat.universe.UniverseModel;

import java.util.Random;

/**
 * Picks a nearby random target and moves toward it.
 * Chooses a new target when the current one is reached.
 * Typically used as a low-priority fallback when nothing else is active.
 */
public class WanderBehavior implements MovementBehavior {

    private final double maxVelocity;
    private final double priority;
    private final double minRadius;
    private final double maxRadius;

    private Vector2 wanderTarget = null;
    private final Random rand = new Random();

    public WanderBehavior(double maxVelocity, double priority,
                           double minRadius, double maxRadius) {
        this.maxVelocity = maxVelocity;
        this.priority    = priority;
        this.minRadius   = minRadius;
        this.maxRadius   = maxRadius;
    }

    /** Convenience: defaults to radius 1–3, priority 10. */
    public WanderBehavior(double maxVelocity) {
        this(maxVelocity, 10.0, 1.0, 3.0);
    }

    @Override
    public double getPriority(AbstractEntityModel npc, UniverseModel universe) {
        return priority;
    }

    @Override
    public MovementRequest computeRequest(AbstractEntityModel npc, UniverseModel universe, double dt) {
        if (wanderTarget == null || npc.contains(wanderTarget)) {
            wanderTarget = pickNewTarget(npc);
        }

        Vector2 npcPos    = npc.getTransform().getTranslation();
        Vector2 direction = wanderTarget.copy().subtract(npcPos);

        if (direction.getMagnitudeSquared() < 0.01) {
            wanderTarget = null;
            return MovementRequest.idle();
        }

        Vector2 desiredVelocity = direction.getNormalized().multiply(maxVelocity);
        return MovementRequest.of(desiredVelocity, desiredVelocity.getDirection());
    }

    private Vector2 pickNewTarget(AbstractEntityModel npc) {
        double angle  = rand.nextDouble() * Math.PI * 2.0;
        double radius = minRadius + rand.nextDouble() * (maxRadius - minRadius);
        Vector2 pos   = npc.getTransform().getTranslation();
        return new Vector2(pos.x + Math.cos(angle) * radius,
                           pos.y + Math.sin(angle) * radius);
    }
}
