package uni.gaben.iscat.universe.enviroment.starfield;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import uni.gaben.iscat.universe.lib.interfaces.view.Drawable;
import uni.gaben.iscat.universe.UniverseSettings;
import uni.gaben.iscat.utils.theme.ThemeManager;

public class StarfieldView implements Drawable<StarfieldModel> {

    private final DoubleProperty cameraX = new SimpleDoubleProperty(0);
    private final DoubleProperty cameraY = new SimpleDoubleProperty(0);
    private final DoubleProperty w = new SimpleDoubleProperty(UniverseSettings.DEFAULT_WIDTH);
    private final DoubleProperty h = new SimpleDoubleProperty(UniverseSettings.DEFAULT_HEIGHT);

    public StarfieldView() {
        // Constructor left intentionality lean. Caching dynamic instances here causes reference stale-outs.
    }

    @Override
    public void draw(StarfieldModel entity, GraphicsContext gc) {
        if (entity == null || entity.getStars().isEmpty()) return;

        // FIX 1: Grab live references from ThemeManager directly during the active frame draw pass.
        ThemeManager theme = ThemeManager.getInstance();
        Color primary = theme.getAccentPrimary();
        Color secondary = theme.getAccentSecondary();
        Color ternary = theme.getAccentTernary();

        gc.setGlobalAlpha(UniverseSettings.STAR_ALPHA);

        // Utilize internal layout property configurations safely
        double currentW = getW() > 0 ? getW() : UniverseSettings.DEFAULT_WIDTH;
        double currentH = getH() > 0 ? getH() : UniverseSettings.DEFAULT_HEIGHT;

        for (StarModel star : entity.getStars()) {
            double s = star.getSize();

            // Calcolo del fattore di parallasse in base alla profondità della stella
            double depth = UniverseSettings.PARALLAX_BASE +
                    (s / UniverseSettings.PARALLAX_SIZE_DIVISOR) * UniverseSettings.PARALLAX_FACTOR;

            // FIX 2: Dynamic Chromatic Depth Mapping instead of raw index modulo.
            // Tiny background micro-stars get the subtle dark ternary accents.
            // Mid-range dust gets secondary accents, and foreground giants pop with primary accents.
            if (s < 1.5) {
                gc.setFill(ternary);
            } else if (s < 2.8) {
                gc.setFill(secondary);
            } else {
                gc.setFill(primary);
            }

            // Movimento relativo opposto alla telecamera con avvolgimento (wrapping) dello schermo
            double renderX = (star.getX() - getCameraX() * depth) % currentW;
            if (renderX < 0) renderX += currentW;

            double renderY = (star.getY() - getCameraY() * depth) % currentH;
            if (renderY < 0) renderY += currentH;

            // Use fillOval instead of fillRect for circular high-fidelity stars if preferred,
            // but keeping fillRect to preserve your exact performance constraints.
            gc.fillRect(renderX, renderY, s, s);
        }
        gc.setGlobalAlpha(1.0);
    }

    // ── GETTER E SETTER PROPERTISTICI ─────────────────────────────────────────
    public double getCameraX() { return cameraX.get(); }
    public DoubleProperty cameraXProperty() { return cameraX; }
    public void setCameraX(double cameraX) { this.cameraX.set(cameraX); }

    public double getCameraY() { return cameraY.get(); }
    public DoubleProperty cameraYProperty() { return cameraY; }
    public void setCameraY(double cameraY) { this.cameraY.set(cameraY); }

    public double getW() { return w.get(); }
    public DoubleProperty wProperty() { return w; }
    public void setW(double w) { this.w.set(w); }

    public double getH() { return h.get(); }
    public DoubleProperty hProperty() { return h; }
    public void setH(double h) { this.h.set(h); }
}