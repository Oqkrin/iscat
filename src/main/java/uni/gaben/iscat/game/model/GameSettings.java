package uni.gaben.iscat.game.model;

import uni.gaben.iscat.utils.Interpolator;

/**
 * Costanti fisiche e di gameplay per la schermata di gioco.
 * Modificare qui per bilanciare senza toccare la logica.
 */
public final class GameSettings {

    private GameSettings() {}

    // --- Fisica giocatore ---

    /** Forza spinta per tick mentre si tiene un tasto direzionale. */
    public static final double FORZA_SPINTA = 4;

    /** Attrito applicato alla velocità ogni tick (0=stop istantaneo, 1=no attrito). */
    public static final double ATTRITO = 0.92;

    /** Velocità massima in pixel/tick. */
    public static final double VELOCITA_MAX = 9.0;

    /** Massa del giocatore in kg (unità di gioco). */
    public static final double MASSA_GIOCATORE = 10.0;

    /** Raggio collisione giocatore in pixel. */
    public static final double RAGGIO_COLLISIONE = 20.0;

    // --- Dodge ---

    /** Impulso scatto in pixel/tick. */
    public static final double IMPULSO_SCATTO = 14.0;

    /** Velocità massima durante lo scatto (sovrascrive VELOCITA_MAX). */
    public static final double VELOCITA_MAX_SCATTO = 14.0;

    /** Drag ridotto durante lo scatto (più alto = scivola più lontano). */
    public static final double ATTRITO_SCATTO = 0.98;

    /** Tick di cooldown prima del prossimo scatto. */
    public static final int COOLDOWN_SCATTO_TICK = 67;

    /** Tick di durata della fase scatto (drag ridotto attivo). */
    public static final int DURATA_SCATTO_TICK = 14;

    // --- Stelle di sfondo ---

    /** Numero di stelle generate nello spazio. */
    public static final int NUMERO_STELLE = 500;

    /** Fattore lerp scorrimento stelle. Più basso = più morbido. */
    public static final double LERP_STELLE = 0.12;

    /** Dimensione minima stella in pixel. */
    public static final double DIMENSIONE_STELLA_MIN = 0.5;
    
    /** Dimensione massima stella in pixel. */
    public static final double DIMENSIONE_STELLA_MAX = 4.5;
    
    /** Esponente per distribuzione dimensioni stelle (>1 = più stelle piccole). */
    public static final double STELLA_SIZE_POWER = 2.5;

    /** Impulso visivo alle stelle al dodge (frazione di IMPULSO_SCATTO). */
    public static final double FATTORE_IMPULSO_STELLE = 0.4;

    // --- Rendering ---

    /** Dimensione tile in pixel (sprite giocatore). */
    public static final double DIMENSIONE_TILE = 32 * 2;

    /** Offset rotazione sprite: 90° se punta a nord, 0° se punta a est. */
    public static final double OFFSET_NORD_SPRITE = 90.0;

    // --- Loop ---

    /** Delta-time fisso passato al mondo fisico ogni tick. */
    public static final double DT = 1.0;

    /** Fattore lerp per l'accelerazione della spinta. Più basso = più immediato. */
    public static final double LERP_SPINTA = 0.18;

    // --- Preset easing ---

    /** Curva usata per lo scorrimento delle stelle. */
    public static final Interpolator.Preset EASING_STELLE = Interpolator.Preset.SMOOTHER;

    /** Curva usata per la spinta del giocatore. */
    public static final Interpolator.Preset EASING_SPINTA = Interpolator.Preset.EASE_OUT;
}
