package uni.gaben.iscat.universe.enemies.master;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.lib.behaviurs.AiController;
import uni.gaben.iscat.universe.lib.behaviurs.AttackBehavior;
import uni.gaben.iscat.universe.lib.behaviurs.LineOfSightChecker;  // <-- reusable class
import uni.gaben.iscat.universe.lib.behaviurs.MovementStrategy;
import uni.gaben.iscat.universe.lib.behaviurs.ShooterBehaviour;
import uni.gaben.iscat.universe.lib.behaviurs.modifiers.ObstacleAvoidanceModifier;
import uni.gaben.iscat.universe.lib.behaviurs.modifiers.ProjectileAvoidanceModifier;
import uni.gaben.iscat.universe.lib.behaviurs.modifiers.SeparationModifier;
import uni.gaben.iscat.universe.player.PlayerModel;
import uni.gaben.iscat.universe.projectiles.ProjectileType;
import uni.gaben.iscat.universe.lib.implementations.attacks.*;
import uni.gaben.iscat.universe.UniverseSpawnable;
import uni.gaben.iscat.universe.UU;

import java.util.Random;

import static uni.gaben.iscat.universe.enemies.master.IscatMasterSettings.ISCATMASTER;

public class IscatMasterController extends AiController {

    private final ShooterBehaviour shooterBehaviour;
    private final LineOfSightChecker losChecker;
    private final Random rand = new Random();

    // For seek‑line‑of‑sight movement
    private double seekAngleSign = 1.0;
    private double seekTimer = 0.0;
    private Vector2 wanderTarget = null;

    public IscatMasterController(IscatMasterModel master) {
        super(master, ISCATMASTER.force, ISCATMASTER.maxVelocity, ISCATMASTER.rotationSpeed);

        // Create LoS checker, ignoring the master's own body
        this.losChecker = new LineOfSightChecker(master); // assumes getBody() exists

        // Movement strategy
        setMovementStrategy(new MasterMovementStrategy());

        // Avoidance modifiers
        addModifier(new SeparationModifier(UU.pxToM(32.0), ISCATMASTER.force * 0.8));
        addModifier(new ObstacleAvoidanceModifier());
        addModifier(new ProjectileAvoidanceModifier());

        // Attack (wrapped to respect LoS)
        shooterBehaviour = new ShooterBehaviour(
                80.0,
                ISCATMASTER.combatRange,
                ISCATMASTER.fireCooldownS,
                ProjectileType.ENEMY_BULLET,
                new RepeaterAttack(3, new SummonAttack(1, UniverseSpawnable.ISCAT_DASHER, 0)),
                new RepeaterAttack(3, new SummonAttack(1, UniverseSpawnable.ISCAT_HEALER, 0)),
                new RepeaterAttack(3, new SummonAttack(1, UniverseSpawnable.ISCAT_CORE, 0)),
                new RepeaterAttack(5, new MultiDirectionAttack(3, rand.nextInt(90),
                        new SpreadAttack(rand.nextInt((int) ISCATMASTER.combatRange) / 3, rand.nextInt(180)))),
                new RepeaterAttack(3, new FigureAttack(3, FigureAttack.FigureType.STAR))
        );
        addAttack(new LosAwareAttack(shooterBehaviour));
    }

    @Override
    public void update(UniverseModel universe, double dt) {
        if (entity == null || entity.shouldRemove()) return;
        if (entity instanceof IscatMasterModel iscatMasterModel) {
            iscatMasterModel.shockwave().update(dt);
            if (iscatMasterModel.getAnimationState() == IscatMasterModel.AnimationState.DEATH) return;
            super.update(universe, dt);
        }
    }

    // ── Movement strategy ────────────────────────────────────────────────

    private class MasterMovementStrategy implements MovementStrategy {
        @Override
        public Vector2 computeDesiredVelocity(AbstractEntityModel e, UniverseModel world, double dt) {
            IscatMasterModel master = (IscatMasterModel) e;
            PlayerModel player = world.getPlayer();
            if (player == null) return new Vector2();

            Vector2 pos = master.getTransform().getTranslation();
            Vector2 playerPos = player.getTransform().getTranslation();
            double dist = pos.distance(playerPos);

            losChecker.update(pos, playerPos, world);

            if (!losChecker.hasLineOfSight()) {
                return computeSeekLineOfSightVelocity(pos, playerPos, dt);
            }

            if (dist > ISCATMASTER.detectionRange) {
                return computeWanderVelocity(master, dt);
            } else {
                return computeOrbitVelocity(pos, playerPos, ISCATMASTER.preferredRange, 45.0);
            }
        }
    }

    private Vector2 computeSeekLineOfSightVelocity(Vector2 pos, Vector2 playerPos, double dt) {
        seekTimer -= dt;
        if (seekTimer <= 0) {
            seekAngleSign *= -1;
            seekTimer = 1.5 + rand.nextDouble() * 1.0;
        }
        Vector2 toPlayer = playerPos.copy().subtract(pos);
        double angle = toPlayer.getDirection() + Math.toRadians(45.0 * seekAngleSign);
        Vector2 desiredDir = new Vector2(Math.cos(angle), Math.sin(angle));
        return desiredDir.multiply(ISCATMASTER.maxVelocity);
    }

    private Vector2 computeOrbitVelocity(Vector2 pos, Vector2 playerPos, double radius, double angleOffsetDeg) {
        Vector2 toPlayer = playerPos.copy().subtract(pos);
        double dist = toPlayer.getMagnitude();
        double radialCorrection = (dist - radius) * 0.8;
        Vector2 radial = toPlayer.getNormalized().multiply(radialCorrection);

        double orbitAngle = toPlayer.getDirection() + Math.toRadians(angleOffsetDeg) + Math.PI / 2;
        Vector2 tangent = new Vector2(Math.cos(orbitAngle), Math.sin(orbitAngle)).multiply(ISCATMASTER.maxVelocity * 0.7);
        return radial.add(tangent);
    }

    private Vector2 computeWanderVelocity(IscatMasterModel master, double dt) {
        if (wanderTarget == null) {
            double angle = rand.nextDouble() * 2 * Math.PI;
            double distance = 1.0 + rand.nextDouble() * 2.0;
            wanderTarget = master.getTransform().getTranslation().copy()
                    .add(Vector2.create(distance, angle));
        }
        Vector2 toTarget = wanderTarget.copy().subtract(master.getTransform().getTranslation());
        if (toTarget.getMagnitude() < 0.2) {
            wanderTarget = null;
            return new Vector2();
        }
        return toTarget.getNormalized().multiply(ISCATMASTER.maxVelocity * 0.5);
    }

    // ── LoS‑aware attack ─────────────────────────────────────────────────

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