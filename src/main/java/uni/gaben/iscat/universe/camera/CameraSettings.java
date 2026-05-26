package uni.gaben.iscat.universe.camera;

import uni.gaben.iscat.universe.UniverseVelocitySettings;

public final class CameraSettings {
    private CameraSettings() {}

    // Spring stiffness: higher = snappier camera (try 60–150)
    public static final double SPRING_STIFFNESS = UniverseVelocitySettings.PLAYER_MAX_VELOCITY/2;
    // Spring mass: 1.0 gives critical damping at stiffness^0.5 * 2
    public static final double SPRING_MASS = 1.0;

}
