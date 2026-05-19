package uni.gaben.iscat.game.universe.enemies.iscat_worm;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.game.lib.implementations.AiBehaviours;
import uni.gaben.iscat.game.lib.interfaces.controller.AiController;
import uni.gaben.iscat.game.lib.utils.UU;
import uni.gaben.iscat.game.universe.UniverseModel;
import uni.gaben.iscat.game.universe.player.PlayerModel;
import uni.gaben.iscat.utils.Interpolator;

public class IscatWormController implements AiController {

    private final IscatWormModel worm;

    public IscatWormController(IscatWormModel worm) {
        this.worm = worm;
    }

    @Override
    public void aiUpdate(UniverseModel universeModel, double dt) {
        PlayerModel player = universeModel.getPlayer();
        if (player == null) return;

        for (IscatWormSegment segment : worm.getSegments()) {
            if (segment.isConsumed()) continue;
            segment.updateCooldowns(dt);

            switch (segment.getType()) {
                case HEAD -> updateHead(segment, player, dt);
                case BODY -> updateBody(segment, player, dt);
                case TAIL -> updateTail(segment, dt);
            }
        }
    }

    private void updateHead(IscatWormSegment head, PlayerModel player, double dt) {
        Vector2 dir = dirTo(head, player.getTransform().getTranslation());
        double dist = dir.getMagnitude();

        // Attacco da contatto
        if (dist < UU.pxToM(IscatWormSettings.HEAD_ATTACK_RADIUS)) {
            if (head.canAttack()) {
                player.deltaToLife(-IscatWormSettings.HEAD_ATTACK_POWER);
                head.startAttackCooldown();
            }
            return;
        }

        // Inseguimento
        rotateTo(head, dir.getDirection(), dt, 0.18);
        if (head.getLinearVelocity().getMagnitude() < IscatWormSettings.HEAD_MAX_SPEED) {
            head.applyForce(dir.getNormalized().multiply(IscatWormSettings.HEAD_FORCE));
        }
    }

    private void updateBody(IscatWormSegment body, PlayerModel player, double dt) {
        IscatWormSegment prev = body.getPreviousSegment();

        // Promozione: il precedente è morto → diventa head
        if (prev != null && prev.isConsumed()) {
            body.promoteToHead();
            body.setPreviousSegment(null);
            updateHead(body, player, dt); // agisce già come testa questo tick
            return;
        }

        followSegment(body, prev, IscatWormSettings.BODY_FOLLOW_FORCE, dt);
    }

    private void updateTail(IscatWormSegment tail, double dt) {
        IscatWormSegment prev = tail.getPreviousSegment();
        followSegment(tail, prev, IscatWormSettings.TAIL_FOLLOW_FORCE, dt);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private void followSegment(IscatWormSegment me, IscatWormSegment target,
                               double force, double dt) {
        if (target == null || target.isConsumed()) return;

        Vector2 dir = dirTo(me, target.getPosition());
        double dist = dir.getMagnitude();
        double desired = UU.pxToM(IscatWormSettings.FOLLOW_DISTANCE_PX);

        if (dist > desired) {
            double excess = dist - desired;
            me.applyForce(dir.getNormalized().multiply(force * (1 + excess * 8)));
            rotateTo(me, dir.getDirection(), dt, 0.28);
        }

        // Clamp velocità
        Vector2 vel = me.getLinearVelocity();
        double maxSpeed = IscatWormSettings.HEAD_MAX_SPEED * 0.85;
        if (vel.getMagnitude() > maxSpeed) {
            me.setLinearVelocity(vel.getNormalized().multiply(maxSpeed));
        }
    }

    private Vector2 dirTo(IscatWormSegment from, Vector2 to) {
        return to.copy().subtract(from.getPosition());
    }

    private void rotateTo(IscatWormSegment seg, double targetAngle, double dt, double alpha) {
        double cur = seg.getRotation();
        double diff = targetAngle - cur;
        while (diff < -Math.PI) diff += Math.PI * 2;
        while (diff >  Math.PI) diff -= Math.PI * 2;
        seg.setRotation(Interpolator.smootherStep(cur, cur + diff, alpha));
    }
}