package uni.gaben.iscat.universe;

/**
 * Impostazioni globali dell'universo fisico e grafico.
 * Definisce le costanti di scala, la densità delle stelle e le forze universali.
 */
public final class UniverseSettings {

    public static final double DEFAULT_SCALE = 64.0;

    private UniverseSettings() {
        // Impedisce l'istanza di questa classe di utility
    }
    public static final double DEFAULT_HEIGHT = 720.0;
    public static final double DEFAULT_WIDTH = 1280.0;

    // --- Starfield Configuration ---
    public static final double STAR_DENSITY = 0.0015;
    public static final double STAR_MIN_SIZE = 0.9;
    public static final double STAR_MAX_SIZE_ADD = 2.3;
    public static final double STAR_ALPHA = 0.8;

    // --- Parallax Constants ---
    public static final double PARALLAX_BASE = 0.03;
    public static final double PARALLAX_FACTOR = 0.20;
    public static final double PARALLAX_SIZE_DIVISOR = 3.0;

}
