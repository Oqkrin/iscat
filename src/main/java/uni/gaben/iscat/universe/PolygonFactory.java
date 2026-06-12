package uni.gaben.iscat.universe;

import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Vector2;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PolygonFactory {
    private PolygonFactory() {
        /* This utility class should not be instantiated */
    }

    // Cache of pre-computed normalized hulls (radius = 1.0)
    private static final List<Vector2[]> CACHED_SHAPES = new ArrayList<>();
    private static final int SHAPE_VARIANTS = 15;
    
    static {
        Random rand = new Random(12345);
        for (int i = 0; i < SHAPE_VARIANTS; i++) {
            int numVertices = AsteroidSettings.MIN_VERTICES + rand.nextInt(AsteroidSettings.VERTICE_VARIATION);
            Vector2[] rawPoints = new Vector2[numVertices];
            double angleStep = Math.PI * 2 / numVertices;

            for (int j = 0; j < numVertices; j++) {
                double angle = j * angleStep;
                double r = AsteroidSettings.RADIUS_VARIATION_MIN + rand.nextDouble() * AsteroidSettings.RADIUS_VARIATION_RANGE;
                rawPoints[j] = new Vector2(Math.cos(angle) * r, Math.sin(angle) * r);
            }

            List<Vector2> hull = convexHull(rawPoints);

            Polygon tempPoly = new Polygon(hull.toArray(new Vector2[0]));
            Vector2 center = tempPoly.getCenter();
            tempPoly.translate(-center.x, -center.y);
            
            CACHED_SHAPES.add(tempPoly.getVertices());
        }
    }
    
    private static final Random dynamicRand = new Random();

    /**
     * Retrieves a pre-computed unit shape and scales it to the required radius.
     * @param radiusMeters the scale factor
     * @return the scaled vertices ready for the physics engine
     */
    public static Vector2[] getVerticesByRadius(double radiusMeters) {
        Vector2[] baseShape = CACHED_SHAPES.get(dynamicRand.nextInt(CACHED_SHAPES.size()));
        Vector2[] scaled = new Vector2[baseShape.length];
        for (int i = 0; i < baseShape.length; i++) {
            scaled[i] = new Vector2(baseShape[i].x * radiusMeters, baseShape[i].y * radiusMeters);
        }
        return scaled;
    }

    private static List<Vector2> convexHull(Vector2[] points) {
        if (points == null || points.length < 3) {
            return points == null ? new ArrayList<>() : java.util.Arrays.asList(points);
        }

        List<Vector2> sorted = new ArrayList<>(java.util.Arrays.asList(points));
        sorted.sort((a, b) -> {
            if (a.x != b.x) return Double.compare(a.x, b.x);
            return Double.compare(a.y, b.y);
        });

        List<Vector2> lower = new ArrayList<>();
        for (Vector2 point : sorted) {
            while (lower.size() >= 2 && ccw(lower.get(lower.size() - 2), lower.getLast(), point) <= 0) {
                lower.removeLast();
            }
            lower.add(point);
        }

        List<Vector2> upper = new ArrayList<>();
        for (int i = sorted.size() - 1; i >= 0; i--) {
            Vector2 point = sorted.get(i);
            while (upper.size() >= 2 && ccw(upper.get(upper.size() - 2), upper.getLast(), point) <= 0) {
                upper.removeLast();
            }
            upper.add(point);
        }

        lower.removeLast();
        upper.removeLast();

        List<Vector2> hull = new ArrayList<>();
        hull.addAll(lower);
        hull.addAll(upper);

        return hull;
    }

    private static double ccw(Vector2 a, Vector2 b, Vector2 c) {
        return (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x);
    }
}
