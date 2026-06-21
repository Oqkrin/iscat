package uni.gaben.iscat.universe.effects;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Registra la storia posizionale di un'entità in un dash e ne gestisce il fading graduato.
 * Ogni punto nella storia decaya nel tempo. Quando tutti i punti sono svaniti, il trail è inattivo.
 */
public class DashTrail {

    /** Un singolo punto della scia con posizione e alpha residua. */
    public record TrailPoint(double x, double y, double alpha, double width) {}

    // INCREASED: Da 48 a 150 per contenere una coda molto più lunga
    private static final int MAX_POINTS = 373;

    // DECREASED: Da 1.8 a 0.4 per far svanire la scia molto più lentamente
    private static final double FADE_RATE = 0.1;

    private final Deque<TrailPoint> points = new ArrayDeque<>(MAX_POINTS);

    public void addPoint(double x, double y, double width) {
        if (points.size() >= MAX_POINTS) {
            points.pollFirst();
        }
        points.addLast(new TrailPoint(x, y, 1.0, width));
    }

    public void update(double dt) {
        points.removeIf(p -> p.alpha() <= 0);

        TrailPoint[] arr = points.toArray(new TrailPoint[0]);
        points.clear();
        for (TrailPoint p : arr) {
            double newAlpha = Math.max(0, p.alpha() - FADE_RATE * dt);
            if (newAlpha > 0) {
                points.addLast(new TrailPoint(p.x(), p.y(), newAlpha, p.width()));
            }
        }
    }

    public boolean isEmpty() {
        return points.isEmpty();
    }

    public Deque<TrailPoint> getPoints() {
        return points;
    }

    public void clear() {
        points.clear();
    }
}