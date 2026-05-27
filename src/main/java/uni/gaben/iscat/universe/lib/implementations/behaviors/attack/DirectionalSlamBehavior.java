package uni.gaben.iscat.universe.lib.implementations.behaviors.attack;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.lib.implementations.behaviors.interfaces.AttackBehavior;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.player.PlayerModel;
import uni.gaben.iscat.utils.Cooldown;

/**
 * Slams the NPC in the direction of one of its four cardinal axes when the
 * player is close and roughly aligned (within a ~18° cone).
 *
 * <p>Unlike {@link PlungeAttackBehavior}, this does <em>not</em> implement
 * {@code MovementBehavior}; the slam is brief enough that we just apply the
 * impulse here and let the physics engine carry it through.</p>
 */
public class DirectionalSlamBehavior implements AttackBehavior {

    private static final double CONE_TOLERANCE = 0.95; // cos(≈18°)

    private final double   maxDist;
    private final double   slamForce;
    private final double   slamDuration;
    private final Cooldown slamCooldown = new Cooldown();

    private boolean isSlamming = false;
    private double  slamTimer  = 0.0;

    public DirectionalSlamBehavior(double maxDist, double slamForce,
                                    double cooldownSeconds, double slamDuration) {
        this.maxDist      = maxDist;
        this.slamForce    = slamForce;
        this.slamDuration = slamDuration;
        this.slamCooldown.start(cooldownSeconds);
    }

    public DirectionalSlamBehavior(double maxDist, double slamForce, double cooldownSeconds) {
        this(maxDist, slamForce, cooldownSeconds, 0.6);
    }

    @Override
    public double getPriority(AbstractEntityModel npc, UniverseModel universe) {
        if (isSlamming) return 95.0;
        if (!slamCooldown.isCoolingDown() && playerAligned(npc, universe)) return 95.0;
        return 0.0;
    }

    @Override
    public void execute(AbstractEntityModel npc, UniverseModel universe, double dt) {
        PlayerModel player = universe.getPlayer();
        if (player == null) return;

        if (!isSlamming && !slamCooldown.isCoolingDown()) {
            isSlamming = true;
            slamTimer  = slamDuration;

            Vector2 bestAxis = bestAlignedAxis(npc, player);
            npc.applyImpulse(bestAxis.multiply(slamForce));
            slamCooldown.start(4.0);
        } else if (isSlamming) {
            slamTimer -= dt;
            if (slamTimer <= 0) isSlamming = false;
        }
    }

    @Override
    public void tick(AbstractEntityModel npc, UniverseModel universe, double dt) {
        slamCooldown.update(dt);
    }

    // ─────────────────────────────────────────────────────────────────────────

    private Vector2[] cardinalAxes(AbstractEntityModel npc) {
        double rot = npc.getTransform().getRotationAngle();
        double c   = Math.cos(rot);
        double s   = Math.sin(rot);
        return new Vector2[] {
            new Vector2( c,  s),  // forward
            new Vector2(-c, -s),  // back
            new Vector2(-s,  c),  // left
            new Vector2( s, -c)   // right
        };
    }

    private boolean playerAligned(AbstractEntityModel npc, UniverseModel universe) {
        PlayerModel player = universe.getPlayer();
        if (player == null) return false;

        Vector2 npcPos    = npc.getTransform().getTranslation();
        Vector2 playerPos = player.getTransform().getTranslation();
        if (playerPos.distance(npcPos) > maxDist) return false;

        Vector2 dir = playerPos.copy().subtract(npcPos).getNormalized();
        for (Vector2 axis : cardinalAxes(npc)) {
            if (dir.dot(axis) > CONE_TOLERANCE) return true;
        }
        return false;
    }

    private Vector2 bestAlignedAxis(AbstractEntityModel npc, PlayerModel player) {
        Vector2 npcPos    = npc.getTransform().getTranslation();
        Vector2 playerPos = player.getTransform().getTranslation();
        Vector2 dir       = playerPos.copy().subtract(npcPos).getNormalized();

        Vector2 best    = null;
        double  maxDot  = -2.0;
        for (Vector2 axis : cardinalAxes(npc)) {
            double dot = dir.dot(axis);
            if (dot > maxDot) { maxDot = dot; best = axis; }
        }
        return best;
    }
}
