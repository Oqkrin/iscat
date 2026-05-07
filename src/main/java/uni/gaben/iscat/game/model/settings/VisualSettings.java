package uni.gaben.iscat.game.model.settings;

import uni.gaben.iscat.utils.Interpolator;

public class VisualSettings {

    private VisualSettings() {}

    // === Tile ===
    /** Dimensione tile base (px) */
    public static final double DIMENSIONE_TILE = 64.0;

    /** Offset rotazione sprite: 90° se punta a nord, 0° se punta a est. */
    public static final double OFFSET_NORD_SPRITE = 90.0;

    // === Stelle ===
    /** Numero stelle sfondo */
    public static final int NUMERO_STELLE = 200;

    /** Dimensione minima stella (px) */
    public static final double DIMENSIONE_STELLA_MIN = 1.0;

    /** Dimensione massima stella (px) */
    public static final double DIMENSIONE_STELLA_MAX = 3.0;

    /** Potenza distribuzione dimensioni (bias verso piccole) */
    public static final double POTENZA_DIMENSIONE_STELLA = 2.5;

    // === Parallasse ===
    /** Fattore lerp parallasse (0-1, più basso = più lento) */
    public static final double LERP_STELLE = 0.05;

    /** Preset easing parallasse */
    public static Interpolator.Preset EASING_STELLE = Interpolator.Preset.LINEAR;

    // === FPS ===
    /** Mostra contatore FPS */
    public static boolean MOSTRA_FPS = false;
}