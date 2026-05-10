package uni.gaben.iscat.utils;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class ThemeManager {
    private static final ThemeManager instance = new ThemeManager();
    public static ThemeManager getInstance() { return instance; }

    // La proprietà che GameCanvas osserverà
    private final ObjectProperty<Color> globalTint = new SimpleObjectProperty<>(Color.WHITE);
    private Timeline animation;

    private ThemeManager() {
        globalTint.set(Color.WHITE);
    }

    public ObjectProperty<Color> globalTintProperty() { return globalTint; }

    public void applyTheme(Color newColor, double seconds) {
        if (animation != null) animation.stop();

        KeyValue kv = new KeyValue(globalTint, newColor);
        KeyFrame kf = new KeyFrame(Duration.seconds(seconds), kv);
        animation = new Timeline(kf);
        animation.play();
    }
}