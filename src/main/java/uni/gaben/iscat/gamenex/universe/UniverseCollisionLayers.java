package uni.gaben.iscat.gamenex.universe;

import org.dyn4j.collision.CategoryFilter;

/**
 * Centralized collision filtering for the Gamenex universe.
 * Ensures that entities only collide with what they are supposed to.
 */
public final class UniverseCollisionLayers {
    private UniverseCollisionLayers() {}

    // Categories (Powers of 2)
    public static final long PLAYER     = 1;
    public static final long ASTEROID   = 2;
    public static final long ENEMY      = 4;
    public static final long PROJECTILE = 8;
    public static final long BOOST = 16;
    public static final long ENEMY_PROJECTILE = 32;

    // Filters
    public static final CategoryFilter PLAYER_FILTER = new CategoryFilter(PLAYER, 
        PLAYER | ASTEROID | ENEMY | ENEMY_PROJECTILE | BOOST);
        
    public static final CategoryFilter ASTEROID_FILTER = new CategoryFilter(ASTEROID, 
        PLAYER | ASTEROID | ENEMY | PROJECTILE | ENEMY_PROJECTILE);
        
    public static final CategoryFilter ENEMY_FILTER = new CategoryFilter(ENEMY, 
        PLAYER | ASTEROID | ENEMY | PROJECTILE);

    public static final CategoryFilter BOOST_FILTER = new CategoryFilter(BOOST,
            PLAYER );

    public static final CategoryFilter ENEMY_PROJECTILE_FILTER = new CategoryFilter(ENEMY_PROJECTILE,
            PLAYER | ASTEROID);

    public static final CategoryFilter PROJECTILE_FILTER = new CategoryFilter(PROJECTILE,
            ASTEROID | ENEMY);
}
