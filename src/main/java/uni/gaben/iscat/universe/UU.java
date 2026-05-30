package uni.gaben.iscat.universe;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.dyn4j.geometry.Vector2;


/**
 * Universe Units (UU) - Utility class for scaling between
 * Dyn4j physics units (meters, seconds) and visual engine units (pixels, ticks).
 */
public class UU {
    public static double getUniverseScale() {
        return UNIVERSE_SCALE.get();
    }

    public static DoubleProperty UNIVERSE_SCALEProperty() {
        return UNIVERSE_SCALE;
    }

    public static void setUniverseScale(double universeScale) {
        UNIVERSE_SCALE.set(universeScale);
    }

    // 64 pixels = 1 meter
    public static DoubleProperty UNIVERSE_SCALE = new SimpleDoubleProperty(UniverseSettings.DEFAULT_SCALE);
    // 1 tick = 1/60th of a second (assuming a standard 60Hz physics loop)
    public static final double UNIVERSE_TICK = 1.0 / 60.0;

    private Double pixelValue = null;
    private Double metersValue = null;
    private Double secondsValue = null;
    private Double ticksValue = null;



    private UU() {}

    /*Contenitore di Valori nelle unita di misura del sistema fisico e del game (metri - pixels) (secondi - tick)*/
    public UU(double value, units valueType) {
        switch(valueType) {
            case SECONDS -> {
                secondsValue = value;
                ticksValue = UU.sToTicks(secondsValue);
            }
            case TICKS -> {
                ticksValue = value * UU.UNIVERSE_TICK;
                secondsValue = UU.ticksToS(ticksValue);
            }
            case METERS -> {
                metersValue = value;
                pixelValue = UU.mToPx(metersValue);
            }
            case PIXELS -> {
                pixelValue = value;
                metersValue = UU.pxToM(pixelValue);
            }
        }
    }

    // --- DISTANCE & POSITION CONVERSIONS ---

    public static double pxToM(double px) {
        return px / getUniverseScale();
    }

    public static double mToPx(double m) {
        return m * getUniverseScale();
    }

    /** Converts pixel coordinates into a Dyn4j physics vector */
    public static Vector2 pxToM(double x, double y) {
        return new Vector2(x / getUniverseScale(), y / getUniverseScale());
    }

    /** Converts a Dyn4j physics vector into pixel coordinates */
    public static Vector2 mToPx(Vector2 metersVec) {
        return new Vector2(metersVec.x * getUniverseScale(), metersVec.y * getUniverseScale());
    }

    // --- TIME & FRAME CONVERSIONS ---

    /** Converts game ticks/frames into seconds */
    public static double ticksToS(double ticks) {
        return ticks * UNIVERSE_TICK;
    }

    /** Converts seconds into game ticks/frames */
    public static double sToTicks(double seconds) {
        return seconds / UNIVERSE_TICK;
    }

    public Double px() {
        return pixelValue;
    }

    public Double m() {
        return metersValue;
    }

    public Double ticks() {
        return ticksValue;
    }

    public Double s() {
        return secondsValue;
    }

    public enum units {
        SECONDS,
        TICKS,
        METERS,
        PIXELS
    }

    public static Vector2 vector2zero() {
        return new Vector2(0.0, 0.0);
    }
}