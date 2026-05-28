package uni.gaben.iscat.utils.theme;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.imageio.ImageIO;

public class DynamicColors {

    private static final double MIN_CONTRAST_RATIO = 3.0;
    private static final int K_MEANS_ITERATIONS = 10;
    private static final int PIXEL_SAMPLE_STEP = 5;

    private static final Map<String, List<Color>> THEME_CACHE = new LinkedHashMap<>(50, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, List<Color>> eldest) {
            return size() > 50;
        }
    };

    public static List<Color> getPaletteForFile(File file, int limit, boolean isLightMode) {
        String key = file.getAbsolutePath() + ":" + limit + ":" + isLightMode;
        if (THEME_CACHE.containsKey(key)) return THEME_CACHE.get(key);
        try {
            BufferedImage image = ImageIO.read(file);
            List<Color> palette = getTopDistinctColors(image, limit, isLightMode);
            THEME_CACHE.put(key, palette);
            return palette;
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    public static List<Color> getTopDistinctColors(BufferedImage image, int limit, boolean isLightMode) {
        List<Color> rawPalette = extractViaKMeans(image, limit * 2);
        List<Color> palette = pruneSimilarColorsAdaptive(rawPalette, limit);

        if (palette.isEmpty()) return fallbackDefault(isLightMode);

        float dominantHue = calculateDominantHue(palette);
        Color background = selectBackground(palette, isLightMode);

        // Calculate average saturation of existing palette to inform synthetic generation
        double avgSaturation = palette.stream().mapToDouble(DynamicColors::getSaturation).average().orElse(0.5);

        List<Color> accentCandidates = new ArrayList<>();
        for (Color c : palette) {
            if (c.equals(background)) continue;
            if (getContrastRatio(c, background) >= MIN_CONTRAST_RATIO) {
                accentCandidates.add(c);
            }
        }

        // Generate synthetic accents if needed, using the image's inherent saturation
        if (accentCandidates.size() < 3) {
            List<Color> generated = generateHarmoniousColors(
                    dominantHue, isLightMode, background, (float)avgSaturation,
                    3 - accentCandidates.size());
            accentCandidates.addAll(generated);
        }

        // --- NEW: Weighted Vibrancy Scoring instead of just Saturation ---
        // Scores based on 60% Hue Match, 40% Saturation
        accentCandidates.sort((c1, c2) -> {
            double score1 = calculateVibrancyScore(c1, dominantHue);
            double score2 = calculateVibrancyScore(c2, dominantHue);
            return Double.compare(score2, score1); // Descending
        });

        List<Color> topAccents = accentCandidates.subList(0, Math.min(3, accentCandidates.size()));

        List<Color> finalPalette = new ArrayList<>(topAccents);
        while (finalPalette.size() < 3) finalPalette.add(background);
        finalPalette.add(background);

        return finalPalette;
    }

    private static double calculateVibrancyScore(Color c, float targetHue) {
        double hDist = hueDistance(c, targetHue); // 0.0 to 0.5
        double saturation = getSaturation(c);
        // Normalize hue distance: 0 (match) becomes 1.0, 0.5 (opposite) becomes 0.0
        double hueScore = 1.0 - (hDist * 2.0);
        return (hueScore * 0.6) + (saturation * 0.4);
    }

    // --- Refined Synthetic Generation ---
    private static List<Color> generateHarmoniousColors(
            float dominantHue, boolean isLightMode, Color background, float baseSat, int needed) {

        List<Color> generated = new ArrayList<>();
        // Clamp saturation to reasonable range (0.3 - 0.6) so it's never neon
        float targetSaturation = (float) Math.max(0.3, Math.min(0.6, baseSat));

        float[] offsets = {0.33f, 0.66f, 0.5f}; // Triadic + Complementary
        for (float offset : offsets) {
            float hue = (dominantHue + offset) % 1.0f;
            Color c = createContrastingColor(hue, isLightMode, background, targetSaturation);
            if (c != null && getContrastRatio(c, background) >= MIN_CONTRAST_RATIO) {
                generated.add(c);
                if (generated.size() >= needed) break;
            }
        }
        return generated;
    }

    private static Color createContrastingColor(float hue, boolean isLightMode, Color background, float saturation) {
        // Adapt brightness based on mode, but keep saturation grounded
        float brightness = isLightMode ? 0.4f : 0.7f;
        return Color.getHSBColor(hue, saturation, brightness);
    }


    // --- Adaptive pruning (unchanged) ---
    private static List<Color> pruneSimilarColorsAdaptive(List<Color> palette, int limit) {
        if (palette.size() <= 1) return new ArrayList<>(palette);
        double maxDeltaE = 0;
        for (int i = 0; i < palette.size(); i++) {
            for (int j = i + 1; j < palette.size(); j++) {
                double de = getDeltaE(palette.get(i), palette.get(j));
                if (de > maxDeltaE) maxDeltaE = de;
            }
        }
        double threshold = (maxDeltaE < 5) ? 5 : 0.15 * maxDeltaE;
        List<Color> distinct = new ArrayList<>();
        for (Color c : palette) {
            boolean tooClose = false;
            for (Color existing : distinct) {
                if (getDeltaE(c, existing) < threshold) {
                    tooClose = true;
                    break;
                }
            }
            if (!tooClose) distinct.add(c);
            if (distinct.size() >= limit) break;
        }
        return distinct;
    }

    // --- K‑Means++ (unchanged) ---
    private static List<Color> extractViaKMeans(BufferedImage image, int k) {
        List<int[]> pixels = new ArrayList<>();
        for (int x = 0; x < image.getWidth(); x += PIXEL_SAMPLE_STEP) {
            for (int y = 0; y < image.getHeight(); y += PIXEL_SAMPLE_STEP) {
                int rgb = image.getRGB(x, y);
                pixels.add(new int[]{(rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF});
            }
        }
        List<int[]> centroids = new ArrayList<>();
        Random rand = new Random();
        centroids.add(pixels.get(rand.nextInt(pixels.size())));
        while (centroids.size() < k) {
            centroids.add(chooseNextCentroid(pixels, centroids));
        }
        for (int iter = 0; iter < K_MEANS_ITERATIONS; iter++) {
            List<List<int[]>> clusters = new ArrayList<>();
            for (int i = 0; i < k; i++) clusters.add(new ArrayList<>());
            for (int[] p : pixels) {
                int closestIndex = 0;
                double minDist = Double.MAX_VALUE;
                for (int i = 0; i < k; i++) {
                    double dist = Math.pow(p[0]-centroids.get(i)[0], 2) +
                            Math.pow(p[1]-centroids.get(i)[1], 2) +
                            Math.pow(p[2]-centroids.get(i)[2], 2);
                    if (dist < minDist) { minDist = dist; closestIndex = i; }
                }
                clusters.get(closestIndex).add(p);
            }
            for (int i = 0; i < k; i++) {
                if (clusters.get(i).isEmpty()) continue;
                long r = 0, g = 0, b = 0;
                for (int[] p : clusters.get(i)) { r += p[0]; g += p[1]; b += p[2]; }
                int count = clusters.get(i).size();
                centroids.set(i, new int[]{(int)(r/count), (int)(g/count), (int)(b/count)});
            }
        }
        List<Color> result = new ArrayList<>();
        for (int[] c : centroids) result.add(new Color(c[0], c[1], c[2]));
        return result;
    }

    private static int[] chooseNextCentroid(List<int[]> pixels, List<int[]> currentCentroids) {
        int[] best = pixels.get(0);
        double maxDist = -1;
        for (int[] p : pixels) {
            double minDist = Double.MAX_VALUE;
            for (int[] c : currentCentroids) {
                minDist = Math.min(minDist,
                        Math.pow(p[0]-c[0], 2) + Math.pow(p[1]-c[1], 2) + Math.pow(p[2]-c[2], 2));
            }
            if (minDist > maxDist) { maxDist = minDist; best = p; }
        }
        return best;
    }

    // --- Color science utilities (unchanged) ---
    private static double getDeltaE(Color c1, Color c2) {
        double[] lab1 = rgbToLab(c1);
        double[] lab2 = rgbToLab(c2);
        return Math.sqrt(Math.pow(lab1[0]-lab2[0], 2) +
                Math.pow(lab1[1]-lab2[1], 2) +
                Math.pow(lab1[2]-lab2[2], 2));
    }

    private static double[] rgbToLab(Color c) {
        double r = pivot(c.getRed()/255.0);
        double g = pivot(c.getGreen()/255.0);
        double b = pivot(c.getBlue()/255.0);
        double x = labPivot((r * 0.4124 + g * 0.3576 + b * 0.1805) / 0.95047);
        double y = labPivot((r * 0.2126 + g * 0.7152 + b * 0.0722) / 1.00000);
        double z = labPivot((r * 0.0193 + g * 0.1192 + b * 0.9505) / 1.08883);
        return new double[]{(116.0 * y) - 16.0, 500.0 * (x - y), 200.0 * (y - z)};
    }

    private static double pivot(double n) {
        return (n > 0.04045) ? Math.pow((n + 0.055) / 1.055, 2.4) : (n / 12.92);
    }

    private static double labPivot(double n) {
        return (n > 0.008856) ? Math.pow(n, 1.0/3.0) : (7.787 * n) + (16.0/116.0);
    }

    private static double getRelativeLuminance(Color c) {
        double r = c.getRed() / 255.0;
        double g = c.getGreen() / 255.0;
        double b = c.getBlue() / 255.0;
        r = (r <= 0.03928) ? r / 12.92 : Math.pow((r + 0.055) / 1.055, 2.4);
        g = (g <= 0.03928) ? g / 12.92 : Math.pow((g + 0.055) / 1.055, 2.4);
        b = (b <= 0.03928) ? b / 12.92 : Math.pow((b + 0.055) / 1.055, 2.4);
        return 0.2126 * r + 0.7152 * g + 0.0722 * b;
    }

    private static double getContrastRatio(Color c1, Color c2) {
        double l1 = getRelativeLuminance(c1);
        double l2 = getRelativeLuminance(c2);
        return (Math.max(l1, l2) + 0.05) / (Math.min(l1, l2) + 0.05);
    }

    private static List<Color> generateHarmoniousColors(
            float dominantHue, boolean isLightMode, Color background, int needed) {
        List<Color> generated = new ArrayList<>();
        float[] offsets = {0.33f, 0.66f};
        for (float offset : offsets) {
            float hue = (dominantHue + offset) % 1.0f;
            Color c = createContrastingColor(hue, isLightMode, background);
            if (c != null && getContrastRatio(c, background) >= MIN_CONTRAST_RATIO) {
                generated.add(c);
                if (generated.size() >= needed) break;
            }
        }
        if (generated.size() < needed) {
            float hue = (dominantHue + 0.5f) % 1.0f;
            Color c = createContrastingColor(hue, isLightMode, background);
            if (c != null && getContrastRatio(c, background) >= MIN_CONTRAST_RATIO) {
                generated.add(c);
            }
        }
        while (generated.size() < needed) {
            Color neutral = isLightMode ? Color.DARK_GRAY : Color.LIGHT_GRAY;
            if (getContrastRatio(neutral, background) >= MIN_CONTRAST_RATIO) {
                generated.add(neutral);
            } else {
                generated.add(isLightMode ? Color.BLACK : Color.WHITE);
            }
        }
        return generated;
    }

    private static Color createContrastingColor(float hue, boolean isLightMode, Color background) {
        float saturation = 0.7f;
        float brightness = isLightMode ? 0.35f : 0.75f;
        return Color.getHSBColor(hue, saturation, brightness);
    }


    private static float calculateDominantHue(List<Color> palette) {
        float sum = 0;
        for (Color c : palette) sum += Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null)[0];
        return sum / palette.size();
    }

    private static double hueDistance(Color c, float targetHue) {
        float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
        float diff = Math.abs(hsb[0] - targetHue);
        if (diff > 0.5) diff = 1.0f - diff;
        return diff;
    }

    private static double getSaturation(Color c) {
        return Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null)[1];
    }

    private static Color selectBackground(List<Color> palette, boolean isLightMode) {
        return isLightMode
                ? Collections.max(palette, Comparator.comparingDouble(DynamicColors::getRelativeLuminance))
                : Collections.min(palette, Comparator.comparingDouble(DynamicColors::getRelativeLuminance));
    }

    private static List<Color> fallbackDefault(boolean isLightMode) {
        return Arrays.asList(new Color(0x3498db), new Color(0x2ecc71), new Color(0xf1c40f), isLightMode ? Color.WHITE : Color.BLACK);
    }
}