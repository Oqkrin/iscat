package uni.gaben.iscat.utils.theme;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.imageio.ImageIO;

/**
 * Perceptual Theme Engine
 * Reworked for "Color Stealing": Dynamically promotes image-native neutrals
 * to surface backgrounds while prioritizing vibrant accents.
 */
public class DynamicColors {

    private static final double LAB_DISTANCE_THRESHOLD = 35.0;
    private static final float MIN_SATURATION = 0.05f; // Lowered to allow neutral surfaces
    private static final float MIN_BRIGHTNESS = 0.05f;

    private record LabColor(double l, double a, double b) {}

    public static List<String> getTopDistinctColorsHex(File imageFile, int limit, boolean isLightMode) {
        List<String> hexColors = new ArrayList<>();
        try {
            BufferedImage image = ImageIO.read(imageFile);
            if (image == null) return hexColors;

            List<Color> palette = getTopDistinctColors(image, limit, isLightMode);
            for (Color c : palette) {
                hexColors.add(String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue()));
            }
        } catch (IOException e) {
            System.err.println("Theme Engine critical IO failure: " + e.getMessage());
        }
        return hexColors;
    }

    public static List<Color> getTopDistinctColors(BufferedImage image, int limit, boolean isLightMode) {
        Map<Integer, Integer> colorCounts = scanPixels(image);
        List<Map.Entry<Integer, Integer>> sorted = new ArrayList<>(colorCounts.entrySet());
        sorted.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

        List<Color> palette = new ArrayList<>();
        List<LabColor> usedLabs = new ArrayList<>();

        // 1. Extraction: Mine the image for distinct color candidates
        for (Map.Entry<Integer, Integer> entry : sorted) {
            Color c = new Color(entry.getKey());
            LabColor lab = rgbToLab(c);
            if (isDistinct(lab, usedLabs)) {
                palette.add(c);
                usedLabs.add(lab);
            }
            if (palette.size() >= limit) break;
        }

        // 2. Identify the best Background candidate (The "Steal" Logic)
        // We look for colors with low saturation that fit the current light/dark target
        Color bestBg = null;
        double bestScore = Double.MAX_VALUE;
        double targetBrightness = isLightMode ? 0.92 : 0.08;

        for (Color c : palette) {
            float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
            // Score rewards low saturation + proximity to target background tone
            double score = (hsb[1] * 0.8) + (Math.abs(hsb[2] - targetBrightness) * 0.2);
            if (score < bestScore) {
                bestScore = score;
                bestBg = c;
            }
        }

        palette.remove(bestBg);

        // 3. Reorder Accents by Vibrancy (Saturation * Brightness)
        palette.sort((c1, c2) -> {
            float[] hsb1 = Color.RGBtoHSB(c1.getRed(), c1.getGreen(), c1.getBlue(), null);
            float[] hsb2 = Color.RGBtoHSB(c2.getRed(), c2.getGreen(), c2.getBlue(), null);
            return Float.compare(hsb2[1] * hsb2[2], hsb1[1] * hsb1[2]);
        });

        // 4. Final Structure: [Accent1, Accent2, ..., Background]
        palette.add(bestBg);
        return palette;
    }

    private static boolean isDistinct(LabColor candidate, List<LabColor> used) {
        for (LabColor existing : used) {
            if (getDeltaE(candidate, existing) < LAB_DISTANCE_THRESHOLD) return false;
        }
        return true;
    }

    private static Map<Integer, Integer> scanPixels(BufferedImage image) {
        Map<Integer, Integer> counts = new HashMap<>();
        for (int x = 0; x < image.getWidth(); x += 4) { // Increased skip for speed
            for (int y = 0; y < image.getHeight(); y += 4) {
                int rgb = image.getRGB(x, y);
                counts.put(rgb & 0x00FFFFFF, counts.getOrDefault(rgb & 0x00FFFFFF, 0) + 1);
            }
        }
        return counts;
    }

    private static LabColor rgbToLab(Color c) {
        double r = pivot(c.getRed() / 255.0);
        double g = pivot(c.getGreen() / 255.0);
        double b = pivot(c.getBlue() / 255.0);
        double x = labPivot((r * 0.4124 + g * 0.3576 + b * 0.1805) / 0.95047);
        double y = labPivot((r * 0.2126 + g * 0.7152 + b * 0.0722) / 1.00000);
        double z = labPivot((r * 0.0193 + g * 0.1192 + b * 0.9505) / 1.08883);
        return new LabColor((116.0 * y) - 16.0, 500.0 * (x - y), 200.0 * (y - z));
    }

    private static double pivot(double n) { return (n > 0.04045) ? Math.pow((n + 0.055) / 1.055, 2.4) : (n / 12.92); }
    private static double labPivot(double n) { return (n > 0.008856) ? Math.pow(n, 1.0 / 3.0) : (7.787 * n) + (16.0 / 116.0); }
    private static double getDeltaE(LabColor c1, LabColor c2) {
        return Math.sqrt(Math.pow(c1.l() - c2.l(), 2) + Math.pow(c1.a() - c2.a(), 2) + Math.pow(c1.b() - c2.b(), 2));
    }
}