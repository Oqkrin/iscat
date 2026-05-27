package uni.gaben.iscat.universe.lib.implementations.behaviors.movement;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.lib.implementations.MovementRequest;
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
        Vector2 toPlayer = playerPos.copy().subtract(npcPos).getNormalized();
        Vector2 tangent  = new Vector2(-toPlayer.y, toPlayer.x);

        // 1. Calculate Radial component (Approach/Retreat)
        double radialStrength = (dist - orbitRadius) * 2.0; // Scaled difference
        Vector2 radialForce = toPlayer.multiply(radialStrength);

        // 2. Calculate Tangential component (Orbit)
        Vector2 tangentialForce = tangent.multiply(maxVelocity);

        // 3. Blend them!
        // This creates a circular path that naturally corrects its distance.
        Vector2 desiredVelocity = radialForce.add(tangentialForce).getNormalized().multiply(maxVelocity);

        return MovementRequest.of(desiredVelocity, facePlayer ? toPlayer.getDirection() : desiredVelocity.getDirection());
    }
}
