package uni.gaben.iscat.game.universe.enemies.iscat_worm;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.game.lib.interfaces.controller.AiController;
import uni.gaben.iscat.game.lib.utils.UU;
import uni.gaben.iscat.game.universe.UniverseModel;
import uni.gaben.iscat.game.universe.player.PlayerModel;
import uni.gaben.iscat.game.universe.projectiles.Shooter;
import uni.gaben.iscat.utils.Cooldown;
import uni.gaben.iscat.utils.Interpolator;

import java.util.Random;

public class IscatWormController implements AiController {

    private final IscatWormModel worm;
    private Vector2 headTarget = null;

    // ── LOGICA DI SPARO CHAOTIC BURST (CODA DA SOLA) ─────────────────────────
    private Shooter<IscatWormSegment> tailShooter = null;
    private final Cooldown fireCooldown = new Cooldown();
    private final Cooldown burstDelay = new Cooldown();
    private final Random rand = new Random();

    private int burstShotsRemaining = 0;

    public IscatWormController(IscatWormModel worm) {
        this.worm = worm;
    }

    @Override
    public void aiUpdate(UniverseModel universeModel, double dt) {
        PlayerModel player = universeModel.getPlayer();
        if (player == null) return;

        // 1. SCANSIONE STATO ATTUALE DEL VERME
        int activeSegments = 0;
        IscatWormSegment activeTail = null;

        for (IscatWormSegment segment : worm.getSegments()) {
            if (!segment.isConsumed()) {
                activeSegments++;
                if (segment.getType() == IscatWormSegment.Type.TAIL) {
                    activeTail = segment;
                }
            }
        }

        boolean isHeadAlone = (activeSegments == 1 && activeTail == null);
        boolean isTailAlone = (activeSegments == 1 && activeTail != null);

        // Aggiorna i timer balistici della Coda se rimasta isolata
        if (isTailAlone) {
            fireCooldown.update(dt);
            burstDelay.update(dt);
        }

        // 2. CICLO DI AGGIORNAMENTO COMPORTAMENTALE
        for (IscatWormSegment segment : worm.getSegments()) {
            if (segment.isConsumed()) continue;
            segment.updateCooldowns(dt);

            switch (segment.getType()) {
                case HEAD -> updateHead(segment, player, universeModel, dt, isHeadAlone);
                case BODY -> updateBody(segment, player, dt);
                case TAIL -> {
                    if (isTailAlone) {
                        updateTailEnraged(segment, player, dt);
                    } else {
                        updateTail(segment, dt);
                    }
                }
            }
        }
    }

