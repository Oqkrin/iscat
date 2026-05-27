package uni.gaben.iscat.universe.enemies.bomber;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.lib.behaviurs.AiController;
import uni.gaben.iscat.universe.lib.behaviurs.AttackBehavior;
import uni.gaben.iscat.universe.lib.behaviurs.MovementStrategy;
import uni.gaben.iscat.universe.lib.behaviurs.modifiers.ProjectileAvoidanceModifier;
import uni.gaben.iscat.universe.lib.behaviurs.modifiers.SeparationModifier;
import uni.gaben.iscat.universe.player.PlayerModel;
import uni.gaben.iscat.universe.projectiles.Projectile;
import uni.gaben.iscat.universe.projectiles.ProjectileType;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.projectiles.StunThreadProjectile;
import uni.gaben.iscat.utils.Cooldown;

import java.util.LinkedList;
import java.util.Random;

import static uni.gaben.iscat.universe.enemies.bomber.IscatBomberSettings.ISCATBOMBER;

public class IscatBomberController extends AiController {

    // Trail recording
    private final LinkedList<Vector2> playerTrail = new LinkedList<>();
    private final Random rand = new Random();

    // Thread attack
    private final Cooldown threadCooldown = new Cooldown();

    public IscatBomberController(IscatBomberModel bomber) {
        super(bomber, ISCATBOMBER.force, ISCATBOMBER.maxVelocity, ISCATBOMBER.rotationSpeed);

        // Collision stun (unchanged)
        bomber.setOnCollision(other -> {
            if (other instanceof PlayerModel) bomber.applyStun();
        });

        // ── MOVEMENT STRATEGY: trail‑follow + orbit when close ────────────
        setMovementStrategy(new BomberTrailStrategy());

        // ── AVOIDANCE MODIFIERS ───────────────────────────────────────────
        addModifier(new SeparationModifier(UU.pxToM(48.0), ISCATBOMBER.force * 0.6));
        addModifier(new ProjectileAvoidanceModifier());   // dodge projectiles

        // ── ATTACK: stunning thread ──────────────────────────────────────
        addAttack(new ThreadShootAttack());
    }

    // ── Movement strategy ─────────────────────────────────────────────────

    private class BomberTrailStrategy implements MovementStrategy {
        @Override
        public Vector2 computeDesiredVelocity(AbstractEntityModel entity, UniverseModel world, double dt) {
            IscatBomberModel bomber = (IscatBomberModel) entity;
            if (bomber.isStunned()) return new Vector2();

            PlayerModel player = world.getPlayer();
            if (player == null) return new Vector2();

            Vector2 playerPos = player.getTransform().getTranslation().copy();
            Vector2 myPos = bomber.getTransform().getTranslation();
            double dist = myPos.distance(playerPos);

            // Record player position for trail
            playerTrail.addLast(playerPos);
            if (playerTrail.size() > IscatBomberSettings.LUNGHEZZA_TRAIL)
                playerTrail.removeFirst();

            // If close enough, orbit
            if (dist <= ISCATBOMBER.combatRange) {
                return computeOrbitVelocity(myPos, playerPos);
            }

            // Otherwise, follow delayed trail
            if (playerTrail.size() <= IscatBomberSettings.RITARDO_TRAIL)
                return new Vector2();

            int idx = Math.max(0, playerTrail.size() - IscatBomberSettings.RITARDO_TRAIL - 1);
            Vector2 delayedPos = playerTrail.get(idx);
            Vector2 toTarget = delayedPos.copy().subtract(myPos);
            double minDist = UU.pxToM(IscatBomberSettings.DISTANZA_MIN_INSEGUIMENTO);
            if (toTarget.getMagnitude() <= minDist) return new Vector2();

            return toTarget.getNormalized().multiply(ISCATBOMBER.maxVelocity);
        }

        private Vector2 computeOrbitVelocity(Vector2 pos, Vector2 playerPos) {
            Vector2 toPlayer = playerPos.copy().subtract(pos);
            double dist = toPlayer.getMagnitude();
            double radius = ISCATBOMBER.combatRange;
            double radialCorrection = (dist - radius) * 0.8;
            Vector2 radial = toPlayer.getNormalized().multiply(radialCorrection);
            double orbitAngle = toPlayer.getDirection() + Math.toRadians(60) + Math.PI / 2; // fixed offset, like spider circling
            Vector2 tangent = new Vector2(Math.cos(orbitAngle), Math.sin(orbitAngle)).multiply(ISCATBOMBER.maxVelocity * 0.7);
            return radial.add(tangent);
        }
    }

    // ── Stunning thread attack ────────────────────────────────────────────

    private class ThreadShootAttack implements AttackBehavior {
        @Override
        public double getPriority(AbstractEntityModel entity, UniverseModel world) {
            if (threadCooldown.isCoolingDown()) return 0.0;
            PlayerModel player = world.getPlayer();
            if (player == null) return 0.0;
            double dist = entity.getTransform().getTranslation()
                    .distance(player.getTransform().getTranslation());
            // Attack only when player is within range
            return dist <= ISCATBOMBER.detectionRange ? 90.0 : 0.0;
        }

        @Override
        public void execute(AbstractEntityModel entity, UniverseModel world, double dt) {
            PlayerModel player = world.getPlayer();
            if (player == null) return;
            Vector2 pos = entity.getTransform().getTranslation().copy();
            Vector2 dir = player.getTransform().getTranslation().copy()
                    .subtract(pos).getNormalized();
            // Create a thread projectile that stuns on hit
            Projectile thread = new StunThreadProjectile(pos, dir, 6); // speed 6 m/s
            world.addEntity(thread);
            threadCooldown.start(ISCATBOMBER.fireCooldownS); // use bomber's fire cooldown
        }

        @Override
        public void tick(AbstractEntityModel entity, UniverseModel world, double dt) {
            threadCooldown.update(dt);
        }
    }
}