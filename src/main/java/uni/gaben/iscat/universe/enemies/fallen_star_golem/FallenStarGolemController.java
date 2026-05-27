package uni.gaben.iscat.universe.enemies.fallen_star_golem;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.lib.implementations.AiBehaviours;
import uni.gaben.iscat.universe.lib.implementations.behaviors.core.MovementRequest;
import uni.gaben.iscat.universe.lib.implementations.behaviors.interfaces.AttackBehavior;
import uni.gaben.iscat.universe.lib.implementations.behaviors.interfaces.MovementBehavior;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.player.PlayerModel;
import uni.gaben.iscat.universe.projectiles.Projectile;
import uni.gaben.iscat.universe.projectiles.ProjectileType;
import uni.gaben.iscat.universe.projectiles.Shooter;
import uni.gaben.iscat.utils.Cooldown;

import java.util.Random;

import static uni.gaben.iscat.universe.enemies.fallen_star_golem.FallenStarGolemSettings.FALLENSTARGOLEM;

/**
 * BUG 2 FIXED: The three anonymous MovementBehavior classes all had
 * {@code computeRequest} returning {@code null}.  SteeringController treats
 * {@code null} as "nothing to do", so the Golem never moved.
 * Also, the spiral-burst trigger inside updateCombat was never called because
 * the old code path was dead — now extracted to a dedicated AttackBehavior.
 */
public class FallenStarGolemController extends AiBehaviours<FallenStarGolemModel> {

    private Vector2 wanderTarget = null;
    private final Random rand = new Random();
    private final double maxMagnitude = 2, minMagnitude = 1;

    private final Shooter<FallenStarGolemModel> shooter;
    private final Projectile bulletTemplate;
    private final Cooldown fireCooldown = new Cooldown();

    private int    spiralBulletsLeft   = 0;
    private double spiralTimer         = 0.0;
    private double currentSpiralAngle  = 0.0;

    private static final double SPIRAL_DELAY       = 0.05;
    private static final int    NUM_SPIRAL_BULLETS  = 12;
    private static final double SPIRAL_ANGLE_STEP   = (2.0 * Math.PI) / NUM_SPIRAL_BULLETS;

