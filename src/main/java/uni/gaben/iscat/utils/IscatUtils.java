package uni.gaben.iscat.utils;

import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;


public class IscatUtils {
    private IscatUtils() {
        /* Classe utils da non inizializzare */
    }

    public static void roundRectangle(Region toClip, Double clipBorderRadius) {
        Rectangle clip = new Rectangle();
        clip.setArcWidth(clipBorderRadius);
        clip.setArcHeight(clipBorderRadius);
        clip.widthProperty().bind(toClip.widthProperty());
        clip.heightProperty().bind(toClip.heightProperty());
        toClip.setClip(clip);
    }
}
