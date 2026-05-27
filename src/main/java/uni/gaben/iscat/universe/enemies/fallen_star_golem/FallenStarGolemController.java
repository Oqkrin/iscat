package uni.gaben.iscat.universe.enemies.fallen_star_golem;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.lib.implementations.AiBehaviours;
import uni.gaben.iscat.universe.lib.implementations.behaviors.core.MovementRequest;
import uni.gaben.iscat.universe.lib.implementations.behaviors.interfaces.MovementBehavior;
import uni.gaben.iscat.universe.lib.interfaces.controller.AiBehavior;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.player.PlayerModel;
import uni.gaben.iscat.universe.projectiles.Projectile;
import uni.gaben.iscat.universe.projectiles.ProjectileType;
import uni.gaben.iscat.universe.projectiles.Shooter;
import uni.gaben.iscat.utils.Cooldown;
import uni.gaben.iscat.utils.Interpolator;

import java.util.Random;

import static uni.gaben.iscat.universe.enemies.fallen_star_golem.FallenStarGolemSettings.FALLENSTARGOLEM;

public class FallenStarGolemController extends AiBehaviours<FallenStarGolemModel> {

    private Vector2 wanderTarget = null;
    private final Random rand = new Random();
    private final double maxMagnitude = 2, minMagnitude = 1;

    private final Shooter<FallenStarGolemModel> shooter;
    private final Projectile bulletTemplate;
    private final Cooldown fireCooldown = new Cooldown();

    private int spiralBulletsLeft = 0;
    private double spiralTimer = 0.0;
    private double currentSpiralAngle = 0.0;
    private static final double SPIRAL_DELAY = 0.05;
    private static final int NUM_SPIRAL_BULLETS = 12;
    private static final double SPIRAL_ANGLE_STEP = (2.0 * Math.PI) / NUM_SPIRAL_BULLETS;

    public FallenStarGolemController(FallenStarGolemModel golem) {
        super(golem, FALLENSTARGOLEM.force, FALLENSTARGOLEM.maxVelocity, FALLENSTARGOLEM.rotationSpeed);

        this.shooter = new Shooter<>(golem);
        this.bulletTemplate = new Projectile(ProjectileType.ENEMY_BULLET);

        // 1. PATTUGLIAMENTO (Wander) - Movement track
        addMovement(new MovementBehavior() {
            @Override
            public double getPriority(AbstractEntityModel npc, UniverseModel universe) {
                return 10.0;
            }

            @Override
            public MovementRequest computeRequest(AbstractEntityModel npc, UniverseModel universe, double dt) {
                return null;
            }

        });

        // 2. INSEGUIMENTO (Chase) - Movement track
        addMovement(new MovementBehavior() {
            @Override
            public double getPriority(AbstractEntityModel npc, UniverseModel universe) {
                PlayerModel player = universe.getPlayer();
                if (player == null) return 0.0;

                double dist = aiEntity.getTransform().getTranslation().distance(player.getTransform().getTranslation());
                if (dist > FALLENSTARGOLEM.combatRange && dist <= FALLENSTARGOLEM.detectionRange) {
                    return 50.0;
                }
                return 0.0;
            }

            @Override
            public MovementRequest computeRequest(AbstractEntityModel npc, UniverseModel universe, double dt) {
                return null;
            }

        });

        // 3. COMBATTIMENTO (Combat) - Movement track (Handles kiting AND triggers shooting logic)
        addMovement(new MovementBehavior() {
            @Override
            public double getPriority(AbstractEntityModel npc, UniverseModel universe) {
                PlayerModel player = universe.getPlayer();
                if (player == null) return 0.0;

                double dist = aiEntity.getTransform().getTranslation().distance(player.getTransform().getTranslation());
                if (dist <= FALLENSTARGOLEM.combatRange) {
                    return 80.0;
                }
                return 0.0;
            }

            @Override
            public MovementRequest computeRequest(AbstractEntityModel npc, UniverseModel universe, double dt) {
                return null;
            }

        });
    }

    @Override
    public void aiUpdate(UniverseModel universeModel, double dt) {
        if (aiEntity == null || aiEntity.shouldRemove()) return;

        fireCooldown.update(dt);

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

    private void updateWander(double dt) {
        if (wanderTarget == null) {
            double currentDir = aiEntity.getTransform().getRotationAngle();
            wanderTarget = Vector2.create(
                    minMagnitude + rand.nextDouble(maxMagnitude),
                    currentDir - 1.5 * Math.PI + rand.nextDouble(1.5 * Math.PI)
            );
        }

        aiEntity.applyForce(wanderTarget.getNormalized().multiply(FALLENSTARGOLEM.force));

        if (aiEntity.contains(wanderTarget)) {
            wanderTarget = null;
        }
    }

    private void updateChase(PlayerModel player, double dt) {
        wanderTarget = null;
        Vector2 toPlayer = directionToPlayer(player);
        aiEntity.applyForce(toPlayer.getNormalized().multiply(FALLENSTARGOLEM.force));
    }

    private void updateCombat(PlayerModel player, double dt) {
        wanderTarget = null;
        Vector2 toPlayer = directionToPlayer(player);
        double dist = toPlayer.getMagnitude();

        if (dist < FALLENSTARGOLEM.preferredRange) {
            aiEntity.applyForce(toPlayer.getNormalized().multiply(-FALLENSTARGOLEM.force * 0.6));
        } else if (dist > FALLENSTARGOLEM.preferredRange * 1.2) {
            aiEntity.applyForce(toPlayer.getNormalized().multiply(FALLENSTARGOLEM.force * 0.4));
        }

        rotateTo(toPlayer.getDirection(), dt);

        if (!fireCooldown.isCoolingDown()) {
            startSpiralBurst();
            fireCooldown.start(FALLENSTARGOLEM.fireCooldownS);
        }
    }

    private void startSpiralBurst() {
        spiralBulletsLeft = NUM_SPIRAL_BULLETS;
        spiralTimer = 0.0;
        currentSpiralAngle = rand.nextDouble() * Math.PI * 2.0;
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

    private void rotateTo(double targetAngle, double dt) {
        aiEntity.setAngularVelocity(0.0);

        double current = aiEntity.getTransform().getRotationAngle();
        double diff = targetAngle - current;

        while (diff < -Math.PI) diff += Math.PI * 2;
        while (diff > Math.PI)  diff -= Math.PI * 2;

        double next = Interpolator.lerp(
                current,
                current + diff,
                Math.min(FALLENSTARGOLEM.rotationSpeed * dt, 1.0)
        );

        aiEntity.getTransform().setRotation(next);
    }
}