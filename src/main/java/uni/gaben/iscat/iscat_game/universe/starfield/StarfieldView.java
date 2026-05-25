package uni.gaben.iscat.iscat_game.universe.starfield;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import uni.gaben.iscat.iscat_game.lib.interfaces.view.Drawable;
import uni.gaben.iscat.iscat_game.universe.UniverseSettings;
import uni.gaben.iscat.utils.ThemeManager;

public class StarfieldView implements Drawable<StarfieldModel> {
    private final Color[] accents;
    private final DoubleProperty cameraX = new SimpleDoubleProperty(0);
    private final DoubleProperty cameraY = new SimpleDoubleProperty(0);
    private final DoubleProperty w = new SimpleDoubleProperty(UniverseSettings.DEFAULT_WIDTH);
    private final DoubleProperty h = new SimpleDoubleProperty(UniverseSettings.DEFAULT_HEIGHT);

    public StarfieldView() {
        this.accents = new Color[]{
                ThemeManager.getInstance().getAccentPrimary(),
                ThemeManager.getInstance().getAccentSecondary(),
                ThemeManager.getInstance().getAccentTertiary()
        };
    }

    @Override
    public void draw(StarfieldModel entity, GraphicsContext gc) {
        // Se il modello è vuoto (es. appena resettato e non ancora ripopolato), non disegna nulla
        if (entity == null || entity.getStars().isEmpty()) return;

        gc.setGlobalAlpha(UniverseSettings.STAR_ALPHA);
        int starIdx = 0;

        // FIXED: Utilizziamo le proprietà interne della View (w e h) che riflettono il Canvas.
        // Forniamo un fallback pulito alle impostazioni di default se non sono ancora bindate (> 0)
        double currentW = getW() > 0 ? getW() : UniverseSettings.DEFAULT_WIDTH;
        double currentH = getH() > 0 ? getH() : UniverseSettings.DEFAULT_HEIGHT;

        for (StarModel star : entity.getStars()) {
            gc.setFill(accents[starIdx % accents.length]);
            double s = star.getSize();

            // Calcolo del fattore di parallasse in base alla profondità della stella
            double depth = UniverseSettings.PARALLAX_BASE +
                    (s / UniverseSettings.PARALLAX_SIZE_DIVISOR) * UniverseSettings.PARALLAX_FACTOR;

            // Movimento relativo opposto alla telecamera con avvolgimento (wrapping) dello schermo
            double renderX = (star.getX() - getCameraX() * depth) % currentW;
            if (renderX < 0) renderX += currentW;

            double renderY = (star.getY() - getCameraY() * depth) % currentH;
            if (renderY < 0) renderY += currentH;

            gc.fillRect(renderX, renderY, s, s);
            starIdx++;
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