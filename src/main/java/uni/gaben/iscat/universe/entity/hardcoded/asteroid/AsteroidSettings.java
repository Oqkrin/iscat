package uni.gaben.iscat.universe.entity.hardcoded.asteroid;

public final class AsteroidSettings {
    public static final int MAXPXSIZE = 64;

    private AsteroidSettings() {}

    public static final int MIN_VERTICES = 5;
    public static final int VERTICE_VARIATION = 5;
    
    public static final double RADIUS_VARIATION_MIN = 0.7;
    public static final double RADIUS_VARIATION_RANGE = 0.3;

    /** Base durability multiplied by the asteroid's mass/density to find its total HP. */
    public static final double BASE_DURABILITY = 20.0;
    /** Scale factor determining how much damage projectiles deal to asteroids. */
    public static final double PROJECTILE_DAMAGE_FACTOR = 10.0;
    /** Below this pixel size, the asteroid shatters into dust instead of splitting. */
    public static final double MIN_SPLIT_SIZE = 32.0;

}
