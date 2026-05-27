package uni.gaben.iscat.universe.enemies.worm;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.lib.implementations.AiBehaviours;
import uni.gaben.iscat.universe.lib.implementations.attacks.MultiDirectionAttack;
import uni.gaben.iscat.universe.lib.implementations.attacks.RepeaterAttack;
import uni.gaben.iscat.universe.lib.implementations.attacks.SingleShotAttack;
import uni.gaben.iscat.universe.lib.implementations.attacks.SpreadAttack;
import uni.gaben.iscat.universe.lib.implementations.behaviors.attack.PlungeAttackBehavior;
import uni.gaben.iscat.universe.lib.implementations.behaviors.attack.ShooterBehaviour;
import uni.gaben.iscat.universe.lib.implementations.behaviors.interfaces.MovementBehavior;
import uni.gaben.iscat.universe.lib.implementations.behaviors.interfaces.PassiveBehavior;
import uni.gaben.iscat.universe.lib.interfaces.controller.AiBehavior;
import uni.gaben.iscat.universe.lib.interfaces.controller.AiController;
import uni.gaben.iscat.universe.player.PlayerModel;
import uni.gaben.iscat.universe.projectiles.ProjectileType;
import uni.gaben.iscat.utils.Interpolator;

import java.util.LinkedHashMap;
import java.util.Map;

public class IscatWormController implements AiController {

    private final IscatWormModel worm;
    private final Map<IscatWormSegment, AiBehaviours<IscatWormSegment>> controllers = new LinkedHashMap<>();

    public IscatWormController(IscatWormModel worm) {
        this.worm = worm;
        for (IscatWormSegment seg : worm.getSegments()) {
            controllers.put(seg, buildController(seg));
        }
    }

    private AiBehaviours<IscatWormSegment> buildController(IscatWormSegment seg) {
        double force = seg.getType() == IscatWormSegment.Type.HEAD ? IscatWormSettings.HEAD_FORCE :
                (seg.getType() == IscatWormSegment.Type.BODY ? IscatWormSettings.BODY_FOLLOW_FORCE : IscatWormSettings.TAIL_FOLLOW_FORCE);
        double maxSpeed = IscatWormSettings.HEAD_MAX_SPEED;
        double rotSpeed = IscatWormSettings.HEAD_ROTATION_SPEED;

        AiBehaviours<IscatWormSegment> ctrl = new AiBehaviours<>(seg, force, maxSpeed, rotSpeed);

        switch (seg.getType()) {
            case HEAD -> {
                ctrl.add(headBehavior());
            }
            case BODY -> ctrl.add(followBehavior());
            case TAIL -> {
                ctrl.add(followBehavior());
                ctrl.add(lockRotationBehavior());
                ctrl.addAttack(new ShooterBehaviour(
                        80.0,
                        IscatWormSettings.TAIL_COMBAT_RANGE,
                        IscatWormSettings.TAIL_FIRE_COOLDOWN,
                        ProjectileType.ENEMY_BULLET,
                        new RepeaterAttack(10,new MultiDirectionAttack(15, 0, new SingleShotAttack())),
                        new RepeaterAttack(10,new MultiDirectionAttack(10, 0, new SpreadAttack(3,15)))
                ) {
                    @Override
                    public double getPriority(AbstractEntityModel npc, UniverseModel universe) {
                        return activeCount() == 1 ? super.getPriority(npc, universe) : 0.0;
                    }
                });
            }
        }
        return ctrl;
    }

    @Override
    public void aiUpdate(UniverseModel universe, double dt) {
        for (IscatWormSegment seg : worm.getSegments()) {
            if (seg.isConsumed()) continue;
            seg.updateCooldowns(dt);
            handlePromotion(seg);
            controllers.get(seg).aiUpdate(universe, dt);
        }
    }

    private void handlePromotion(IscatWormSegment seg) {
        if (seg.getType() != IscatWormSegment.Type.BODY) return;
        IscatWormSegment prev = seg.getPreviousSegment();
        if (prev == null || !prev.isConsumed()) return;

        seg.promoteToHead();
        seg.setPreviousSegment(null);

        AiBehaviours<IscatWormSegment> ctrl = new AiBehaviours<>(seg, IscatWormSettings.HEAD_FORCE, IscatWormSettings.HEAD_MAX_SPEED, IscatWormSettings.HEAD_ROTATION_SPEED);
        ctrl.add(new PlungeAttackBehavior(12.0, IscatWormSettings.HEAD_FORCE * 6.0, 3.0, 1.0));
        ctrl.add(headBehavior());
        controllers.put(seg, ctrl);
    }

    // Behaviors

