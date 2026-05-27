package uni.gaben.iscat.universe.enemies.mob;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.lib.implementations.AiBehaviours;
import uni.gaben.iscat.universe.lib.implementations.behaviors.core.MovementRequest;
import uni.gaben.iscat.universe.lib.implementations.behaviors.interfaces.MovementBehavior;
import uni.gaben.iscat.universe.lib.implementations.behaviors.movement.DodgeProjectileBehavior;
import uni.gaben.iscat.universe.lib.interfaces.controller.AiBehavior;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.player.PlayerModel;
import uni.gaben.iscat.universe.projectiles.Projectile;
import uni.gaben.iscat.universe.projectiles.ProjectileType;
import uni.gaben.iscat.universe.projectiles.Shooter;
import uni.gaben.iscat.utils.Cooldown;
import uni.gaben.iscat.utils.Interpolator;

import uni.gaben.iscat.universe.lib.implementations.behaviors.passive.SeparationBehavior;
import uni.gaben.iscat.universe.UU;

import java.util.Random;

import static uni.gaben.iscat.universe.enemies.mob.IscatMobSettings.ISCATMOB;

public class IscatMobController extends AiBehaviours<IscatMobModel> {

    private enum State { WANDER, CHASE, COMBAT }
    private State state = State.WANDER;

    private Vector2 wanderTarget = null;
    private final Random rand = new Random();
    private final double maxMagnitude = 2, minMagnitude = 1;

    private final Shooter<IscatMobModel> shooter;
    private final Projectile bulletTemplate;
    private final Cooldown fireCooldown = new Cooldown();

    public IscatMobController(IscatMobModel iscat) {
        super(iscat, ISCATMOB.force, ISCATMOB.maxVelocity, ISCATMOB.rotationSpeed);

        shooter = new Shooter<>(iscat);
        bulletTemplate = new Projectile(ProjectileType.ENEMY_BULLET);

        // Comportamento parallelo di separazione (Passive Track)
        this.addPassive(new SeparationBehavior(UU.pxToM(32.0), ISCATMOB.force * 0.8));

        this.addMovement(new DodgeProjectileBehavior(ISCATMOB.force * 1.5, ISCATMOB.combatRange,2.0));

        // 1. WANDER: Movement Track
        addMovement(new MovementBehavior() {
            @Override
            public double getPriority(AbstractEntityModel npc, UniverseModel universe) {
                return 10.0;
            }

            @Override
            public MovementRequest computeRequest(AbstractEntityModel npc, UniverseModel universe, double dt) {
                return updateWander(dt);
            }
        });

        // 2. CHASE: Movement Track
        addMovement(new MovementBehavior() {
            @Override
            public double getPriority(AbstractEntityModel npc, UniverseModel universe) {
                PlayerModel player = universe.getPlayer();
                if (player == null) return 0.0;
                double dist = aiEntity.getTransform().getTranslation().distance(player.getTransform().getTranslation());
                if (dist > ISCATMOB.combatRange && dist <= ISCATMOB.detectionRange) return 50.0;
                return 0.0;
            }

            @Override
            public MovementRequest computeRequest(AbstractEntityModel npc, UniverseModel universe, double dt) {
                return updateChase(universe.getPlayer(), dt);
            }

        });

        // 3. COMBAT: Both Tracks (Movement + Attack)
        add(new AiBehavior() {
            @Override
            public double getPriority(AbstractEntityModel npc, UniverseModel universe) {
                PlayerModel player = universe.getPlayer();
                if (player == null) return 0.0;
                double dist = aiEntity.getTransform().getTranslation().distance(player.getTransform().getTranslation());
                if (dist <= ISCATMOB.combatRange) return 80.0;
                return 0.0;
            }
            @Override
            public void execute(AbstractEntityModel npc, UniverseModel universe, double dt) {
                updateCombat(universe.getPlayer(), dt);
            }
        });
    }

    @Override
    public void aiUpdate(UniverseModel universeModel, double dt) {
        if (aiEntity == null || aiEntity.shouldRemove()) return;

        fireCooldown.update(dt);

        super.aiUpdate(universeModel, dt);
    }

    private MovementRequest updateWander(double dt) {
        if (wanderTarget == null) {
            double currentDir = aiEntity.getTransform().getRotationAngle();
            wanderTarget = Vector2.create(
                    minMagnitude + rand.nextDouble(maxMagnitude),
                    currentDir - 1.5 * Math.PI + rand.nextDouble(1.5 * Math.PI));
        }

        Vector2 lastWander = new Vector2(wanderTarget);
        if (aiEntity.contains(wanderTarget)) wanderTarget = null;
        return MovementRequest.of(lastWander.getNormalized().multiply(ISCATMOB.force), wanderTarget.getDirection());
    }

    private MovementRequest updateChase(PlayerModel player, double dt) {
        wanderTarget = null;
        Vector2 toPlayer = directionToPlayer(player);
        return MovementRequest.of(toPlayer.getNormalized().multiply(ISCATMOB.force),toPlayer.getDirection());
    }

    private void updateCombat(PlayerModel player, double dt) {
        wanderTarget = null;
        Vector2 toPlayer = directionToPlayer(player);
        double dist = toPlayer.getMagnitude();

        if (dist < ISCATMOB.preferredRange) {
            aiEntity.applyForce(toPlayer.getNormalized().multiply(-ISCATMOB.force * 0.6));
        } else if (dist > ISCATMOB.preferredRange * 1.2) {
            aiEntity.applyForce(toPlayer.getNormalized().multiply(ISCATMOB.force * 0.4));
        }

        rotateTo(toPlayer.getDirection(), dt);

        if (!fireCooldown.isCoolingDown()) {
            shooter.shoot(bulletTemplate);
            fireCooldown.start(ISCATMOB.fireCooldownS);
        }
    }

    private Vector2 directionToPlayer(PlayerModel player) {
        return player.getTransform().getTranslation()
                .copy()
                .subtract(aiEntity.getTransform().getTranslation());
    }

    private void rotateTo(double targetAngle, double dt) {
        aiEntity.setAngularVelocity(0.0);

        double current = aiEntity.getTransform().getRotationAngle();
        double diff = targetAngle - current;

        while (diff < -Math.PI) diff += Math.PI * 2;
        while (diff >  Math.PI) diff -= Math.PI * 2;

        double next = Interpolator.lerp(current, current + diff, Math.min(ISCATMOB.rotationSpeed * dt, 1.0));
        aiEntity.getTransform().setRotation(next);
    }
}