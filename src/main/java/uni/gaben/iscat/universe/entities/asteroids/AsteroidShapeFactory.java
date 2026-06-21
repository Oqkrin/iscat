package uni.gaben.iscat.universe.entities.asteroids;

import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Vector2;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Fabbrica procedurale e cache geometrica per le forme degli asteroidi (Procedural Shape Factory).
 * <p>
 * Genera un set pre-computato di varianti poligonali normalizzate (raggio unitario $\approx 1.0$)
 * durante l'inizializzazione della classe per evitare calcoli matematici pesanti nel loop di gioco.
 * </p>
 * <p>
 * <b>Algoritmi e Ottimizzazioni:</b>
 * </p>
 * <ul>
 * <li><b>Convex Hull (Monotone Chain):</b> Garantisce che la mesh poligonale generata sia strettamente convessa,
 * requisito fondamentale per la stabilità degli algoritmi di collisione di GJK/EPA del motore dyn4j.</li>
 * <li><b>Barycenter Normalization:</b> Trasla i vertici calcolati in modo che il baricentro geometrico coincida esattamente
 * con l'origine $(0,0)$, prevenendo disallineamenti o sfarfallii (jitter) durante le rotazioni fisiche.</li>
 * </ul>
 */
public class AsteroidShapeFactory {

    /** Lista statica di forme poligonali pre-computate a raggio unitario. */
    private static final List<Vector2[]> CACHED_SHAPES = new ArrayList<>();

    /** Numero totale di varianti poligonali uniche da generare nella cache ($15$). */
    private static final int SHAPE_VARIANTS = 15;

    static {
        // Inizializzazione con seed fisso per garantire il determinismo visivo tra esecuzioni diverse
        Random rand = new Random(12345);
        for (int i = 0; i < SHAPE_VARIANTS; i++) {
            int numVertices = AsteroidSettings.MIN_VERTICES + rand.nextInt(AsteroidSettings.VERTICE_VARIATION);
            Vector2[] rawPoints = new Vector2[numVertices];
            double angleStep = Math.PI * 2 / numVertices;

            // Generazione radiale dei punti grezzi con applicazione di jitter casuale sul raggio
            for (int j = 0; j < numVertices; j++) {
                double angle = j * angleStep;
                double r = AsteroidSettings.RADIUS_VARIATION_MIN + rand.nextDouble() * AsteroidSettings.RADIUS_VARIATION_RANGE;
                rawPoints[j] = new Vector2(Math.cos(angle) * r, Math.sin(angle) * r);
            }

            // Calcolo dell'inviluppo convesso per ripulire eventuali auto-intersezioni o concavità
            List<Vector2> hull = convexHull(rawPoints);

            // Centratura del poligono: calcola il baricentro e trasla i vertici sull'origine (0,0)
            Polygon tempPoly = new Polygon(hull.toArray(new Vector2[0]));
            Vector2 center = tempPoly.getCenter();
            tempPoly.translate(-center.x, -center.y);

            CACHED_SHAPES.add(tempPoly.getVertices());
        }
    }

    private static final Random dynamicRand = new Random();

    /**
     * Estrae una forma geometrica casuale dalla cache e la scala alla dimensione metrica richiesta.
     *
     * @param radiusMeters Il raggio di scala (in metri del mondo fisico) da applicare alla mesh.
     * @return Un array di vettori {@link Vector2} pronti per essere passati alle fixture del motore fisico.
     */
    public static Vector2[] getScaledShape(double radiusMeters) {
        Vector2[] baseShape = CACHED_SHAPES.get(dynamicRand.nextInt(CACHED_SHAPES.size()));
        Vector2[] scaled = new Vector2[baseShape.length];
        for (int i = 0; i < baseShape.length; i++) {
            scaled[i] = new Vector2(baseShape[i].x * radiusMeters, baseShape[i].y * radiusMeters);
        }
        return scaled;
    }

    /**
     * Calcola l'inviluppo convesso (Convex Hull) di un set di punti bidimensionali.
     * Implementa l'algoritmo di Andrew (Monotone Chain), con complessità temporale $O(n \log n)$ legata all'ordinamento,
     * separando il calcolo nella catena inferiore (lower) e superiore (upper).
     *
     * @param points L'array di punti grezzi generati radialmente.
     * @return Una lista ordinata di vertici che compongono il guscio convesso esterno.
     */
    private static List<Vector2> convexHull(Vector2[] points) {
        if (points == null || points.length < 3) {
            return points == null ? new ArrayList<>() : java.util.Arrays.asList(points);
        }

        // Ordinamento dei punti per coordinata X crescente, e Y crescente a parità di X
        List<Vector2> sorted = new ArrayList<>(java.util.Arrays.asList(points));
        sorted.sort((a, b) -> {
            if (a.x != b.x) return Double.compare(a.x, b.x);
            return Double.compare(a.y, b.y);
        });

        // Costruzione dello scafo inferiore (Lower Hull)
        List<Vector2> lower = new ArrayList<>();
        for (Vector2 point : sorted) {
            while (lower.size() >= 2 && ccw(lower.get(lower.size() - 2), lower.getLast(), point) <= 0) {
                lower.removeLast();
            }
            lower.add(point);
        }

        // Costruzione dello scafo superiore (Upper Hull)
        List<Vector2> upper = new ArrayList<>();
        for (int i = sorted.size() - 1; i >= 0; i--) {
            Vector2 point = sorted.get(i);
            while (upper.size() >= 2 && ccw(upper.get(upper.size() - 2), upper.getLast(), point) <= 0) {
                upper.removeLast();
            }
            upper.add(point);
        }

        // Rozione dei duplicati agli estremi di giunzione delle due catene
        lower.removeLast();
        upper.removeLast();

        List<Vector2> hull = new ArrayList<>();
        hull.addAll(lower);
        hull.addAll(upper);

        return hull;
    }

    /**
     * Calcola il prodotto vettoriale (Cross Product) dei vettori $AB$ e $AC$ per determinare l'orientamento.
     * Sfrutta il segno del determinante 2D per capire la direzione della svolta (Counter-Clockwise).
     *
     * @return Un valore {@code > 0} per svolte a sinistra (antiorario), {@code < 0} per svolte a destra (orario),
     * e {@code 0} se i tre punti sono collineari.
     */
    private static double ccw(Vector2 a, Vector2 b, Vector2 c) {
        return (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x);
    }

    /**
     * Costruttore privato per impedire l'istanza di una classe di utilità a soli scopi statici.
     */
    private AsteroidShapeFactory() {
        /* This utility class should not be instantiated */
    }
}