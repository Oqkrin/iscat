package uni.gaben.iscat.utils.design;

import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.List;

import static uni.gaben.iscat.utils.design.ScalareAureo.*;

/**
 * Colori basati sul rapporto aureo: scala di opacità e sistema GRC (Golden Ratio Colors).
 * I 10 livelli GRC mappano i valori RGB: {0, 14, 37, 60, 97, 158, 195, 218, 241, 255}.
 */
public final class ColoreAureo {

    private ColoreAureo() {}

    public static final int[] GRC      = { 0, 14, 37, 60, 97, 158, 195, 218, 241, 255 };
    public static final int[] GRC_CORE = { 0, 97, 158, 255 };

    /**
     * Crea un {@link Color} da tre indici GRC (0–9).
     * Es: {@code grc(0, 5, 4)} → rgb(0, 158, 97) = verde aureo.
     */
    public static Color grc(int r, int g, int b) {
        return Color.rgb(GRC[r], GRC[g], GRC[b]);
    }

    /** Come {@link #grc(int, int, int)} con opacità. */
    public static Color grc(int r, int g, int b, double opacity) {
        return Color.rgb(GRC[r], GRC[g], GRC[b], opacity);
    }

    /** @return Grigio basato sulla scala GRC. Indice 0 (nero) a 9 (bianco). */
    public static Color grigioGrc(int indice) {
        int v = GRC[Math.min(9, Math.max(0, indice))];
        return Color.rgb(v, v, v);
    }

    /** Indice GRC (0–9) più vicino a un valore RGB grezzo (0–255). */
    public static int indicePiuVicino(int valore) {
        int best = 0, minDist = Integer.MAX_VALUE;
        for (int i = 0; i < GRC.length; i++) {
            int dist = Math.abs(GRC[i] - valore);
            if (dist < minDist) { minDist = dist; best = i; }
        }
        return best;
    }

    /**
     * Converte un {@link Color} JavaFX nei tre indici GRC più vicini.
     * @return array [r, g, b] di indici GRC (0–9)
     */
    public static int[] toGRC(Color c) {
        return new int[]{
                indicePiuVicino((int) Math.round(c.getRed()   * 255)),
                indicePiuVicino((int) Math.round(c.getGreen() * 255)),
                indicePiuVicino((int) Math.round(c.getBlue()  * 255)),
        };
    }

    // ========================================================================
    // ARMONIE HSB E ANGOLO AUREO
    // ========================================================================

    /**
     ** Genera il "prossimo" colore armonico ruotando la tonalità (Hue) dell'Angolo Aureo.
     ** Questo garantisce una distribuzione dei colori che non si sovrappongono mai visivamente.
     ** @param c Colore di partenza.
     ** @return Un nuovo colore armonico.
     */
    public static Color armonicoSuccessivo(Color c) {
        double h = (c.getHue() + ANGOLO_AUREO_GRADI) % 360;
        return Color.hsb(h, c.getSaturation(), c.getBrightness(), c.getOpacity());
    }

    /**
     ** Genera una palette di N colori armonici partendo da un colore base,
     ** utilizzando la rotazione dell'Angolo Aureo.
     */
    public static List<Color> paletteAurea(Color base, int n) {
        List<Color> palette = new ArrayList<>();
        Color corrente = base;
        for (int i = 0; i < n; i++) {
            palette.add(corrente);
            corrente = armonicoSuccessivo(corrente);
        }
        return palette;
    }

    /**
     ** Applica la scala aurea alla saturazione o alla luminosità.
     ** @param c Colore originale.
     ** @param livelloSaturazione Passi di Φ per la saturazione.
     ** @param livelloLuminosita Passi di Φ per la luminosità.
     */
    public static Color variazioneHsbAurea(Color c, int livelloSaturazione, int livelloLuminosita) {
        double s = Math.min(1.0, Math.max(0.0, ScalareAureo.scalaAurea(c.getSaturation(), livelloSaturazione)));
        double b = Math.min(1.0, Math.max(0.0, ScalareAureo.scalaAurea(c.getBrightness(), livelloLuminosita)));
        return Color.hsb(c.getHue(), s, b, c.getOpacity());
    }

    /** Le 64 core colors GRC: tutte le combinazioni di {0, 97, 158, 255} per R, G, B. */
    public static Color[] coreColors() {
        Color[] colors = new Color[64];
        int i = 0;
        for (int r : GRC_CORE)
            for (int g : GRC_CORE)
                for (int b : GRC_CORE)
                    colors[i++] = Color.rgb(r, g, b);
        return colors;
    }

    /** @return Colore con opacità scalata di {@code livello} passi aurali, bloccata in [0,1]. */
    public static Color scalaAurea(Color c, int livello) {
        double op = Math.min(1.0, Math.max(0.0, ScalareAureo.scalaAurea(c.getOpacity(), livello)));
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), op);
    }

    /**
     * Genera {@code n} varianti del colore con opacità decrescente (×IPHI ad ogni passo).
     * Es: [1.0, 0.618, 0.382, 0.236, …]
     */
    public static Color[] scalaDiOpacita(Color c, int n) {
        Color[] v = new Color[n];
        double op = c.getOpacity();
        for (int i = 0; i < n; i++) {
            v[i] = new Color(c.getRed(), c.getGreen(), c.getBlue(), op);
            op = phiMinore(op);
        }
        return v;
    }
}