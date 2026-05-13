package uni.gaben.iscat.utils;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.Image;

public class IscatSpriteLoader {
    StringProperty path = new SimpleStringProperty();
    Image[] sprites;
    IscatSpriteLoader(String path, int framesCount, Double dim) {
        sprites = new Image[framesCount];
        sprites[0] = new Image(getClass().getResourceAsStream(path));
    }
}
