package uni.gaben.iscat.utils.sprite;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;

/**
 * Classe di utilità per la manipolazione software dei pixel degli sprite.
 * Fornisce funzionalità avanzate come il tinting dinamico (colorazione) delle immagini
 * integrando un sistema di caching a due livelli per non gravare sulle performance della CPU.
 */
public final class SpriteUtils {

    // Cache a due livelli: ImmagineSorgente -> (ColoreTint -> NuovaImmagineRicolorata)
    private static final Map<Image, Map<Color, WritableImage>> TINT_CACHE = new HashMap<>();

    private SpriteUtils() {
        /* Questa classe di utilità non deve essere istanziata */
    }

    /**
     * Applica una maschera di colore (tint) a un'immagine pixel per pixel tramite moltiplicazione cromatica.
     * Se il colore passato è {@link Color#WHITE}, restituisce l'immagine originale senza variazioni o allocazioni.
     *
     * @param source L'{@link Image} sorgente da ricolorare.
     * @param tint   Il {@link Color} da applicare come filtro molecolare.
     * @return Una nuova istanza di {@link Image} tinta, oppure l'immagine originale recuperata dalla cache.
     */
    public static Image tinted(Image source, Color tint) {
        if (Color.WHITE.equals(tint)) {
            return source;
        }

        return TINT_CACHE
                .computeIfAbsent(source, k -> new HashMap<>())
                .computeIfAbsent(tint, c -> buildTinted(source, c));
    }

    /**
     * Esegue la ricolorazione software dell'immagine moltiplicando i canali RGB
     * della sorgente con i canali del colore di destinazione, preservando il canale Alpha.
     */
    private static WritableImage buildTinted(Image source, Color tint) {
        int w = (int) source.getWidth();
        int h = (int) source.getHeight();

        WritableImage out = new WritableImage(w, h);
        PixelReader r = source.getPixelReader();
        PixelWriter pw = out.getPixelWriter();

        double tintRed = tint.getRed();
        double tintGreen = tint.getGreen();
        double tintBlue = tint.getBlue();

        // Ciclo bidimensionale di scansione e riscrittura della matrice di pixel
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Color c = r.getColor(x, y);

                pw.setColor(x, y, Color.color(
                        c.getRed()   * tintRed,
                        c.getGreen() * tintGreen,
                        c.getBlue()  * tintBlue,
                        c.getOpacity() // Preserva la trasparenza originale del frame
                ));
            }
        }
        return out;
    }

    /**
     * Svuota completamente la cache delle immagini ricolorate.
     * Da invocare nei cambi di scena o nello svuotamento dei livelli per liberare memoria Heap.
     */
    public static void clearCaches() {
        TINT_CACHE.clear();
    }
}