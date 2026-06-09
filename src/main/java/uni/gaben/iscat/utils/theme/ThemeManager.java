package uni.gaben.iscat.utils.theme;

import javafx.animation.AnimationTimer;
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

public class ThemeManager {
    private static final ThemeManager instance = new ThemeManager();
    public static ThemeManager getInstance() { return instance; }

    private final ObjectProperty<Color> globalTint;
    private final Map<String, Color> currentPalette = new HashMap<>();
    private final Map<TintKey, Image> tintCache = new HashMap<>();

    private String currentCssPath = "/uni/gaben/iscat/styles/iscat-color-theme.css";
    private Timeline animation;

    private AnimationTimer rainbowTimer;
    private double rainbowHue = 0.0;
    private boolean rainbowActive = false;
    private Color currentRainbowColor = Color.WHITE;
    private Scene activeSceneRef = null;

    private ThemeManager() {
        loadPalette(currentCssPath);
        globalTint = new SimpleObjectProperty<>(getAccentPrimary());
    }

    public void startRainbowMode(Scene currentScene) {
        if (rainbowTimer != null) {
            rainbowTimer.stop();
        }

        this.activeSceneRef = currentScene;
        rainbowActive = true;

        rainbowTimer = new AnimationTimer() {
            private long lastCssUpdate = 0;
            private long lastSpriteUpdate = 0;

            @Override
            public void handle(long now) {
                rainbowHue = (rainbowHue + 1.0) % 360.0;

                double quantizedHue = Math.round(rainbowHue / 10.0) * 10.0;
                currentRainbowColor = Color.hsb(quantizedHue, 0.85, 0.9);

                currentPalette.put("accent-primary", currentRainbowColor);
                currentPalette.put("accent-secondary", currentRainbowColor);
                currentPalette.put("accent-tertiary", currentRainbowColor);

                if (now - lastSpriteUpdate > 100_000_000) {
                    globalTint.set(currentRainbowColor);
                    lastSpriteUpdate = now;
                }

                if (now - lastCssUpdate > 100_000_000) {
                    if (activeSceneRef != null && activeSceneRef.getRoot() != null) {
                        String hex = toHexStr(currentRainbowColor);

                        activeSceneRef.getRoot().setStyle(
                                "-accent-primary: " + hex + ";" +
                                        "-accent-secondary: " + hex + ";" +
                                        "-accent-tertiary: " + hex + ";"
                        );
                    }
                    lastCssUpdate = now;
                }
            }
        };
        rainbowTimer.start();
    }

    public void stopRainbowMode() {
        rainbowActive = false;
        if (rainbowTimer != null) {
            rainbowTimer.stop();
        }

        // Ripristiniamo lo stile pulito sul root rimuovendo i colori iniettati a mano
        if (activeSceneRef != null && activeSceneRef.getRoot() != null) {
            activeSceneRef.getRoot().setStyle("");
        }

        activeSceneRef = null;
        SpriteUtils.clearCaches();
        loadPalette(currentCssPath);
        globalTint.set(getAccentPrimary());
    }

    public boolean isRainbowModeActive() { return rainbowActive; }

    private String toHexStr(Color color) {
        return String.format("#%02x%02x%02x",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255)
        );
    }

    public void switchTheme(Scene scene, String newCssPath, Color targetSpriteTint, double durationSec) {
        stopRainbowMode();

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

        SpriteUtils.clearCaches();
        animateTint(targetSpriteTint, durationSec);
    }

    private void loadPalette(String path) {
        currentPalette.clear();
        currentPalette.putAll(CssColorParser.parseColors(path));
    }

    public Color getColor(String key) {
        Color color = currentPalette.get(key);
        if (color == null) {
            return Color.MAGENTA;
        }
        return color;
    }

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

    private record TintKey(Image image, Color color) {}

    private File activeDynamicCssFile = null;

    public void applyHexColorsTheme(Scene scene, List<String> topHexColors, double durationSec) {
        if (topHexColors == null || topHexColors.isEmpty()) return;
        applyHexColorsThemeInternal(scene, topHexColors);

        currentPalette.clear();
        currentPalette.putAll(CssColorParser.parseExternalColors(this.activeDynamicCssFile));
        SpriteUtils.clearCaches();
        animateTint(getAccentPrimary(), durationSec);
    }

    private void applyHexColorsThemeInternal(Scene scene, List<String> topHexColors) {
        File newCssFile = CssThemeGenerator.createDynamicStylesheet("/uni/gaben/iscat/styles/iscat-color-theme.css", topHexColors);
        if (newCssFile == null) return;

        if (scene != null) {
            scene.getStylesheets().removeIf(url -> url.contains("iscat-color-theme.css") || url.contains("iscat-dynamic-theme"));
            String newStylesheetUrl = newCssFile.toURI().toString();
            scene.getStylesheets().add(newStylesheetUrl);
        }
        this.activeDynamicCssFile = newCssFile;
    }
}