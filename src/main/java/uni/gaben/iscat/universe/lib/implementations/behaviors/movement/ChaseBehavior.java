package uni.gaben.iscat.universe.lib.implementations.behaviors.movement;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.lib.implementations.behaviors.core.MovementRequest;
import uni.gaben.iscat.universe.lib.implementations.behaviors.interfaces.MovementBehavior;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.player.PlayerModel;

/**
 * Steers the NPC toward the player.
 *
 * <p>Does <em>not</em> apply forces; returns a {@link MovementRequest} that the
 * {@code SteeringController} translates into physics. This eliminates conflicts
 * when other movement behaviors are also registered.</p>
 */
public class ChaseBehavior implements MovementBehavior {

    private final double maxVelocity;
    private final double detectionRange;
    private final double priority;

    public ChaseBehavior(double maxVelocity, double detectionRange, double priority) {
        this.maxVelocity    = maxVelocity;
        this.detectionRange = detectionRange;
        this.priority       = priority;
    }

    /** Convenience: reasonable defaults (priority 50, detection 15 units). */
    public ChaseBehavior(double maxVelocity) {
        this(maxVelocity, 15.0, 50.0);
    }

    @Override
    public double getPriority(AbstractEntityModel npc, UniverseModel universe) {
        PlayerModel player = universe.getPlayer();
        if (player == null) return 0.0;
        double dist = player.getTransform().getTranslation()
                            .distance(npc.getTransform().getTranslation());
        return dist <= detectionRange ? priority : 0.0;
    }

    @Override
    public MovementRequest computeRequest(AbstractEntityModel npc, UniverseModel universe, double dt) {
        PlayerModel player = universe.getPlayer();
        if (player == null) return MovementRequest.idle();

        Vector2 npcPos    = npc.getTransform().getTranslation();
        Vector2 playerPos = player.getTransform().getTranslation();
        Vector2 direction = playerPos.copy().subtract(npcPos);
        double  dist      = direction.getMagnitude();

        if (dist < 0.5) return MovementRequest.idle();

        Vector2 desiredVelocity = direction.getNormalized().multiply(maxVelocity);
        double  rotationTarget  = direction.getDirection();
        return MovementRequest.of(desiredVelocity, rotationTarget);
    }
}
