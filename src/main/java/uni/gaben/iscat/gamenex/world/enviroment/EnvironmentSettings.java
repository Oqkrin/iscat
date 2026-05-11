package uni.gaben.iscat.gamenex.world.enviroment;

public final class EnvironmentSettings {
    public static final double DEFAULT_HEIGHT = 1080.0;
    public static final double DEFAULT_WIDTH = 1920.0;

    private EnvironmentSettings() {}

    // Starfield Settings
    public static final double STAR_DENSITY = 0.001; // Stars per pixel^2
    public static final double STAR_MIN_SIZE = 1.0;
    public static final double STAR_MAX_SIZE_ADD = 2.0; // r*r * 2.0
    public static final double STAR_ALPHA = 0.6;
    
    // Parallax Constants
    public static final double PARALLAX_BASE = 0.02;
    public static final double PARALLAX_FACTOR = 0.15;
    public static final double PARALLAX_SIZE_DIVISOR = 3.0;

    // Gravity / Suction
    public static final double ORBITAL_G = 20.0;
    public static final double SUCTION_RANGE_M = 8.0;
    public static final double CIRCULARIZE_GAIN = 3.0;

    // Initial Asteroid
    public static final double TEST_ASTEROID_X = 500;
    public static final double TEST_ASTEROID_Y = 300;
    public static final double TEST_ASTEROID_RADIUS = 20;
    public static final double TEST_ASTEROID_VEL_X = -1.0;
}
