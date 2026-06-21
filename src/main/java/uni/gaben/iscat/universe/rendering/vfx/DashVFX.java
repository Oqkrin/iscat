package uni.gaben.iscat.universe.rendering.vfx;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.Glow;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.StrokeLineCap;
import uni.gaben.iscat.utils.theme.ThemeManager;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Gestore grafico per l'effetto visivo del dash (scatto rapido).
 * <p>
 * Disegna due componenti sovrapposti:
 * <ol>
 *   <li>Un bagliore ad impulso nell'istante del dash (basato sulla velocità corrente).</li>
 *   <li>Una scia persistente basata sulla storia posizionale del {@link DashTrail},
 *       che decade gradualmente nel tempo anche dopo la fine del dash.</li>
 * </ol>
 */
public final class DashVFX {

    private static final Glow GLOW = new Glow(0.6);
    private static final double MAX_LENGTH = 80; // pixel
    private static final double MIN_LENGTH = 15;

    private DashVFX() {}

    /**
     * Renderizza il flash istantaneo del dash (visuale basata sulla velocità corrente).
     *
     * @param gc     Contesto grafico.
     * @param cx     Centro X dell'entità (world-pixel).
     * @param cy     Centro Y dell'entità.
     * @param vx     Velocità X in m/s (convertita via scala visiva).
     * @param vy     Velocità Y.
     * @param width  Larghezza dell'entità in pixel.
     * @param height Altezza dell'entità in pixel.
     */
    public static void drawDashRaw(GraphicsContext gc, double cx, double cy, double vx, double vy,
                                   double width, double height) {
        double speed = Math.sqrt(vx * vx + vy * vy);
        if (speed < 0.5) return;

        double dirX = vx / speed;
        double dirY = vy / speed;

        double refSpeed = 80.0;
        double intensity = Math.min(speed / refSpeed, 1.0);

        double length = MIN_LENGTH + (MAX_LENGTH - MIN_LENGTH) * intensity;
        Color accent = ThemeManager.getInstance().getAccentPrimary();
        double alpha = intensity * 0.9;

        gc.save();
        gc.translate(cx, cy);
        gc.setGlobalBlendMode(BlendMode.ADD);
        gc.setEffect(GLOW);

        double endX = -dirX * length * 2.5;
        double endY = -dirY * length * 2.5;

        Color startColor = Color.color(
                Math.min(1, accent.getRed() + 0.3 * (1 - accent.getRed())),
                Math.min(1, accent.getGreen() + 0.3 * (1 - accent.getGreen())),
                Math.min(1, accent.getBlue() + 0.3 * (1 - accent.getBlue())),
                alpha
        );
        Color endColor = Color.color(accent.getRed(), accent.getGreen(), accent.getBlue(), 0.0);

        LinearGradient gradient = new LinearGradient(
                0, 0, endX, endY,
                false, CycleMethod.NO_CYCLE,
                new Stop(0.0, startColor),
                new Stop(1.0, endColor)
        );

        double lineWidth = Math.max(width * 0.8 * intensity, 4.0);
        gc.setStroke(gradient);
        gc.setLineWidth(lineWidth);
        gc.setLineCap(StrokeLineCap.ROUND);

        double startOffsetX = -dirX * (width * 0.4);
        double startOffsetY = -dirY * (width * 0.4);
        gc.strokeLine(startOffsetX, startOffsetY, startOffsetX + endX, startOffsetY + endY);

        if (intensity > 0.1) {
            double glowSize = 8 + 16 * intensity;
            gc.setFill(Color.WHITE);
            gc.setGlobalAlpha(intensity * 0.5);
            gc.fillOval(-glowSize / 2, -glowSize / 2, glowSize, glowSize);
        }

        gc.restore();
    }

    /**
     * Renderizza la scia storica persistente del dash dal {@link DashTrail}.
     * Ogni segmento collegante due punti successivi viene disegnato con un gradiente
     * che riflette il decadimento temporale (alpha) di ciascun punto.
     *
     * @param gc    Contesto grafico.
     * @param trail La scia da renderizzare.
     */
    public static void drawDashTrail(GraphicsContext gc, DashTrail trail) {
        if (trail == null || trail.isEmpty()) return;

        Deque<DashTrail.TrailPoint> points = trail.getPoints();
        if (points.size() < 2) return;

        Color accent = ThemeManager.getInstance().getAccentPrimary();
        Color brightAccent = Color.color(
                Math.min(1, accent.getRed() + 0.3 * (1 - accent.getRed())),
                Math.min(1, accent.getGreen() + 0.3 * (1 - accent.getGreen())),
                Math.min(1, accent.getBlue() + 0.3 * (1 - accent.getBlue())),
                1.0
        );

        List<DashTrail.TrailPoint> list = new ArrayList<>(points);

        gc.save();
        gc.setGlobalBlendMode(BlendMode.ADD);
        gc.setEffect(GLOW);
        gc.setLineCap(StrokeLineCap.ROUND);

        for (int i = 1; i < list.size(); i++) {
            DashTrail.TrailPoint prev = list.get(i - 1);
            DashTrail.TrailPoint curr = list.get(i);

            double avgAlpha = (prev.alpha() + curr.alpha()) / 2.0;
            if (avgAlpha <= 0.01) continue;

            // Gradiente dal punto precedente (più vecchio, più trasparente) al corrente
            Color fromColor = Color.color(accent.getRed(), accent.getGreen(), accent.getBlue(), prev.alpha() * 0.8);
            Color toColor   = Color.color(brightAccent.getRed(), brightAccent.getGreen(), brightAccent.getBlue(), curr.alpha() * 0.9);

            LinearGradient segGradient = new LinearGradient(
                    prev.x(), prev.y(), curr.x(), curr.y(),
                    false, CycleMethod.NO_CYCLE,
                    new Stop(0.0, fromColor),
                    new Stop(1.0, toColor)
            );

            double lineWidth = Math.max(prev.width() * 0.5 * avgAlpha, 2.0);
            gc.setStroke(segGradient);
            gc.setLineWidth(lineWidth);
            gc.strokeLine(prev.x(), prev.y(), curr.x(), curr.y());
        }

        gc.restore();
    }
}