package uni.gaben.iscat.utils.rapporto_aureo;

import javafx.util.Duration;

/** Scalatura aurea di {@link Duration} e sequenze di stagger. */
public final class AnimazioneAurea {
    private AnimazioneAurea() {}

    /** @return Durata ridotta con phiMinore. */
    public static Duration phiMinore(Duration d)   { return Duration.millis(ScalareAureo.phiMinore(d.toMillis())); }

    /** @return Durata espansa con phiMaggiore. */
    public static Duration phiMaggiore(Duration d)   { return Duration.millis(ScalareAureo.phiMaggiore(d.toMillis())); }

    /**
     * Sequenza di {@code n} ritardi decrescenti per animazioni a cascata.
     * Ogni ritardo è phiMinore del precedente.
     */
    public static Duration[] stagger(Duration base, int n) {
        Duration[] r = new Duration[n];
        r[0] = base;
        for (int i = 1; i < n; i++) r[i] = phiMinore(r[i - 1]);
        return r;
    }

    /** Tre durate armoniose [rapida, normale, lenta] a partire da {@code baseMillis}. */
    public static Duration[] durate(double baseMillis) {
        return new Duration[]{
                phiMinore(Duration.millis(baseMillis)),
                Duration.millis(baseMillis),
                phiMaggiore(Duration.millis(baseMillis)),
        };
    }
}
