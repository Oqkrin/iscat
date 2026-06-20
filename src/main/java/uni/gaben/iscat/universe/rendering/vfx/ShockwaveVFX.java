package uni.gaben.iscat.universe.rendering.vfx;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.paint.Color;
import uni.gaben.iscat.universe.effects.Shockwave;

/**
 * Utility class responsabile del rendering delle onde d'urto (shockwave) e delle
 * distorsioni gravitazionali dei buchi neri (black hole VFX) all'interno del mondo di gioco.
 * <p>
 * Per garantire le massime prestazioni ed evitare l'allocazione ripetuta di memoria (Garbage Collection)
 * all'interno del ciclo di rendering principale, la classe pre-calcola tabelle trigonometriche (seni e coseni)
 * e riutilizza array di buffer statici per la generazione geometrica dei poligoni perturbati da rumore.
 * </p>
 */
public final class ShockwaveVFX {

    /** Numero massimo di segmenti utilizzati per approssimare la geometria circolare delle onde e delle distorsioni. */
    private static final int MAX_SEGMENTS = 64;

    /** Tabella di pre-calcolo per i valori di coseno. */
    private static final double[] COS_TABLE = new double[MAX_SEGMENTS];

    /** Tabella di pre-calcolo per i valori di seno. */
    private static final double[] SIN_TABLE = new double[MAX_SEGMENTS];

    /** Buffer statico riutilizzabile per memorizzare le coordinate X dei vertici dei poligoni da disegnare. */
    private static final double[] X_BUFFER = new double[MAX_SEGMENTS];

    /** Buffer statico riutilizzabile per memorizzare le coordinate Y dei vertici dei poligoni da disegnare. */
    private static final double[] Y_BUFFER = new double[MAX_SEGMENTS];

    static {
        for (int i = 0; i < MAX_SEGMENTS; i++) {
            double angle = (Math.PI * 2 * i) / MAX_SEGMENTS;
            COS_TABLE[i] = Math.cos(angle);
            SIN_TABLE[i] = Math.sin(angle);
        }
    }

    /**
     * Costruttore privato per prevenire l'istanziamento della classe utility.
     */
    private ShockwaveVFX() {}

    /**
     * Disegna un'onda d'urto standard ad anelli concentrici sfumati ed espandibili.
     * <p>
     * L'effetto visivo simula un'esplosione energetica ortogonale semitrasparente attraverso passaggi multipli
     * di riempimento e tracciatura di ovali concentrici a spessore decrescente e opacità controllata.
     * </p>
     *
     * @param gc        il {@link GraphicsContext} del canvas su cui effettuare il disegno
     * @param cx        la coordinata X del centro dell'onda d'urto in pixel (screen space)
     * @param cy        la coordinata Y del centro dell'onda d'urto in pixel (screen space)
     * @param shockwave l'oggetto modello contenente lo stato dinamico dell'onda (raggio attuale, opacità, spessore linea)
     */
    public static void drawShockwaveRaw(GraphicsContext gc, double cx, double cy, Shockwave shockwave) {
        double radius = shockwave.getRadius();
        double alpha = shockwave.getAlpha();
        double d = radius * 2;
        double baseLineWidth = shockwave.getLineWidth();

        gc.setFill(Color.WHITE);
        gc.setStroke(Color.WHITE);

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
     * Disegna un effetto VFX avanzato per un buco nero in collasso gravitazionale.
     * <p>
     * L'effetto combina un'aura oscura centrale, onde poligonali esterne increspate da una funzione di rumore sinusoidale
     * dipendente dal tempo, un anello principale perturbato rotante, particelle orbitali ad attrazione centripeta
     * e un nucleo di singolarità compatto e luminoso in modalità di miscelazione cromatica additiva {@link BlendMode#SCREEN}.
     * </p>
     *
     * @param gc        il {@link GraphicsContext} del canvas su cui effettuare il disegno
     * @param cx        la coordinata X del centro della singolarità in pixel (screen space)
     * @param cy        la coordinata Y del centro della singolarità in pixel (screen space)
     * @param shockwave l'oggetto modello da cui si ricavano i parametri fisici di base (raggio dell'orizzonte degli eventi, opacità generale)
     */
    public static void drawBlackHoleRaw(GraphicsContext gc, double cx, double cy, Shockwave shockwave) {
        double radius = shockwave.getRadius();
        double alpha = shockwave.getAlpha();
        double time = System.currentTimeMillis() * 0.002;
        double lineWidth = shockwave.getLineWidth();

        gc.setGlobalBlendMode(BlendMode.SCREEN);

        // Dark aura
        gc.setGlobalAlpha(alpha * 0.45);
        gc.setFill(Color.rgb(20, 15, 30));
        gc.fillOval(cx - radius * 1.15, cy - radius * 1.15, radius * 2.3, radius * 2.3);

        // Outer collapsing waves (6 iterations)
        gc.setStroke(Color.rgb(170, 110, 255));
        gc.setLineWidth(lineWidth * 1.1);
        for (int w = 0; w < 6; w++) {
            double waveProgress = (time * 0.8 + w * 0.18) % 1.0;
            double waveRadius = radius * (1.0 - waveProgress);
            double waveAlpha = alpha * (1.0 - waveProgress) * 0.55;
            gc.setGlobalAlpha(waveAlpha);
            for (int i = 0; i < MAX_SEGMENTS; i++) {
                double angle = (Math.PI * 2 * i) / MAX_SEGMENTS;
                double noise = Math.sin(angle * 10 + time * 3.5) * 6.0
                        + Math.cos(angle * 6 - time * 2.8) * 4.0;
                double r = waveRadius + noise;
                X_BUFFER[i] = cx + COS_TABLE[i] * r;
                Y_BUFFER[i] = cy + SIN_TABLE[i] * r;
            }
            gc.strokePolygon(X_BUFFER, Y_BUFFER, MAX_SEGMENTS);
        }

        // Main outer ring
        gc.setStroke(Color.rgb(160, 100, 255));
        gc.setLineWidth(lineWidth * 1.6);
        gc.setGlobalAlpha(alpha * 0.95);
        double t = time * 0.35;
        for (int i = 0; i < MAX_SEGMENTS; i++) {
            double angle = (Math.PI * 2 * i) / MAX_SEGMENTS;
            double wave = Math.sin(angle * 8 + time * 6.0) * radius * 0.04
                    + Math.cos(angle * 5 - time * 4.5) * radius * 0.02;
            double r = radius + wave;
            double finalAngle = angle + t;
            X_BUFFER[i] = cx + Math.cos(finalAngle) * r;
            Y_BUFFER[i] = cy + Math.sin(finalAngle) * r;
        }
        gc.strokePolygon(X_BUFFER, Y_BUFFER, MAX_SEGMENTS);

        // Inner ring
        double innerRadius = radius * (0.72 + Math.sin(time * 3) * 0.02);
        gc.setStroke(Color.rgb(180, 110, 255));
        gc.setLineWidth(lineWidth * 1.2);
        gc.setGlobalAlpha(alpha * 0.75);
        gc.strokeOval(cx - innerRadius, cy - innerRadius, innerRadius * 2, innerRadius * 2);

        // Particles
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

        // Singularity core
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