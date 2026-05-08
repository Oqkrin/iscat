package uni.gaben.iscat.game.utils.settings;

import uni.gaben.iscat.utils.Interpolator;

/**
 * Configurazione completa del gioco.
 * Tutti i valori numerici, costanti e impostazioni sono qui.
 *
 * <p>Organizzato in classi nidificate per categoria.
 * Nessun "magic number" nel codice - tutto qui!
 */
public final class GameSettings {

    private GameSettings() {} // Classe utility

    // --- Flat aliases for GameController / GameCanvas ---

    /** @see LoopSettings#DT */
    public static final double DT = LoopSettings.DT;
    /** @see LoopSettings#LERP_SPINTA */
    public static final double LERP_SPINTA = LoopSettings.LERP_SPINTA;
    /** @see LoopSettings#EASING_SPINTA */
    public static final Interpolator.Preset EASING_SPINTA = LoopSettings.EASING_SPINTA;
    /** @see LoopSettings#FATTORE_IMPULSO_STELLE */
    public static final double FATTORE_IMPULSO_STELLE = LoopSettings.FATTORE_IMPULSO_STELLE;
    /** @see VisualSettings#OFFSET_NORD_SPRITE */
    public static final double OFFSET_NORD_SPRITE = VisualSettings.OFFSET_NORD_SPRITE;
    /** @see VisualSettings#DIMENSIONE_TILE */
    public static final double DIMENSIONE_TILE_D = VisualSettings.DIMENSIONE_TILE;

    /** @see WorldSettings#COEFFICIENTE_RESTITUZIONE */
    public static final double COLLISION_RESTITUTION = WorldSettings.COEFFICIENTE_RESTITUZIONE;
}
