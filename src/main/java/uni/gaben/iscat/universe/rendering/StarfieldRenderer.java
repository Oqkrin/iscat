package uni.gaben.iscat.universe.rendering;

import javafx.scene.canvas.GraphicsContext;
import uni.gaben.iscat.universe.UniverseSettings;
import uni.gaben.iscat.universe.Star;
import uni.gaben.iscat.universe.Starfield;
import uni.gaben.iscat.utils.theme.ThemeManager;

/**
 * Stateless parallax starfield renderer.
 *
 * <p>Width, height and camera position are plain doubles set each frame by
 * the renderer — no JavaFX property bindings are needed or used. This
 * eliminates the binding leak that caused the black screen on game restart.</p>
 */
public class StarfieldRenderer implements Renderable<Starfield> {

    private double cameraX;
    private double cameraY;
    private double w = UniverseSettings.DEFAULT_WIDTH;
    private double h = UniverseSettings.DEFAULT_HEIGHT;

    @Override
    public void render(Starfield model, GraphicsContext gc) {
        if (model == null || model.getStars().isEmpty()) return;

        ThemeManager theme = ThemeManager.getInstance();
        double safeW = w > 0 ? w : UniverseSettings.DEFAULT_WIDTH;
        double safeH = h > 0 ? h : UniverseSettings.DEFAULT_HEIGHT;

        gc.setGlobalAlpha(UniverseSettings.STAR_ALPHA);

        for (Star star : model.getStars()) {
            double s = star.getSize();
            double depth = UniverseSettings.PARALLAX_BASE
                    + (s / UniverseSettings.PARALLAX_SIZE_DIVISOR) * UniverseSettings.PARALLAX_FACTOR;

            if      (s < 1.5) gc.setFill(theme.getAccentTernary());
            else if (s < 2.8) gc.setFill(theme.getAccentSecondary());
            else              gc.setFill(theme.getAccentPrimary());

            double rx = (star.getX() - cameraX * depth) % safeW;
            if (rx < 0) rx += safeW;

            double ry = (star.getY() - cameraY * depth) % safeH;
            if (ry < 0) ry += safeH;

            gc.fillRect(rx, ry, s, s);
        }

        gc.setGlobalAlpha(1.0);
    }

    // -------------------------------------------------------------------------
    // Per-frame setters (called by UniverseRenderer before draw)
    // -------------------------------------------------------------------------

    public void setCameraX(double x) { this.cameraX = x; }
    public void setCameraY(double y) { this.cameraY = y; }
    public void setW(double w)       { this.w = w; }
    public void setH(double h)       { this.h = h; }

    public double getCameraX() { return cameraX; }
    public double getCameraY() { return cameraY; }
    public double getW()       { return w; }
    public double getH()       { return h; }
}
