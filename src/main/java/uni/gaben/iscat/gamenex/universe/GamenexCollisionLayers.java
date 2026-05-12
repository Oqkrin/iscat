package uni.gaben.iscat.gamenex.universe;

import org.dyn4j.collision.CategoryFilter;

/**
 * Centralized collision filtering for the Gamenex universe.
 * Ensures that entities only collide with what they are supposed to.
 */
public final class GamenexCollisionLayers {
    private GamenexCollisionLayers() {}

    // Categories (Powers of 2)
    public static final long PLAYER     = 1;
    public static final long ASTEROID   = 2;
    public static final long ENEMY      = 4;
    public static final long PROJECTILE = 8;
    public static final long BOOST = 16;

    // Filters
    public static final CategoryFilter PLAYER_FILTER = new CategoryFilter(PLAYER, 
        PLAYER | ASTEROID | ENEMY | PROJECTILE | BOOST);
        
    public static final CategoryFilter ASTEROID_FILTER = new CategoryFilter(ASTEROID, 
        PLAYER | ASTEROID | ENEMY); // Asteroids hit everything except projectiles (for now)
        
    public static final CategoryFilter ENEMY_FILTER = new CategoryFilter(ENEMY, 
        PLAYER | ASTEROID | ENEMY | PROJECTILE);

    public static final CategoryFilter BOOST_FILTER = new CategoryFilter(BOOST,
            PLAYER ); // Boost collide only with player
}
