package uni.gaben.iscat.utils.sprite;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;

public final class SpriteUtils {
    private static final Map<Image, Map<Color, WritableImage>> tintCache = new HashMap<>();

    public static Image tinted(Image source, Color tint) {
        if (tint.equals(Color.WHITE)) return source;
        return tintCache
                .computeIfAbsent(source, k -> new HashMap<>())
                .computeIfAbsent(tint, c -> buildTinted(source, c));
    }

    private static WritableImage buildTinted(Image source, Color tint) {
        int w = (int) source.getWidth(), h = (int) source.getHeight();
        WritableImage out = new WritableImage(w, h);
        PixelReader r = source.getPixelReader();
        PixelWriter pw = out.getPixelWriter();
        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++) {
                Color c = r.getColor(x, y);
                pw.setColor(x, y, Color.color(
                        c.getRed()   * tint.getRed(),
                        c.getGreen() * tint.getGreen(),
                        c.getBlue()  * tint.getBlue(),
                        c.getOpacity()));
            }
        return out;
    }

    public static void clearCaches() {
        tintCache.clear();
    }
}