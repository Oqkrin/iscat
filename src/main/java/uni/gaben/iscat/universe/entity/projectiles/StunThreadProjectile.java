package uni.gaben.iscat.universe.entity.projectiles;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.entity.player.PlayerModel;

/**
 * A projectile fired by the IscatBomber that stuns the player on impact.
 * <p>
 * The thread deals no damage but temporarily immobilizes the player,
 * giving the bomber an opportunity to reposition or land other attacks.
 */
public class StunThreadProjectile extends Projectile {

    /** Default stun duration in seconds when not specified. */
    private static final double DEFAULT_STUN_DURATION = 2.0;

    private final double stunDuration;

    /**
     * Creates a stun thread projectile with default stun duration.
     *
     * @param position  spawn position (world coordinates, meters)
     * @param direction normalized direction vector
     * @param speed     projectile speed (meters/second)
     */
    public StunThreadProjectile(Vector2 position, Vector2 direction, double speed) {
        this(position, direction, speed, DEFAULT_STUN_DURATION);
    }

    /**
     * Creates a stun thread projectile with a custom stun duration.
     *
     * @param position     spawn position (world coordinates, meters)
     * @param direction    normalized direction vector
     * @param speed        projectile speed (meters/second)
     * @param stunDuration how long the player will be stunned (seconds)
     */
    public StunThreadProjectile(Vector2 position, Vector2 direction, double speed, double stunDuration) {
        // Reuse ENEMY_BULLET type – correct collision filter, zero damage set below
        super(ProjectileType.ENEMY_BULLET);
        this.stunDuration = stunDuration;

        // Place and launch the projectile
        getTransform().setTranslation(position);
        setLinearVelocity(direction.multiply(speed));

        // Override damage: stun threads should not hurt, only disable
        setLife(0.0);
        setMaxLife(0.0);

        // Custom collision handler: apply stun to player, then vanish
        setOnCollision(other -> {
            if (other instanceof PlayerModel player) {
                player.applyStun(stunDuration);
                kill(true);
            } else if (!(other instanceof StunThreadProjectile)) {
                // Remove on contact with any other solid object (walls, other enemies, etc.)
                kill(true);
            }
        });
    }
}