    private AiBehavior headBehavior() {
        return new AiBehavior() {
            Vector2 headTarget = null;

            @Override
            public double getPriority(AbstractEntityModel npc, UniverseModel universe) {
                return universe.getPlayer() != null ? 50.0 : 0.0;
            }

            @Override
            public void execute(AbstractEntityModel npc, UniverseModel universe, double dt) {
                IscatWormSegment head = (IscatWormSegment) npc;
                PlayerModel player = universe.getPlayer();
                if (player == null) return;

                boolean isAlone = activeCount() == 1;
                double speedMul = isAlone ? 3.0 : 1.0;
                double maxSpeed = IscatWormSettings.HEAD_MAX_SPEED * speedMul;
                double force    = IscatWormSettings.HEAD_FORCE * speedMul;
                double rotSpeed = isAlone ? 1.0 : IscatWormSettings.HEAD_ROTATION_SPEED;
                double precision = isAlone ? 0.2 : 1.5;

                Vector2 headPos   = head.getWorldCenter();
                Vector2 playerPos = player.getWorldCenter();

                if (headTarget == null || headPos.distanceSquared(headTarget) < precision)
                    headTarget = playerPos.copy();

                Vector2 dir = headTarget.copy().subtract(headPos);
                if (dir.getMagnitude() < 0.1) return;

                rotateTo(head, dir.getDirection(), dt, rotSpeed);
                head.setAtRest(false);

                if (head.getLinearVelocity().getMagnitude() <= maxSpeed)
                    head.applyForce(dir.getNormalized().multiply(force));
                else
                    head.setLinearVelocity(head.getLinearVelocity().getNormalized().multiply(maxSpeed));
            }
        };
    }

    private AiBehavior followBehavior() {
        return new AiBehavior() {
            @Override
            public double getPriority(AbstractEntityModel npc, UniverseModel universe) {
                IscatWormSegment seg = (IscatWormSegment) npc;
                IscatWormSegment prev = seg.getPreviousSegment();
                return (prev != null && !prev.isConsumed()) ? 50.0 : 0.0;
            }

            @Override
            public void execute(AbstractEntityModel npc, UniverseModel universe, double dt) {
                IscatWormSegment seg  = (IscatWormSegment) npc;
                IscatWormSegment prev = seg.getPreviousSegment();
                if (prev == null || prev.isConsumed()) return;

                double force = seg.getType() == IscatWormSegment.Type.BODY
                        ? IscatWormSettings.BODY_FOLLOW_FORCE
                        : IscatWormSettings.TAIL_FOLLOW_FORCE;

                Vector2 dir  = prev.getWorldCenter().copy().subtract(seg.getWorldCenter());
                double  dist = dir.getMagnitude();
                double  scale = switch (seg.getType()) {
                    case HEAD -> IscatWormSettings.HEAD_SCALE;
                    case BODY -> IscatWormSettings.BODY_SCALE;
                    case TAIL -> IscatWormSettings.TAIL_SCALE;
                };
                double desired = uni.gaben.iscat.universe.UU.pxToM(IscatWormSettings.FOLLOW_DISTANCE_PX * scale);

                if (dist > desired) {
                    double excess = dist - desired;
                    seg.setAtRest(false);
                    seg.applyForce(dir.getNormalized().multiply(force * (1.0 + excess * 12.0)));
                    rotateTo(seg, dir.getDirection(), dt, 0.28);
                }

                Vector2 vel = seg.getLinearVelocity();
                double maxSpeed = IscatWormSettings.HEAD_MAX_SPEED * 0.95;
                if (vel.getMagnitude() > maxSpeed)
                    seg.setLinearVelocity(vel.getNormalized().multiply(maxSpeed));
            }
        };
    }

    private AiBehavior lockRotationBehavior() {
        return new AiBehavior() {
            @Override public double getPriority(AbstractEntityModel npc, UniverseModel universe) { return -1.0; }
            @Override public void execute(AbstractEntityModel npc, UniverseModel universe, double dt) {
                npc.setAngularVelocity(0);
                npc.getTransform().setRotation(0);
            }
        };
    }

    private long activeCount() {
        return worm.getSegments().stream().filter(s -> !s.isConsumed()).count();
    }

    private static void rotateTo(IscatWormSegment seg, double targetAngle, double dt, double alpha) {
        double cur  = seg.getRotation();
        double diff = targetAngle - cur;
        while (diff < -Math.PI) diff += Math.PI * 2;
        while (diff >  Math.PI) diff -= Math.PI * 2;
        seg.setRotation(Interpolator.smootherStep(cur, cur + diff, alpha));
    }
}