package uni.gaben.iscat.universe.enemies.worm;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.lib.behaviurs.AiController;
import uni.gaben.iscat.universe.lib.behaviurs.AttackBehavior;
import uni.gaben.iscat.universe.lib.behaviurs.MovementStrategy;
import uni.gaben.iscat.universe.lib.behaviurs.ShooterBehaviour;
import uni.gaben.iscat.universe.lib.behaviurs.modifiers.ProjectileAvoidanceModifier;
import uni.gaben.iscat.universe.lib.behaviurs.modifiers.ObstacleAvoidanceModifier;
import uni.gaben.iscat.universe.player.PlayerModel;
import uni.gaben.iscat.universe.projectiles.ProjectileType;
import uni.gaben.iscat.universe.lib.implementations.attacks.*;
import uni.gaben.iscat.utils.Cooldown;

import java.util.*;

import static uni.gaben.iscat.universe.enemies.worm.IscatWormSettings.*;

public class IscatWormController {

    private final IscatWormModel worm;
    private final Map<IscatWormSegment, AiController> controllers = new LinkedHashMap<>();

    public IscatWormController(IscatWormModel worm) {
        this.worm = worm;
        for (IscatWormSegment seg : worm.getSegments()) {
            controllers.put(seg, createSegmentController(seg));
        }
    }

    public void update(UniverseModel universe, double dt) {
        for (IscatWormSegment seg : worm.getSegments()) {
            if (seg.isConsumed()) continue;
            seg.updateCooldowns(dt);
            handlePromotion(seg);
            AiController ctrl = controllers.get(seg);
            if (ctrl != null) ctrl.update(universe, dt);
        }
    }

    // ── Concrete AiController subclass (solves abstract instantiation) ───

    private static class SegmentAiController extends AiController {
        public SegmentAiController(AbstractEntityModel entity, double maxForce,
                                   double maxVelocity, double rotationSpeed) {
            super(entity, maxForce, maxVelocity, rotationSpeed);
        }
    }

    // ── Segment controller factory ────────────────────────────────────────

    private AiController createSegmentController(IscatWormSegment seg) {
        double force, maxVel, rotSpeed;
        switch (seg.getType()) {
            case HEAD -> {
                force = HEAD_FORCE;
                maxVel = HEAD_MAX_SPEED;
                rotSpeed = HEAD_ROTATION_SPEED;
            }
            case BODY -> {
                force = BODY_FOLLOW_FORCE;
                maxVel = HEAD_MAX_SPEED * 0.95;
                rotSpeed = 0.28;
            }
            default -> {
                force = TAIL_FOLLOW_FORCE;
                maxVel = HEAD_MAX_SPEED * 0.95;
                rotSpeed = 0.0;
            }
        }
        SegmentAiController ctrl = new SegmentAiController(seg, force, maxVel, rotSpeed);

        // Avoidance for all segments
        ctrl.addModifier(new ProjectileAvoidanceModifier());
        if (seg.getType() == IscatWormSegment.Type.HEAD) {
            ctrl.addModifier(new ObstacleAvoidanceModifier());
        }

        // Movement and attacks per type
        switch (seg.getType()) {
            case HEAD -> configureHead(ctrl);
            case BODY -> configureBody(ctrl);
            case TAIL -> configureTail(ctrl);
        }
        return ctrl;
    }

    private void configureHead(AiController ctrl) {
        ctrl.setMovementStrategy(new HeadMovement());
        ctrl.addAttack(new HeadPlungeAttack());
    }

    private void configureBody(AiController ctrl) {
        ctrl.setMovementStrategy(new FollowSegmentMovement());
    }

    private void configureTail(AiController ctrl) {
        ctrl.setMovementStrategy(new FollowSegmentMovement());
        ctrl.addAttack(new TailShooter());
    }

    // ── Movement strategies ──────────────────────────────────────────────

    private class HeadMovement implements MovementStrategy {
        private Vector2 chaseTarget = null;
        @Override
        public Vector2 computeDesiredVelocity(AbstractEntityModel entity, UniverseModel world, double dt) {
            IscatWormSegment head = (IscatWormSegment) entity;
            PlayerModel player = world.getPlayer();
            if (player == null) return new Vector2();

            boolean alone = activeCount() == 1;
            double speedMul = alone ? 3.0 : 1.0;
            double maxSpeed = HEAD_MAX_SPEED * speedMul;
            Vector2 headPos = head.getWorldCenter();
            Vector2 playerPos = player.getWorldCenter();

            if (chaseTarget == null || headPos.distanceSquared(chaseTarget) < (alone ? 0.2 : 1.5))
                chaseTarget = playerPos.copy();

            Vector2 toTarget = chaseTarget.copy().subtract(headPos);
            if (toTarget.getMagnitude() < 0.1) return new Vector2();
            return toTarget.getNormalized().multiply(maxSpeed);
        }
    }

