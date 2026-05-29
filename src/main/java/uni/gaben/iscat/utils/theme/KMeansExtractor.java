package uni.gaben.iscat.utils.theme;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * K-Means++ color extraction from a {@link BufferedImage}.
 *
 * Responsibilities:
 *   • Sub-sampling pixels from the image at a configurable step
 *   • Seeding centroids via the K-Means++ distance-weighted strategy
 *   • Running Lloyd's iterations to converge cluster centroids
 *
 * The result is a list of {@code k} representative {@link Color} values.
 * No perceptual math lives here — see {@link ColorScience} for that.
 */
final class KMeansExtractor {

    /** Sample every Nth pixel in both dimensions to keep extraction fast. */
    static final int PIXEL_SAMPLE_STEP = 5;

    /** Number of Lloyd's iterations to run. */
    private static final int ITERATIONS = 10;

    private KMeansExtractor() {}

    /**
     * Extracts {@code k} representative colors from {@code image} using K-Means++.
     *
     * @param image source image (not null)
     * @param k     number of clusters / colors to return
     * @return list of exactly {@code k} {@link Color} centroids
     */
    static List<Color> extract(BufferedImage image, int k) {
        List<int[]> pixels = samplePixels(image);
        List<int[]> centroids = seedCentroids(pixels, k);
        centroids = iterate(pixels, centroids, k);

        List<Color> result = new ArrayList<>(k);
        for (int[] c : centroids) {
            result.add(new Color(c[0], c[1], c[2]));
        }
        return result;
    }

    // ─── Pixel Sampling ──────────────────────────────────────────────────────

    private static List<int[]> samplePixels(BufferedImage image) {
        List<int[]> pixels = new ArrayList<>();
        for (int x = 0; x < image.getWidth(); x += PIXEL_SAMPLE_STEP) {
            for (int y = 0; y < image.getHeight(); y += PIXEL_SAMPLE_STEP) {
                int rgb = image.getRGB(x, y);
                pixels.add(new int[]{
                        (rgb >> 16) & 0xFF,
                        (rgb >>  8) & 0xFF,
                         rgb        & 0xFF
                });
            }
        }
        return pixels;
    }

    // ─── K-Means++ Seeding ───────────────────────────────────────────────────

    /** Initialises centroids using the K-Means++ distance-weighted strategy. */
    private static List<int[]> seedCentroids(List<int[]> pixels, int k) {
        List<int[]> centroids = new ArrayList<>(k);
        Random rand = new Random();
        centroids.add(pixels.get(rand.nextInt(pixels.size())));

        while (centroids.size() < k) {
            centroids.add(farthestPixel(pixels, centroids));
        }
        return centroids;
    }

    /**
     * Picks the pixel whose minimum squared distance to any existing centroid
     * is the greatest — the K-Means++ "farthest point" seed step.
     */
    private static int[] farthestPixel(List<int[]> pixels, List<int[]> centroids) {
        int[] best    = pixels.get(0);
        double maxDist = -1;

        for (int[] p : pixels) {
            double minDist = Double.MAX_VALUE;
            for (int[] c : centroids) {
                double d = squaredDist(p, c);
                if (d < minDist) minDist = d;
            }
            if (minDist > maxDist) {
                maxDist = minDist;
                best    = p;
            }
        }
        return best;
    }

    // ─── Lloyd's Iterations ──────────────────────────────────────────────────

    private static List<int[]> iterate(List<int[]> pixels, List<int[]> centroids, int k) {
        for (int iter = 0; iter < ITERATIONS; iter++) {
            // Assign each pixel to its nearest centroid
            List<List<int[]>> clusters = new ArrayList<>(k);
            for (int i = 0; i < k; i++) clusters.add(new ArrayList<>());

            for (int[] p : pixels) {
                int nearest  = 0;
                double minD  = Double.MAX_VALUE;
                for (int i = 0; i < k; i++) {
                    double d = squaredDist(p, centroids.get(i));
                    if (d < minD) { minD = d; nearest = i; }
                }
                clusters.get(nearest).add(p);
            }

            // Recompute each centroid as the mean of its cluster
            for (int i = 0; i < k; i++) {
                List<int[]> cluster = clusters.get(i);
                if (cluster.isEmpty()) continue;

                long r = 0, g = 0, b = 0;
                for (int[] p : cluster) { r += p[0]; g += p[1]; b += p[2]; }
                int n = cluster.size();
                centroids.set(i, new int[]{ (int)(r/n), (int)(g/n), (int)(b/n) });
            }
        }
        return centroids;
    }

    // ─── Util ────────────────────────────────────────────────────────────────

    private static double squaredDist(int[] a, int[] b) {
        return Math.pow(a[0] - b[0], 2)
             + Math.pow(a[1] - b[1], 2)
             + Math.pow(a[2] - b[2], 2);
    }
}
