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

    // --- Rainbow Mode Properties ---
    private AnimationTimer rainbowTimer;
    private double rainbowHue = 0.0;
    private boolean rainbowActive = false;
    private Color currentRainbowColor = Color.WHITE;
    private Scene activeSceneRef = null; // Riferimento per aggiornare il CSS a schermo

    private ThemeManager() {
        // Core initialization sync
        loadPalette(currentCssPath);
        globalTint = new SimpleObjectProperty<>(getAccentPrimary());
    }

    /**
     * Attiva la modalità arcobaleno graduale per Primary, Secondary e Tertiary.
     * Aggiorna anche il CSS iniettato nella scena passata come parametro.
     */
    public void startRainbowMode(Scene currentScene) {
        if (rainbowTimer != null) {
            rainbowTimer.stop();
        }

        this.activeSceneRef = currentScene;
        rainbowActive = true;

        rainbowTimer = new AnimationTimer() {
            private long lastCacheClear = 0;
            private long lastCssUpdate = 0;
            private long lastSpriteUpdate = 0;

            @Override
            public void handle(long now) {
                // Incremento graduale della tonalità (uguale a prima per la fluidità del Canvas)
                rainbowHue = (rainbowHue + 0.4) % 360.0;
                currentRainbowColor = Color.hsb(rainbowHue, 0.85, 0.9);

                // Aggiorna la palette locale in memoria usata dai getter dei proiettili e scritte
                currentPalette.put("accent-primary", currentRainbowColor);
                currentPalette.put("accent-secondary", currentRainbowColor);
                currentPalette.put("accent-tertiary", currentRainbowColor);

                // LIMITATORE DI TEXTURE HARDWARE (Ogni 200ms)
                // Aggiorna globalTint (usato dagli sprite ricolorati) solo 5 volte al secondo.
                // In questo modo la GPU deve istanziare pochissime varianti e non va in crash.
                if (now - lastSpriteUpdate > 200_000_000) {
                    globalTint.set(currentRainbowColor);
                    lastSpriteUpdate = now;
                }

                // Rigenera e applica il file CSS reale ogni 15 frame (circa ogni 250ms)
                if (now - lastCssUpdate > 250_000_000) {
                    if (activeSceneRef != null) {
                        String hex = toHexStr(currentRainbowColor);
                        List<String> hexPalette = List.of(hex, hex, hex, toHexStr(getBgPrimary()));
                        applyHexColorsThemeInternal(activeSceneRef, hexPalette);
                    }
                    lastCssUpdate = now;
                }

                // SVUOTAMENTO DI SICUREZZA (Ogni 2 secondi anziché 200ms)
                // Svuotare troppo spesso manda in panico la pipeline D3DSwapChain se ci sono draw calls attive.
                if (now - lastCacheClear > 2_000_000_000L) {
                    tintCache.clear();
                    lastCacheClear = now;
                }
            }
        };
        rainbowTimer.start();
    }

    /**
     * Disattiva la modalità arcobaleno ripristinando i colori originari del tema.
     */
    public void stopRainbowMode() {
        rainbowActive = false;
        if (rainbowTimer != null) {
            rainbowTimer.stop();
        }
        activeSceneRef = null;
        tintCache.clear();
        loadPalette(currentCssPath);
        globalTint.set(getAccentPrimary());
    }

    public boolean isRainbowModeActive() {
        return rainbowActive;
    }

    /**
     * Helper interno veloce per convertire il colore senza alterare la pipeline dei fogli di stile esterni
     */
    private String toHexStr(Color color) {
        return String.format("#%02x%02x%02x",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255)
        );
    }

    /**
     * Hot-swaps the underlying presentation stylesheet.
     */
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

        tintCache.clear();
        animateTint(targetSpriteTint, durationSec);
    }

    private void loadPalette(String path) {
        currentPalette.clear();
        currentPalette.putAll(CssColorParser.parseColors(path));
    }

    public Color getColor(String key) {
        Color color = currentPalette.get(key);
        if (color == null) {
            System.err.println("ThemeManager Warning: Looked-up color key target not found: '" + key + "'");
            return Color.MAGENTA;
        }
        return color;
    }

    // --- Explicit Unified Property Access Layer ---
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

    /**
     * Esposizione pubblica standard per modifiche manuali da ColorPicker o Immagini.
     */
    public void applyHexColorsTheme(Scene scene, List<String> topHexColors, double durationSec) {
        if (topHexColors == null || topHexColors.isEmpty()) return;
        applyHexColorsThemeInternal(scene, topHexColors);

        currentPalette.clear();
        currentPalette.putAll(CssColorParser.parseExternalColors(this.activeDynamicCssFile));
        tintCache.clear();
        animateTint(getAccentPrimary(), durationSec);
    }

    /**
     * Pipeline di riscrittura fisica del CSS sul disco e swap immediato nell'albero di JavaFX
     */
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