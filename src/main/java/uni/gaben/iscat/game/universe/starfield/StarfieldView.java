package uni.gaben.iscat.game.universe.starfield;

import javafx.beans.property.DoubleProperty;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import uni.gaben.iscat.game.lib.interfaces.view.Drawable;
import uni.gaben.iscat.game.universe.UniverseSettings;
import uni.gaben.iscat.utils.ThemeColors;

public class StarfieldView implements Drawable<StarfieldModel> {
    private final Color[] accents;

    public double getCameraX() {
        return cameraX.get();
    }

    public DoubleProperty cameraXProperty() {
        return cameraX;
    }

    public void setCameraX(double cameraX) {
        this.cameraX.set(cameraX);
    }

    public double getCameraY() {
        return cameraY.get();
    }

    public DoubleProperty cameraYProperty() {
        return cameraY;
    }

    public void setCameraY(double cameraY) {
        this.cameraY.set(cameraY);
    }

    public double getW() {
        return w.get();
    }

    public DoubleProperty wProperty() {
        return w;
    }

    public void setW(double w) {
        this.w.set(w);
    }

    public double getH() {
        return h.get();
    }

    public DoubleProperty hProperty() {
        return h;
    }

    public void setH(double h) {
        this.h.set(h);
    }

    DoubleProperty cameraX, cameraY, w, h;

    public StarfieldView() {
        this.accents = new Color[]{
                ThemeColors.getAccentPrimary(),
                ThemeColors.getAccentSecondary(),
                ThemeColors.getAccentTertiary()
        };
        this.cameraX = new javafx.beans.property.SimpleDoubleProperty(0);
        this.cameraY = new javafx.beans.property.SimpleDoubleProperty(0);
        this.w = new javafx.beans.property.SimpleDoubleProperty(UniverseSettings.DEFAULT_WIDTH);
        this.h = new javafx.beans.property.SimpleDoubleProperty(UniverseSettings.DEFAULT_HEIGHT);
    }

    @Override
    public void draw(StarfieldModel entity, GraphicsContext gc) {
        gc.setGlobalAlpha(UniverseSettings.STAR_ALPHA);
        int starIdx = 0;
        for (StarModel star : entity.getStars()) {
            gc.setFill(accents[starIdx % accents.length]);
            double s = star.getSize();

            // Parallax calculation
            double depth = UniverseSettings.PARALLAX_BASE + (s / UniverseSettings.PARALLAX_SIZE_DIVISOR) * UniverseSettings.PARALLAX_FACTOR;

            double renderX = (star.getX() - getCameraX() * depth) % getW();
            if (renderX < 0) renderX += getW();
            double renderY = (star.getY() - getCameraY() * depth) % getH();
            if (renderY < 0) renderY += getH();

            gc.fillRect(renderX, renderY, s, s);
            starIdx++;
        }
        gc.setGlobalAlpha(1.0);
    }
}