    private void updateHead(IscatWormSegment head, PlayerModel player, UniverseModel universe, double dt, boolean speedBoost) {
        Vector2 headPos = head.getWorldCenter();
        Vector2 playerPos = player.getWorldCenter();

        double speedMultiplier = speedBoost ? 3.0 : 1.0;
        double currentMaxSpeed = IscatWormSettings.HEAD_MAX_SPEED * speedMultiplier;
        double currentForce = IscatWormSettings.HEAD_FORCE * speedMultiplier;
        double currentRotationSpeed = speedBoost ? 1.0 : IscatWormSettings.HEAD_ROTATION_SPEED;

        double targetPrecision = speedBoost ? 0.2 : 1.5;
        if (headTarget == null || headPos.distanceSquared(headTarget) < targetPrecision) {
            headTarget = playerPos.copy();
        }

        Vector2 direction = headTarget.copy().subtract(headPos);
        double distanceToTarget = direction.getMagnitude();
        double distanceToPlayer = playerPos.copy().subtract(headPos).getMagnitude();

        // Attacco Corpo a Corpo
        double attackRadius = UU.pxToM(IscatWormSettings.DIM_SPRITE * IscatWormSettings.HEAD_SCALE) * 1.2;
        if (distanceToPlayer < attackRadius) {
            if (head.canAttack()) {
                player.deltaToLife(-IscatWormSettings.HEAD_ATTACK_POWER);
                head.startAttackCooldown();
            }
        }

        // Spinta fisica ed inseguimento
        if (distanceToTarget > 0.1) {
            rotateTo(head, direction.getDirection(), dt, currentRotationSpeed);
            head.setAtRest(false);

            if (head.getLinearVelocity().getMagnitude() <= currentMaxSpeed) {
                head.applyForce(direction.getNormalized().multiply(currentForce));
            } else {
                Vector2 vel = head.getLinearVelocity();
                head.setLinearVelocity(vel.getNormalized().multiply(currentMaxSpeed));
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

    // ── UNICO ATTACCO: TEMPESTA RAPIDISSIMA A DIREZIONI CASUALI ──────────────
    private void updateTailEnraged(IscatWormSegment tail, PlayerModel player, double dt) {
        if (tailShooter == null) {
            tailShooter = new Shooter<>(tail);
        }

        Vector2 tailPos = tail.getWorldCenter();
        Vector2 playerPos = player.getWorldCenter();
        Vector2 toPlayer = playerPos.copy().subtract(tailPos);
        double distance = toPlayer.getMagnitude();
        double angleToPlayer = toPlayer.getDirection();

        tail.setAtRest(false);
        // La grafica della coda continua a guardare il player per coerenza visiva
        rotateTo(tail, angleToPlayer, dt, 0.40);

        // Kiting: mantiene le distanze dal player
        if (distance < 5.0) {
            tail.applyForce(toPlayer.getNormalized().multiply(-IscatWormSettings.TAIL_FOLLOW_FORCE * 0.8));
        } else if (distance > 9.0) {
            tail.applyForce(toPlayer.getNormalized().multiply(IscatWormSettings.TAIL_FOLLOW_FORCE * 0.5));
        }

        // 1. GESTIONE DELLA RAFFICA CONTINUA
        if (burstShotsRemaining > 0 && !burstDelay.isCoolingDown()) {
            // Genera una direzione completamente casuale (da 0 a 360 gradi in radianti)
            double randomAngle = rand.nextDouble() * 2 * Math.PI;

            executeRandomShot(tail, randomAngle);
            burstShotsRemaining--;

            // VELOCISSIMO: Solo 0.03 secondi di attesa tra un proiettile e l'altro
            burstDelay.start(0.03);
        }

        // 2. PREPARAZIONE IMMEDIATA DELLA RAFFICA SUCCESSIVA
        if (!fireCooldown.isCoolingDown() && burstShotsRemaining == 0) {
            // Prepara una scarica da 20 proiettili casuali distruttivi
            burstShotsRemaining = 20;
            burstDelay.start(0.0);

            // Pausa infinitesima tra le raffiche (0.2 secondi) per non dare tregua
            fireCooldown.start(0.2);
        }
    }

    // ── METODO DI SPARO RANDOMIZZATO ─────────────────────────────────────────
    private void executeRandomShot(IscatWormSegment tail, double randomAngle) {
        tailShooter.shoot(tail.getProjectile(), randomAngle);
    }

    private void followSegment(IscatWormSegment me, IscatWormSegment target, double force, double dt) {
        if (target == null || target.isConsumed()) return;

        Vector2 dir = target.getWorldCenter().copy().subtract(me.getWorldCenter());
        double dist = dir.getMagnitude();

        double currentScale = switch (me.getType()) {
            case HEAD -> IscatWormSettings.HEAD_SCALE;
            case BODY -> IscatWormSettings.BODY_SCALE;
            case TAIL -> IscatWormSettings.TAIL_SCALE;
        };

        double scaledDistancePx = IscatWormSettings.FOLLOW_DISTANCE_PX * currentScale;
        double desired = UU.pxToM(scaledDistancePx);

        if (dist > desired) {
            double excess = dist - desired;
            me.setAtRest(false);
            me.applyForce(dir.getNormalized().multiply(force * (1.0 + excess * 12.0)));
            rotateTo(me, dir.getDirection(), dt, 0.28);
        }

        Vector2 vel = me.getLinearVelocity();
        double maxSpeed = IscatWormSettings.HEAD_MAX_SPEED * 0.95;
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