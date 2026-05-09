package uni.gaben.iscat.utils;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

/**
 * Utility per accedere ai colori del tema CSS.
 * Usa il sistema di lookup di JavaFX per ottenere i colori definiti in iscat-color-theme.css.
 */
public final class ThemeColors {
    
    private ThemeColors() {}
    
    // Cache dei colori per evitare lookup ripetuti
    private static Color accentPrimary;
    private static Color accentSecondary;
    private static Color accentTertiary;
    private static Color textPrimary;
    private static Color textSecondary;
    private static Color colorSuccess;
    private static Color colorWarning;
    private static Color colorError;
    
    /**
     * Ottiene un colore dal CSS usando il lookup.
     * Se il lookup fallisce, ritorna il colore di fallback.
     */
    private static Color lookupColor(String cssVar, Color fallback) {
        try {
            Paint paint = Paint.valueOf(cssVar);
            if (paint instanceof Color) {
                return (Color) paint;
            }
        } catch (Exception e) {
            // Lookup fallito, usa fallback
        }
        return fallback;
    }
    
    // =========================================================================
    // ACCENT COLORS
    // =========================================================================
    
    public static Color getAccentPrimary() {
        if (accentPrimary == null) {
            accentPrimary = Color.rgb(203, 203, 203); // #cbcbcb
        }
        return accentPrimary;
    }
    
    public static Color getAccentSecondary() {
        if (accentSecondary == null) {
            accentSecondary = Color.rgb(169, 169, 169); // #a9a9a9
        }
        return accentSecondary;
    }
    
    public static Color getAccentTertiary() {
        if (accentTertiary == null) {
            accentTertiary = Color.rgb(51, 51, 51); // #333333
        }
        return accentTertiary;
    }
    
    // =========================================================================
    // TEXT COLORS
    // =========================================================================
    
    public static Color getTextPrimary() {
        if (textPrimary == null) {
            textPrimary = Color.rgb(255, 255, 255); // #ffffff
        }
        return textPrimary;
    }
    
    public static Color getTextSecondary() {
        if (textSecondary == null) {
            textSecondary = Color.rgb(220, 220, 220); // #dcdcdc
        }
        return textSecondary;
    }
    
    // =========================================================================
    // SEMANTIC COLORS
    // =========================================================================
    
    public static Color getColorSuccess() {
        if (colorSuccess == null) {
            colorSuccess = Color.rgb(50, 205, 50); // #32cd32
        }
        return colorSuccess;
    }
    
    public static Color getColorWarning() {
        if (colorWarning == null) {
            colorWarning = Color.rgb(255, 215, 0); // #ffd700
        }
        return colorWarning;
    }
    
    public static Color getColorError() {
        if (colorError == null) {
            colorError = Color.rgb(255, 99, 71); // #ff6347
        }
        return colorError;
    }
}
