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
 * Gestore grafico degli effetti particellari di spinta e scia termica dei motori (Thrust VFX).
 * Simula il flusso di plasma applicando distribuzioni gaussiane stocastiche, derive vettoriali
 * da inerzia dei fluidi (drift) e una degradazione cromatica progressiva per raffreddamento termico.
 * Opera in modalità additiva ({@link BlendMode#ADD}) accoppiata a un filtro {@link Glow}.
 */
public final class ThrustVFX {

    private static final Random RANDOM = new Random();
    private static final Effect thrustEffect = new Glow();

    private ThrustVFX() {}

    /**
     * Esegue la trasformazione affine locale ed inserisce le particelle della scia gassosa nel contesto grafico.
     * Le coordinate fornite devono essere già proiettate in pixel rispetto alla telecamera (Screen Space).
     *
     * @param angle  L'angolo di rotazione attuale della nave espresso in gradi.
     * @param thrust Il modello con lo stato fisico, le dimensioni della nave ed i vettori di drift inerziale.
     */
    public static void drawThrustRaw(GraphicsContext gc, double cx, double cy, double angle, Thrust thrust) {
        if (thrust == null || !thrust.isActive() || thrust.getIntensity() < 0.01) return;

        double intensity = Math.min(thrust.getIntensity(), 1.0);
        double w = thrust.getShipWidth();
        double h = thrust.getShipHeight();
        Vector2 drift = thrust.getLocalDrift();

        int particleCount = (int) (Thrust.THRUST_MIN_PARTICLES + intensity * Thrust.THRUST_EXTRA_PARTICLES);
        double maxThrustHeight = ScalareAureo.phiMaggiore(h) * 1.2;
        Color accent = ThemeManager.getInstance().getAccentPrimary();

        gc.save();
        gc.translate(cx, cy);
        gc.rotate(angle);
        gc.setGlobalBlendMode(BlendMode.ADD);
        gc.setEffect(thrustEffect);

        // --- Generazione Stocastica del Flusso Particellare ---
        for (int i = 0; i < particleCount; i++) {
            double distRatio = RANDOM.nextDouble();
            double spreadX = w * (0.15 + Math.pow(distRatio, 1.5) * Thrust.THRUST_SPREAD_X_FACTOR);

            // Calcolo dell'effetto sferzata (whip) causato dalla deriva laterale del fluido
            double whipX = 0;
            if (distRatio > 0.15) {
                double curveRatio = (distRatio - 0.15) / 0.85;
                whipX = drift.x * Math.pow(curveRatio, 5) * (w * 2);
            }

            double offsetX = (RANDOM.nextGaussian() * 0.22) * spreadX + whipX;
            double offsetY = (h / 2) + (distRatio * maxThrustHeight) + drift.y * distRatio * (h * 0.1);
            offsetY = Math.max(offsetY, h / 2);

            double size = (Thrust.THRUST_MIN_PARTICLE_SIZE + RANDOM.nextDouble() * Thrust.THRUST_PARTICLE_SIZE_VARIATION)
                    * (1.2 - distRatio * 0.9) * (0.7 + intensity * 0.5);

            gc.setFill(getParticleColor(distRatio, intensity, accent));
            gc.fillOval(offsetX - size / 2, offsetY - size / 2, size, size);
        }

        gc.setGlobalBlendMode(BlendMode.SRC_OVER);
        gc.restore();
    }

    /**
     * Calcola la degradazione termica e la variazione cromatica della particella lungo la scia.
     * Segue 3 stadi di simulazione gassosa:
     * <ol>
     * <li><b>0% - 25% (Combustione):</b> Plasma caldo ad altissima temperatura che vira al bianco puro.</li>
     * <li><b>25% - 70% (Emissione):</b> Stabilizzazione sul colore d'accento primario della fazione.</li>
     * <li><b>70% - 100% (Dissipazione):</b> Raffreddamento rapido con decadimento cromatico RGB e dissolvenza Alpha.</li>
     * </ol>
     *
     * @param distanceRatio La posizione normalizzata della particella lungo la scia da {@code 0.0} a {@code 1.0}.
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