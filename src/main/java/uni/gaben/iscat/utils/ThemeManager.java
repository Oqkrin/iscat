package uni.gaben.iscat.utils;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import javafx.scene.image.Image;
import uni.gaben.iscat.game.components.entities.npcs.SpriteUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ThemeManager {
    private static final ThemeManager instance = new ThemeManager();
    public static ThemeManager getInstance() { return instance; }

    // --- CACHE TINTAGGIO ---
    private final Map<TintKey, Image> tintCache = new HashMap<>();

    // La proprietà che GameCanvas osserverà
    private final ObjectProperty<Color> globalTint = new SimpleObjectProperty<>(Color.WHITE);
    private Timeline animation;

    private ThemeManager() {
        globalTint.set(Color.WHITE);
    }

    public ObjectProperty<Color> globalTintProperty() { return globalTint; }

    /**
     * Restituisce una versione tinta dell'immagine sorgente.
     * Utilizza una cache interna per evitare ricalcoli costosi.
     */
    public Image getTintedImage(Image source, Color color) {
        TintKey key = new TintKey(source, color);
        return tintCache.computeIfAbsent(key, k -> SpriteUtils.tinted(source, color));
    }

    public void applyTheme(Color newColor, double seconds) {
        if (animation != null) animation.stop();

        KeyValue kv = new KeyValue(globalTint, newColor);
        KeyFrame kf = new KeyFrame(Duration.seconds(seconds), kv);
        animation = new Timeline(kf);
        animation.play();
    }

    // Helper per la chiave della cache
    private record TintKey(Image image, Color color) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TintKey tintKey = (TintKey) o;
            return Objects.equals(image, tintKey.image) && Objects.equals(color, tintKey.color);
        }

        @Override
        public int hashCode() {
            return Objects.hash(image, color);
        }
    }
}