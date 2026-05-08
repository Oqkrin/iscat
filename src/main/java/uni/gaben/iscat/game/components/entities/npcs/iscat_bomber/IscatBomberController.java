package uni.gaben.iscat.game.components.entities.npcs.iscat_bomber;

import uni.gaben.iscat.game.GameModel;
import uni.gaben.iscat.game.utils.interfaces.AI;
import uni.gaben.iscat.game.utils.physics.Vec2;

import java.util.ArrayList;
import java.util.List;

/**
 * IscatBomberController — AI behaviour for IscatBomber.
 *
 * Implements the AI interface so GameModel can call updateAI() each tick.
 * Owns: player trail, follow logic, direction smoothing.
 * Reads stun state from the model; applies forces to the model.
 */
public class IscatBomberController implements AI {

    private final IscatBomberModel model;
    private final List<Vec2> playerTrail = new ArrayList<>();

    public IscatBomberController(IscatBomberModel model) {
        this.model = model;
    }

    // -------------------------------------------------------------------------
    // AI interface
    // -------------------------------------------------------------------------

    @Override
    public void updateAI(GameModel world, double dt) {
        model.tickCooldowns();

        if (model.isStunned()) return;

        // Record player position in trail
        Vec2 playerPos = world.getPlayer().getColliderCenter();
        playerTrail.add(playerPos);
        if (playerTrail.size() > IscatBomberSettings.LUNGHEZZA_TRAIL) {
            playerTrail.remove(0);
        }

        followPlayer(playerPos);
    }

    @Override
    public void resetAI() {
        playerTrail.clear();
    }

    // -------------------------------------------------------------------------
    // Follow logic
    // -------------------------------------------------------------------------

    /**
     * @param currentPlayerPos current player collider center — used for facing direction.
     */
    private void followPlayer(Vec2 currentPlayerPos) {
        if (playerTrail.size() <= IscatBomberSettings.RITARDO_TRAIL) return;

        int targetIndex = Math.max(0,
                Math.min(playerTrail.size() - IscatBomberSettings.RITARDO_TRAIL,
                         playerTrail.size() - 1));

        // Move toward the delayed trail position
        Vec2 target = playerTrail.get(targetIndex);
        Vec2 pos    = model.getColliderCenter();
        double dx   = target.x - pos.x;
        double dy   = target.y - pos.y;
        double dist = Math.sqrt(dx * dx + dy * dy);

        if (dist > IscatBomberSettings.DISTANZA_MIN_INSEGUIMENTO) {
            double nx = dx / dist;
            double ny = dy / dist;
            model.applyForce(new Vec2(
                    nx * IscatBomberSettings.VELOCITA_INSEGUIMENTO,
                    ny * IscatBomberSettings.VELOCITA_INSEGUIMENTO));
        }

        // Face the CURRENT player position, not the trail target
        Vec2 myCenter = model.getColliderCenter();
        model.updateDirectionSmooth(
                currentPlayerPos.x - myCenter.x,
                currentPlayerPos.y - myCenter.y,
                IscatBomberSettings.SMOOTHING_ROTAZIONE);
    }

    /** Expose the model so GameModel can register it in entity collections. */
    public IscatBomberModel getModel() { return model; }
}
