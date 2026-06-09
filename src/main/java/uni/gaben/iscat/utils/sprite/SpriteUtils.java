package uni.gaben.iscat.utils.sprite;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.Bloom;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;

public final class SpriteUtils {
    private static final Map<Image, Map<Color, WritableImage>> tintCache = new HashMap<>();
    private static final Map<Image, Map<Double, WritableImage>> bloomCache = new HashMap<>();

    public static Image tinted(Image source, Color tint) {
        if (tint.equals(Color.WHITE)) return source;
        return tintCache
                .computeIfAbsent(source, k -> new HashMap<>())
                .computeIfAbsent(tint, c -> buildTinted(source, c));
    }

    public static Image bloomed(Image source, double intensity) {
        if (intensity <= 0.01) return source;
        return bloomCache
                .computeIfAbsent(source, k -> new HashMap<>())
                .computeIfAbsent(intensity, i -> applyBloom(source, i));
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

    private static WritableImage applyBloom(Image source, double intensity) {
        int w = (int) source.getWidth();
        int h = (int) source.getHeight();
        Canvas canvas = new Canvas(w, h);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setEffect(new Bloom(intensity));
        gc.drawImage(source, 0, 0, w, h);
        gc.setEffect(null);
        return canvas.snapshot(null, new WritableImage(w, h));
    }

    public static void clearCaches() {
        tintCache.clear();
        bloomCache.clear();
    }
}