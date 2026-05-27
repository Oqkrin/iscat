package uni.gaben.iscat.utils;

import javax.imageio.ImageIO;
import java.awt.Color; // Using AWT for heavy image pixel arithmetic processing
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * High-performance color extraction utility. Parses raw asset image matrices,
 * prunes out disruptive neutral frames using HSB boundaries, and runs structural color
 * deduplication using human-accurate CIELAB (Delta E) geometric distancing.
 */
public class DynamicColors {

    /**
     * Delta E distance threshold in the CIELAB color space.
     * In CIELAB, a distance of ~2.3 represents a Just Noticeable Difference (JND).
     * For aesthetic palette distribution, a value between 15.0 and 25.0 ensures
     * selected accent channels feel distinct without blending into matching hues.
     */
    private static final double LAB_DISTANCE_THRESHOLD = 20.0;

    /**
     * Lightness threshold in CIELAB space (Scale 0.0 - 100.0).
     * Values below 38.0 represent deep, dark shadow tones (e.g., deep navy, maroon, forest green).
     * These are excluded from Primary/Secondary roles and relegated to Ternary positions.
     */
    private static final double DARK_LIGHTNESS_THRESHOLD = 38.0;

    // HSB filter constraints targeting muddy/neutral pixels (Range: 0.0 - 1.0)
    private static final float MIN_SATURATION = 0.15f;  // Drops gray tones lacking vibrant tint
    private static final float MIN_BRIGHTNESS = 0.12f;  // Drops deep blacks that muffle visual layouts
    private static final float MAX_BRIGHTNESS = 0.95f;  // Combined with saturation to filter out harsh whites

    /**
     * Immutable data record representing a point in the 3D CIELAB color space.
     * L* = Lightness (0 = Black, 100 = White)
     * a* = Red/Green Vector (Negative = Green, Positive = Red)
     * b* = Yellow/Blue Vector (Negative = Blue, Positive = Yellow)
     */
    private record LabColor(double l, double a, double b) {}

    /**
     * Extracts the top N perceptually distinct colors from an image file as web-safe Hex strings.
     *
     * @param imageFile The physical target asset on disk.
     * @param limit      Maximum number of unique aesthetic accents required (e.g., 3 for Primary/Sec/Tertiary).
     * @return A list of formatting-ready CSS hex values (e.g., ["#FF0055", "#00AABB"]).
     */
    public static List<String> getTopDistinctColorsHex(File imageFile, int limit) {
        List<String> hexColors = new ArrayList<>();
        try {
            BufferedImage image = ImageIO.read(imageFile);
            if (image == null) return hexColors;

            List<Color> topColors = getTopDistinctColors(image, limit);
            for (Color c : topColors) {
                String hex = String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
                hexColors.add(hex);
            }
        } catch (IOException e) {
            System.err.println("Theme Engine critical IO failure reading asset: " + e.getMessage());
        }
        return hexColors;
    }

    private static List<Color> getTopDistinctColors(BufferedImage image, int limit) {
        // Phase 1: Scan image matrix with strict vibrancy criteria active
        Map<Integer, Integer> colorCounts = scanPixels(image, true);

        // Fallback: If image is monochrome/grayscale, the filter returns empty.
        // If triggered, re-scan ignoring filters to extract pure structural shades safely.
        if (colorCounts.isEmpty()) {
            colorCounts = scanPixels(image, false);
        }

        // Phase 2: Sort frequencies descending
        List<Map.Entry<Integer, Integer>> sortedColors = new ArrayList<>(colorCounts.entrySet());
        sortedColors.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

        // Separate accent pools to guarantee light/vibrant primary and secondary assignments
        List<Color> brightAccents = new ArrayList<>();
        List<LabColor> brightLabs = new ArrayList<>();

        List<Color> darkAccents = new ArrayList<>();
        List<LabColor> darkLabs = new ArrayList<>();

        // Phase 3: Evaluate candidates and split them based on perceived lightness
        for (Map.Entry<Integer, Integer> entry : sortedColors) {
            Color candidateColor = new Color(entry.getKey());
            LabColor candidateLab = rgbToLab(candidateColor);

            // Cross-verify uniqueness across both pools to avoid duplicate hue selections
            boolean isDistinct = true;
            for (LabColor existingLab : brightLabs) {
                if (getDeltaE(candidateLab, existingLab) < LAB_DISTANCE_THRESHOLD) {
                    isDistinct = false;
                    break;
                }
            }
            if (isDistinct) {
                for (LabColor existingLab : darkLabs) {
                    if (getDeltaE(candidateLab, existingLab) < LAB_DISTANCE_THRESHOLD) {
                        isDistinct = false;
                        break;
                    }
                }
            }

            // Categorize distinct values into their respective lightness tiers
            if (isDistinct) {
                if (candidateLab.l() < DARK_LIGHTNESS_THRESHOLD) {
                    darkAccents.add(candidateColor);
                    darkLabs.add(candidateLab);
                } else {
                    brightAccents.add(candidateColor);
                    brightLabs.add(candidateLab);
                }
            }
        }

        // Phase 4: Assemble the finalized hierarchical palette
        List<Color> finalizedPalette = new ArrayList<>();

        // 1. Fill Primary and Secondary slots strictly using bright accents
        int maxBrightAccents = Math.min(limit - 1, brightAccents.size());
        for (int i = 0; i < maxBrightAccents; i++) {
            finalizedPalette.add(brightAccents.get(i));
        }

        // 2. Relegate the top dominant dark color explicitly to the Ternary slot
        if (finalizedPalette.size() < limit && !darkAccents.isEmpty()) {
            finalizedPalette.add(darkAccents.get(0));
        }

        // 3. Fallback: If no dark colors were found, populate the remaining slots with bright colors
        int brightIndex = maxBrightAccents;
        while (finalizedPalette.size() < limit && brightIndex < brightAccents.size()) {
            finalizedPalette.add(brightAccents.get(brightIndex++));
        }

        // 4. Absolute Fallback: If the image is extremely limited in distinct colors,
        // fill any remaining empty spots with any leftover dark tones.
        int darkIndex = finalizedPalette.contains(darkAccents.isEmpty() ? null : darkAccents.get(0)) ? 1 : 0;
        while (finalizedPalette.size() < limit && darkIndex < darkAccents.size()) {
            finalizedPalette.add(darkAccents.get(darkIndex++));
        }

        return finalizedPalette;
    }

