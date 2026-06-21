package uni.gaben.iscat.universe.rendering.vfx;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.paint.Color;
import uni.gaben.iscat.universe.effects.Shockwave;

/**
 * Gestore grafico per il rendering di onde d'urto e distorsioni gravitazionali (Shockwave & Black Hole VFX).
 * Ottimizzato per prestazioni ad alto refresh rate ed a impatto zero sulla Garbage Collection (Zero-Allocation):
 * pre-calcola tabelle trigonometriche fisse e riutilizza array di buffer primitivi statici per generare i poligoni.
 */
public final class ShockwaveVFX {

    private static final int MAX_SEGMENTS = 64;
    private static final double[] COS_TABLE = new double[MAX_SEGMENTS];
    private static final double[] SIN_TABLE = new double[MAX_SEGMENTS];
    private static final double[] X_BUFFER = new double[MAX_SEGMENTS];
    private static final double[] Y_BUFFER = new double[MAX_SEGMENTS];

    static {
        for (int i = 0; i < MAX_SEGMENTS; i++) {
            double angle = (Math.PI * 2 * i) / MAX_SEGMENTS;
            COS_TABLE[i] = Math.cos(angle);
            SIN_TABLE[i] = Math.sin(angle);
        }
    }

    private ShockwaveVFX() {}

    /**
     * Disegna un'onda d'urto ad anelli concentrici sfumati sovrapponendo ovali a opacità e spessore variabili.
     * Le coordinate fornite devono essere già proiettate in pixel rispetto alla telecamera (Screen Space).
     */
    public static void drawShockwaveRaw(GraphicsContext gc, double cx, double cy, Shockwave shockwave) {
        double radius = shockwave.getRadius();
        double alpha = shockwave.getAlpha();
        double d = radius * 2;
        double baseLineWidth = shockwave.getLineWidth();

        gc.setFill(Color.WHITE);
        gc.setStroke(Color.WHITE);

        // Disegno a passaggi multipli (Multi-pass rendering) per l'effetto di alone sfumato
        gc.setGlobalAlpha(alpha * 0.15);
        gc.fillOval(cx - radius, cy - radius, d, d);
        gc.setGlobalAlpha(alpha * 0.3);
        gc.setLineWidth(baseLineWidth * 3.5);
        gc.strokeOval(cx - radius, cy - radius, d, d);
        gc.setGlobalAlpha(alpha * 0.6);
        gc.setLineWidth(baseLineWidth * 1.8);
        gc.strokeOval(cx - radius, cy - radius, d, d);
        gc.setGlobalAlpha(alpha);
        gc.setLineWidth(baseLineWidth);
        gc.strokeOval(cx - radius, cy - radius, d, d);
    }

