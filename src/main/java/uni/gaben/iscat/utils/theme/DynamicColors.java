package uni.gaben.iscat.utils.theme;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.imageio.ImageIO;

/**
 * Perceptual Theme Engine
 *
 * Features:
 * - Distinct color extraction using LAB distance
 * - Semantic role assignment
 * - Adaptive dark/light backgrounds
 * - Accent prioritization
 * - Surface synthesis fallback
 *
 * Final palette structure:
 *
 * [ PRIMARY, SECONDARY, TERNARY, BACKGROUND ]
 */
public class DynamicColors {

    private static final double LAB_DISTANCE_THRESHOLD = 35.0;

    private record LabColor(
            double l,
            double a,
            double b
    ) {}

    /**
     * Returns palette as HEX strings.
     */
    public static List<String> getTopDistinctColorsHex(
            File imageFile,
            int limit,
            boolean isLightMode
    ) {

        List<String> hexColors = new ArrayList<>();

        try {

            BufferedImage image = ImageIO.read(imageFile);

            if (image == null) {
                return hexColors;
            }

            List<Color> palette =
                    getTopDistinctColors(
                            image,
                            limit,
                            isLightMode
                    );

            for (Color c : palette) {

                hexColors.add(
                        String.format(
                                "#%02x%02x%02x",
                                c.getRed(),
                                c.getGreen(),
                                c.getBlue()
                        )
                );
            }

        } catch (IOException e) {

            System.err.println(
                    "Theme Engine critical IO failure: "
                            + e.getMessage()
            );
        }

        return hexColors;
    }

    /**
     * Final structure:
     *
     * [ PRIMARY, SECONDARY, TERNARY, BACKGROUND ]
     */
    public static List<Color> getTopDistinctColors(
            BufferedImage image,
            int limit,
            boolean isLightMode
    ) {

        // ─────────────────────────────────────────────
        // RAW EXTRACTION
        // ─────────────────────────────────────────────

        Map<Integer, Integer> colorCounts =
                scanPixels(image);

        List<Map.Entry<Integer, Integer>> sorted =
                new ArrayList<>(colorCounts.entrySet());

        sorted.sort(
                (e1, e2) ->
                        e2.getValue().compareTo(
                                e1.getValue()
                        )
        );

        List<Color> palette =
                new ArrayList<>();

        List<LabColor> usedLabs =
                new ArrayList<>();

        for (Map.Entry<Integer, Integer> entry : sorted) {

            Color c =
                    new Color(entry.getKey());

            LabColor lab =
                    rgbToLab(c);

            if (isDistinct(lab, usedLabs)) {

                palette.add(c);
                usedLabs.add(lab);
            }

            if (palette.size() >= limit) {
                break;
            }
        }

        // Safety fallback

        if (palette.isEmpty()) {

            return List.of(
                    Color.GRAY,
                    Color.LIGHT_GRAY,
                    Color.DARK_GRAY,
                    isLightMode
                            ? Color.WHITE
                            : Color.BLACK
            );
        }

        // ─────────────────────────────────────────────
        // ROLE CLASSIFICATION
        // ─────────────────────────────────────────────

        List<Color> darkCandidates =
                new ArrayList<>();

        List<Color> lightCandidates =
                new ArrayList<>();

        List<Color> accentCandidates =
                new ArrayList<>();

        for (Color c : palette) {

            float[] hsb =
                    Color.RGBtoHSB(
                            c.getRed(),
                            c.getGreen(),
                            c.getBlue(),
                            null
                    );

            float saturation = hsb[1];
            float brightness = hsb[2];

            // Surface candidates

            if (brightness < 0.25f) {
                darkCandidates.add(c);
            }

            if (brightness > 0.75f) {
                lightCandidates.add(c);
            }

            // Accent candidates

            if (saturation > 0.18f) {
                accentCandidates.add(c);
            }
        }

        // If we somehow have no accents,
        // fallback to all palette colors

        if (accentCandidates.isEmpty()) {
            accentCandidates.addAll(palette);
        }

        // ─────────────────────────────────────────────
        // BACKGROUND SELECTION
        // ─────────────────────────────────────────────

        Color background;

        if (isLightMode) {

            if (!lightCandidates.isEmpty()) {

                background = Collections.max(
                        lightCandidates,
                        Comparator.comparingDouble(c -> {

                            float[] hsb =
                                    Color.RGBtoHSB(
                                            c.getRed(),
                                            c.getGreen(),
                                            c.getBlue(),
                                            null
                                    );

                            return hsb[2]
                                    - (hsb[1] * 0.4);
                        })
                );

            } else {

                background =
                        synthesizeSurface(
                                palette.get(0),
                                true
                        );
            }

        } else {

            if (!darkCandidates.isEmpty()) {

                background = Collections.min(
                        darkCandidates,
                        Comparator.comparingDouble(c -> {

                            float[] hsb =
                                    Color.RGBtoHSB(
                                            c.getRed(),
                                            c.getGreen(),
                                            c.getBlue(),
                                            null
                                    );

                            return hsb[2]
                                    + (hsb[1] * 0.4);
                        })
                );

            } else {

                background =
                        synthesizeSurface(
                                palette.get(0),
                                false
                        );
            }
        }

        accentCandidates.remove(background);

        // ─────────────────────────────────────────────
        // ACCENT RANKING
        // ─────────────────────────────────────────────

        accentCandidates.sort((c1, c2) -> {

            float[] hsb1 =
                    Color.RGBtoHSB(
                            c1.getRed(),
                            c1.getGreen(),
                            c1.getBlue(),
                            null
                    );

            float[] hsb2 =
                    Color.RGBtoHSB(
                            c2.getRed(),
                            c2.getGreen(),
                            c2.getBlue(),
                            null
                    );

            float score1 =
                    (hsb1[1] * 0.75f)
                            + (hsb1[2] * 0.25f);

            float score2 =
                    (hsb2[1] * 0.75f)
                            + (hsb2[2] * 0.25f);

            return Float.compare(score2, score1);
        });

        // ─────────────────────────────────────────────
        // FINAL PALETTE
        // [PRIMARY, SECONDARY, TERNARY, BG]
        // ─────────────────────────────────────────────

        List<Color> finalPalette =
                new ArrayList<>();

        for (
                int i = 0;
                i < Math.min(3, accentCandidates.size());
                i++
        ) {

            finalPalette.add(
                    accentCandidates.get(i)
            );
        }

        // Ensure minimum size

        while (finalPalette.size() < 3) {
            finalPalette.add(background);
        }

        finalPalette.add(background);

        return finalPalette;
    }

