package uni.gaben.iscat.universe.camera;

import uni.gaben.iscat.universe.UniverseVelocitySettings;

/**
 * Central configuration for camera physics and behavior.
 *
 * <p>All values are static and final to ensure consistent camera feel across the
 * application. Tune {@link #SPRING_STIFFNESS} and {@link #SPRING_MASS} to adjust
 * how snappy or smooth the camera follows the target.</p>
 */
public final class CameraSettings {
    private CameraSettings() {
        // Private constructor to prevent instantiation (utility class)
    }

    /**
     * Spring stiffness (k) for the camera's X‑axis.
     * <p>Higher values make the camera respond more quickly to target dynamics.
     * The value is derived from the player's maximum velocity to maintain a
     * natural visual relationship between dynamics and camera lag.</p>
     */
    public static final double SPRING_STIFFNESS = UniverseVelocitySettings.PLAYER_MAX_VELOCITY/2;

    /**
     * Spring mass (m) for both X and Y springs.
     * <p>With a mass of 1.0 and a damping ratio of 1.0 (critical damping),
     * the spring returns to its target as fast as possible without overshoot.</p>
     */
    public static final double SPRING_MASS = 1.0;

    /**
     * Stiffness multiplier for the Y‑axis spring.
     * <p>Vertical following is made tighter (stiffer) than horizontal dynamics
     * to reduce unnecessary vertical bounce and keep the target vertically
     * centred more aggressively.</p>
     */
    public static final double Y_STIFFNESS_MULTIPLIER = 3.0;
    // Increased modifier for higher contrast between slow and fast
    public static final double MAX_ZOOM_OUT_MODIFIER = 0.5; // Was 0.45, now pulls back further
    public static final double ZOOM_SMOOTHING_SPEED  = 3;  // Slightly slower for a more "cinematic" pull

    // Limits for the user's manual base zoom
    public static final double MIN_MANUAL_ZOOM = 0.8;
    public static final double MAX_MANUAL_ZOOM = 2.0;
}
