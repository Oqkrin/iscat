package uni.gaben.iscat.gamenex.universe.hearth;

import uni.gaben.iscat.gamenex.lib.utils.UU;
import uni.gaben.iscat.gamenex.universe.UniverseSettings;

public class HearthSettings {

    /** Dimensione base dello sprite in pixel. */
    public static final int DIM_SPRITE = 32;
    /** Numero di frame dell'animazione. */
    public static final int NUMERO_FRAMES = 1;
    /** Scala di rendering. */
    public static final double SCALE = 1;
    /** Raggio della collisione fisica. */
    public static final double RAGGIO_COLLISIONE_PX = (DIM_SPRITE / 2.0) * 0.9;
    /** Vita data in collisione */
    public static final int HP_BOOST = 50;
    /** Raggio in pixel (es. 300px), convertito in metri */
    public static final double RANGE_ATTIVAZIONE = UU.pxToM(300.0);
    /** Velocità di "scatto" del cuore */
    public static final double VELOCITA_INSEGUIMENTO = 8.0;
}
