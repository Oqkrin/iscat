package uni.gaben.iscat.universe.enemies.fallen_star_golem;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.lib.behaviurs.AiController;
import uni.gaben.iscat.universe.lib.behaviurs.AttackBehavior;
import uni.gaben.iscat.universe.lib.behaviurs.MovementStrategy;
import uni.gaben.iscat.universe.lib.behaviurs.modifiers.ObstacleAvoidanceModifier;
import uni.gaben.iscat.universe.lib.behaviurs.modifiers.ProjectileAvoidanceModifier;
import uni.gaben.iscat.universe.lib.behaviurs.modifiers.SeparationModifier;
import uni.gaben.iscat.universe.player.PlayerModel;
import uni.gaben.iscat.universe.projectiles.ProjectileType;
import uni.gaben.iscat.universe.projectiles.Shooter;
import uni.gaben.iscat.utils.Cooldown;

import static uni.gaben.iscat.universe.enemies.fallen_star_golem.FallenStarGolemSettings.FALLENSTARGOLEM;

public class FallenStarGolemController extends AiController {

    private final Shooter<FallenStarGolemModel> shooter;

    public FallenStarGolemController(FallenStarGolemModel golem) {
        super(golem, FALLENSTARGOLEM.force, FALLENSTARGOLEM.maxVelocity, FALLENSTARGOLEM.rotationSpeed);

        this.shooter = new Shooter<>(golem);

        // ── MOVEMENT: very slow orbit ─────────────────────────────────────
        setMovementStrategy(new SlowOrbitStrategy());

        // ── AVOIDANCE MODIFIERS ───────────────────────────────────────────
        addModifier(new SeparationModifier(2.0, FALLENSTARGOLEM.force * 0.6));
        addModifier(new ObstacleAvoidanceModifier());
        addModifier(new ProjectileAvoidanceModifier());   // dodges projectiles while orbiting

        // ── ATTACK: continuous stream ────────────────────────────────────
        addAttack(new ContinuousFireAttack());
    }

    // ── Extremely slow orbit around the player ────────────────────────────

    private class SlowOrbitStrategy implements MovementStrategy {
        @Override
        public Vector2 computeDesiredVelocity(AbstractEntityModel entity, UniverseModel world, double dt) {
            PlayerModel player = world.getPlayer();
            if (player == null) return new Vector2();

            Vector2 pos = entity.getTransform().getTranslation();
            Vector2 playerPos = player.getTransform().getTranslation();
            Vector2 toPlayer = playerPos.copy().subtract(pos);
            double dist = toPlayer.getMagnitude();
            double orbitRadius = FALLENSTARGOLEM.preferredRange;   // use preferred range as orbit radius

            // Gentle radial correction to stay at the desired radius
            double radialCorrection = (dist - orbitRadius) * 0.3;  // very slow correction
            Vector2 radial = toPlayer.getNormalized().multiply(radialCorrection);

            // Tangential movement – very slow speed
            double tangentSpeed = FALLENSTARGOLEM.maxVelocity * 0.5;  // you can tweak this
            // Orbit always in the same direction (e.g. clockwise)
            Vector2 tangentDir = new Vector2(-toPlayer.y, toPlayer.x).getNormalized();
            Vector2 tangent = tangentDir.multiply(tangentSpeed);

            return radial.add(tangent);
        }
    }

    // ── Attack that fires almost every frame ──────────────────────────────

    private class ContinuousFireAttack implements AttackBehavior {
        private final Cooldown fireTimer = new Cooldown();   // fires every 0.15 seconds

        @Override
        public double getPriority(AbstractEntityModel entity, UniverseModel world) {
            PlayerModel player = world.getPlayer();
            if (player == null) return 0.0;
            double dist = entity.getTransform().getTranslation()
                    .distance(player.getTransform().getTranslation());
            return dist <= FALLENSTARGOLEM.detectionRange ? 100.0 : 0.0;
        }

        @Override
        public void execute(AbstractEntityModel entity, UniverseModel world, double dt) {
            // Nothing to do here – the actual firing is handled in tick() for continuous behaviour
        }

        @Override
        public void tick(AbstractEntityModel entity, UniverseModel world, double dt) {
            fireTimer.update(dt);
            if (fireTimer.isCoolingDown()) return;

            PlayerModel player = world.getPlayer();
            if (player == null) return;

            Vector2 pos = entity.getTransform().getTranslation();
            Vector2 dir = player.getTransform().getTranslation().copy()
                    .subtract(pos).getNormalized();
            shooter.shoot(ProjectileType.ENEMY_BULLET, dir.getDirection());   // aim directly at player
            fireTimer.start(FALLENSTARGOLEM.fireCooldownS);
        }
    }
}