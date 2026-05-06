package uni.gaben.iscat.utils.settings;

import uni.gaben.iscat.utils.Interpolator;

/**
 * Costanti fisiche e di gameplay per la schermata di gioco.
 * Modificare qui per bilanciare senza toccare la logica.
 */
public final class GameSettings {

    private GameSettings() {}

    // --- Fisica giocatore ---

    /** Forza spinta per tick mentre si tiene un tasto direzionale. */
    public static final double PLAYER_THRUST_FORCE = 3.5;

    /** Drag applicato alla velocità ogni tick (0=stop istantaneo, 1=no attrito). */
    public static final double PLAYER_DRAG = 0.92;

    /** Velocità massima in pixel/tick. */
    public static final double PLAYER_MAX_SPEED = 7.0;

    /** Massa del giocatore in kg (unità di gioco). */
    public static final double PLAYER_MASS = 10.0;

    /** Raggio collisione giocatore in pixel. */
    public static final double PLAYER_COLLISION_RADIUS = 20.0;

    // --- Dodge ---

    /** Impulso applicato al dodge (pixel/tick). */
    public static final double DODGE_IMPULSE = 18.0;

    /** Durata del cooldown dodge in tick. */
    public static final int DODGE_COOLDOWN_TICKS = 45;

    // --- Stelle di sfondo ---

    /** Numero di stelle generate nello spazio. */
    public static final int STAR_COUNT = 300;

    /** Fattore lerp scorrimento stelle. Più basso = più morbido. */
    public static final double STAR_SCROLL_LERP = 0.12;

    /** Dimensione in pixel del punto stella. */
    public static final double STAR_SIZE = 2.0;

    /** Impulso visivo applicato alle stelle al dodge (frazione di DODGE_IMPULSE). */
    public static final double DODGE_STAR_IMPULSE_FACTOR = 0.4;

    // --- Rendering ---

    /** Dimensione tile in pixel (sprite giocatore). */
    public static final double TILE_SIZE = 32 * 2;

    /** Offset rotazione sprite: 90° se punta a nord, 0° se punta a est. */
    public static final double SPRITE_NORTH_OFFSET = 90.0;

    // --- Loop ---

    /** Delta-time fisso passato al mondo fisico ogni tick. */
    public static final double DT = 1.0;

    /**
     * Fattore lerp per l'accelerazione della spinta (0=istantaneo, 1=mai).
     * Più basso = risposta più immediata ai tasti.
     */
    public static final double THRUST_LERP = 0.18;

    // --- Preset easing ---

    /** Curva usata per lo scorrimento delle stelle. */
    public static final Interpolator.Preset STAR_SCROLL_EASING = Interpolator.Preset.LINEAR;

    /** Curva usata per la spinta del giocatore. */
    public static final Interpolator.Preset THRUST_EASING = Interpolator.Preset.EASE_OUT;
}
