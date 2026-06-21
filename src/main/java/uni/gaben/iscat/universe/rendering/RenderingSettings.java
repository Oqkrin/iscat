package uni.gaben.iscat.universe.rendering;

/**
 * Costanti globali di configurazione del motore grafico (Rendering Settings).
 * Centralizza gli offset angolari per allineare l'orientamento nativo dei modelli fisici alle texture del gioco.
 */
public class RenderingSettings {

    private RenderingSettings() {}

    /** Offset di rotazione base espresso in gradi. */
    public static final double BASE_ROTDEG_OFFSET = -90.0;

    /** Offset di rotazione base espresso in radianti. */
    public static final double BASE_ROTRAD_OFFSET = Math.toRadians(BASE_ROTDEG_OFFSET);
}