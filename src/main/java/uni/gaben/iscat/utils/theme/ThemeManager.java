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

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Manager centralizzato per il controllo dei temi, delle palette di colore cromatiche
 * e degli effetti visivi globali (come la modalità Rainbow o le transizioni di colore animate).
 * Controlla sia i fogli di stile CSS dell'UI JavaFX sia le maschere di colorazione (tinting) degli sprite di gioco.
 */
public class ThemeManager {

    private static final ThemeManager INSTANCE = new ThemeManager();

    public static ThemeManager getInstance() {
        return INSTANCE;
    }

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
    private File activeDynamicCssFile = null;

    private ThemeManager() {
        loadPalette(currentCssPath);
        globalTint = new SimpleObjectProperty<>(getAccentPrimary());
    }

    /**
     * Attiva l'effetto Rainbow (arcobaleno continuo) sulla scena corrente.
     * Altera dinamicamente e a intervalli regolari sia le costanti CSS dell'interfaccia
     * sia la proprietà di colorazione globale degli sprite di gioco.
     *
     * @param currentScene La scena attiva su cui applicare le variazioni di colore.
     */
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
                // Incrementa l'angolo di tonalità (Hue) nello spazio colore HSB
                rainbowHue = (rainbowHue + 1.0) % 360.0;

                // Quantizza la tonalità a step di 10 gradi per ridurre i calcoli di ricolorazione pixel
                double quantizedHue = Math.round(rainbowHue / 10.0) * 10.0;
                currentRainbowColor = Color.hsb(quantizedHue, 0.85, 0.9);

                currentPalette.put("accent-primary", currentRainbowColor);
                currentPalette.put("accent-secondary", currentRainbowColor);
                currentPalette.put("accent-tertiary", currentRainbowColor);

                // Aggiornamento rallentato per il tinting software degli sprite (ottimizzazione CPU)
                if (now - lastSpriteUpdate > 100_000_000) { // ~100ms
                    globalTint.set(currentRainbowColor);
                    lastSpriteUpdate = now;
                }

                // Aggiornamento rallentato per l'iniezione inline dello stile CSS sul root del grafico
                if (now - lastCssUpdate > 100_000_000) { // ~100ms
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

    /**
     * Disattiva immediatamente la modalità Rainbow ripristinando lo stile originale della scena.
     */
    public void stopRainbowMode() {
        rainbowActive = false;
        if (rainbowTimer != null) {
            rainbowTimer.stop();
        }

        // Rimuove lo stile CSS inline per ripristinare le regole pulite dei fogli di stile collegati
        if (activeSceneRef != null && activeSceneRef.getRoot() != null) {
            activeSceneRef.getRoot().setStyle("");
        }

        activeSceneRef = null;
        SpriteUtils.clearCaches();
        loadPalette(currentCssPath);
        globalTint.set(getAccentPrimary());
    }

    /**
     * Sostituisce il foglio di stile CSS corrente di una scena con uno nuovo,
     * avviando un'animazione di transizione fluida (interpolazione lineare) per la tinta degli sprite.
     */
    public void switchTheme(Scene scene, String newCssPath, Color targetSpriteTint, double durationSec) {
        stopRainbowMode();

        if (scene != null) {
            var oldRes = getClass().getResource(currentCssPath);
            if (oldRes != null) {
                scene.getStylesheets().remove(oldRes.toExternalForm());
            }

            var newRes = Objects.requireNonNull(getClass().getResource(newCssPath), "Stylesheet target mancante: " + newCssPath);
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

    /**
     * Compila e applica a runtime una palette personalizzata estratta in tempo reale (ad esempio tramite i file salvati).
     */
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

    private void animateTint(Color newColor, double seconds) {
        if (animation != null) animation.stop();
        animation = new Timeline(new KeyFrame(Duration.seconds(seconds), new KeyValue(globalTint, newColor)));
        animation.play();
    }

    private void loadPalette(String path) {
        currentPalette.clear();
        currentPalette.putAll(CssColorParser.parseColors(path));
    }

    public Color getColor(String key) {
        Color color = currentPalette.get(key);
        return (color == null) ? Color.MAGENTA : color;
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
    public Color getAccentTertiary()   { return getColor("accent-tertiary"); }
    public Color getColorSuccess()     { return getColor("color-success"); }
    public Color getColorWarning()     { return getColor("color-warning"); }
    public Color getColorError()       { return getColor("color-error"); }
    public Color getColorInfo()        { return getColor("color-info"); }
    public Color getColorTransparent() { return getColor("color-transparent"); }

    private String toHexStr(Color color) {
        return String.format("#%02x%02x%02x",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255)
        );
    }

    private record TintKey(Image image, Color color) {}
}