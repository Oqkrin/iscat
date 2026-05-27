package uni.gaben.iscat.utils.theme;

import javax.imageio.ImageIO;
import java.awt.Color; // Using AWT for heavy image pixel arithmetic processing
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DynamicColors {

    private static final double LAB_DISTANCE_THRESHOLD = 20.0;
    private static final double DARK_LIGHTNESS_THRESHOLD = 38.0;

    // HSB filters targeting muddy or washed out pixels
    private static final float MIN_SATURATION = 0.15f;
    private static final float MIN_BRIGHTNESS = 0.12f;
    private static final float MAX_BRIGHTNESS = 0.95f;

    private record LabColor(double l, double a, double b) {}

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
        Map<Integer, Integer> colorCounts = scanPixels(image, true);

        // Fallback if image filters leave the map empty (monochrome/grayscale assets)
        if (colorCounts.isEmpty()) {
            colorCounts = scanPixels(image, false);
        }

        List<Map.Entry<Integer, Integer>> sortedColors = new ArrayList<>(colorCounts.entrySet());
        sortedColors.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

        List<Color> brightAccents = new ArrayList<>();
        List<LabColor> brightLabs = new ArrayList<>();

        List<Color> darkAccents = new ArrayList<>();
        List<LabColor> darkLabs = new ArrayList<>();

        // Group candidates into explicit bright and dark clusters using Delta E distancing
        for (Map.Entry<Integer, Integer> entry : sortedColors) {
            Color candidateColor = new Color(entry.getKey());
            LabColor candidateLab = rgbToLab(candidateColor);

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

        List<Color> finalizedPalette = new ArrayList<>();
        List<Color> availableBright = new ArrayList<>(brightAccents);
        List<Color> availableDark = new ArrayList<>(darkAccents);

        // 1. ALLOCATE ACCENTS: Pull 3 high-vibrancy light colors
        while (finalizedPalette.size() < 3 && !availableBright.isEmpty()) {
            finalizedPalette.add(availableBright.remove(0));
        }
        // Fallback: Use dark tones if the image has no bright profiles
        while (finalizedPalette.size() < 3 && !availableDark.isEmpty()) {
            finalizedPalette.add(availableDark.remove(0));
        }
        // Absolute fallback padding to keep the collection intact
        if (finalizedPalette.size() < 1) finalizedPalette.add(new Color(0, 255, 204));
        if (finalizedPalette.size() < 2) finalizedPalette.add(new Color(235, 52, 161));
        if (finalizedPalette.size() < 3) finalizedPalette.add(new Color(255, 166, 0));

        // 2. ALLOCATE BACKGROUND: Position 4th color strictly as a deep shadow tone
        Color bgPrimaryColor;
        if (!availableDark.isEmpty()) {
            bgPrimaryColor = availableDark.get(0);
        } else if (!darkAccents.isEmpty()) {
            bgPrimaryColor = darkAccents.get(0);
        } else {
            // Creative Fallback: If image is pure light, derive an elegant dark tone from the primary accent
            Color primaryAccent = finalizedPalette.get(0);
            int r = Math.max(2, (int) (primaryAccent.getRed() * 0.06));
            int g = Math.max(3, (int) (primaryAccent.getGreen() * 0.08));
            int b = Math.max(5, (int) (primaryAccent.getBlue() * 0.10));
            bgPrimaryColor = new Color(r, g, b);
        }

        finalizedPalette.add(bgPrimaryColor);
        return finalizedPalette;
    }

    private static Map<Integer, Integer> scanPixels(BufferedImage image, boolean useVibrancyFilter) {
        Map<Integer, Integer> counts = new HashMap<>();
        float[] hsbBuffer = new float[3];

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

                counts.put(rgb & 0x00FFFFFF, counts.getOrDefault(rgb & 0x00FFFFFF, 0) + 1);
            }
        }
        return counts;
    }

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

        double xN = x / 0.95047;
        double yN = y / 1.00000;
        double zN = z / 1.08883;

        xN = (xN > 0.008856) ? Math.pow(xN, 1.0 / 3.0) : (7.787 * xN) + (16.0 / 116.0);
        yN = (yN > 0.008856) ? Math.pow(yN, 1.0 / 3.0) : (7.787 * yN) + (16.0 / 116.0);
        zN = (zN > 0.008856) ? Math.pow(zN, 1.0 / 3.0) : (7.787 * zN) + (16.0 / 116.0);

        return new LabColor((116.0 * yN) - 16.0, 500.0 * (xN - yN), 200.0 * (yN - zN));
    }

    private static double getDeltaE(LabColor c1, LabColor c2) {
        double dL = c1.l() - c2.l();
        double dA = c1.a() - c2.a();
        double dB = c1.b() - c2.b();
        return Math.sqrt((dL * dL) + (dA * dA) + (dB * dB));
    }
}