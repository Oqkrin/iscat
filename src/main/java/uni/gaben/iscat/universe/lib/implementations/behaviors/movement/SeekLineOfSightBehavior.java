package uni.gaben.iscat.universe.lib.implementations.behaviors.movement;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.lib.implementations.behaviors.core.MovementRequest;
import uni.gaben.iscat.universe.lib.implementations.behaviors.interfaces.MovementBehavior;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.player.PlayerModel;

/**
 * Strafes perpendicular to the player to maneuver around obstacles and
 * regain line-of-sight. Periodically reverses strafe direction to prevent
 * getting stuck on one side.
 *
 * <p>Typically combined with a {@link CheckLineOfSight} passive so that this
 * behavior is only activated when LoS is actually blocked.</p>
 */
public class SeekLineOfSightBehavior implements MovementBehavior {

    private final double maxVelocity;
    private final double priority;

    private double strafeDirection       = 1.0;
    private double changeDirectionTimer  = 0.0;

    public SeekLineOfSightBehavior(double maxVelocity, double priority) {
        this.maxVelocity = maxVelocity;
        this.priority    = priority;
    }

    @Override
    public double getPriority(AbstractEntityModel npc, UniverseModel universe) {
        return universe.getPlayer() != null ? priority : 0.0;
    }

    @Override
    public MovementRequest computeRequest(AbstractEntityModel npc, UniverseModel universe, double dt) {
        PlayerModel player = universe.getPlayer();
        if (player == null) return MovementRequest.idle();

        // Periodically reverse strafe direction
        changeDirectionTimer -= dt;
        if (changeDirectionTimer <= 0.0) {
            strafeDirection      = Math.random() > 0.5 ? 1.0 : -1.0;
            changeDirectionTimer = 2.0 + Math.random() * 2.0;
        }

        Vector2 npcPos          = npc.getTransform().getTranslation();
        Vector2 dirToPlayer     = player.getTransform().getTranslation()
                                        .copy().subtract(npcPos).getNormalized();
        Vector2 strafe          = new Vector2(-dirToPlayer.y, dirToPlayer.x)
                                        .multiply(strafeDirection);
        Vector2 desiredVelocity = strafe.multiply(maxVelocity);

        return MovementRequest.of(desiredVelocity, desiredVelocity.getDirection());
    }
}
