package uni.gaben.iscat.universe.lib.abstracts;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.lib.interfaces.model.AttackPattern;
import uni.gaben.iscat.universe.projectiles.Projectile;
import uni.gaben.iscat.universe.projectiles.ProjectileType;
import uni.gaben.iscat.universe.projectiles.Shooter;
import uni.gaben.iscat.utils.Cooldown;

import java.util.function.Consumer;

/**
 * Core AI controller that separates movement decision from attack execution.
 * <p>
 * Subclasses only need to implement {@link #decideMovement(UniverseModel, double)}
 * to return the desired velocity vector. The controller automatically applies
 * steering forces, limits speed, and handles rotation facing.
 * </p>
 * <p>
 * Attack logic is managed via {@link AttackPattern}s. Call {@link #(AttackPattern)}
 * or {@link #shoot(ProjectileType, double)} to fire projectiles.
 * </p>
 *
 * @param <T> the entity model type (must extend AbstractEntityModel)
 */
public abstract class AbstractBrainController<T extends AbstractEntityModel> {

    protected final T entity;
    protected final Shooter<T> shooter;
    protected final double maxForce;
    protected final double maxVelocity;
    protected final double rotationSpeed;

    // Optional: cooldown for generic attacks (can be ignored if per-pattern)
    protected final Cooldown globalAttackCooldown = new Cooldown();

    /**
     * Creates a new AI controller.
     *
     * @param entity         the entity being controlled
     * @param maxForce       maximum steering force (N)
     * @param maxVelocity    maximum speed (m/s)
     * @param rotationSpeed  rotation speed (radians per second), 0 = no automatic facing
     */
    public AbstractBrainController(T entity, double maxForce, double maxVelocity, double rotationSpeed) {
        this.entity = entity;
        this.maxForce = maxForce;
        this.maxVelocity = maxVelocity;
        this.rotationSpeed = rotationSpeed;
        this.shooter = new Shooter<>(entity);
    }

    // ------------------------------------------------------------------------
    // Core update loop
    // ------------------------------------------------------------------------

    /**
     * Called every frame. Updates movement and attacks.
     *
     * @param world the game world
     * @param dt    time step in seconds
     */
    public void update(UniverseModel world, double dt) {
        if (entity.shouldRemove()) return;

        // 1. Movement: compute desired velocity and apply steering
        Vector2 desired = decideMovement(world, dt);
        if (desired != null) {
            applySteering(desired, dt);
        }

        // 2. Automatic facing (optional)
        if (rotationSpeed > 0 && desired != null && desired.getMagnitudeSquared() > 0.01) {
            faceDirection(desired.getDirection(), dt);
        }

        // 3. Attacks (implemented by subclasses via queue or explicit calls)
        updateAttacks(world, dt);
    }

    /**
     * Movement decision – to be implemented by each enemy.
     * Return the desired velocity vector (m/s) in world coordinates.
     *
     * @param world the game world
     * @param dt    time step
     * @return desired velocity, or null/zero for no movement
     */
    protected abstract Vector2 decideMovement(UniverseModel world, double dt);

    /**
     * Override this to handle attack triggering each frame.
     * Default implementation does nothing.
     */
    protected void updateAttacks(UniverseModel world, double dt) {
        // subclasses can implement attack logic here
    }

    // ------------------------------------------------------------------------
    // Steering helpers
    // ------------------------------------------------------------------------

    private void applySteering(Vector2 desired, double dt) {
        Vector2 currentVel = entity.getLinearVelocity();
        Vector2 steering = desired.copy().subtract(currentVel);
        double mag = steering.getMagnitude();
        if (mag > maxForce) {
            steering.multiply(maxForce / mag);
        }
        entity.applyForce(steering);
    }

    private void faceDirection(double targetAngle, double dt) {
        double current = entity.getTransform().getRotationAngle();
        double diff = targetAngle - current;
        while (diff < -Math.PI) diff += 2 * Math.PI;
        while (diff > Math.PI) diff -= 2 * Math.PI;
        double step = rotationSpeed * dt;
        if (Math.abs(diff) < step) step = Math.abs(diff);
        entity.getTransform().setRotation(current + Math.signum(diff) * step);
    }

    // ------------------------------------------------------------------------
    // Shooting API
    // ------------------------------------------------------------------------

    /**
     * Shoots a single projectile in the given direction.
     *
     * @param type  projectile type
     * @param angle direction in radians
     */
    protected void shoot(ProjectileType type, double angle) {
        shooter.shoot(new Projectile(type), angle);
    }

    /**
     * Shoots a single projectile with a customiser lambda.
     *
     * @param type       projectile type
     * @param angle      direction in radians
     * @param customizer callback to modify the projectile before spawn
     */
    protected void shoot(ProjectileType type, double angle, Consumer<Projectile> customizer) {
        shooter.shoot(new Projectile(type), angle, customizer);
    }

    /**
     * Executes an attack pattern (e.g., spread, ring, multi‑direction).
     *
     * @param pattern the attack pattern to execute
     * @param angle   base aiming angle (usually toward player)
     */
    protected void executeAttack(AttackPattern pattern, double angle) {
        pattern.execute(shooter, new Projectile(ProjectileType.ENEMY_BULLET), angle, null);
    }

    /**
     * Executes an attack pattern with a projectile customizer.
     */
    protected void executeAttack(AttackPattern pattern, double angle, Consumer<Projectile> customizer) {
        pattern.execute(shooter, new Projectile(ProjectileType.ENEMY_BULLET), angle, customizer);
    }
}
