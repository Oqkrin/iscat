package uni.gaben.iscat.universe.enemies.mob;

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
import uni.gaben.iscat.universe.projectiles.Projectile;
import uni.gaben.iscat.universe.projectiles.ProjectileType;
import uni.gaben.iscat.universe.projectiles.Shooter;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.utils.Cooldown;

import java.util.Random;

import static uni.gaben.iscat.universe.enemies.mob.IscatMobSettings.ISCATMOB;

public class IscatMobController extends AiController {

    // ── Wander state ────────────────────────────────────────────────────────
    private Vector2 wanderTarget = null;
    private final Random rand = new Random();
    private final double maxWanderDist = 2.0;
    private final double minWanderDist = 1.0;

    // ── Shooting ────────────────────────────────────────────────────────────
    private final Shooter<IscatMobModel> shooter;
    private final Projectile bulletTemplate;
    private final Cooldown fireCooldown = new Cooldown();

    // ── Movement strategy (selects behaviour each frame) ────────────────────
    private final MovementStrategy mobMovement = (entity, world, dt) -> {
        IscatMobModel mob = (IscatMobModel) entity;
        PlayerModel player = world.getPlayer();
        if (player == null) return new Vector2();

        double hpRatio = mob.getLife() / mob.getMaxLife();
        double dist = mob.getTransform().getTranslation()
                .distance(player.getTransform().getTranslation());
        Vector2 toPlayer = player.getTransform().getTranslation()
                .copy().subtract(mob.getTransform().getTranslation());

        // 1. Hide when low health
        if (hpRatio <= 0.3) {
            wanderTarget = null;
            return computeHideVelocity(mob, world, player);
        }

        // 2. Combat kiting inside range
        if (dist <= ISCATMOB.combatRange) {
            wanderTarget = null;
            return computeKiteVelocity(mob, toPlayer, dist);
        }

        // 3. Chase when outside combat but within detection
        if (dist <= ISCATMOB.detectionRange) {
            wanderTarget = null;
            return toPlayer.getNormalized().multiply(ISCATMOB.maxVelocity);
        }

        // 4. Wander as fallback
        return computeWanderVelocity(mob);
    };

    public IscatMobController(IscatMobModel iscat) {
        super(iscat, ISCATMOB.force, ISCATMOB.maxVelocity, ISCATMOB.rotationSpeed);

        shooter = new Shooter<>(iscat);
        bulletTemplate = new Projectile(ProjectileType.ENEMY_BULLET);

        // Movement strategy (defined above)
        setMovementStrategy(mobMovement);

        // Avoidance modifiers (order matters)
        addModifier(new SeparationModifier(UU.pxToM(32.0), ISCATMOB.force * 0.6));
        addModifier(new ObstacleAvoidanceModifier());
        addModifier(new ProjectileAvoidanceModifier());   // replaced DangerAvoidanceModifier

        // Attack
        addAttack(new AttackBehavior() {
            @Override
            public double getPriority(AbstractEntityModel npc, UniverseModel universe) {
                PlayerModel player = universe.getPlayer();
                if (player == null) return 0.0;
                double dist = npc.getTransform().getTranslation()
                        .distance(player.getTransform().getTranslation());
                return dist <= ISCATMOB.combatRange ? 80.0 : 0.0;
            }

            @Override
            public void execute(AbstractEntityModel npc, UniverseModel universe, double dt) {
                if (!fireCooldown.isCoolingDown()) {
                    shooter.shoot(bulletTemplate);
                    fireCooldown.start(ISCATMOB.fireCooldownS);
                }
            }

            @Override
            public void tick(AbstractEntityModel npc, UniverseModel universe, double dt) {
                fireCooldown.update(dt);
            }
        });
    }

    @Override
    public void update(UniverseModel universe, double dt) {
        if (entity == null || entity.shouldRemove()) return;
        super.update(universe, dt);
    }

    // ── Helper methods (exact logic from original, now returning velocities) ──

    private Vector2 computeHideVelocity(IscatMobModel mob, UniverseModel world, PlayerModel player) {
        // Find nearest ally (excluding healers) to hide behind
        AbstractEntityModel bestAlly = null;
        double bestDist = Double.MAX_VALUE;
        for (AbstractEntityModel e : world.getEntitiesOfType(AbstractEntityModel.class)) {
            if (e == mob || e == player) continue;
            if (e.getClass().getSimpleName().contains("Healer")) continue;
            double d = e.getTransform().getTranslation()
                    .distance(mob.getTransform().getTranslation());
            if (d < bestDist) {
                bestDist = d;
                bestAlly = e;
            }
        }
        if (bestAlly == null) {
            // No ally – flee directly away
            Vector2 away = mob.getTransform().getTranslation().copy()
                    .subtract(player.getTransform().getTranslation());
            return away.getNormalized().multiply(ISCATMOB.maxVelocity);
        }
        // Move to a point behind the ally relative to the player
        Vector2 allyPos = bestAlly.getTransform().getTranslation();
        Vector2 toAlly = allyPos.copy()
                .subtract(player.getTransform().getTranslation())
                .getNormalized();
        Vector2 target = allyPos.copy().add(toAlly.multiply(3.0));  // hideDistance
        Vector2 toTarget = target.copy().subtract(mob.getTransform().getTranslation());
        if (toTarget.getMagnitude() < 0.1) return new Vector2();
        return toTarget.getNormalized().multiply(ISCATMOB.maxVelocity);
    }

    private Vector2 computeKiteVelocity(IscatMobModel mob, Vector2 toPlayer, double dist) {
        if (dist < ISCATMOB.preferredRange) {
            // Too close → retreat
            return toPlayer.getNormalized().multiply(-ISCATMOB.maxVelocity * 0.6);
        } else if (dist > ISCATMOB.preferredRange * 1.2) {
            // Too far → advance slowly
            return toPlayer.getNormalized().multiply(ISCATMOB.maxVelocity * 0.4);
        } else {
            // In sweet spot → strafe perpendicular
            Vector2 perp = new Vector2(-toPlayer.y, toPlayer.x);
            if (rand.nextBoolean()) perp.negate();
            return perp.getNormalized().multiply(ISCATMOB.maxVelocity * 0.5);
        }
    }

    private Vector2 computeWanderVelocity(IscatMobModel mob) {
        if (wanderTarget == null) {
            double currentDir = mob.getTransform().getRotationAngle();
            wanderTarget = Vector2.create(
                    minWanderDist + rand.nextDouble() * (maxWanderDist - minWanderDist),
                    currentDir - 1.5 * Math.PI + rand.nextDouble() * 3 * Math.PI
            );
        }
        Vector2 dir = wanderTarget.copy();
        if (mob.contains(wanderTarget)) {
            wanderTarget = null;
        }
        return dir.getNormalized().multiply(ISCATMOB.maxVelocity * 0.4);
    }
}