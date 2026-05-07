package uni.gaben.iscat.game.model;

import uni.gaben.iscat.utils.Interpolator;

/**
 * Costanti fisiche e di gameplay per la schermata di gioco.
 * Modificare qui per bilanciare senza toccare la logica.
 */
public final class GameSettings {

    private GameSettings() {}

    // --- COSTANTI FISICHE (Non modificabili dal menu) ---
    public static final double FORZA_SPINTA = 4; /** Forza spinta per tick mentre si tiene un tasto direzionale. */
    public static final double ATTRITO = 0.92; /** Attrito applicato alla velocità ogni tick (0=stop istantaneo, 1=no attrito). */
    public static final double VELOCITA_MAX = 9.0; /** Velocità massima in pixel/tick. */
    public static final double MASSA_GIOCATORE = 10.0; /** Massa del giocatore in kg (unità di gioco). */
    public static final double RAGGIO_COLLISIONE = 20.0; /** Raggio collisione giocatore in pixel. */

    // --- DODGE ---
    public static final double IMPULSO_SCATTO = 14.0; /** Impulso scatto in pixel/tick. */
    public static final double VELOCITA_MAX_SCATTO = 14.0; /** Velocità massima durante lo scatto (sovrascrive VELOCITA_MAX). */
    public static final double ATTRITO_SCATTO = 0.98; /** Drag ridotto durante lo scatto (più alto = scivola più lontano). */
    public static final int COOLDOWN_SCATTO_TICK = 67; /** Tick di cooldown prima del prossimo scatto. */
    public static final int DURATA_SCATTO_TICK = 14; /** Tick di durata della fase scatto (drag ridotto attivo). */

    // --- STELLE (SFONDO) ---
    public static final int NUMERO_STELLE = 500; /** Numero di stelle generate nello spazio. */
    public static final double LERP_STELLE = 0.12; /** Fattore lerp scorrimento stelle. Più basso = più morbido. */
    public static final double DIMENSIONE_STELLA_MIN = 0.5; /** Dimensione minima stella in pixel. */
    public static final double DIMENSIONE_STELLA_MAX = 4.5; /** Dimensione massima stella in pixel. */
    public static final double STELLA_SIZE_POWER = 2.5; /** Esponente per distribuzione dimensioni stelle (>1 = più stelle piccole). */
    public static final double FATTORE_IMPULSO_STELLE = 0.4; /** Impulso visivo alle stelle al dodge (frazione di IMPULSO_SCATTO). */

    // --- RENDERING ---
    public static final double DIMENSIONE_TILE = 32 * 2; /** Dimensione tile in pixel (sprite giocatore). */
    public static final double OFFSET_NORD_SPRITE = 90.0; /** Offset rotazione sprite: 90° se punta a nord, 0° se punta a est. */

    // --- LOOP ---
    public static final double DT = 1.0; /** Delta-time fisso passato al mondo fisico ogni tick. */
    public static final double LERP_SPINTA = 0.18; /** Fattore lerp per l'accelerazione della spinta. Più basso = più immediato. */

    // --- EASING PRESETS ---
    public static final Interpolator.Preset EASING_STELLE = Interpolator.Preset.SMOOTHER; /** Curva usata per lo scorrimento delle stelle. */
    public static final Interpolator.Preset EASING_SPINTA = Interpolator.Preset.EASE_OUT; /** Curva usata per la spinta del giocatore. */

    // ===============================================================
    // --- IMPOSTAZIONI UTENTE (Modificabili dal menu OptionsMenu) ---
    // ===============================================================

    public static double BGM_VOLUME = 0.5; /** Volume Musica (0.0 - 1.0) */
    public static double SFX_VOLUME = 0.7; /** Volume Effetti (0.0 - 1.0) */
    public static double SENSITIVITY = 1.0; /** Moltiplicatore sensibilità puntamento mouse */
    public static boolean SHOW_FPS = false; /** Toggle per mostrare il contatore FPS sul Canvas */
    public static boolean SCREENSHAKE_ENABLED = true; /** Toggle per l'effetto scuotimento camera */
}
