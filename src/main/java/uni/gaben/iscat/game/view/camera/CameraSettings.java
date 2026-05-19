package uni.gaben.iscat.game.view.camera;

public final class CameraSettings {
    private CameraSettings() {}

    // Spring stiffness: higher = snappier camera (try 60–150)
    public static final double SPRING_STIFFNESS = 120.0;
    // Spring mass: 1.0 gives critical damping at stiffness^0.5 * 2
    public static final double SPRING_MASS = 1.0;

    // @deprecated kept for reference; camera now driven by Spring
    public static final double CAMERA_FOLLOW_SPEED = 0.25;
}
