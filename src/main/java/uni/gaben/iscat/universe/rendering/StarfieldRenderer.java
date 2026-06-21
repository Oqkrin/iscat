package uni.gaben.iscat.universe.rendering;

import javafx.scene.canvas.GraphicsContext;
import uni.gaben.iscat.universe.UniverseSettings;
import uni.gaben.iscat.universe.effects.Star;
import uni.gaben.iscat.universe.effects.Starfield;
import uni.gaben.iscat.utils.theme.ThemeManager;

/**
 * Renderer stateless per lo sfondo stellato in parallasse (Starfield Renderer).
 * <p>
 * Riceve le coordinate e le dimensioni dello schermo tramite setter primitivi a ogni frame.
 * L'assenza di property binding JavaFX previene i memory leak che causavano lo schermo nero al riavvio.
 * </p>
 */
public class StarfieldRenderer implements Renderable<Starfield> {

    private double cameraX;
    private double cameraY;
    private double w = UniverseSettings.DEFAULT_WIDTH;
    private double h = UniverseSettings.DEFAULT_HEIGHT;

    /**
     * Renderizza il campo stellato applicando l'effetto parallasse basato sulla profondità (dimensione) di ogni stella.
     */
    @Override
    public void render(Starfield model, GraphicsContext gc) {
        gc.save();
        if (model == null || model.getStars().isEmpty()) return;

        ThemeManager theme = ThemeManager.getInstance();
        double safeW = w > 0 ? w : UniverseSettings.DEFAULT_WIDTH;
        double safeH = h > 0 ? h : UniverseSettings.DEFAULT_HEIGHT;

        gc.setGlobalAlpha(UniverseSettings.STAR_ALPHA);

        for (Star star : model.getStars()) {
            double s = star.getSize();
            // Calcolo del fattore di parallasse accoppiato alla profondità visiva della stella
            double depth = UniverseSettings.PARALLAX_BASE + (s / UniverseSettings.PARALLAX_SIZE_DIVISOR) * UniverseSettings.PARALLAX_FACTOR;

            if      (s < 1.5) gc.setFill(theme.getAccentTernary());
            else if (s < 2.8) gc.setFill(theme.getAccentSecondary());
            else              gc.setFill(theme.getAccentPrimary());

            // Avvolgimento (wrapping) logico delle coordinate per l'effetto di sfondo infinito
            double rx = (star.getX() - cameraX * depth) % safeW;
            if (rx < 0) rx += safeW;

            double ry = (star.getY() - cameraY * depth) % safeH;
            if (ry < 0) ry += safeH;

            gc.fillRect(rx, ry, s, s);
        }

        gc.setGlobalAlpha(1.0);
        gc.restore();
    }

    // --- Setters e Getters di Runtime per Frame ---

    public void setCameraX(double x) { this.cameraX = x; }
    public void setCameraY(double y) { this.cameraY = y; }
    public void setW(double w)       { this.w = w; }
    public void setH(double h)       { this.h = h; }

    public double getW()       { return w; }
    public double getH()       { return h; }
}