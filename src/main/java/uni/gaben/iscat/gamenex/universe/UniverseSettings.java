package uni.gaben.iscat.gamenex.universe;

/**
 * Impostazioni globali dell'universo fisico e grafico.
 * Definisce le costanti di scala, la densità delle stelle e le forze universali.
 */
public final class UniverseSettings {
    private UniverseSettings() {
    }

    /**
     * Fattore di scala tra pixel e metri (64 pixel = 1 metro).
     */
    public static final double SCALE = 64.0;
    /**
     * Altezza predefinita dell'universo in pixel.
     */
    public static final double DEFAULT_HEIGHT = 1080.0;
    /**
     * Larghezza predefinita dell'universo in pixel.
     */
    public static final double DEFAULT_WIDTH = 1920.0;

    // --- Impostazioni Campo Stellare (Starfield) ---
    /**
     * Numero di stelle per pixel quadrato.
     */
    public static final double STAR_DENSITY = 0.0015;
    public static final double STAR_MIN_SIZE = .9;
    public static final double STAR_MAX_SIZE_ADD = 2.3;
    public static final double STAR_ALPHA = 0.8;

    // --- Costanti di Parallasse ---
    /**
     * Velocità base dello sfondo.
     */
    public static final double PARALLAX_BASE = 0.03;
    /**
     * Intensità dell'effetto di profondità.
     */
    public static final double PARALLAX_FACTOR = 0.20;
    public static final double PARALLAX_SIZE_DIVISOR = 3.;

    // --- Gravità e Forze di Aspirazione ---
    /**
     * Forza di gravità orbitale degli oggetti massicci.
     */
    public static final double ORBITAL_G = 20.0;
    /**
     * Raggio di azione dell'aspirazione dei detriti (metri).
     */
    public static final double SUCTION_RANGE_M = 8.0;
    /**
     * Guadagno di circolarizzazione per le orbite.
     */
    public static final double CIRCULARIZE_GAIN = 3.0;

    // --- Configurazione Test ---
    public static final double TEST_ASTEROID_X = 500;
    public static final double TEST_ASTEROID_Y = 300;
    public static final double TEST_ASTEROID_RADIUS = 20;
}
