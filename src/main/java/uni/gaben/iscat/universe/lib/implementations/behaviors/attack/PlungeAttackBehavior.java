package uni.gaben.iscat.universe.lib.implementations.behaviors.attack;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.lib.implementations.behaviors.core.MovementRequest;
import uni.gaben.iscat.universe.lib.implementations.behaviors.interfaces.AttackBehavior;
import uni.gaben.iscat.universe.lib.implementations.behaviors.interfaces.MovementBehavior;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.player.PlayerModel;
import uni.gaben.iscat.utils.Cooldown;

/**
 * A charge attack: the NPC dashes toward the player with a strong impulse,
 * then course-corrects for the duration of the plunge.
 *
 * <h2>Dual interface</h2>
 * <p>This class implements <em>both</em> {@link AttackBehavior} and
 * {@link MovementBehavior}. While a plunge is active,
 * {@link #computeRequest} returns a <em>locked</em> {@link MovementRequest}
 * so the {@code AiBehaviours} orchestrator gives this behavior full movement
 * control and suppresses normal movement behaviors (chase, orbit, etc.).</p>
 *
 * <p>When not plunging, {@link #getPriority(AbstractEntityModel, UniverseModel)}
 * on the movement side returns 0, so it never interferes with normal movement.</p>
 */
public class PlungeAttackBehavior implements AttackBehavior, MovementBehavior {

    private final double triggerRadius;
    private final double plungeForce;
    private final double plungeDurationSeconds;
    private final double correctionForce;      // lateral correction during plunge
    private final Cooldown plungeCooldown      = new Cooldown();

    private boolean isPlunging   = false;
    private double  plungeTimer  = 0.0;

    public PlungeAttackBehavior(double triggerRadius, double plungeForce,
                                 double cooldownSeconds, double plungeDurationSeconds) {
        this.triggerRadius          = triggerRadius;
        this.plungeForce            = plungeForce;
        this.plungeDurationSeconds  = plungeDurationSeconds;
        this.correctionForce        = plungeForce * 0.4;
        this.plungeCooldown.start(cooldownSeconds);
    }

    public PlungeAttackBehavior(double triggerRadius, double plungeForce, double cooldownSeconds) {
        this(triggerRadius, plungeForce, cooldownSeconds, 0.5);
    }

    // ── AttackBehavior ───────────────────────────────────────────────────────

    @Override
    public double getPriority(AbstractEntityModel npc, UniverseModel universe) {
        if (isPlunging) return 90.0;
        if (!plungeCooldown.isCoolingDown()) {
            PlayerModel p = universe.getPlayer();
            if (p != null) {
                double dist = p.getTransform().getTranslation()
                               .distance(npc.getTransform().getTranslation());
                if (dist <= triggerRadius) return 90.0;
            }
        }
        return 0.0;
    }

    @Override
    public void execute(AbstractEntityModel npc, UniverseModel universe, double dt) {
        PlayerModel player = universe.getPlayer();
        if (player == null) return;

        if (!isPlunging && !plungeCooldown.isCoolingDown()) {
            // Launch
            isPlunging  = true;
            plungeTimer = plungeDurationSeconds;

            Vector2 dir = player.getTransform().getTranslation()
                                .copy().subtract(npc.getTransform().getTranslation())
                                .getNormalized();
            npc.applyImpulse(dir.multiply(plungeForce));
            plungeCooldown.start(3.0);
        }
    }

    @Override
    public void tick(AbstractEntityModel npc, UniverseModel universe, double dt) {
        plungeCooldown.update(dt);

        if (isPlunging) {
            plungeTimer -= dt;
            if (plungeTimer <= 0) isPlunging = false;
        }
    }

    // ── MovementBehavior ─────────────────────────────────────────────────────

    /**
     * While plunging, applies a small correction force toward the player and
     * locks out all other movement behaviors.
     */
    @Override
    public MovementRequest computeRequest(AbstractEntityModel npc, UniverseModel universe, double dt) {
        if (!isPlunging) return MovementRequest.idle();

        PlayerModel player = universe.getPlayer();
        if (player == null) return MovementRequest.idle();

        Vector2 npcPos    = npc.getTransform().getTranslation();
        Vector2 targetPos = player.getTransform().getTranslation();
        Vector2 dir       = targetPos.copy().subtract(npcPos).getNormalized();

        // Correction force is applied directly (impulse already set the velocity).
        // We pass a locked request so the SteeringController adds a gentle correction
        // without letting any other movement behavior interfere.
        npc.applyForce(dir.multiply(correctionForce));

        return MovementRequest.locked(null, dir.getDirection());
    }
}
