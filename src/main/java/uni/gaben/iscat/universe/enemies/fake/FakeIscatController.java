package uni.gaben.iscat.universe.enemies.fake;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.lib.behaviurs.AiController;
import uni.gaben.iscat.universe.lib.behaviurs.AttackBehavior;
import uni.gaben.iscat.universe.lib.behaviurs.LineOfSightChecker;
import uni.gaben.iscat.universe.lib.behaviurs.MovementStrategy;
import uni.gaben.iscat.universe.lib.behaviurs.ShooterBehaviour;
import uni.gaben.iscat.universe.lib.behaviurs.modifiers.ProjectileAvoidanceModifier;
import uni.gaben.iscat.universe.lib.behaviurs.modifiers.SeparationModifier;
import uni.gaben.iscat.universe.player.PlayerModel;
import uni.gaben.iscat.universe.projectiles.ProjectileType;
import uni.gaben.iscat.universe.lib.implementations.attacks.*;
import uni.gaben.iscat.universe.UU;

import java.util.Random;

import static uni.gaben.iscat.universe.enemies.fake.FakeIscatSettings.FAKEISCAT;

public class FakeIscatController extends AiController {

    private final ShooterBehaviour shooterBehaviour;
    private final LineOfSightChecker losChecker;
    private final Random rand = new Random();

    // Wander state
    private Vector2 wanderTarget = null;

    // Seek‑LoS state
    private double seekAngleSign = 1.0;
    private double seekTimer = 0.0;

    // Erratic orbit state
    private double erraticTimer = 0.0;
    private double erraticAngleOffset = 0.0;

    public FakeIscatController(FakeIscatModel fake) {
        super(fake, FAKEISCAT.force, FAKEISCAT.maxVelocity, FAKEISCAT.rotationSpeed);

        this.losChecker = new LineOfSightChecker(fake);

        // Movement strategy
        setMovementStrategy(new FakeMovementStrategy());

        // Avoidance
        addModifier(new SeparationModifier(UU.pxToM(64.0), FAKEISCAT.force * 0.8));
        addModifier(new ProjectileAvoidanceModifier());

        // Attacks
        shooterBehaviour = new ShooterBehaviour(
                80.0,
                FAKEISCAT.combatRange,
                FAKEISCAT.fireCooldownS,
                ProjectileType.ENEMY_BULLET,
                new RepeaterAttack(5, new SingleShotAttack()),
                new RepeaterAttack(2, new SpreadAttack(3, 30.0)),
                new MultiDirectionAttack(4, 0, new SingleShotAttack())
        );
        addAttack(new LosAwareAttack(shooterBehaviour));
    }

    @Override
    public void update(UniverseModel universe, double dt) {
        if (entity == null || entity.shouldRemove()) return;
        super.update(universe, dt);
    }

    // ── Movement strategy ─────────────────────────────────────────────────

    private class FakeMovementStrategy implements MovementStrategy {
        @Override
        public Vector2 computeDesiredVelocity(AbstractEntityModel e, UniverseModel world, double dt) {
            FakeIscatModel fake = (FakeIscatModel) e;
            PlayerModel player = world.getPlayer();
            if (player == null) return new Vector2();

            Vector2 pos = fake.getTransform().getTranslation();
            Vector2 playerPos = player.getTransform().getTranslation();
            double dist = pos.distance(playerPos);

            losChecker.update(pos, playerPos, world);

            // 1. No player detection → wander
            if (dist > FAKEISCAT.detectionRange) {
                return computeWanderVelocity(fake);
            }

            // 2. No line‑of‑sight → seek a clear angle
            if (!losChecker.hasLineOfSight()) {
                return computeSeekLoSVelocity(pos, playerPos, dt);
            }

            // 3. LoS clear → combat behavior
            if (dist < FAKEISCAT.combatRange * 0.8) {
                // Too close → retreat
                Vector2 toPlayer = playerPos.copy().subtract(pos);
                return toPlayer.getNormalized().multiply(-FAKEISCAT.maxVelocity * 0.6);
            } else if (dist > FAKEISCAT.combatRange * 1.3) {
                // Too far → chase
                Vector2 toPlayer = playerPos.copy().subtract(pos);
                return toPlayer.getNormalized().multiply(FAKEISCAT.maxVelocity);
            } else {
                // In sweet spot → erratic orbit
                return computeErraticOrbitVelocity(pos, playerPos, dt);
            }
        }
    }

    // ── Movement helpers ──────────────────────────────────────────────────

    private Vector2 computeErraticOrbitVelocity(Vector2 pos, Vector2 playerPos, double dt) {
        erraticTimer -= dt;
        if (erraticTimer <= 0) {
            erraticTimer = 0.8 + rand.nextDouble() * 1.2;
            erraticAngleOffset = (rand.nextDouble() - 0.5) * Math.PI;
        }

        Vector2 toPlayer = playerPos.copy().subtract(pos);
        double dist = toPlayer.getMagnitude();
        double idealRadius = FAKEISCAT.combatRange;
        double radialCorrection = (dist - idealRadius) * 0.6;
        Vector2 radial = toPlayer.getNormalized().multiply(radialCorrection);

        double sign = erraticAngleOffset > 0 ? 1 : -1;
        Vector2 tangentDir = new Vector2(-toPlayer.y, toPlayer.x).multiply(sign).getNormalized();
        Vector2 tangent = tangentDir.multiply(FAKEISCAT.maxVelocity * 0.7);

        return radial.add(tangent);
    }

    private Vector2 computeSeekLoSVelocity(Vector2 pos, Vector2 playerPos, double dt) {
        seekTimer -= dt;
        if (seekTimer <= 0) {
            seekAngleSign *= -1;
            seekTimer = 1.5 + rand.nextDouble() * 1.0;
        }
        Vector2 toPlayer = playerPos.copy().subtract(pos);
        double angle = toPlayer.getDirection() + Math.toRadians(45.0 * seekAngleSign);
        Vector2 desiredDir = new Vector2(Math.cos(angle), Math.sin(angle));
        return desiredDir.multiply(FAKEISCAT.maxVelocity);
    }

    private Vector2 computeWanderVelocity(FakeIscatModel fake) {
        if (wanderTarget == null) {
            double angle = rand.nextDouble() * 2 * Math.PI;
            double distance = 1.0 + rand.nextDouble() * 2.0; // 1..3
            wanderTarget = fake.getTransform().getTranslation().copy()
                    .add(Vector2.create(distance, angle));
        }
        Vector2 toTarget = wanderTarget.copy().subtract(fake.getTransform().getTranslation());
        if (toTarget.getMagnitude() < 0.2) {
            wanderTarget = null;
            return new Vector2();
        }
        return toTarget.getNormalized().multiply(FAKEISCAT.maxVelocity * 0.5);
    }

    // ── LoS‑aware attack wrapper ─────────────────────────────────────────

    private class LosAwareAttack implements AttackBehavior {
        private final AttackBehavior delegate;

        LosAwareAttack(AttackBehavior delegate) {
            this.delegate = delegate;
        }

        @Override
        public double getPriority(AbstractEntityModel entity, UniverseModel world) {
            if (!losChecker.hasLineOfSight()) return 0.0;
            return delegate.getPriority(entity, world);
        }

        @Override
        public void execute(AbstractEntityModel entity, UniverseModel world, double dt) {
            if (losChecker.hasLineOfSight()) {
                delegate.execute(entity, world, dt);
            }
        }

        @Override
        public void tick(AbstractEntityModel entity, UniverseModel world, double dt) {
            delegate.tick(entity, world, dt);
        }
    }
}