    /**
     * Creates synthetic readable surfaces
     * when image lacks proper neutrals.
     */
    private static Color synthesizeSurface(
            Color seed,
            boolean lightMode
    ) {

        float[] hsb =
                Color.RGBtoHSB(
                        seed.getRed(),
                        seed.getGreen(),
                        seed.getBlue(),
                        null
                );

        return Color.getHSBColor(
                hsb[0],
                Math.min(
                        hsb[1] * 0.15f,
                        0.10f
                ),
                lightMode
                        ? 0.96f
                        : 0.08f
        );
    }

    /**
     * LAB distinctness test.
     */
    private static boolean isDistinct(
            LabColor candidate,
            List<LabColor> used
    ) {

        for (LabColor existing : used) {

            if (
                    getDeltaE(candidate, existing)
                            < LAB_DISTANCE_THRESHOLD
            ) {

                return false;
            }
        }

        return true;
    }

    /**
     * Pixel scanning with skipping for performance.
     */
    private static Map<Integer, Integer> scanPixels(
            BufferedImage image
    ) {

        Map<Integer, Integer> counts =
                new HashMap<>();

        for (
                int x = 0;
                x < image.getWidth();
                x += 4
        ) {

            for (
                    int y = 0;
                    y < image.getHeight();
                    y += 4
            ) {

                int rgb =
                        image.getRGB(x, y);

                counts.put(
                        rgb & 0x00FFFFFF,
                        counts.getOrDefault(
                                rgb & 0x00FFFFFF,
                                0
                        ) + 1
                );
            }
        }

        return counts;
    }

    /**
     * RGB → LAB conversion.
     */
    private static LabColor rgbToLab(Color c) {

        double r =
                pivot(c.getRed() / 255.0);

        double g =
                pivot(c.getGreen() / 255.0);

        double b =
                pivot(c.getBlue() / 255.0);

        double x =
                labPivot(
                        (
                                r * 0.4124
                                        + g * 0.3576
                                        + b * 0.1805
                        ) / 0.95047
                );

        double y =
                labPivot(
                        (
                                r * 0.2126
                                        + g * 0.7152
                                        + b * 0.0722
                        ) / 1.00000
                );

        double z =
                labPivot(
                        (
                                r * 0.0193
                                        + g * 0.1192
                                        + b * 0.9505
                        ) / 1.08883
                );

        return new LabColor(
                (116.0 * y) - 16.0,
                500.0 * (x - y),
                200.0 * (y - z)
        );
    }

    private static double pivot(double n) {

        return (n > 0.04045)
                ? Math.pow(
                (n + 0.055) / 1.055,
                2.4
        )
                : (n / 12.92);
    }

    private static double labPivot(double n) {

        return (n > 0.008856)
                ? Math.pow(n, 1.0 / 3.0)
                : (7.787 * n)
                  + (16.0 / 116.0);
    }

    /**
     * Delta-E distance.
     */
    private static double getDeltaE(
            LabColor c1,
            LabColor c2
    ) {

        return Math.sqrt(
                Math.pow(c1.l() - c2.l(), 2)
                        + Math.pow(c1.a() - c2.a(), 2)
                        + Math.pow(c1.b() - c2.b(), 2)
        );
    }
}