    /**
     * Renderizza l'effetto VFX avanzato per un buco nero in collasso gravitazionale.
     * Combina passaggi di alone oscuro, onde poligonali increspate da formule di rumore dinamico (Noise),
     * anelli orbitali rotanti perturbati, particelle centripete e un nucleo centrale in modalità {@link BlendMode#SCREEN}.
     */
    public static void drawBlackHoleRaw(GraphicsContext gc, double cx, double cy, Shockwave shockwave) {
        double radius = shockwave.getRadius();
        double alpha = shockwave.getAlpha();
        double time = System.currentTimeMillis() * 0.002;
        double lineWidth = shockwave.getLineWidth();

        gc.setGlobalBlendMode(BlendMode.SCREEN);

        // 1. Alone Scuro Sfondo
        gc.setGlobalAlpha(alpha * 0.45);
        gc.setFill(Color.rgb(20, 15, 30));
        gc.fillOval(cx - radius * 1.15, cy - radius * 1.15, radius * 2.3, radius * 2.3);

        // 2. Onde Poligonali Esterne in Collasso (6 iterazioni con rumore armonico)
        gc.setStroke(Color.rgb(170, 110, 255));
        gc.setLineWidth(lineWidth * 1.1);
        for (int w = 0; w < 6; w++) {
            double waveProgress = (time * 0.8 + w * 0.18) % 1.0;
            double waveRadius = radius * (1.0 - waveProgress);
            double waveAlpha = alpha * (1.0 - waveProgress) * 0.55;
            gc.setGlobalAlpha(waveAlpha);
            for (int i = 0; i < MAX_SEGMENTS; i++) {
                double angle = (Math.PI * 2 * i) / MAX_SEGMENTS;
                double noise = Math.sin(angle * 10 + time * 3.5) * 6.0 + Math.cos(angle * 6 - time * 2.8) * 4.0;
                double r = waveRadius + noise;
                X_BUFFER[i] = cx + COS_TABLE[i] * r;
                Y_BUFFER[i] = cy + SIN_TABLE[i] * r;
            }
            gc.strokePolygon(X_BUFFER, Y_BUFFER, MAX_SEGMENTS);
        }

        // 3. Anello Principale Esterno Perturbato e Ruotato
        gc.setStroke(Color.rgb(160, 100, 255));
        gc.setLineWidth(lineWidth * 1.6);
        gc.setGlobalAlpha(alpha * 0.95);
        double t = time * 0.35;
        for (int i = 0; i < MAX_SEGMENTS; i++) {
            double angle = (Math.PI * 2 * i) / MAX_SEGMENTS;
            double wave = Math.sin(angle * 8 + time * 6.0) * radius * 0.04 + Math.cos(angle * 5 - time * 4.5) * radius * 0.02;
            double r = radius + wave;
            double finalAngle = angle + t;
            X_BUFFER[i] = cx + Math.cos(finalAngle) * r;
            Y_BUFFER[i] = cy + Math.sin(finalAngle) * r;
        }
        gc.strokePolygon(X_BUFFER, Y_BUFFER, MAX_SEGMENTS);

        // 4. Anello Interno Pulsante
        double innerRadius = radius * (0.72 + Math.sin(time * 3) * 0.02);
        gc.setStroke(Color.rgb(180, 110, 255));
        gc.setLineWidth(lineWidth * 1.2);
        gc.setGlobalAlpha(alpha * 0.75);
        gc.strokeOval(cx - innerRadius, cy - innerRadius, innerRadius * 2, innerRadius * 2);

        // 5. Particelle ad Attrazione Centripeta
        int particleCount = 28;
        double step = (Math.PI * 2) / particleCount;
        for (int i = 0; i < particleCount; i++) {
            double angle = step * i + time + Math.sin(time + i) * 0.5;
            double movement = (Math.sin(time * 2 + i * 1.7) + 1) * 0.5;
            double distance = radius * (0.95 - movement * 0.85);
            double px = cx + Math.cos(angle) * distance;
            double py = cy + Math.sin(angle) * distance;
            double size = 3.5 + Math.sin(time * 4 + i) * 1.5;

            gc.setGlobalAlpha(alpha);
            gc.setFill(Color.WHITE);
            gc.fillOval(px - size * 0.5, py - size * 0.5, size, size);
            gc.setGlobalAlpha(alpha * 0.22);
            gc.setFill(Color.rgb(220, 180, 255));
            gc.fillOval(px - size, py - size, size * 2, size * 2);
        }

        // 6. Nucleo di Singolarità e Centro ad Alto Bagliore
        double coreRadius = radius * (0.16 + Math.sin(time * 5) * 0.01);
        gc.setGlobalAlpha(alpha * 0.95);
        gc.setFill(Color.rgb(45, 20, 70));
        gc.fillOval(cx - coreRadius, cy - coreRadius, coreRadius * 2, coreRadius * 2);
        double centerGlow = coreRadius * 0.45;
        gc.setGlobalAlpha(alpha * 0.35);
        gc.setFill(Color.WHITE);
        gc.fillOval(cx - centerGlow, cy - centerGlow, centerGlow * 2, centerGlow * 2);

        gc.setGlobalBlendMode(BlendMode.SRC_OVER);
    }
}