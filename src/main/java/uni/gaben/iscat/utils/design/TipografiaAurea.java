package uni.gaben.iscat.utils.design;

import uni.gaben.iscat.IscatSettings;

import static uni.gaben.iscat.utils.design.ScalareAureo.*;

/**
 * Scala tipografica ispirata a Material 3: 5 ruoli × 3 dimensioni,
 * taglie derivate dal rapporto aureo a partire da {@link IscatSettings#BASE_FONT_SIZE}.
 *
 * Ruoli: DISPLAY, HEADLINE, TITLE, BODY, LABEL — dimensioni: LARGE(0), MEDIUM(1), SMALL(2).
 */
public final class TipografiaAurea {

    private TipografiaAurea() {}

    public static final int LARGE = 0, MEDIUM = 1, SMALL = 2;

    private static final double BASE = IscatSettings.BASE_FONT_SIZE;

    // --- Taglie per ruolo (LARGE, MEDIUM, SMALL) ---

    /** Display: testi hero, numerali grandi. Usa font espressivo. */
    public static final double[] DISPLAY  = {
        scalaAurea(BASE, 4),  // ≈ 94sp
        scalaAurea(BASE, 3),  // ≈ 58sp
        scalaAurea(BASE, 2),  // ≈ 36sp
    };

    /** Headline: testo breve ad alta enfasi su schermi piccoli. */
    public static final double[] HEADLINE = {
        scalaAurea(BASE, 2),  // ≈ 36sp
        scalaAurea(BASE, 1),  // ≈ 22sp  (phiMaggiore)
        BASE * PHI_D * PHI_D, // ≈ 22sp → aggiustato: phiMaggiore(BASE) ≈ 22sp
    };

    /** Title: enfasi media, testo relativamente breve. */
    public static final double[] TITLE    = {
        phiMaggiore(BASE),    // ≈ 22sp
        BASE,                  // 14sp
        phiMinore(BASE),      // ≈  8sp
    };

    /** Body: testo lungo, leggibile. */
    public static final double[] BODY     = {
        phiMaggiore(BASE),    // ≈ 22sp  (large body, es. articoli)
        BASE,                  // 14sp   (body standard)
        phiMinore(BASE),      // ≈  8sp  (body compatto)
    };

    /** Label: testo piccolo utilitario, bottoni, caption, nav. */
    public static final double[] LABEL    = {
        BASE,                  // 14sp
        phiMinore(BASE),      // ≈  8sp
        scalaAurea(BASE, -2), // ≈  5sp
    };

    // --- Line height ---

    /**
     * Line height per display/headline/title: ×1.2 (Material spec per testo grande).
     * @param size taglia in sp
     */
    public static double lineHeightDisplay(double size) { return size * 1.2; }

    /**
     * Line height per body/label: ×1.5 (Material spec per testo corpo).
     * @param size taglia in sp
     */
    public static double lineHeightBody(double size) { return size * 1.5; }

    // --- Spaziatura aurea ---

    /** Scala di spaziatura aurea a 7 livelli (indice 2 = base). */
    public static double[] scalaSpaziatura(double base) {
        return new double[]{
                scalaAurea(base, -2), // micro
                scalaAurea(base, -1), // stretto
                base,                  // base
                scalaAurea(base,  1), // normale
                scalaAurea(base,  2), // ampio
                scalaAurea(base,  3), // molto ampio
                scalaAurea(base,  4), // massimo
        };
    }
}
