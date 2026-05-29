package uni.gaben.iscat.utils.theme;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.imageio.ImageIO;

/**
 * Public API for image-derived color palette extraction.
 *
 * Delegates all pixel work to {@link KMeansExtractor} and all perceptual
 * math to {@link ColorScience}.  This class owns only:
 *   • The LRU result cache ({@link #THEME_CACHE})
 *   • The pruning + accent-selection pipeline
 *   • The fallback palette
 *
 * <h3>Limit contract (BUG FIX)</h3>
 * The {@code limit} parameter now controls the <em>exact</em> size of the
 * returned list.  Previously the list was always 4 regardless of {@code limit}
 * because the accent cap was hardcoded to 3.
 *
 * <pre>
 *   returned size = limit
 *   composition   = (limit - 1) accent colors  +  1 background color
 * </pre>
 *
 * Callers that pass {@code limit = 4} get the same structure as before
 * (3 accents + 1 bg); callers that pass {@code limit = 2} get 1 accent + 1 bg,
 * {@code limit = 6} gets 5 accents + 1 bg, and so on.
 */
public final class DynamicColors {

    // ─── Constants ───────────────────────────────────────────────────────────

    private static final double MIN_CONTRAST_RATIO = 3.0;

    // ─── LRU Cache ───────────────────────────────────────────────────────────