    /**
     * Loops through the physical pixel buffer to map color counts, utilizing
     * high-speed math scaling to screen out unwanted hues before sorting.
     */
    private static Map<Integer, Integer> scanPixels(BufferedImage image, boolean useVibrancyFilter) {
        Map<Integer, Integer> counts = new HashMap<>();
        float[] hsbBuffer = new float[3]; // Reusable array context prevents aggressive GC collection cycles

        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                if (useVibrancyFilter) {
                    Color.RGBtoHSB(r, g, b, hsbBuffer);
                    float saturation = hsbBuffer[1];
                    float brightness = hsbBuffer[2];

                    if (brightness < MIN_BRIGHTNESS) continue;
                    if (brightness > MAX_BRIGHTNESS && saturation < MIN_SATURATION) continue;
                    if (saturation < MIN_SATURATION) continue;
                }

                int maskedRgb = rgb & 0x00FFFFFF; // Strip alpha channel details
                counts.put(maskedRgb, counts.getOrDefault(maskedRgb, 0) + 1);
            }
        }
        return counts;
    }

    /**
     * Converts a standard AWT color coordinate cleanly into the 3D CIELAB color space.
     * Pipeline Architecture: sRGB -> Linear sRGB -> CIEXYZ (D65 White Point) -> CIELAB
     */
    private static LabColor rgbToLab(Color color) {
        double rLinear = color.getRed() / 255.0;
        double gLinear = color.getGreen() / 255.0;
        double bLinear = color.getBlue() / 255.0;

        rLinear = (rLinear > 0.04045) ? Math.pow((rLinear + 0.055) / 1.055, 2.4) : (rLinear / 12.92);
        gLinear = (gLinear > 0.04045) ? Math.pow((gLinear + 0.055) / 1.055, 2.4) : (gLinear / 12.92);
        bLinear = (bLinear > 0.04045) ? Math.pow((bLinear + 0.055) / 1.055, 2.4) : (bLinear / 12.92);

        double x = rLinear * 0.4124 + gLinear * 0.3576 + bLinear * 0.1805;
        double y = rLinear * 0.2126 + gLinear * 0.7152 + bLinear * 0.0722;
        double z = rLinear * 0.0193 + gLinear * 0.1192 + bLinear * 0.9505;

        double xReferenceNormalized = x / 0.95047;
        double yReferenceNormalized = y / 1.00000;
        double zReferenceNormalized = z / 1.08883;

        xReferenceNormalized = (xReferenceNormalized > 0.008856) ? Math.pow(xReferenceNormalized, 1.0 / 3.0) : (7.787 * xReferenceNormalized) + (16.0 / 116.0);
        yReferenceNormalized = (yReferenceNormalized > 0.008856) ? Math.pow(yReferenceNormalized, 1.0 / 3.0) : (7.787 * yReferenceNormalized) + (16.0 / 116.0);
        zReferenceNormalized = (zReferenceNormalized > 0.008856) ? Math.pow(zReferenceNormalized, 1.0 / 3.0) : (7.787 * zReferenceNormalized) + (16.0 / 116.0);

        double l = (116.0 * yReferenceNormalized) - 16.0;
        double a = 500.0 * (xReferenceNormalized - yReferenceNormalized);
        double b = 200.0 * (yReferenceNormalized - zReferenceNormalized);

        return new LabColor(l, a, b);
    }

    /**
     * Calculates the Perceptual Distance (Delta E) between two points in CIELAB space.
     */
    private static double getDeltaE(LabColor c1, LabColor c2) {
        double deltaL = c1.l() - c2.l();
        double deltaA = c1.a() - c2.a();
        double deltaB = c1.b() - c2.b();
        return Math.sqrt((deltaL * deltaL) + (deltaA * deltaA) + (deltaB * deltaB));
    }
}