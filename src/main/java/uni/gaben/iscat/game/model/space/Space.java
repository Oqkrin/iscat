package uni.gaben.iscat.game.model.space;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import uni.gaben.iscat.game.model.GameSettings;
import uni.gaben.iscat.game.model.entities.Star;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Sfondo stellato con parallasse.
 * La velocità di scorrimento segue quella del giocatore via lerp (stabile con dt=1.0).
 * Il dodge applica un impulso diretto alla velocità di scorrimento corrente,
 * che poi decade naturalmente verso il target tramite il lerp successivo.
 */
public class Space {

    private final IntegerProperty width  = new SimpleIntegerProperty();
    private final IntegerProperty height = new SimpleIntegerProperty();

    public List<Star> stars = new ArrayList<>();

    /** Velocità di scorrimento corrente — lerp verso il target ogni tick. */
    private double scrollVx = 0;
    private double scrollVy = 0;

    private final Random rand = new Random();

    public Space(int width, int height) {
        this.width.set(width);
        this.height.set(height);
        instantiateSpace();
        this.width .addListener((ov, o, n) -> instantiateSpace());
        this.height.addListener((ov, o, n) -> instantiateSpace());
    }

    void instantiateSpace() {
        stars = new ArrayList<>(GameSettings.Visuale.NUMERO_STELLE);
        for (int i = 0; i < GameSettings.Visuale.NUMERO_STELLE; i++) {
            double x = rand.nextDouble() * Math.max(1, getWidth());
            double y = rand.nextDouble() * Math.max(1, getHeight());
            double t = Math.pow(rand.nextDouble(), GameSettings.Visuale.POTENZA_DIMENSIONE_STELLA);
            double size = GameSettings.Visuale.DIMENSIONE_STELLA_MIN + 
                         t * (GameSettings.Visuale.DIMENSIONE_STELLA_MAX - GameSettings.Visuale.DIMENSIONE_STELLA_MIN);
            stars.add(new Star(x, y, size));
        }
    }

    public void update(double targetVx, double targetVy) {
        scrollVx = GameSettings.Visuale.EASING_STELLE.apply(scrollVx, targetVx, GameSettings.Visuale.LERP_STELLE);
        scrollVy = GameSettings.Visuale.EASING_STELLE.apply(scrollVy, targetVy, GameSettings.Visuale.LERP_STELLE);

        int w = Math.max(1, getWidth());
        int h = Math.max(1, getHeight());

        for (Star star : stars) {
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