    private class HeadPlungeAttack implements AttackBehavior {
        private final Cooldown cooldown = new Cooldown();
        @Override public double getPriority(AbstractEntityModel entity, UniverseModel world) {
            return cooldown.isCoolingDown() ? 0.0 : 90.0;
        }
        @Override public void execute(AbstractEntityModel entity, UniverseModel world, double dt) {
            PlayerModel player = world.getPlayer();
            if (player == null) return;
            Vector2 dir = player.getTransform().getTranslation().copy()
                    .subtract(entity.getTransform().getTranslation()).getNormalized();
            entity.applyImpulse(dir.multiply(HEAD_FORCE * 6.0));
            cooldown.start(HEAD_ATTACK_COOLDOWN);
        }
        @Override public void tick(AbstractEntityModel entity, UniverseModel world, double dt) {
            cooldown.update(dt);
        }
    }

    private class FollowSegmentMovement implements MovementStrategy {
        @Override
        public Vector2 computeDesiredVelocity(AbstractEntityModel entity, UniverseModel world, double dt) {
            IscatWormSegment seg = (IscatWormSegment) entity;
            IscatWormSegment prev = seg.getPreviousSegment();
            if (prev == null || prev.isConsumed()) return new Vector2();

            double scale = switch (seg.getType()) {
                case BODY -> BODY_SCALE;
                case TAIL -> TAIL_SCALE;
                default -> 1.0;
            };
            double desiredDist = uni.gaben.iscat.universe.UU.pxToM(FOLLOW_DISTANCE_PX * scale);
            Vector2 toPrev = prev.getWorldCenter().copy().subtract(seg.getWorldCenter());
            double dist = toPrev.getMagnitude();

            if (dist > desiredDist) {
                double excess = dist - desiredDist;
                return toPrev.getNormalized().multiply(HEAD_MAX_SPEED * (1.0 + excess * 12.0));
            }
            return new Vector2();
        }
    }

    private class TailShooter implements AttackBehavior {
        private final ShooterBehaviour shooter;
        TailShooter() {
            shooter = new ShooterBehaviour(
                    100.0,
                    TAIL_COMBAT_RANGE,
                    TAIL_FIRE_COOLDOWN,
                    ProjectileType.ENEMY_BULLET,
                    new RepeaterAttack(10, new MultiDirectionAttack(15, 0, new SingleShotAttack())),
                    new RepeaterAttack(10, new MultiDirectionAttack(10, 0, new SpreadAttack(3, 15)))
            );
        }
        @Override public double getPriority(AbstractEntityModel entity, UniverseModel world) {
            return activeCount() == 1 ? shooter.getPriority(entity, world) : 0.0;
        }
        @Override public void execute(AbstractEntityModel entity, UniverseModel world, double dt) {
            shooter.execute(entity, world, dt);
        }
        @Override public void tick(AbstractEntityModel entity, UniverseModel world, double dt) {
            shooter.tick(entity, world, dt);
        }
    }

    // ── Segment promotion ─────────────────────────────────────────────────

    private void handlePromotion(IscatWormSegment seg) {
        if (seg.getType() != IscatWormSegment.Type.BODY) return;
        IscatWormSegment prev = seg.getPreviousSegment();
        if (prev == null || !prev.isConsumed()) return;

        seg.promoteToHead();
        seg.setPreviousSegment(null);

        SegmentAiController newCtrl = new SegmentAiController(
                seg, HEAD_FORCE, HEAD_MAX_SPEED, HEAD_ROTATION_SPEED);
        newCtrl.addModifier(new ProjectileAvoidanceModifier());
        newCtrl.addModifier(new ObstacleAvoidanceModifier());
        newCtrl.setMovementStrategy(new HeadMovement());
        newCtrl.addAttack(new HeadPlungeAttack());
        controllers.put(seg, newCtrl);
    }

    private long activeCount() {
        return worm.getSegments().stream().filter(s -> !s.isConsumed()).count();
    }
}