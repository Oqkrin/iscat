package uni.gaben.iscat.universe.camera;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import uni.gaben.iscat.utils.Spring;

/**
 * Pure model of the camera.
 *
 * <p>Stores the camera's current view centre in world coordinates and the viewport
 * dimensions. The centre position is driven by two critically damped springs
 * (one for X, one for Y) that smoothly chase the desired target position.</p>
 *
 * <p>The model also provides helper methods to convert the centre position into
 * viewport top‑left coordinates, which are essential for rendering.</p>
 */
public class CameraModel {
    private final Spring springX;
    private final Spring springY;
    private boolean snapped = false;

    // Viewport dimensions (the visible area in screen pixels)
    private final DoubleProperty screenWidth  = new SimpleDoubleProperty(800);
    private final DoubleProperty screenHeight = new SimpleDoubleProperty(600);

    /**
     * Constructs a new camera model with default spring configurations.
     *
     * <p>X‑axis spring uses the base stiffness from {@link CameraSettings}.
     * Y‑axis spring uses an increased stiffness (multiplied by
     * {@link CameraSettings#Y_STIFFNESS_MULTIPLIER}) to reduce vertical lag.</p>
     */
    public CameraModel() {
        this.springX = Spring.critico(0,
                CameraSettings.SPRING_STIFFNESS,
                CameraSettings.SPRING_MASS);

        this.springY = Spring.critico(0,
                CameraSettings.SPRING_STIFFNESS * CameraSettings.Y_STIFFNESS_MULTIPLIER,
                CameraSettings.SPRING_MASS);
    }

    // ------------------------------------------------------------------------
    // Spring accessors (used by CameraController to update targets)
    // ------------------------------------------------------------------------

    /**
     * Returns the spring that controls the camera's X coordinate.
     *
     * @return the X‑axis spring (never {@code null})
     */
    public Spring getSpringX() {
        return springX;
    }

    /**
     * Returns the spring that controls the camera's Y coordinate.
     *
     * @return the Y‑axis spring (never {@code null})
     */
    public Spring getSpringY() {
        return springY;
    }

    // ------------------------------------------------------------------------
    // Smoothed camera centre (world coordinates)
    // ------------------------------------------------------------------------

    /**
     * Returns the current smoothed world X coordinate of the camera centre.
     *
     * @return camera centre X (world pixels)
     */
    public double getX() {
        return springX.getPosition();
    }

    /**
     * Returns the current smoothed world Y coordinate of the camera centre.
     *
     * @return camera centre Y (world pixels)
     */
    public double getY() {
        return springY.getPosition();
    }

    // ------------------------------------------------------------------------
    // Snapping flag (avoids initial lerp glitch)
    // ------------------------------------------------------------------------

    /**
     * Returns whether the camera has been snapped to its initial position.
     *
     * @return {@code true} if the camera has already performed its first snap
     */
    public boolean isSnapped() {
        return snapped;
    }

    /**
     * Sets the snapped flag.
     *
     * @param snapped new snapped state
     */
    public void setSnapped(boolean snapped) {
        this.snapped = snapped;
    }

    // ------------------------------------------------------------------------
    // Viewport dimensions (screen pixels)
    // ------------------------------------------------------------------------

    /**
     * Returns the current screen (viewport) width in pixels.
     *
     * @return viewport width
     */
    public double getScreenWidth() {
        return screenWidth.get();
    }

    /**
     * JavaFX property for the screen width.
     *
     * @return the width property
     */
    public DoubleProperty screenWidthProperty() {
        return screenWidth;
    }

    /**
     * Sets the screen (viewport) width.
     *
     * @param width new width in pixels
     */
    public void setScreenWidth(double width) {
        this.screenWidth.set(width);
    }

    /**
     * Returns the current screen (viewport) height in pixels.
     *
     * @return viewport height
     */
    public double getScreenHeight() {
        return screenHeight.get();
    }

    /**
     * JavaFX property for the screen height.
     *
     * @return the height property
     */
    public DoubleProperty screenHeightProperty() {
        return screenHeight;
    }

    /**
     * Sets the screen (viewport) height.
     *
     * @param height new height in pixels
     */
    public void setScreenHeight(double height) {
        this.screenHeight.set(height);
    }

    /**
     * Returns half of the screen width.
     * <p>Equivalent to {@code getScreenWidth() / 2.0}.</p>
     *
     * @return screen centre X offset
     */
    public double getScreenCenterX() {
        return getScreenWidth() / 2.0;
    }

    /**
     * Returns half of the screen height.
     * <p>Equivalent to {@code getScreenHeight() / 2.0}.</p>
     *
     * @return screen centre Y offset
     */
    public double getScreenCenterY() {
        return getScreenHeight() / 2.0;
    }

    // ------------------------------------------------------------------------
    // Rendering helpers – the "golden equations"
    // ------------------------------------------------------------------------

    /**
     * Computes the world X coordinate of the viewport's left edge.
     * <p>Use this value to translate the graphics context so that the camera's
     * centre appears in the middle of the screen.</p>
     *
     * @return left edge world X (pixels)
     */
    public double getViewportLeftX() {
        return getX() - getScreenCenterX();
    }

    /**
     * Computes the world Y coordinate of the viewport's top edge.
     *
     * @return top edge world Y (pixels)
     */
    public double getViewportTopY() {
        return getY() - getScreenCenterY();
    }
}