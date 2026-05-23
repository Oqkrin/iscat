package uni.gaben.iscat.utils;

import javafx.scene.paint.Color;

import java.util.Map;

/**
 * Utility per accedere ai colori del tema CSS.
 * Usa il sistema di lookup di JavaFX per ottenere i colori definiti in iscat-color-theme.css.
 */
public final class ThemeColors {
    
    private ThemeColors() {}
    
    // Mappa dei colori parsata dinamicamente dal file CSS
    public static Map<String, Color> parsedColors = null;

    public static void ensureLoaded() {
        if (parsedColors == null) {
            parsedColors = CssColorParser.parseColors("/uni/gaben/iscat/styles/iscat-color-theme.css");
        }
    }
    
    // =========================================================================
    // ACCENT COLORS
    // =========================================================================
    
    public static Color getAccentPrimary() {
        ensureLoaded();
        return parsedColors.getOrDefault("accent-primary", Color.rgb(203, 203, 203)); // #cbcbcb
    }
    
    public static Color getAccentSecondary() {
        ensureLoaded();
        return parsedColors.getOrDefault("accent-secondary", Color.rgb(169, 169, 169)); // #a9a9a9
    }
    
    public static Color getAccentTertiary() {
        ensureLoaded();
        return parsedColors.getOrDefault("accent-tertiary", Color.rgb(51, 51, 51)); // #333333
    }
    
    // =========================================================================
    // TEXT COLORS
    // =========================================================================
    
    public static Color getTextPrimary() {
        ensureLoaded();
        return parsedColors.getOrDefault("text-primary", Color.rgb(255, 255, 255)); // #ffffff
    }
    
    public static Color getTextSecondary() {
        ensureLoaded();
        return parsedColors.getOrDefault("text-secondary", Color.rgb(220, 220, 220)); // #dcdcdc
    }
    
    // =========================================================================
    // SEMANTIC COLORS
    // =========================================================================
    
    public static Color getColorSuccess() {
        ensureLoaded();
        return parsedColors.getOrDefault("color-success", Color.rgb(50, 205, 50)); // #32cd32
    }
    
    public static Color getColorWarning() {
        ensureLoaded();
        return parsedColors.getOrDefault("color-warning", Color.rgb(255, 215, 0)); // #ffd700
    }
    
    public static Color getColorError() {
        ensureLoaded();
        return parsedColors.getOrDefault("color-error", Color.rgb(255, 99, 71)); // #ff6347
    }
}
