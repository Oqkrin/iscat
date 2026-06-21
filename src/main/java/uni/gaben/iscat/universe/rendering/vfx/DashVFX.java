package uni.gaben.iscat.universe.rendering.vfx;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.Glow;
import javafx.scene.paint.Color;
import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.utils.theme.ThemeManager;

/**
 * Gestore grafico per l'effetto visivo del dash (scatto rapido).
 * Le linee di velocità sono generate nella direzione opposta al movimento,
 * con intensità proporzionale alla velocità corrente.
 * L'effetto è ancorato al corpo dell'entità (hugging).
 */
public final class DashVFX {

    private static final Glow GLOW = new Glow(0.5);
    private static final int STREAKS = 12;
    private static final double MAX_LENGTH = 80; // pixel
    private static final double MIN_LENGTH = 15;

    private DashVFX() {}

    /**
     * Renderizza l'effetto dash.
     *
     * @param gc Contesto grafico.
     * @param cx Centro X dell'entità (screen space).
     * @param cy Centro Y dell'entità.
     * @param vx Velocità X (metri/secondo) – sarà convertita in pixel per la scala visiva.
     * @param vy Velocità Y.
     * @param width Larghezza dell'entità (pixel) – usata per scalare la dimensione delle scie.
     * @param height Altezza dell'entità (pixel).
     */
    public static void drawDashRaw(GraphicsContext gc, double cx, double cy, double vx, double vy,
                                   double width, double height) {
        double speed = Math.sqrt(vx * vx + vy * vy);
        if (speed < 0.5) return; // soglia minima per evitare flicker

        // Direzione di movimento (normalizzata)
        double dirX = vx / speed;
        double dirY = vy / speed;

        // Intensità (0..1) basata sulla velocità rispetto a un valore di riferimento
        double refSpeed = 80.0; // velocità di riferimento in px/s (da regolare)
        double intensity = Math.min(speed / refSpeed, 1.5);
        intensity = Math.min(intensity, 1.0);

        // Lunghezza delle scie: proporzionale a speed, ma con minimo/massimo
        double length = MIN_LENGTH + (MAX_LENGTH - MIN_LENGTH) * intensity;

        Color accent = ThemeManager.getInstance().getAccentPrimary();
        double alpha = intensity * 0.9;

        gc.save();
        gc.translate(cx, cy);
        gc.setGlobalBlendMode(BlendMode.ADD);
        gc.setEffect(GLOW);

        // Numero di scie – più scie ad alta velocità
        int numLines = (int) (STREAKS * (1.0 + intensity));

        // Angolo di diffusione: più ampio per rendere l'effetto più evidente
        double spread = Math.PI / 4 + (1 - intensity) * Math.PI / 3; // 45° .. 105°

        for (int i = 0; i < numLines; i++) {
            double t = (double) i / (numLines - 1) - 0.5; // -0.5 .. 0.5
            double theta = t * spread;
            double cos = Math.cos(theta);
            double sin = Math.sin(theta);

            // Direzione della scia: opposta al movimento, con lieve deviazione laterale
            double baseX = -dirX;
            double baseY = -dirY;

            // Applica rotazione per diffusione laterale
            double rotatedX = baseX * cos - baseY * sin;
            double rotatedY = baseX * sin + baseY * cos;

            // Lunghezza scalata con posizione laterale (le scie laterali più corte)
            double lenScale = 1.0 - Math.abs(t) * 0.6;
            double lineLen = length * lenScale;

            // Posizione di partenza: sul bordo dell'entità, nella direzione di movimento
            // Per "hugging", partiamo a metà tra il centro e il bordo, poi allunghiamo all'indietro.
            // In pratica, partiamo dal centro e la scia va all'indietro, ma con un offset iniziale.
            double startX = 0;
            double startY = 0;

            // L'offset iniziale è lungo la direzione opposta al movimento, per far sì che la scia inizi
            // già un po' dietro il centro, dando l'effetto di "hugging".
            double startOffset = width * 0.6;
            double endX = startX + rotatedX * (lineLen + startOffset);
            double endY = startY + rotatedY * (lineLen + startOffset);

            // Larghezza della linea: maggiore al centro, minore ai bordi
            double lineWidth = 2.5 + 4.0 * (1 - Math.abs(t) * 0.5) * intensity;
            lineWidth = Math.max(1.0, lineWidth);

            // Colore: accento con aggiunta di bianco e opacità decrescente verso la fine della scia
            Color color = Color.color(
                    accent.getRed() + 0.3 * (1 - accent.getRed()),
                    accent.getGreen() + 0.3 * (1 - accent.getGreen()),
                    accent.getBlue() + 0.3 * (1 - accent.getBlue()),
                    alpha
            );

            gc.setStroke(color);
            gc.setLineWidth(lineWidth);
            gc.strokeLine(startX, startY, endX, endY);
        }

        // Aggiungi un bagliore centrale per enfatizzare, più pronunciato
        if (intensity > 0.1) {
            double glowSize = 8 + 16 * intensity;
            gc.setFill(Color.WHITE);
            gc.setGlobalAlpha(intensity * 0.5);
            gc.fillOval(-glowSize/2, -glowSize/2, glowSize, glowSize);
        }

        gc.restore();
    }
}