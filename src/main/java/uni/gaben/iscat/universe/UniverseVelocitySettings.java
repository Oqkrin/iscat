package uni.gaben.iscat.universe;

/**
 * Single source of truth for all entity velocities in the game.
 *
 * <p>Tuning guide: these are the values to touch when the overall
 * feel of the game needs to be faster or slower. Individual
 * Settings files simply reference the constants here rather than
 * declaring their own magic numbers.
 *
 * <p>All values are in <b>meters per second</b> (physics world
 * coordinates), unless a "PX" suffix indicates pixel-based input.
 */
public final class UniverseVelocitySettings {

    private UniverseVelocitySettings() {}

    // ─── PROJECTILES ──────────────────────────────────────────────────────────

    /** Velocity of a player bullet (m/s). */
    public static final double PLAYER_PROJECTILE_VELOCITY = 11.0;

    /** Velocity of a generic enemy bullet (m/s). */
    public static final double ENEMY_PROJECTILE_VELOCITY = PLAYER_PROJECTILE_VELOCITY;

    // ─── PLAYER ───────────────────────────────────────────────────────────────

    /** Top speed the player ship can reach under thrust (m/s). */
    public static final double PLAYER_MAX_VELOCITY    = ENEMY_PROJECTILE_VELOCITY *.8;

    /** Impulse magnitude of the player dash (m/s added instantly). */
    public static final double PLAYER_DASH_IMPULSE    = ENEMY_PROJECTILE_VELOCITY;


    /**
     * Computes the terminal velocity for an asteroid of given pixel size.
     * Larger asteroids are capped at lower speeds.
     *
     * @param sizePx diameter in pixels
     * @return terminal velocity in m/s
     */
    public static double asteroidTerminalVelocity(double sizePx) {
        return Math.max(2.0, 14.0 - (sizePx / 8.0));
    }
}
