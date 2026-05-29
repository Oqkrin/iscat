package uni.gaben.iscat.utils.theme;

import java.awt.Color;
import java.util.List;

/**
 * Stateless perceptual color-science utilities.
 *
 * Covers:
 *   • sRGB → CIE L*a*b* conversion (D65 illuminant)
 *   • CIE76 ΔE (perceptual distance)
 *   • WCAG 2.1 relative luminance and contrast ratio
 *   • HSB convenience helpers (hue distance, saturation, dominant hue)
 *   • Vibrancy score used for accent ranking
 *   • Synthetic contrasting-color generation
 *
 * All methods are package-private static; callers live in the same package.
 */
final class ColorScience {

    private ColorScience() {}

    // ─── WCAG Luminance & Contrast ────────────────────────────────────────────

    /** WCAG 2.1 relative luminance [0, 1]. */
    static double relativeLuminance(Color c) {
        double r = linearize(c.getRed()   / 255.0);
        double g = linearize(c.getGreen() / 255.0);
        double b = linearize(c.getBlue()  / 255.0);
        return 0.2126 * r + 0.7152 * g + 0.0722 * b;
    }

    /** WCAG 2.1 contrast ratio (≥1). */
    static double contrastRatio(Color c1, Color c2) {
        double l1 = relativeLuminance(c1);
        double l2 = relativeLuminance(c2);
        return (Math.max(l1, l2) + 0.05) / (Math.min(l1, l2) + 0.05);
    }

    private static double linearize(double channel) {
        return (channel <= 0.03928)
                ? channel / 12.92
                : Math.pow((channel + 0.055) / 1.055, 2.4);
    }

    // ─── CIE L*a*b* Conversion ────────────────────────────────────────────────

    /** sRGB → [L*, a*, b*] under D65 illuminant. */
    static double[] rgbToLab(Color c) {
        double r = srgbPivot(c.getRed()   / 255.0);
        double g = srgbPivot(c.getGreen() / 255.0);
        double b = srgbPivot(c.getBlue()  / 255.0);
        double x = labPivot((r * 0.4124 + g * 0.3576 + b * 0.1805) / 0.95047);
        double y = labPivot((r * 0.2126 + g * 0.7152 + b * 0.0722) / 1.00000);
        double z = labPivot((r * 0.0193 + g * 0.1192 + b * 0.9505) / 1.08883);
        return new double[]{ (116.0 * y) - 16.0, 500.0 * (x - y), 200.0 * (y - z) };
    }

    /** CIE76 ΔE perceptual distance. */
    static double deltaE(Color c1, Color c2) {
        double[] lab1 = rgbToLab(c1);
        double[] lab2 = rgbToLab(c2);
        return Math.sqrt(
                Math.pow(lab1[0] - lab2[0], 2) +
                Math.pow(lab1[1] - lab2[1], 2) +
                Math.pow(lab1[2] - lab2[2], 2));
    }

    private static double srgbPivot(double n) {
        return (n > 0.04045) ? Math.pow((n + 0.055) / 1.055, 2.4) : (n / 12.92);
    }

    private static double labPivot(double n) {
        return (n > 0.008856) ? Math.pow(n, 1.0 / 3.0) : (7.787 * n) + (16.0 / 116.0);
    }

    // ─── HSB Helpers ─────────────────────────────────────────────────────────

    /** HSB saturation component [0, 1]. */
    static double saturation(Color c) {
        return Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null)[1];
    }

    /**
     * Circular hue distance [0, 0.5].
     * 0 = same hue, 0.5 = complementary.
     */
    static double hueDistance(Color c, float targetHue) {
        float hue  = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null)[0];
        float diff = Math.abs(hue - targetHue);
        return (diff > 0.5f) ? 1.0f - diff : diff;
    }

    /**
     * Mean hue across a palette.
     * Simple arithmetic mean — suitable as a "dominant hue" approximation.
     */
    static float dominantHue(List<Color> palette) {
        float sum = 0f;
        for (Color c : palette) {
            sum += Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null)[0];
        }
        return sum / palette.size();
    }

    // ─── Vibrancy Scoring ────────────────────────────────────────────────────

    /**
     * Accent vibrancy score for ranking.
     * Weights: 60 % hue proximity to the palette's dominant hue,
     *          40 % saturation.
     */
    static double vibrancyScore(Color c, float dominantHue) {
        double hueScore = 1.0 - (hueDistance(c, dominantHue) * 2.0); // [0, 1]
        return (hueScore * 0.6) + (saturation(c) * 0.4);
    }

    // ─── Synthetic Color Generation ──────────────────────────────────────────

    /**
     * Generates {@code needed} harmonious accent colors from the given hue offsets
     * (triadic + complementary), filtered by contrast against {@code background}.
     * Saturation is clamped to [0.3, 0.6] so synthetics are never neon.
     *
     * @param dominantHue  image's dominant hue [0, 1]
     * @param isLightMode  drives brightness target
     * @param background   contrast reference
     * @param baseSat      image's average saturation, used to flavour synthetics
     * @param needed       number of accents to produce
     * @param minContrast  minimum WCAG contrast ratio required
     */
    static List<Color> generateHarmoniousAccents(
            float dominantHue, boolean isLightMode, Color background,
            float baseSat, int needed, double minContrast) {

        float sat        = (float) Math.max(0.3, Math.min(0.6, baseSat));
        float brightness = isLightMode ? 0.4f : 0.7f;

        float[] offsets  = { 0.33f, 0.66f, 0.5f }; // triadic + complementary
        List<Color> out  = new java.util.ArrayList<>();

        for (float offset : offsets) {
            if (out.size() >= needed) break;
            float hue  = (dominantHue + offset) % 1.0f;
            Color candidate = Color.getHSBColor(hue, sat, brightness);
            if (contrastRatio(candidate, background) >= minContrast) {
                out.add(candidate);
            }
        }

        // Last-resort neutral if harmonic generation falls short
        while (out.size() < needed) {
            Color neutral = isLightMode ? Color.DARK_GRAY : Color.LIGHT_GRAY;
            out.add(contrastRatio(neutral, background) >= minContrast
                    ? neutral
                    : (isLightMode ? Color.BLACK : Color.WHITE));
        }

        return out;
    }
}
