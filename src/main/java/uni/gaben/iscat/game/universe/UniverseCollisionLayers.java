package uni.gaben.iscat.game.universe;

import org.dyn4j.collision.CategoryFilter;

/**
 * Centralized collision filtering for the Gamenex universe.
 */
public final class UniverseCollisionLayers {
    private UniverseCollisionLayers() {}

    // Categories (Powers of 2)
    public static final long PLAYER           = 1;
    public static final long ASTEROID         = 2;
    public static final long ENEMY            = 4;
    public static final long PROJECTILE       = 8;
    public static final long BOOST            = 16;
    public static final long ENEMY_PROJECTILE = 32;
    public static final long WORM_BODY        = 64; // Categoria per corpo e coda del verme

    // Filters
    public static final CategoryFilter PLAYER_FILTER = new CategoryFilter(PLAYER,
            PLAYER | ASTEROID | ENEMY | WORM_BODY | ENEMY_PROJECTILE | BOOST);

    public static final CategoryFilter ASTEROID_FILTER = new CategoryFilter(ASTEROID,
            PLAYER | ASTEROID | ENEMY | WORM_BODY | PROJECTILE | ENEMY_PROJECTILE);

    public static final CategoryFilter ENEMY_FILTER = new CategoryFilter(ENEMY,
            PLAYER | ASTEROID | ENEMY | PROJECTILE);

    // Il corpo/coda del verme collide con player, asteroidi e proiettili
    public static final CategoryFilter WORM_BODY_FILTER = new CategoryFilter(WORM_BODY,
            PLAYER | ASTEROID | PROJECTILE);

    public static final CategoryFilter BOOST_FILTER = new CategoryFilter(BOOST,
            PLAYER );

    public static final CategoryFilter ENEMY_PROJECTILE_FILTER = new CategoryFilter(ENEMY_PROJECTILE,
            PLAYER | ASTEROID | PROJECTILE);

    // FIX CRITICO: Il proiettile deve avere il bit WORM_BODY nella maschera, altrimenti lo trapassa!
    public static final CategoryFilter PROJECTILE_FILTER = new CategoryFilter(PROJECTILE,
            ASTEROID | ENEMY | WORM_BODY | ENEMY_PROJECTILE);
}