    /** LRU cache keyed on "absolutePath:limit:isLightMode". Capacity = 50. */
    private static final Map<String, List<Color>> THEME_CACHE =
            new LinkedHashMap<>(64, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, List<Color>> eldest) {
                    return size() > 50;
                }
            };

    private DynamicColors() {}

    // ─── Public API ──────────────────────────────────────────────────────────

    /**
     * Extracts a palette from {@code file}, using a cache to avoid re-processing
     * the same file with the same parameters.
     *
     * @param file        source image file
     * @param limit       exact number of colors to return (≥ 2)
     * @param isLightMode when {@code true} the background will be light, accents dark
     * @return list of exactly {@code limit} colors; first (limit-1) are accents,
     *         last is the background
     */
    public static List<Color> getPaletteForFile(File file, int limit, boolean isLightMode) {
        String key = file.getAbsolutePath() + ":" + limit + ":" + isLightMode;
        if (THEME_CACHE.containsKey(key)) return THEME_CACHE.get(key);

        try {
            BufferedImage image = ImageIO.read(file);
            if (image == null) return fallback(isLightMode, limit);
            List<Color> palette = buildPalette(image, limit, isLightMode);
            THEME_CACHE.put(key, palette);
            return palette;
        } catch (IOException e) {
            return fallback(isLightMode, limit);
        }
    }

    /**
     * Extracts a palette directly from a {@link BufferedImage} (no caching).
     *
     * @param image       source image (not null)
     * @param limit       exact number of colors to return (≥ 2)
     * @param isLightMode light or dark background mode
     * @return list of exactly {@code limit} colors
     */
    public static List<Color> getTopDistinctColors(BufferedImage image, int limit, boolean isLightMode) {
        return buildPalette(image, limit, isLightMode);
    }

    // ─── Core Pipeline ───────────────────────────────────────────────────────

    /**
     * Full pipeline:
     * <ol>
     *   <li>K-Means++ extraction of {@code limit * 2} raw centroids</li>
     *   <li>Adaptive pruning to {@code limit} perceptually distinct colors</li>
     *   <li>Background selection (lightest or darkest)</li>
     *   <li>Contrast-filtered accent candidates</li>
     *   <li>Synthetic generation if candidates fall short of {@code limit - 1}</li>
     *   <li>Vibrancy-score ranking</li>
     *   <li>Truncate / pad to exactly {@code limit - 1} accents + 1 background</li>
     * </ol>
     */
    private static List<Color> buildPalette(BufferedImage image, int limit, boolean isLightMode) {
        if (limit < 2) throw new IllegalArgumentException("limit must be >= 2, got " + limit);

        // 1 – 2: Extract then prune
        List<Color> raw     = KMeansExtractor.extract(image, limit * 2);
        List<Color> palette = pruneToDistinct(raw, limit);

        if (palette.isEmpty()) return fallback(isLightMode, limit);

        // 3: Background
        Color background = selectBackground(palette, isLightMode);

        // 4: Accent candidates with sufficient contrast
        double avgSat = palette.stream()
                .mapToDouble(ColorScience::saturation)
                .average().orElse(0.5);
        float dominantHue = ColorScience.dominantHue(palette);

        List<Color> accents = new ArrayList<>();
        for (Color c : palette) {
            if (!c.equals(background)
                    && ColorScience.contrastRatio(c, background) >= MIN_CONTRAST_RATIO) {
                accents.add(c);
            }
        }

        // 5: Synthesise if needed
        int accentsNeeded = limit - 1;
        if (accents.size() < accentsNeeded) {
            accents.addAll(ColorScience.generateHarmoniousAccents(
                    dominantHue, isLightMode, background,
                    (float) avgSat,
                    accentsNeeded - accents.size(),
                    MIN_CONTRAST_RATIO));
        }

        // 6: Rank by vibrancy
        accents.sort((a, b) ->
                Double.compare(
                        ColorScience.vibrancyScore(b, dominantHue),
                        ColorScience.vibrancyScore(a, dominantHue)));

        // 7: Assemble final palette of exactly `limit` entries
        List<Color> result = new ArrayList<>(limit);
        result.addAll(accents.subList(0, Math.min(accentsNeeded, accents.size())));

        // Safety pad (shouldn't be needed after synthesis, but guarantees the contract)
        while (result.size() < accentsNeeded) result.add(background);

        result.add(background); // always last
        return result;          // size == limit ✓
    }

    // ─── Pruning ─────────────────────────────────────────────────────────────

    /**
     * Adaptive perceptual pruning: keeps colors that are sufficiently distinct
     * from every already-kept color.
     *
     * The ΔE threshold is 15 % of the palette's maximum pairwise ΔE, with a
     * floor of 5 to avoid merging truly different colors on low-contrast images.
     */
    private static List<Color> pruneToDistinct(List<Color> palette, int limit) {
        if (palette.size() <= 1) return new ArrayList<>(palette);

        // Measure spread
        double maxDeltaE = 0;
        for (int i = 0; i < palette.size(); i++) {
            for (int j = i + 1; j < palette.size(); j++) {
                maxDeltaE = Math.max(maxDeltaE, ColorScience.deltaE(palette.get(i), palette.get(j)));
            }
        }
        double threshold = Math.max(5.0, 0.15 * maxDeltaE);

        List<Color> distinct = new ArrayList<>();
        for (Color candidate : palette) {
            boolean tooClose = false;
            for (Color kept : distinct) {
                if (ColorScience.deltaE(candidate, kept) < threshold) {
                    tooClose = true;
                    break;
                }
            }
            if (!tooClose) distinct.add(candidate);
            if (distinct.size() >= limit) break;
        }
        return distinct;
    }

    // ─── Background Selection ────────────────────────────────────────────────

    /**
     * Selects the background from the palette: the lightest color for light
     * mode, the darkest for dark mode (by WCAG relative luminance).
     */
    private static Color selectBackground(List<Color> palette, boolean isLightMode) {
        return isLightMode
                ? Collections.max(palette, Comparator.comparingDouble(ColorScience::relativeLuminance))
                : Collections.min(palette, Comparator.comparingDouble(ColorScience::relativeLuminance));
    }

    // ─── Fallback ────────────────────────────────────────────────────────────

    /**
     * Returns a sensible default palette of exactly {@code limit} colors when
     * image processing fails completely.
     */
    private static List<Color> fallback(boolean isLightMode, int limit) {
        Color[] defaults = {
                new Color(0x3498DB), // blue
                new Color(0x2ECC71), // green
                new Color(0xF1C40F), // yellow
                new Color(0xE74C3C), // red
                new Color(0x9B59B6), // purple
                new Color(0x1ABC9C), // teal
        };
        Color bg = isLightMode ? Color.WHITE : Color.BLACK;

        List<Color> result = new ArrayList<>(limit);
        int accentsNeeded  = limit - 1;
        for (int i = 0; i < accentsNeeded; i++) {
            result.add(defaults[i % defaults.length]);
        }
        result.add(bg);
        return result;
    }
}