    public FallenStarGolemController(FallenStarGolemModel golem) {
        super(golem, FALLENSTARGOLEM.force, FALLENSTARGOLEM.maxVelocity, FALLENSTARGOLEM.rotationSpeed);

        this.shooter       = new Shooter<>(golem);
        this.bulletTemplate = new Projectile(ProjectileType.ENEMY_BULLET);

        // ── MOVEMENT TRACK ────────────────────────────────────────────────────

        // 1. WANDER – low-priority fallback movement
        addMovement(new MovementBehavior() {
            @Override
            public double getPriority(AbstractEntityModel npc, UniverseModel universe) {
                return 10.0;
            }
            @Override
            public MovementRequest computeRequest(AbstractEntityModel npc, UniverseModel universe, double dt) {
                // FIX: was returning null — wired to actual wander logic
                if (wanderTarget == null) {
                    double dir = aiEntity.getTransform().getRotationAngle();
                    wanderTarget = Vector2.create(
                            minMagnitude + rand.nextDouble(maxMagnitude),
                            dir - 1.5 * Math.PI + rand.nextDouble(1.5 * Math.PI));
                }
                if (aiEntity.contains(wanderTarget)) {
                    wanderTarget = null;
                    return MovementRequest.idle();
                }
                Vector2 desired = wanderTarget.copy().getNormalized()
                                               .multiply(FALLENSTARGOLEM.maxVelocity);
                return MovementRequest.of(desired, wanderTarget.getDirection());
            }
        });

        // 2. CHASE – medium priority, move toward player outside combat range
        addMovement(new MovementBehavior() {
            @Override
            public double getPriority(AbstractEntityModel npc, UniverseModel universe) {
                PlayerModel player = universe.getPlayer();
                if (player == null) return 0.0;
                double dist = aiEntity.getTransform().getTranslation()
                                      .distance(player.getTransform().getTranslation());
                return (dist > FALLENSTARGOLEM.combatRange && dist <= FALLENSTARGOLEM.detectionRange)
                        ? 50.0 : 0.0;
            }
            @Override
            public MovementRequest computeRequest(AbstractEntityModel npc, UniverseModel universe, double dt) {
                // FIX: was returning null
                wanderTarget = null;
                PlayerModel player = universe.getPlayer();
                if (player == null) return MovementRequest.idle();
                Vector2 toPlayer = directionToPlayer(player);
                Vector2 desired  = toPlayer.getNormalized().multiply(FALLENSTARGOLEM.maxVelocity);
                return MovementRequest.of(desired, toPlayer.getDirection());
            }
        });

        // 3. COMBAT – high priority kiting movement (shooting is the AttackBehavior below)
        addMovement(new MovementBehavior() {
            @Override
            public double getPriority(AbstractEntityModel npc, UniverseModel universe) {
                PlayerModel player = universe.getPlayer();
                if (player == null) return 0.0;
                double dist = aiEntity.getTransform().getTranslation()
                                      .distance(player.getTransform().getTranslation());
                return dist <= FALLENSTARGOLEM.combatRange ? 80.0 : 0.0;
            }
            @Override
            public MovementRequest computeRequest(AbstractEntityModel npc, UniverseModel universe, double dt) {
                // FIX: was returning null — now handles kiting logic
                wanderTarget = null;
                PlayerModel player = universe.getPlayer();
                if (player == null) return MovementRequest.idle();

                Vector2 toPlayer = directionToPlayer(player);
                double  dist     = toPlayer.getMagnitude();

                Vector2 desiredVelocity = null;
                if (dist < FALLENSTARGOLEM.preferredRange) {
                    desiredVelocity = toPlayer.getNormalized().multiply(-FALLENSTARGOLEM.maxVelocity * 0.6);
                } else if (dist > FALLENSTARGOLEM.preferredRange * 1.2) {
                    desiredVelocity = toPlayer.getNormalized().multiply(FALLENSTARGOLEM.maxVelocity * 0.4);
                }
                // Always face the player during combat (rotation returned even when kiting is idle)
                return MovementRequest.of(desiredVelocity, toPlayer.getDirection());
            }
        });

        // ── ATTACK TRACK ─────────────────────────────────────────────────────
        // FIX: spiral trigger was inside the dead computeRequest path — now its own AttackBehavior
        addAttack(new AttackBehavior() {
            @Override
            public double getPriority(AbstractEntityModel npc, UniverseModel universe) {
                PlayerModel player = universe.getPlayer();
                if (player == null) return 0.0;
                double dist = aiEntity.getTransform().getTranslation()
                                      .distance(player.getTransform().getTranslation());
                return (dist <= FALLENSTARGOLEM.combatRange && !fireCooldown.isCoolingDown())
                        ? 80.0 : 0.0;
            }
            @Override
            public void execute(AbstractEntityModel npc, UniverseModel universe, double dt) {
                startSpiralBurst();
                fireCooldown.start(FALLENSTARGOLEM.fireCooldownS);
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

        // Sequential spiral spawning — runs every frame, independent of behavior tracks
        if (spiralBulletsLeft > 0) {
            spiralTimer += dt;
            if (spiralTimer >= SPIRAL_DELAY) {
                spiralTimer -= SPIRAL_DELAY;
                spawnSpiralBullet();
                spiralBulletsLeft--;
            }
        }

        super.aiUpdate(universeModel, dt);
    }

    // ─────────────────────────────────────────────────────────────────────────

    private void startSpiralBurst() {
        spiralBulletsLeft   = NUM_SPIRAL_BULLETS;
        spiralTimer         = 0.0;
        currentSpiralAngle  = rand.nextDouble() * Math.PI * 2.0;
    }

    private void spawnSpiralBullet() {
        shooter.shoot(bulletTemplate, currentSpiralAngle);
        currentSpiralAngle += SPIRAL_ANGLE_STEP;
    }

    private Vector2 directionToPlayer(PlayerModel player) {
        return player.getTransform().getTranslation()
                     .copy()
                     .subtract(aiEntity.getTransform().getTranslation());
    }
}
