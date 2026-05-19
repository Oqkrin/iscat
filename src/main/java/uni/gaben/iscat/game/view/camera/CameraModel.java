package uni.gaben.iscat.game.view.camera;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import uni.gaben.iscat.utils.Spring;

/**
 * Pure Model for the Camera.
 * Tracks the center point of the viewport in World Coordinates using physics springs.
 */
public class CameraModel {
    private final Spring springX;
    private final Spring springY;
    private boolean snapped = false;

    // These represent the physical size of the application layout viewport (Window dimensions)
    private final DoubleProperty screenWidth = new SimpleDoubleProperty(800);
    private final DoubleProperty screenHeight = new SimpleDoubleProperty(600);

    public CameraModel() {
        this.springX = Spring.critico(0, CameraSettings.SPRING_STIFFNESS, CameraSettings.SPRING_MASS);
        this.springY = Spring.critico(0, CameraSettings.SPRING_STIFFNESS, CameraSettings.SPRING_MASS);
    }

    public Spring getSpringX() { return springX; }
    public Spring getSpringY() { return springY; }

    /** Returns the smoothed World Center X position of the camera */
    public double getX() { return springX.getPosition(); }
    /** Returns the smoothed World Center Y position of the camera */
    public double getY() { return springY.getPosition(); }

    public boolean isSnapped() { return snapped; }
    public void setSnapped(boolean snapped) { this.snapped = snapped; }

    // --- Viewport Dimension Management ---

    public double getScreenWidth() { return screenWidth.get(); }
    public DoubleProperty screenWidthProperty() { return screenWidth; }
    public void setScreenWidth(double width) { this.screenWidth.set(width); }

    public double getScreenHeight() { return screenHeight.get(); }
    public DoubleProperty screenHeightProperty() { return screenHeight; }
    public void setScreenHeight(double height) { this.screenHeight.set(height); }

    /** Helper: Returns the exact half-width offset of the player's view screen */
    public double getScreenCenterX() { return getScreenWidth() / 2.0; }
    /** Helper: Returns the exact half-height offset of the player's view screen */
    public double getScreenCenterY() { return getScreenHeight() / 2.0; }

    /**
     * THE GOLDEN RENDERING EQUATION:
     * Computes the top-left offset point needed to shift your JavaFX GraphicsContext matrix.
     */
    public double getViewportLeftX() {
        return getX() - getScreenCenterX();
    }

    public double getViewportTopY() {
        return getY() - getScreenCenterY();
    }
}