package uni.gaben.iscat.universe.enemies.bomber;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.lib.implementations.AiBehaviours;
import uni.gaben.iscat.universe.lib.implementations.behaviors.core.MovementRequest;
import uni.gaben.iscat.universe.lib.implementations.behaviors.interfaces.MovementBehavior;
import uni.gaben.iscat.universe.lib.implementations.behaviors.movement.DodgeProjectileBehavior;
import uni.gaben.iscat.universe.lib.implementations.behaviors.passive.SeparationBehavior;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.player.PlayerModel;

import java.util.LinkedList;

import static uni.gaben.iscat.universe.enemies.bomber.IscatBomberSettings.ISCATBOMBER;

/**
 * BUG 6 FIXED: Trail-following was implemented as an {@code AttackBehavior}
 * that called {@code applyForce()} directly.  When {@code DodgeProjectileBehavior}
 * also ran on the movement track, both applied forces in the same frame — forces
 * conflicted and the Bomber jittered or was pushed in wrong directions.
 * Rotation was also missing because it bypassed {@code SteeringController}.
 *
 * FIX: Trail-following is now a {@link MovementBehavior} that returns a
 * {@link MovementRequest}.  {@code SteeringController} applies exactly one
 * physics call per frame, and {@code DodgeProjectileBehavior} (priority 80)
 * cleanly overrides trail-follow (priority 50) when a bullet is detected.
 */
public class IscatBomberController extends AiBehaviours<IscatBomberModel> {

    private final LinkedList<Vector2> playerTrail = new LinkedList<>();

    public IscatBomberController(IscatBomberModel iscat) {
        super(iscat, ISCATBOMBER.force, ISCATBOMBER.maxVelocity, ISCATBOMBER.rotationSpeed);

        aiEntity.setOnCollision(other -> {
            if (other instanceof PlayerModel) aiEntity.applyStun();
        });

        // ── PASSIVE ───────────────────────────────────────────────────────────
        addPassive(new SeparationBehavior(UU.pxToM(48.0), ISCATBOMBER.force * 0.8));

        // ── MOVEMENT: dodge (priority 80 when bullet in range) ────────────────
        addMovement(new DodgeProjectileBehavior(
                ISCATBOMBER.force * 1.5, ISCATBOMBER.detectionRange, 2.0));

        // ── MOVEMENT: trail-following chase (priority 50 normally) ────────────
        // FIX BUG 6: was AttackBehavior calling applyForce() directly
        addMovement(new MovementBehavior() {
            @Override
            public double getPriority(AbstractEntityModel npc, UniverseModel universe) {
                // Priority 0 when stunned → behavior deselected, NPC stops naturally
                return (!aiEntity.isStunned() && universe.getPlayer() != null) ? 50.0 : 0.0;
            }

            @Override
            public MovementRequest computeRequest(AbstractEntityModel npc, UniverseModel universe, double dt) {
                PlayerModel player = universe.getPlayer();
                if (player == null) return MovementRequest.idle();

                // Record current player position in trail
                Vector2 playerPos = player.getTransform().getTranslation().copy();
                playerTrail.addLast(playerPos);
                if (playerTrail.size() > IscatBomberSettings.LUNGHEZZA_TRAIL)
                    playerTrail.removeFirst();

                if (playerTrail.size() <= IscatBomberSettings.RITARDO_TRAIL)
                    return MovementRequest.idle();

                // Chase the delayed position in the trail
                int     idx          = Math.max(0, playerTrail.size() - IscatBomberSettings.RITARDO_TRAIL - 1);
                Vector2 delayedPos   = playerTrail.get(idx);
                Vector2 pos          = aiEntity.getTransform().getTranslation();
                Vector2 toTarget     = delayedPos.copy().subtract(pos);
                double  dist         = toTarget.getMagnitude();
                double  minDistMeters = UU.pxToM(IscatBomberSettings.DISTANZA_MIN_INSEGUIMENTO);

                if (dist <= minDistMeters) return MovementRequest.idle();

                // Desired velocity toward delayed target; rotation faces real player position
                Vector2 desiredVelocity = toTarget.getNormalized().multiply(ISCATBOMBER.maxVelocity);
                double  rotationTarget  = playerPos.copy().subtract(pos).getDirection();

                return MovementRequest.of(desiredVelocity, rotationTarget);
            }
        });
    }

    @Override
    public void aiUpdate(UniverseModel universeModel, double dt) {
        if (aiEntity == null || aiEntity.shouldRemove()) return;
        aiEntity.update(dt);
        super.aiUpdate(universeModel, dt);
    }
}
