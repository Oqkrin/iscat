package uni.gaben.iscat.universe.rendering.vfx;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Registra la storia posizionale di un'entità in un dash e ne gestisce il fading graduato.
 * Ogni punto nella storia decaya nel tempo. Quando tutti i punti sono svaniti, il trail è inattivo.
 */
public class DashTrail {

    /** Un singolo punto della scia con posizione e alpha residua. */
    public record TrailPoint(double x, double y, double alpha, double width) {}

    /** Numero massimo di campioni posizionali mantenuti nella scia. */
    private static final int MAX_POINTS = 48;

    /** Velocità di decadimento dell'alpha per ogni punto al secondo. */
    private static final double FADE_RATE = 1.8;

    private final Deque<TrailPoint> points = new ArrayDeque<>(MAX_POINTS);

    /**
     * Aggiunge un nuovo campione posizionale alla scia.
     *
     * @param x     Posizione X in world-pixel del centro dell'entità.
     * @param y     Posizione Y in world-pixel.
     * @param width Larghezza dell'entità (usata per scalare lo spessore della scia).
     */
    public void addPoint(double x, double y, double width) {
        if (points.size() >= MAX_POINTS) {
            points.pollFirst(); // Rimuove il punto più vecchio
        }
        points.addLast(new TrailPoint(x, y, 1.0, width));
    }

    /**
     * Avanza il tempo per ogni punto, riducendone l'alpha. I punti completamente svaniti vengono rimossi.
     *
     * @param dt Delta time in secondi.
     */
    public void update(double dt) {
        points.removeIf(p -> p.alpha() <= 0);

        // Aggiorna in place — dato che TrailPoint è un record, ricostruiamo la deque
        TrailPoint[] arr = points.toArray(new TrailPoint[0]);
        points.clear();
        for (TrailPoint p : arr) {
            double newAlpha = Math.max(0, p.alpha() - FADE_RATE * dt);
            if (newAlpha > 0) {
                points.addLast(new TrailPoint(p.x(), p.y(), newAlpha, p.width()));
            }
        }
    }

    /**
     * @return {@code true} se non ci sono punti con alpha residua (trail completamente svanita).
     */
    public boolean isEmpty() {
        return points.isEmpty();
    }

    /**
     * @return La sequenza ordinata (dal più vecchio al più recente) dei punti della scia.
     */
    public Deque<TrailPoint> getPoints() {
        return points;
    }

    /**
     * Svuota manualmente la scia.
     */
    public void clear() {
        points.clear();
    }
}
