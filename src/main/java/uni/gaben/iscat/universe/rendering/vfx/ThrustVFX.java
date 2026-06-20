package uni.gaben.iscat.universe.rendering.vfx;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.Effect;
import javafx.scene.effect.Glow;
import javafx.scene.paint.Color;
import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.effects.Thrust;
import uni.gaben.iscat.utils.design.ScalareAureo;
import uni.gaben.iscat.utils.theme.ThemeManager;

import java.util.Random;

/**
 * Utility class responsabile del rendering degli effetti particellari di spinta dei motori (Thrust VFX)
 * delle navi spaziali all'interno del mondo di gioco.
 * <p>
 * L'effetto simula la scia termica di un propulsore generando un sistema dinamico di particelle
 * basato su distribuzioni gaussiane. Gestisce l'effetto di deriva laterale (drift) simulando
 * l'inerzia del fluido di scarico ed effettua una degradazione del colore per simulare il raffreddamento
 * del plasma man mano che ci si allontana dall'ugello di scarico.
 * </p>
 * <p>
 * Questa classe non è istanziabile e opera in modalità nativa additiva ({@link BlendMode#ADD})
 * accoppiata a un effetto di post-processing {@link Glow} per massimizzare la resa visiva della luce emessa.
 * </p>
 */
public final class ThrustVFX {

    /** Generatore di numeri pseudo-casuali per il comportamento stocastico delle particelle. */
    private static final Random RANDOM = new Random();

    /** Effetto di bagliore (Glow) applicato al contesto grafico durante la renderizzazione del propulsore. */
    private static final Effect thrustEffect = new Glow();

    /**
     * Costruttore privato per prevenire l'istanziamento della classe utility.
     */
    private ThrustVFX() {}

    /**
     * Disegna l'effetto particellare completo del motore di spinta basandosi sullo stato attuale del propulsore.
     * <p>
     * Il metodo esegue internamente una trasformazione affine (traslazione e rotazione) sul {@link GraphicsContext}
     * per allineare l'emissione dei vettori particellari all'orientamento locale della nave spaziale. Lo stato del
     * canvas viene salvato e ripristinato correttamente alla fine del processo.
     * </p>
     *
     * @param gc     il {@link GraphicsContext} del canvas su cui effettuare il disegno
     * @param cx     la coordinata X del punto di ancoraggio/origine del propulsore in pixel (screen space)
     * @param cy     la coordinata Y del punto di ancoraggio/origine del propulsore in pixel (screen space)
     * @param angle  l'angolo di rotazione attuale della nave espresso in gradi
     * @param thrust il modello contenente i dati di intensità, dimensioni fisiche della nave e vettori di deriva inerziale dello scarico
     */
    public static void drawThrustRaw(GraphicsContext gc, double cx, double cy, double angle, Thrust thrust) {
        if (thrust == null || !thrust.isActive() || thrust.getIntensity() < 0.01) return;

        double intensity = Math.min(thrust.getIntensity(), 1.0);
        double w = thrust.getShipWidth();
        double h = thrust.getShipHeight();
        Vector2 drift = thrust.getLocalDrift();

        int particleCount = (int) (Thrust.THRUST_MIN_PARTICLES
                + intensity * Thrust.THRUST_EXTRA_PARTICLES);
        double maxThrustHeight = ScalareAureo.phiMaggiore(h) * 1.2;
        Color accent = ThemeManager.getInstance().getAccentPrimary();

        gc.save();
        gc.translate(cx, cy);
        gc.rotate(angle);
        gc.setGlobalBlendMode(BlendMode.ADD);
        gc.setEffect(thrustEffect);

        for (int i = 0; i < particleCount; i++) {
            double distRatio = RANDOM.nextDouble();
            double spreadX = w * (0.15 + Math.pow(distRatio, 1.5) * Thrust.THRUST_SPREAD_X_FACTOR);
            double whipX = 0;
            if (distRatio > 0.15) {
                double curveRatio = (distRatio - 0.15) / 0.85;
                whipX = drift.x * Math.pow(curveRatio, 5) * (w * 2);
            }
            double offsetX = (RANDOM.nextGaussian() * 0.22) * spreadX + whipX;
            double offsetY = (h / 2) + (distRatio * maxThrustHeight) + drift.y * distRatio * (h * 0.1);
            offsetY = Math.max(offsetY, h / 2);
            double size = (Thrust.THRUST_MIN_PARTICLE_SIZE
                    + RANDOM.nextDouble() * Thrust.THRUST_PARTICLE_SIZE_VARIATION)
                    * (1.2 - distRatio * 0.9) * (0.7 + intensity * 0.5);

            gc.setFill(getParticleColor(distRatio, intensity, accent));
            gc.fillOval(offsetX - size / 2, offsetY - size / 2, size, size);
        }

        gc.setGlobalBlendMode(BlendMode.SRC_OVER);
        gc.restore();
    }

    /**
     * Calcola dinamicamente il colore di una singola particella in base alla sua distanza dall'ugello di scarico
     * e all'intensità attuale del motore.
     * <p>
     * L'algoritmo suddivide la scia in tre macro-aree di simulazione termica:
     * <ol>
     * <li><b>Zona di combustione interna (0% - 25% della distanza):</b> Il plasma è estremamente caldo e vira cromaticamente
     * verso il bianco puro man mano che si avvicina al punto di origine.</li>
     * <li><b>Zona di emissione stabile (25% - 70% della distanza):</b> La particella si stabilizza sul colore primario di accento
     * configurato nel tema dell'applicazione.</li>
     * <li><b>Zona di dissipazione e raffreddamento (70% - 100% della distanza):</b> Il plasma si raffredda rapidamente,
     * riducendo progressivamente i canali RGB e abbattendo l'opacità (effetto dissolvenza).</li>
     * </ol>
     * </p>
     *
     * @param distanceRatio la posizione della particella lungo la scia, espressa come valore normalizzato compreso tra {@code 0.0} (origine) e {@code 1.0} (estremità)
     * @param intensity     il livello di spinta attuale del propulsore, limitato superiormente a {@code 1.0}
     * @param accent        il {@link Color} primario del brand/fazione recuperato dal {@link ThemeManager}
     * @return il {@link Color} risultante calcolato per la particella, completo del canale alpha di trasparenza
     */
    private static Color getParticleColor(double distanceRatio, double intensity, Color accent) {
        double alpha = (1.0 - distanceRatio) * (0.4 + intensity * 0.6);
        if (distanceRatio < 0.25) {
            double t = distanceRatio / 0.25;
            return Color.color(
                    accent.getRed()   + (1.0 - accent.getRed())   * (1.0 - t),
                    accent.getGreen() + (1.0 - accent.getGreen()) * (1.0 - t),
                    accent.getBlue()  + (1.0 - accent.getBlue())  * (1.0 - t),
                    alpha
            );
        } else if (distanceRatio < 0.7) {
            return Color.color(accent.getRed(), accent.getGreen(), accent.getBlue(), alpha * 0.85);
        } else {
            double t = (distanceRatio - 0.7) / 0.3;
            double cooling = 1.0 - (t * 0.75);
            return Color.color(
                    Math.max(0.0, accent.getRed()   * cooling),
                    Math.max(0.0, accent.getGreen() * cooling),
                    Math.max(0.0, accent.getBlue()  * cooling),
                    alpha * (1.0 - t)
            );
        }
    }
}