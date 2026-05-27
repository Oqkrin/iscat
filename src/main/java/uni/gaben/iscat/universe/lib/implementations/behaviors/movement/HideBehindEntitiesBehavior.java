package uni.gaben.iscat.universe.lib.implementations.behaviors.movement;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.lib.implementations.LivingEntityModel;
import uni.gaben.iscat.universe.lib.implementations.behaviors.core.MovementRequest;
import uni.gaben.iscat.universe.lib.implementations.behaviors.interfaces.MovementBehavior;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.player.PlayerModel;

/**
 * Moves the NPC to a position behind the nearest ally, using that ally as
 * a shield against the player.
 *
 * <p>Falls back to fleeing directly away from the player if no ally is found.</p>
 */
public class HideBehindEntitiesBehavior implements MovementBehavior {

    private final double maxVelocity;
    private final double hideDistance; // how far behind the ally to stand
    private final double priority;

    public HideBehindEntitiesBehavior(double maxVelocity, double hideDistance, double priority) {
        this.maxVelocity  = maxVelocity;
        this.hideDistance = hideDistance;
        this.priority     = priority;
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

        Vector2 targetPos = findHideTarget(npc, player, npcPos, playerPos, universe);
        Vector2 direction = targetPos.subtract(npcPos);

        if (direction.getMagnitudeSquared() < 0.01) return MovementRequest.idle();

        Vector2 desiredVelocity = direction.getNormalized().multiply(maxVelocity);
        return MovementRequest.of(desiredVelocity, desiredVelocity.getDirection());
    }

    private Vector2 findHideTarget(AbstractEntityModel npc, PlayerModel player,
                                    Vector2 npcPos, Vector2 playerPos, UniverseModel universe) {
        AbstractEntityModel nearestAlly  = null;
        double              minDist      = Double.MAX_VALUE;

        for (AbstractEntityModel e : universe.getEntitiesOfType(LivingEntityModel.class)) {
            if (e == npc || e == player) continue;
            if (e.getClass().getSimpleName().contains("Healer")) continue; // don't use healers as shields
            double dist = e.getTransform().getTranslation().distance(npcPos);
            if (dist < minDist) {
                minDist     = dist;
                nearestAlly = e;
            }
        }

        if (nearestAlly != null) {
            Vector2 allyPos      = nearestAlly.getTransform().getTranslation();
            Vector2 playerToAlly = allyPos.copy().subtract(playerPos).getNormalized();
            return allyPos.copy().add(playerToAlly.multiply(hideDistance));
        }

        // Fallback: flee directly away from player
        Vector2 playerToNpc = npcPos.copy().subtract(playerPos).getNormalized();
        return npcPos.copy().add(playerToNpc.multiply(10.0));
    }
}
