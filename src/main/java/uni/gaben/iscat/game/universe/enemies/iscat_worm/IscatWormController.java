package uni.gaben.iscat.game.universe.enemies.iscat_worm;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.game.lib.interfaces.controller.AiController;
import uni.gaben.iscat.game.lib.utils.UU;
import uni.gaben.iscat.game.universe.UniverseModel;
import uni.gaben.iscat.game.universe.player.PlayerModel;
import uni.gaben.iscat.utils.Interpolator;

public class IscatWormController implements AiController {

    private final IscatWormModel worm;
    private Vector2 headTarget = null; // Mantiene il target della testa per l'inseguimento a scatti

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
                case HEAD -> updateHead(segment, player, universeModel, dt);
                case BODY -> updateBody(segment, player, dt);
                case TAIL -> updateTail(segment, dt);
            }
        }
    }

    private void updateHead(IscatWormSegment head, PlayerModel player, UniverseModel universe, double dt) {
        Vector2 headPos = head.getWorldCenter();
        Vector2 playerPos = player.getWorldCenter();

        // --- 1. LOGICA DI CALCOLO DEL TARGET (Ripristinata dal vecchio codice) ---
        // Se non ha un target o ha quasi raggiunto il precedente punto in cui si trovava il player, ricalcola
        if (headTarget == null || headPos.distanceSquared(headTarget) < 1.5) {
            headTarget = playerPos.copy();
        }

        Vector2 direction = headTarget.copy().subtract(headPos);
        double distanceToTarget = direction.getMagnitude();

        // Distanza REALE dal player per l'attacco
        double distanceToPlayer = playerPos.copy().subtract(headPos).getMagnitude();

        // --- 2. LOGICA DI ATTACCO (Priorità Massima) ---
        // Raggio calcolato come nel vecchio sistema a priorità
        double attackRadius = UU.pxToM(IscatWormSettings.DIM_SPRITE * IscatWormSettings.HEAD_SCALE) * 1.2;

        if (distanceToPlayer < attackRadius) {
            if (head.canAttack()) {
                player.deltaToLife(-IscatWormSettings.HEAD_ATTACK_POWER);
                head.startAttackCooldown();
            }
        }

        // --- 3. LOGICA DI MOVIMENTO ED INSEGUIMENTO ---
        if (distanceToTarget > 0.1) {
            // FIX: Ora usa la costante ad alta velocità definita nei settings!
            rotateTo(head, direction.getDirection(), dt, IscatWormSettings.HEAD_ROTATION_SPEED);

            // Sveglia forzata per evitare freeze fisici di dyn4j
            head.setAtRest(false);

            if (head.getLinearVelocity().getMagnitude() <= IscatWormSettings.HEAD_MAX_SPEED) {
                Vector2 force = direction.getNormalized().multiply(IscatWormSettings.HEAD_FORCE);
                head.applyForce(force);
            } else {
                Vector2 vel = head.getLinearVelocity();
                head.setLinearVelocity(vel.getNormalized().multiply(IscatWormSettings.HEAD_MAX_SPEED));
            }
        }
    }

    private void updateBody(IscatWormSegment body, PlayerModel player, double dt) {
        IscatWormSegment prev = body.getPreviousSegment();

        if (prev != null && prev.isConsumed()) {
            body.promoteToHead();
            body.setPreviousSegment(null);
            return;
        }

        followSegment(body, prev, IscatWormSettings.BODY_FOLLOW_FORCE, dt);
    }

    private void updateTail(IscatWormSegment tail, double dt) {
        IscatWormSegment prev = tail.getPreviousSegment();
        followSegment(tail, prev, IscatWormSettings.TAIL_FOLLOW_FORCE, dt);
    }

    private void followSegment(IscatWormSegment me, IscatWormSegment target, double force, double dt) {
        if (target == null || target.isConsumed()) return;

        Vector2 dir = target.getWorldCenter().copy().subtract(me.getWorldCenter());
        double dist = dir.getMagnitude();
        double desired = UU.pxToM(IscatWormSettings.FOLLOW_DISTANCE_PX);

        if (dist > desired) {
            double excess = dist - desired;
            me.setAtRest(false);
            me.applyForce(dir.getNormalized().multiply(force * (1.0 + excess * 8.0)));
            rotateTo(me, dir.getDirection(), dt, 0.28);
        }

        Vector2 vel = me.getLinearVelocity();
        double maxSpeed = IscatWormSettings.HEAD_MAX_SPEED * 0.85;
        if (vel.getMagnitude() > maxSpeed) {
            me.setLinearVelocity(vel.getNormalized().multiply(maxSpeed));
        }
    }

    private void rotateTo(IscatWormSegment seg, double targetAngle, double dt, double alpha) {
        double cur = seg.getRotation();
        double diff = targetAngle - cur;

        while (diff < -Math.PI) diff += Math.PI * 2;
        while (diff >  Math.PI) diff -= Math.PI * 2;

        seg.setRotation(Interpolator.smootherStep(cur, cur + diff, alpha));
    }
}