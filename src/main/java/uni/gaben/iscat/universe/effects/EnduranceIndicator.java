package uni.gaben.iscat.universe.effects;

import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.camera.CameraModel;
import uni.gaben.iscat.universe.entity.interfaces.Removable;
import uni.gaben.iscat.universe.entity.interfaces.Stateful;
import uni.gaben.iscat.utils.Updatable;

public class EnduranceIndicator implements Stateful, Updatable, Removable {

    // Configuration constants – can be made configurable if needed
    public static final double LIFETIME = 1.2;        // seconds
    public static final double RISE_SPEED = 60.0;     // pixels per second
    public static final Font FONT = Font.font("Miracode", FontWeight.BOLD, 18);

    public final double x;          // initial screen X
    public double y;                // current screen Y (moves up)
    public final double value;      // displayed value (negative = damage, positive = heal)
    private double duration = 0.0;   // elapsed time
    public double alpha = 1.0;
    private boolean shouldRemove = false;

    private EnduranceIndicator(double screenX, double screenY, double value) {
        this.x = screenX;
        this.y = screenY;
        this.value = value;
    }

    /**
     * Factory method: converts a world position to screen coordinates and creates a new indicator.
     */
    public static EnduranceIndicator create(Vector2 worldPos, CameraModel camera,
                                            double value, double canvasWidth, double canvasHeight) {
        double px = UU.mToPx(worldPos.x);
        double py = UU.mToPx(worldPos.y);
        double zoom = camera.getZoom();

        double screenX = (px - camera.getX()) * zoom + canvasWidth / 2.0;
        double screenY = (py - camera.getY()) * zoom + canvasHeight / 2.0;

        return new EnduranceIndicator(screenX, screenY, value);
    }

    // ------------------------------------------------------------------------
    // Lifecycle methods
    // ------------------------------------------------------------------------

    @Override
    public void update(double dt) {
        duration += dt;
        if (duration >= LIFETIME) {
            shouldRemove = true;
            return;
        }
        y -= RISE_SPEED * dt;
        alpha = 1.0 - (duration / LIFETIME);
    }

    @Override
    public boolean shouldRemove() {
        return shouldRemove;
    }

    @Override
    public boolean setShouldRemove(boolean remove) {
        this.shouldRemove = remove;
        return true;
    }

    // ------------------------------------------------------------------------
    // Rendering
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Stateful interface (if needed by other parts of the engine)
    // ------------------------------------------------------------------------

    @Override
    public int getState() { return 0; }

    @Override
    public void setState(int state) { /* not used */ }

    @Override
    public double getStateTime() { return duration; }

    @Override
    public void setStateTime(double stateTime) { this.duration = stateTime; }

    @Override
    public void updateStateTime(double dt) { duration += dt; }
}