package uni.gaben.iscat.game.universe;

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
public final class VelocitySettings {

    private VelocitySettings() {}

    // ─── PROJECTILES ──────────────────────────────────────────────────────────

    /** Velocity of a player bullet (m/s). */
    public static final double PLAYER_BULLET_VELOCITY = 30.0;

    /** Velocity of a generic enemy bullet (m/s). */
    public static final double ENEMY_BULLET_VELOCITY  = 20.0;

    // ─── PLAYER ───────────────────────────────────────────────────────────────

    /** Top speed the player ship can reach under thrust (m/s). */
    public static final double PLAYER_MAX_VELOCITY    = 16.0;

    /** Impulse magnitude of the player dash (m/s added instantly). */
    public static final double PLAYER_DASH_IMPULSE    = 20.0;

    // ─── SMALL / FAST ENEMIES ─────────────────────────────────────────────────

    /** IscatMob max movement speed (m/s). */
    public static final double MOB_MAX_VELOCITY       = 12.0;

    /** IscatEater max movement speed (m/s). */
    public static final double EATER_MAX_VELOCITY     = 60.0;

    /** IscatCore max movement speed (m/s). */
    public static final double CORE_MAX_VELOCITY      = 12.0;

    /** IscatBomber max movement speed (m/s). */
    public static final double BOMBER_MAX_VELOCITY    = 10.0;

    /** FallenStarGolem max movement speed (m/s). */
    public static final double GOLEM_MAX_VELOCITY     = 12.0;

    /** FakeIscat max movement speed (m/s). */
    public static final double FAKE_ISCAT_MAX_VELOCITY = 12;

    // ─── BOSS ENEMIES ─────────────────────────────────────────────────────────

    /** IscatMother boss max movement speed (m/s). */
    public static final double MOTHER_MAX_VELOCITY    = 8.0;

    /** IscatWorm head max movement speed (m/s). */
    public static final double WORM_HEAD_MAX_SPEED    = 15.0;

    // ─── ASTEROIDS ────────────────────────────────────────────────────────────

    /** Initial random drift assigned to newly spawned asteroids (m/s range). */
    public static final double ASTEROID_SPAWN_SPEED_MIN = 0.3;
    public static final double ASTEROID_SPAWN_SPEED_MAX = 1.0;

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
