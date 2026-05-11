package uni.gaben.iscat.gamenex.view.camera;

import uni.gaben.iscat.utils.Spring;

/**
 * Pure Model for the Camera.
 * Holds position and the physics springs used for movement.
 */
public class CameraModel {
    private final Spring springX;
    private final Spring springY;
    private boolean snapped = false;

    public CameraModel() {
        this.springX = Spring.critico(0, CameraSettings.SPRING_STIFFNESS, CameraSettings.SPRING_MASS);
        this.springY = Spring.critico(0, CameraSettings.SPRING_STIFFNESS, CameraSettings.SPRING_MASS);
    }

    public Spring getSpringX() { return springX; }
    public Spring getSpringY() { return springY; }

    public double getX() { return springX.getPosition(); }
    public double getY() { return springY.getPosition(); }

    public boolean isSnapped() { return snapped; }
    public void setSnapped(boolean snapped) { this.snapped = snapped; }
}
