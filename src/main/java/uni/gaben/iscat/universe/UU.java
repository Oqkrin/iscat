package uni.gaben.iscat.universe;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.dyn4j.geometry.Vector2;

/**
 * Utility di conversione spaziale e temporale (Universe Units).
 * Gestisce il mapping bidirezionale e reattivo tra mondo fisico (metri, secondi)
 * e motore grafico (pixel, tick di gioco).
 */
public class UU {

    // Costanti e Proprietà Globali
    public static final DoubleProperty UniversalGravitationalConstant = new SimpleDoubleProperty(UniverseVelocitySettings.PLAYER_DASH_IMPULSE / Math.PI);
    /** Scala di conversione reattiva (Default: 64 px = 1 m). */
    public static DoubleProperty UNIVERSE_SCALE = new SimpleDoubleProperty(UniverseSettings.DEFAULT_SCALE);
    /** Durata nominale di un singolo tick di logica/fisica ($1/60$s). */
    public static final double UNIVERSE_TICK = 1.0 / 60.0;

    public static double getUniverseScale() { return UNIVERSE_SCALE.get(); }

    // Proprietà di Istanza (Data-Binding O(1))
    private DoubleProperty pixelValue = null;
    private DoubleProperty metersValue = null;
    private DoubleProperty secondsValue = null;
    private DoubleProperty ticksValue = null;

    private UU() {}

    /**
     * Costruisce un contenitore atomico vincolato reattivamente per la conversione dinamica delle unità.
     */
    public UU(double value, units valueType) {
        switch(valueType) {
            case SECONDS -> {
                secondsValue = new SimpleDoubleProperty(value);
                ticksValue = new SimpleDoubleProperty();
                ticksValue.bind(secondsValue.divide(UNIVERSE_TICK));
            }
            case TICKS -> {
                ticksValue = new SimpleDoubleProperty(value);
                secondsValue = new SimpleDoubleProperty();
                secondsValue.bind(ticksValue.multiply(UNIVERSE_TICK));
            }
            case METERS -> {
                metersValue = new SimpleDoubleProperty(value);
                pixelValue = new SimpleDoubleProperty();
                pixelValue.bind(metersValue.multiply(UNIVERSE_SCALE));
            }
            case PIXELS -> {
                pixelValue = new SimpleDoubleProperty(value);
                metersValue = new SimpleDoubleProperty();
                metersValue.bind(pixelValue.divide(UNIVERSE_SCALE));
            }
        }
    }

    // DISTANCE & POSITION CONVERSIONS (Meters <-> Pixels)

    public static double pxToM(double px) { return px / getUniverseScale(); }
    public static double mToPx(double m) { return m * getUniverseScale(); }

    /** Trasforma coordinate in pixel (x, y) in un vettore fisico espresso in metri. */
    public static Vector2 pxToM(double x, double y) {
        return new Vector2(x / getUniverseScale(), y / getUniverseScale());
    }

    /** Trasforma un vettore fisico in metri in un vettore grafico espresso in pixel. */
    public static Vector2 mToPx(Vector2 metersVec) {
        return new Vector2(metersVec.x * getUniverseScale(), metersVec.y * getUniverseScale());
    }


    // Getters Proprietà Reattive
    public DoubleProperty px() { return pixelValue; }
    public DoubleProperty m() { return metersValue; }
    public DoubleProperty s() { return secondsValue; }

    /** Enumerazione dei sistemi e delle unità di misura supportati. */
    public enum units { SECONDS, TICKS, METERS, PIXELS }

    /** @return Un'istanza immutabile di un vettore nullo (0.0, 0.0). */
    public static Vector2 vector2zero() { return new Vector2(0.0, 0.0); }
}