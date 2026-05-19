package uni.gaben.iscat.utils;

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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Orchestratore centrale per l'estetica del gioco.
 * Gestisce CSS, Palette colori dinamica e Tintaggio Sprite.
 */
public class ThemeManager {
    private static final ThemeManager instance = new ThemeManager();
    public static ThemeManager getInstance() { return instance; }

    // --- STATO E CACHE ---
    private final ObjectProperty<Color> globalTint = new SimpleObjectProperty<>(Color.WHITE);
    private final Map<String, Color> currentPalette = new HashMap<>();
    private final Map<TintKey, Image> tintCache = new HashMap<>();

    private String currentCssPath = "/uni/gaben/iscat/styles/iscat-color-theme.css";
    private Timeline animation;

    private ThemeManager() {
        // Caricamento iniziale della palette predefinita
        loadPalette(currentCssPath);
    }

    /**
     * Il metodo "Magico": Cambia il look di tutta l'app (UI + Sprite).
     */
    public void switchTheme(Scene scene, String newCssPath, Color targetSpriteTint, double durationSec) {
        // 1. Aggiorna i fogli di stile della Scene (UI)
        if (scene != null) {
            scene.getStylesheets().remove(Objects.requireNonNull(getClass().getResource(currentCssPath)).toExternalForm());
            String newUrl = Objects.requireNonNull(getClass().getResource(newCssPath)).toExternalForm();
            if (!scene.getStylesheets().contains(newUrl)) {
                scene.getStylesheets().add(newUrl);
            }
        }

        // 2. Aggiorna la logica interna
        this.currentCssPath = newCssPath;
        loadPalette(newCssPath);

        // 3. Importante: Svuota la cache delle immagini tinte.
        // I vecchi colori del tema precedente non servono più e occupano RAM.
        tintCache.clear();

        // 4. Anima la transizione del colore degli sprite (Global Tint)
        animateTint(targetSpriteTint, durationSec);
    }

    private void loadPalette(String path) {
        currentPalette.clear();
        currentPalette.putAll(CssColorParser.parseColors(path));
    }

    /**
     * Sostituisce il vecchio ThemeColors. Ritorna un colore dalla palette attuale.
     */
    public Color getColor(String key, Color defaultValue) {
        return currentPalette.getOrDefault(key, defaultValue);
    }

    // --- LOGICA TINTAGGIO (Per SpriteDrawer) ---

    public ObjectProperty<Color> globalTintProperty() { return globalTint; }

    public Image getTintedImage(Image source, Color color) {
        TintKey key = new TintKey(source, color);
        return tintCache.computeIfAbsent(key, k -> SpriteUtils.tinted(source, color));
    }

    private void animateTint(Color newColor, double seconds) {
        if (animation != null) animation.stop();
        animation = new Timeline(new KeyFrame(Duration.seconds(seconds),
                new KeyValue(globalTint, newColor)));
        animation.play();
    }

    private record TintKey(Image image, Color color) {
        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TintKey(Image image1, Color color1))) return false;
            return image.equals(image1) && color.equals(color1);
        }
        @Override public int hashCode() { return Objects.hash(image, color); }
    }
}