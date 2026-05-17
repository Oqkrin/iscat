package uni.gaben.iscat.gamenex.lib.utils;

import org.dyn4j.geometry.Vector2;

/**
 * Universe Units (UU) - Utility class for scaling between
 * Dyn4j physics units (meters, seconds) and visual engine units (pixels, ticks).
 */
public class UU {
    // 64 pixels = 1 meter
    public static final double UNIVERSE_SCALE = 64.0;
    // 1 tick = 1/60th of a second (assuming a standard 60Hz physics loop)
    public static final double UNIVERSE_TICK = 1.0 / 60.0;

    private UU() {}

    // --- DISTANCE & POSITION CONVERSIONS ---

    public static double pxToM(double px) {
        return px / UNIVERSE_SCALE;
    }

    public static double mToPx(double m) {
        return m * UNIVERSE_SCALE;
    }

    /** Converts pixel coordinates into a Dyn4j physics vector */
    public static Vector2 pxToM(double x, double y) {
        return new Vector2(x / UNIVERSE_SCALE, y / UNIVERSE_SCALE);
    }

    /** Converts a Dyn4j physics vector into pixel coordinates */
    public static Vector2 mToPx(Vector2 metersVec) {
        return new Vector2(metersVec.x * UNIVERSE_SCALE, metersVec.y * UNIVERSE_SCALE);
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
}