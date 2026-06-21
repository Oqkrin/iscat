package uni.gaben.iscat.utils.design;

import uni.gaben.iscat.IscatSettings;

import static uni.gaben.iscat.utils.design.ScalareAureo.*;

/**
 * Scala tipografica ispirata a Material 3: 5 ruoli × 3 dimensioni,
 * taglie derivate dal rapporto aureo a partire da {@link IscatSettings#STANDARD_UNIT}.
 *
 * Ruoli: DISPLAY, HEADLINE, TITLE, BODY, LABEL — dimensioni: LARGE(0), MEDIUM(1), SMALL(2).
 */
public final class TipografiaAurea {

    private TipografiaAurea() {}

    public static final int LARGE = 0, MEDIUM = 1, SMALL = 2;

    private static final double BASE = IscatSettings.STANDARD_UNIT;

    /** Label: testo piccolo utilitario, bottoni, caption, nav. */
    public static final double[] LABEL    = {
        BASE,                  // 14sp
        phiMinore(BASE),      // ≈  8sp
        scalaAurea(BASE, -2), // ≈  5sp
    };
}
