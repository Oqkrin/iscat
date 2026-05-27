package uni.gaben.iscat.utils;

import javafx.scene.Scene;

import javax.imageio.ImageIO;
import java.awt.Color; // Use AWT for image processing
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DynamicColors {
    private static final double COLOR_DISTANCE_THRESHOLD = 45.0;

    /**
     * Returns the top N distinct colors as CSS-compatible hex strings (e.g., "#ff0000")
     */
    public static List<String> getTopDistinctColorsHex(File imageFile, int limit) {
        List<String> hexColors = new ArrayList<>();
        try {
            BufferedImage image = ImageIO.read(imageFile);
            if (image == null) return hexColors;

            List<Color> topColors = getTopDistinctColors(image, limit);
            for (Color c : topColors) {
                // Convert AWT Color to standard Hex string
                String hex = String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
                hexColors.add(hex);
            }
        } catch (IOException e) {
            System.err.println("Error reading image: " + e.getMessage());
        }
        return hexColors;
    }

    private static List<Color> getTopDistinctColors(BufferedImage image, int limit) {
        Map<Integer, Integer> colorCounts = new HashMap<>();

        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int rgb = image.getRGB(x, y) & 0x00FFFFFF; // Mask alpha
                colorCounts.put(rgb, colorCounts.getOrDefault(rgb, 0) + 1);
            }
        }

        List<Map.Entry<Integer, Integer>> sortedColors = new ArrayList<>(colorCounts.entrySet());
        sortedColors.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

        List<Color> distinctTopColors = new ArrayList<>();

        for (Map.Entry<Integer, Integer> entry : sortedColors) {
            Color candidateColor = new Color(entry.getKey());

            boolean isDistinct = true;
            for (Color pickedColor : distinctTopColors) {
                if (getColorDistance(candidateColor, pickedColor) < COLOR_DISTANCE_THRESHOLD) {
                    isDistinct = false;
                    break;
                }
            }

            if (isDistinct) {
                distinctTopColors.add(candidateColor);
                if (distinctTopColors.size() == limit) {
                    break;
                }
            }
        }

        return distinctTopColors;
    }

    private static double getColorDistance(Color c1, Color c2) {
        int rDiff = c1.getRed() - c2.getRed();
        int gDiff = c1.getGreen() - c2.getGreen();
        int bDiff = c1.getBlue() - c2.getBlue();
        return Math.sqrt((rDiff * rDiff) + (gDiff * gDiff) + (bDiff * bDiff));
    }

}