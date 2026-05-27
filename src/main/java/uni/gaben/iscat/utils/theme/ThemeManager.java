package uni.gaben.iscat.utils.theme;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import uni.gaben.iscat.utils.sprite.SpriteUtils;
import uni.gaben.iscat.utils.CssThemeGenerator;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Central orchestrator for global application aesthetics. Handles hot-swapping
 * layouts, asset re-tinting pools, and bridges CSS attributes to Java runtime drawing components.
 */
public class ThemeManager {
    private static final ThemeManager instance = new ThemeManager();
    public static ThemeManager getInstance() { return instance; }

    // --- State & Component Cache Control Arrays ---
    private final ObjectProperty<Color> globalTint;
    private final Map<String, Color> currentPalette = new HashMap<>();
    private final Map<TintKey, Image> tintCache = new HashMap<>();

    private String currentCssPath = "/uni/gaben/iscat/styles/iscat-color-theme.css";
    private Timeline animation;

    private ThemeManager() {
        // Core initialization sync
        loadPalette(currentCssPath);
        globalTint = new SimpleObjectProperty<>(getAccentPrimary());
    }

    /**
     * Hot-swaps the underlying presentation stylesheet. Dynamically builds color maps,
     * resets frame buffer image mutations, and updates visual themes cleanly.
     */
    public void switchTheme(Scene scene, String newCssPath, Color targetSpriteTint, double durationSec) {
        if (scene != null) {
            var oldRes = getClass().getResource(currentCssPath);
            if (oldRes != null) {
                scene.getStylesheets().remove(oldRes.toExternalForm());
            }

            var newRes = Objects.requireNonNull(getClass().getResource(newCssPath), "Stylesheet target missing: " + newCssPath);
            String newUrl = newRes.toExternalForm();
            if (!scene.getStylesheets().contains(newUrl)) {
                scene.getStylesheets().add(newUrl);
            }
        }

        this.currentCssPath = newCssPath;
        loadPalette(newCssPath);

        // Instantly dump old assets to prevent broken color bleed or cache leaks across maps
        tintCache.clear();
        animateTint(targetSpriteTint, durationSec);
    }

    private void loadPalette(String path) {
        currentPalette.clear();
        currentPalette.putAll(CssColorParser.parseColors(path));
    }

    /**
     * Look up colors programmatically. If a key is missing, it logs a warning
     * and flashes bright Magenta to instantly highlight formatting errors during development.
     */
    public Color getColor(String key) {
        Color color = currentPalette.get(key);
        if (color == null) {
            System.err.println("ThemeManager Warning: Looked-up color key target not found: '" + key + "'");
            return Color.MAGENTA;
        }
        return color;
    }

    // --- Explicit Unified Property Access Layer (Replaces ThemeColors.java) ---
    public Color getBgPrimary()        { return getColor("bg-primary"); }
    public Color getBgSecondary()      { return getColor("bg-secondary"); }
    public Color getBgTertiary()       { return getColor("bg-tertiary"); }
    public Color getBgElevated()       { return getColor("bg-elevated"); }

    public Color getTextPrimary()      { return getColor("text-primary"); }
    public Color getTextSecondary()    { return getColor("text-secondary"); }
    public Color getTextTertiary()     { return getColor("text-tertiary"); }
    public Color getTextDisabled()     { return getColor("text-disabled"); }

    public Color getAccentPrimary()    { return getColor("accent-primary"); }
    public Color getAccentSecondary()  { return getColor("accent-secondary"); }
    public Color getAccentTernary()   { return getColor("accent-tertiary"); }

    public Color getColorSuccess()     { return getColor("color-success"); }
    public Color getColorWarning()     { return getColor("color-warning"); }
    public Color getColorError()       { return getColor("color-error"); }
    public Color getColorInfo()        { return getColor("color-info"); }
    public Color getColorTransparent() { return getColor("color-transparent"); }

    // --- Asset Tint Allocation Pipelines ---
    public ObjectProperty<Color> globalTintProperty() { return globalTint; }

    public Image getTintedImage(Image source, Color color) {
        TintKey key = new TintKey(source, color);
        return tintCache.computeIfAbsent(key, k -> SpriteUtils.tinted(source, color));
    }

    private void animateTint(Color newColor, double seconds) {
        if (animation != null) animation.stop();
        animation = new Timeline(new KeyFrame(Duration.seconds(seconds), new KeyValue(globalTint, newColor)));
        animation.play();
    }

    private record TintKey(Image image, Color color) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TintKey(Image image1, Color color1))) {
                return false;
            }
            return image.equals(image1) && color.equals(color1);
        }
        @Override
        public int hashCode() {
            return Objects.hash(image, color);
        }
    }

    // Keep track of the currently active external file (if any)
    private File activeDynamicCssFile = null;

    /**
     * Recreates the CSS file dynamically from a list of raw Hex colors,
     * providing a unified pipeline for both image extraction and manual color picker inputs.
     */
    public void applyHexColorsTheme(Scene scene, List<String> topHexColors, double durationSec) {
        if (topHexColors == null || topHexColors.isEmpty()) return;

        // 1. Generate the physical temporary CSS file
        File newCssFile = CssThemeGenerator.createDynamicStylesheet("/uni/gaben/iscat/styles/iscat-color-theme.css", topHexColors);
        if (newCssFile == null) return;

        // 2. Update the JavaFX Scene Graph stylesheets
        if (scene != null) {
            scene.getStylesheets().removeIf(url -> url.contains("iscat-color-theme.css") || url.contains("iscat-dynamic-theme"));
            String newStylesheetUrl = newCssFile.toURI().toString();
            scene.getStylesheets().add(newStylesheetUrl);
        }

        // 3. Update the Java memory state map via the CSS Parser
        currentPalette.clear();
        currentPalette.putAll(CssColorParser.parseExternalColors(newCssFile));

        // Save active disk reference
        this.activeDynamicCssFile = newCssFile;

        // 4. Animate the global game asset sprite tints
        tintCache.clear();
        animateTint(getAccentPrimary(), durationSec);
    }

    /**
     * Recreates the CSS file dynamically by extracting the top distinct colors from an image.
     */
    public void applyDynamicImageTheme(Scene scene, File imageFile, double durationSec) {
        if (imageFile == null) return;
        // Corrected limit parameter from 3 to 4 to ensure the structural canvas profile is loaded
        List<String> topHexColors = DynamicColors.getTopDistinctColorsHex(imageFile, 4);
        applyHexColorsTheme(scene, topHexColors, durationSec);
    }
}