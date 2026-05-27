package uni.gaben.iscat.universe.lib.implementations.behaviors.movement;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.lib.implementations.behaviors.core.MovementRequest;
import uni.gaben.iscat.universe.lib.implementations.behaviors.interfaces.MovementBehavior;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.player.PlayerModel;

/**
 * Keeps the NPC at a preferred orbital radius around the player,
 * circling clockwise or counter-clockwise.
 *
 * <ul>
 *   <li>Too far → approach</li>
 *   <li>Too close → retreat</li>
 *   <li>In band → strafe tangentially</li>
 * </ul>
 */
public class OrbitPlayerBehavior implements MovementBehavior {

    private final double maxVelocity;
    private final double orbitRadius;
    private final double priority;
    private final boolean facePlayer;

    /**
     * @param maxVelocity  Top speed in world units/s.
     * @param orbitRadius  Ideal distance from the player.
     * @param priority     Priority value when active.
     * @param facePlayer   {@code true} = face the player while orbiting;
     *                     {@code false} = face the direction of travel.
     */
    public OrbitPlayerBehavior(double maxVelocity, double orbitRadius,
                                double priority, boolean facePlayer) {
        this.maxVelocity = maxVelocity;
        this.orbitRadius = orbitRadius;
        this.priority    = priority;
        this.facePlayer  = facePlayer;
    }

    @Override
    public double getPriority(AbstractEntityModel npc, UniverseModel universe) {
        return universe.getPlayer() != null ? priority : 0.0;
    }

    @Override
    public MovementRequest computeRequest(AbstractEntityModel npc, UniverseModel universe, double dt) {
        PlayerModel player = universe.getPlayer();
        if (player == null) return MovementRequest.idle();

        Vector2 npcPos    = npc.getTransform().getTranslation();
        Vector2 playerPos = player.getTransform().getTranslation();
        double  dist      = playerPos.distance(npcPos);
        Vector2 toPlayer  = playerPos.copy().subtract(npcPos).getNormalized();

        Vector2 desiredVelocity;
        if (dist > orbitRadius * 1.2) {
            desiredVelocity = toPlayer.multiply(maxVelocity);
        } else if (dist < orbitRadius * 0.8) {
            desiredVelocity = toPlayer.multiply(-maxVelocity);
        } else {
            // Tangent (counter-clockwise)
            desiredVelocity = new Vector2(-toPlayer.y, toPlayer.x).multiply(maxVelocity);
        }

        double rotTarget = facePlayer
                ? toPlayer.getDirection()
                : desiredVelocity.getDirection();

        return MovementRequest.of(desiredVelocity, rotTarget);
    }
}
