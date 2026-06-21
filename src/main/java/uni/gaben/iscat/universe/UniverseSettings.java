package uni.gaben.iscat.universe;

/**
 * Registro statico delle configurazioni globali dell'universo fisico e grafico.
 * Centralizza le costanti di rendering, le impostazioni del campo stellato e i coefficienti di parallasse.
 */
public final class UniverseSettings {

    private UniverseSettings() {}

    // --- Geometria e Proporzioni di Default ---
    /** Fattore di scala nativo pixel-metri ($Px/m$). */
    public static final double DEFAULT_SCALE = 64.0;
    /** Larghezza logica predefinita dell'arena (in pixel). */
    public static final double DEFAULT_WIDTH = 1280.0;
    /** Altezza logica predefinita dell'arena (in pixel). */
    public static final double DEFAULT_HEIGHT = 720.0;

    // --- Configurazione Starfield (Sfondo) ---
    /** Densità superficiale di stelle per pixel quadrato. */
    public static final double STAR_DENSITY = 0.0015;
    /** Diametro minimo di una singola stella in pixel. */
    public static final double STAR_MIN_SIZE = 0.9;
    /** Moltiplicatore quadratico per la variazione di taglia. */
    public static final double STAR_MAX_SIZE_ADD = 2.3;
    /** Canale alpha standard (opacità) per il rendering dei punti. */
    public static final double STAR_ALPHA = 0.8;

    // --- Costanti Effetto Parallasse ---
    /** Spostamento di base minimo applicato allo sfondo. */
    public static final double PARALLAX_BASE = 0.03;
    /** Moltiplicatore di profondità per l'effetto tridimensionale. */
    public static final double PARALLAX_FACTOR = 0.20;
    /** Divisore dimensionale per scalare la velocità in base al diametro. */
    public static final double PARALLAX_SIZE_DIVISOR = 3.0;
}