package uni.gaben.iscat.game.settings;

import uni.gaben.iscat.utils.Interpolator;

public class LoopSettings {
    private LoopSettings() {}

    /** Delta-time fisso passato al mondo fisico ogni tick. */
    public static final double DT = 1.0;

    /** Fattore lerp per l'accelerazione della spinta. Più basso = più immediato. */
    public static final double LERP_SPINTA = 0.18;

    /** Curva usata per la spinta del giocatore. */
    public static final Interpolator.Preset EASING_SPINTA = Interpolator.Preset.EASE_OUT;

    /** Impulso visivo alle stelle al dodge (frazione di IMPULSO_SCATTO). */
    public static final double FATTORE_IMPULSO_STELLE = 0.4;
}