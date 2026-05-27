package uni.gaben.iscat.universe.enemies.mob;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.lib.implementations.AiBehaviours;
import uni.gaben.iscat.universe.lib.implementations.behaviors.core.MovementRequest;
import uni.gaben.iscat.universe.lib.implementations.behaviors.interfaces.AttackBehavior;
import uni.gaben.iscat.universe.lib.implementations.behaviors.interfaces.MovementBehavior;
import uni.gaben.iscat.universe.lib.implementations.behaviors.movement.DodgeProjectileBehavior;
import uni.gaben.iscat.universe.lib.implementations.behaviors.passive.SeparationBehavior;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.player.PlayerModel;
import uni.gaben.iscat.universe.projectiles.Projectile;
import uni.gaben.iscat.universe.projectiles.ProjectileType;
import uni.gaben.iscat.universe.projectiles.Shooter;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.utils.Cooldown;

import java.util.Random;

import static uni.gaben.iscat.universe.enemies.mob.IscatMobSettings.ISCATMOB;

/**
 * BUG 3a FIXED: The COMBAT state used {@code add(new AiBehavior(){...})}.
 * {@code AiBehavior} does not implement {@code MovementBehavior}, {@code AttackBehavior},
 * or {@code PassiveBehavior}, so {@code add()} silently ignored it.
 * The NPC wandered and chased but never entered combat.
 * FIX: split COMBAT into a MovementBehavior (kiting) + AttackBehavior (shooting).
 *
 * BUG 3b FIXED: {@code updateWander} set {@code wanderTarget = null} then immediately
 * called {@code wanderTarget.getDirection()} → NullPointerException every frame.
 * FIX: capture direction from the non-null target before clearing it.
 */
public class IscatMobController extends AiBehaviours<IscatMobModel> {

    private Vector2 wanderTarget = null;
    private final Random rand = new Random();
    private final double maxMagnitude = 2, minMagnitude = 1;

    private final Shooter<IscatMobModel> shooter;
    private final Projectile             bulletTemplate;
    private final Cooldown               fireCooldown = new Cooldown();

    public IscatMobController(IscatMobModel iscat) {
        super(iscat, ISCATMOB.force, ISCATMOB.maxVelocity, ISCATMOB.rotationSpeed);

        shooter        = new Shooter<>(iscat);
        bulletTemplate = new Projectile(ProjectileType.ENEMY_BULLET);

        // ── PASSIVE ──────────────────────────────────────────────────────────
        addPassive(new SeparationBehavior(UU.pxToM(32.0), ISCATMOB.force * 0.8));

        // ── MOVEMENT TRACK ────────────────────────────────────────────────────

        addMovement(new DodgeProjectileBehavior(ISCATMOB.force * 1.5, ISCATMOB.combatRange, 2.0));

        // 1. WANDER
        addMovement(new MovementBehavior() {
            @Override public double getPriority(AbstractEntityModel npc, UniverseModel universe) { return 10.0; }
            @Override public MovementRequest computeRequest(AbstractEntityModel npc, UniverseModel universe, double dt) {
                return updateWander();
            }
        });

        // 2. CHASE
        addMovement(new MovementBehavior() {
            @Override
            public double getPriority(AbstractEntityModel npc, UniverseModel universe) {
                PlayerModel player = universe.getPlayer();
                if (player == null) return 0.0;
                double dist = aiEntity.getTransform().getTranslation()
                                      .distance(player.getTransform().getTranslation());
                return (dist > ISCATMOB.combatRange && dist <= ISCATMOB.detectionRange) ? 50.0 : 0.0;
            }
            @Override
            public MovementRequest computeRequest(AbstractEntityModel npc, UniverseModel universe, double dt) {
                wanderTarget = null;
                PlayerModel player = universe.getPlayer();
                if (player == null) return MovementRequest.idle();
                Vector2 toPlayer = directionToPlayer(player);
                return MovementRequest.of(
                        toPlayer.getNormalized().multiply(ISCATMOB.maxVelocity),
                        toPlayer.getDirection());
            }
        });

        // 3a. COMBAT MOVEMENT — kiting (maintain preferred range, face player)
        // FIX BUG 3a: was add(new AiBehavior()) — now properly typed
        addMovement(new MovementBehavior() {
            @Override
            public double getPriority(AbstractEntityModel npc, UniverseModel universe) {
                PlayerModel player = universe.getPlayer();
                if (player == null) return 0.0;
                double dist = aiEntity.getTransform().getTranslation()
                                      .distance(player.getTransform().getTranslation());
                return dist <= ISCATMOB.combatRange ? 80.0 : 0.0;
            }
            @Override
            public MovementRequest computeRequest(AbstractEntityModel npc, UniverseModel universe, double dt) {
                wanderTarget = null;
                PlayerModel player = universe.getPlayer();
                if (player == null) return MovementRequest.idle();

                Vector2 toPlayer = directionToPlayer(player);
                double  dist     = toPlayer.getMagnitude();

                Vector2 desiredVelocity = null;
                if (dist < ISCATMOB.preferredRange) {
                    desiredVelocity = toPlayer.getNormalized().multiply(-ISCATMOB.maxVelocity * 0.6);
                } else if (dist > ISCATMOB.preferredRange * 1.2) {
                    desiredVelocity = toPlayer.getNormalized().multiply(ISCATMOB.maxVelocity * 0.4);
                }
                // Always face player during combat even when holding range
                return MovementRequest.of(desiredVelocity, toPlayer.getDirection());
            }
        });

        // 3b. COMBAT ATTACK — fire when in combat range
        // FIX BUG 3a: shooting is now an AttackBehavior, runs independently of movement
        addAttack(new AttackBehavior() {
            @Override
            public double getPriority(AbstractEntityModel npc, UniverseModel universe) {
                PlayerModel player = universe.getPlayer();
                if (player == null) return 0.0;
                double dist = aiEntity.getTransform().getTranslation()
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
    public void aiUpdate(UniverseModel universeModel, double dt) {
        if (aiEntity == null || aiEntity.shouldRemove()) return;
        super.aiUpdate(universeModel, dt);
    }

    // ─────────────────────────────────────────────────────────────────────────

    private MovementRequest updateWander() {
        if (wanderTarget == null) {
            double currentDir = aiEntity.getTransform().getRotationAngle();
            wanderTarget = Vector2.create(
                    minMagnitude + rand.nextDouble(maxMagnitude),
                    currentDir - 1.5 * Math.PI + rand.nextDouble(1.5 * Math.PI));
        }

        // FIX BUG 3b: capture direction BEFORE potentially nulling the target
        Vector2 dir = wanderTarget.copy();
        double  rot = wanderTarget.getDirection(); // safe — wanderTarget is non-null here
        if (aiEntity.contains(wanderTarget)) wanderTarget = null;

        return MovementRequest.of(dir.getNormalized().multiply(ISCATMOB.maxVelocity), rot);
    }

    private Vector2 directionToPlayer(PlayerModel player) {
        return player.getTransform().getTranslation()
                     .copy()
                     .subtract(aiEntity.getTransform().getTranslation());
    }
}
