package uni.gaben.iscat.universe.effects;

/**
 * Modello matematico e logico per la gestione di un'onda d'urto circolare (Shockwave).
 * <p>
 * Questa classe controlla lo stato, l'espansione del raggio e la dissolvenza del canale alfa
 * di un singolo anello d'urto bidimensionale. A differenza della versione contenuta in
 * {@link HitSpark}, questa implementazione utilizza un'interpolazione lineare uniforme
 * basata sul tempo trascorso.
 * </p>
 */
public class Shockwave {

    /** Flag di stato che indica se l'animazione dell'onda d'urto è correntemente in esecuzione. */
    private boolean active = false;

    /** Raggio corrente dell'anello espresso in pixel mondo. */
    private double radius = 0.0;

    /** Coefficiente di opacità corrente (Alpha blending) dell'anello, compreso tra 0.0 e 1.0. */
    private double alpha = 1.0;

    /** Timer cumulativo interno (in secondi) che traccia il tempo trascorso dall'attivazione. */
    private double timer = 0.0;

    /** Durata complessiva dell'effetto espressa in secondi. */
    private double duration;

    /** Il raggio massimo geometrico in pixel mondo che l'anello raggiungerà al termine della durata. */
    private double maxRadius;

    /** Lo spessore fisso della linea di tracciamento dell'anello (Stroke Width) per il rendering. */
    private double lineWidth;

    /**
     * Attiva e inizializza i parametri geometrici e temporali dell'onda d'urto.
     * Ripristina i contatori interni e imposta i vincoli di picco per l'esecuzione.
     *
     * @param duration  La durata totale di riproduzione dell'effetto in secondi.
     * @param maxRadius Il raggio finale espresso in pixel mondo.
     * @param lineWidth Lo spessore del tratto grafico dell'anello.
     */
    public void trigger(double duration,
                        double maxRadius,
                        double lineWidth) {

        this.active = true;

        this.timer = 0.0;
        this.radius = 0.0;
        this.alpha = 1.0;

        this.duration = duration;
        this.maxRadius = maxRadius;
        this.lineWidth = lineWidth;
    }

    /**
     * Aggiorna lo stato cinematico dell'anello calcolandone l'espansione e la dissolvenza.
     * Se il progresso normalizzato supera l'unità, l'effetto viene disattivato automaticamente.
     *
     * @param dt Il passo temporale trascorso dall'ultimo ciclo espresso in secondi (Delta Time).
     */
    public void update(double dt) {
        if (!active) return;

        timer += dt;

        double progress = timer / duration;

        if (progress >= 1.0) {
            active = false;
            return;
        }

        // Interpolazione lineare uniforme
        radius = progress * maxRadius;
        alpha = 1.0 - progress;
    }

    /** @return {@code true} se l'effetto è in fase di riproduzione attivo, {@code false} altrimenti. */
    public boolean isActive() {
        return active;
    }

    /** @return Il raggio istantaneo corrente dell'anello in pixel mondo. */
    public double getRadius() {
        return radius;
    }

    /** @return Il coefficiente di opacità attuale (0.0 per trasparente, 1.0 per opaco). */
    public double getAlpha() {
        return alpha;
    }

    /** @return Lo spessore della linea di contorno dell'anello. */
    public double getLineWidth() {
        return lineWidth;
    }
}