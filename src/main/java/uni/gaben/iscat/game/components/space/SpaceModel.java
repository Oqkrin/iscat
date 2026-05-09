package uni.gaben.iscat.game.components.space;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import uni.gaben.iscat.game.components.entities.objects.StarModel;
import uni.gaben.iscat.game.utils.settings.VisualSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Sfondo stellato con parallasse.
 * La velocità di scorrimento segue quella del giocatore via lerp (stabile con dt=1.0).
 * Il dodge applica un impulso diretto alla velocità di scorrimento corrente,
 * che poi decade naturalmente verso il target tramite il lerp successivo.
 */
public class SpaceModel {

    private final IntegerProperty width  = new SimpleIntegerProperty();
    private final IntegerProperty height = new SimpleIntegerProperty();

    public List<StarModel> stars = new ArrayList<>();

    /** Velocità di scorrimento corrente — lerp verso il target ogni tick. */
    private double scrollVx = 0;
    private double scrollVy = 0;

    private final Random rand = new Random();

    public SpaceModel(int width, int height) {
        this.width.set(width);
        this.height.set(height);
        instantiateSpace();
        // Listener only triggers if BOTH dimensions are valid
        this.width.addListener((ov, o, n) -> {
            if (n.intValue() > 0 && getHeight() > 0) {
                instantiateSpace();
            }
        });
        this.height.addListener((ov, o, n) -> {
            if (n.intValue() > 0 && getWidth() > 0) {
                instantiateSpace();
            }
        });
    }

    void instantiateSpace() {
        stars = new ArrayList<>(VisualSettings.NUMERO_STELLE);
        for (int i = 0; i < VisualSettings.NUMERO_STELLE; i++) {
            double x = rand.nextDouble() * Math.max(1, getWidth());
            double y = rand.nextDouble() * Math.max(1, getHeight());
            double t = Math.pow(rand.nextDouble(), VisualSettings.POTENZA_DIMENSIONE_STELLA);
            double size = VisualSettings.DIMENSIONE_STELLA_MIN +
                         t * (VisualSettings.DIMENSIONE_STELLA_MAX - VisualSettings.DIMENSIONE_STELLA_MIN);
            stars.add(new StarModel(x, y, size));
        }
    }

    public void update(double targetVx, double targetVy) {
        scrollVx = VisualSettings.EASING_STELLE.apply(scrollVx, targetVx, VisualSettings.LERP_STELLE);
        scrollVy = VisualSettings.EASING_STELLE.apply(scrollVy, targetVy, VisualSettings.LERP_STELLE);

        int w = Math.max(1, getWidth());
        int h = Math.max(1, getHeight());

        for (StarModel star : stars) {
            double nx = star.getX() - scrollVx;
            double ny = star.getY() - scrollVy;
            if (nx < 0)  nx += w;
            if (nx >= w) nx -= w;
            if (ny < 0)  ny += h;
            if (ny >= h) ny -= h;
            star.setX(nx);
            star.setY(ny);
        }
    }

    /**
     * Impulso diretto alla velocità di scorrimento (es. dodge).
     * Il lerp nel tick successivo lo riassorbe gradualmente — nessuna instabilità.
     */
    public void applyImpulse(double dvx, double dvy) {
        scrollVx += dvx;
        scrollVy += dvy;
    }

    // --- proprietà ---

    public int getWidth()  { return width.get(); }
    public int getHeight() { return height.get(); }

    public IntegerProperty widthProperty()  { return width; }
    public IntegerProperty heightProperty() { return height; }

    public void setWidth(int w)  { width.set(w); }
    public void setHeight(int h) { height.set(h); }
}
