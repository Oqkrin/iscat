package uni.gaben.iscat.universe.rendering;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.Effect;
import javafx.scene.effect.Glow;
import javafx.scene.paint.Color;
import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.effects.EnduranceIndicator;
import uni.gaben.iscat.universe.effects.Shockwave;
import uni.gaben.iscat.universe.effects.Thrust;
import uni.gaben.iscat.universe.entities.hardcoded.player.PlayerSettings;
import uni.gaben.iscat.utils.design.ScalareAureo;
import uni.gaben.iscat.utils.theme.ThemeManager;

import java.util.Random;

public final class DrawVFX {

    private static final Random RANDOM = new Random();
    private static final Effect thrustEffect = new Glow();
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

    private DrawVFX() {}

    // Raw method called from OptimizedLayeredRenderer – no additional save/restore
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

    public static void drawThrustRaw(GraphicsContext gc, double cx, double cy, double angle, Thrust thrust) {
        if (thrust == null || !thrust.isActive() || thrust.getIntensity() < 0.01) return;

        double intensity = Math.min(thrust.getIntensity(), 1.0);
        double w = thrust.getShipWidth();
        double h = thrust.getShipHeight();
        Vector2 drift = thrust.getLocalDrift();

        int particleCount = (int) (PlayerSettings.THRUST_MIN_PARTICLES
                + intensity * PlayerSettings.THRUST_EXTRA_PARTICLES);
        double maxThrustHeight = ScalareAureo.phiMaggiore(h) * 1.2;
        Color accent = ThemeManager.getInstance().getAccentPrimary();

        gc.save();
        gc.translate(cx, cy);
        gc.rotate(angle);
        gc.setGlobalBlendMode(BlendMode.ADD);
        gc.setEffect(thrustEffect);

        for (int i = 0; i < particleCount; i++) {
            double distRatio = RANDOM.nextDouble();
            double spreadX = w * (0.15 + Math.pow(distRatio, 1.5) * PlayerSettings.THRUST_SPREAD_X_FACTOR);
            double whipX = 0;
            if (distRatio > 0.15) {
                double curveRatio = (distRatio - 0.15) / 0.85;
                whipX = drift.x * Math.pow(curveRatio, 5) * (w * 2);
            }
            double offsetX = (RANDOM.nextGaussian() * 0.22) * spreadX + whipX;
            double offsetY = (h / 2) + (distRatio * maxThrustHeight) + drift.y * distRatio * (h * 0.1);
            offsetY = Math.max(offsetY, h / 2);
            double size = (PlayerSettings.THRUST_MIN_PARTICLE_SIZE
                    + RANDOM.nextDouble() * PlayerSettings.THRUST_PARTICLE_SIZE_VARIATION)
                    * (1.2 - distRatio * 0.9) * (0.7 + intensity * 0.5);

            gc.setFill(getParticleColor(distRatio, intensity, RANDOM.nextDouble(), accent));
            gc.fillOval(offsetX - size / 2, offsetY - size / 2, size, size);
        }

        gc.setGlobalBlendMode(BlendMode.SRC_OVER);
        gc.restore();
    }

    private static Color getParticleColor(double distanceRatio, double intensity, double colorMix, Color accent) {
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

    public static void drawEnduranceIndicator(EnduranceIndicator enduranceIndicator, GraphicsContext gc) {
        Color color = (enduranceIndicator.value < 0) ? ThemeManager.getInstance().getColorError()
                : ThemeManager.getInstance().getColorSuccess();
        gc.setFill(color);
        gc.setGlobalAlpha(enduranceIndicator.alpha);
        String text = String.format("%+.0f", enduranceIndicator.value);
        double textWidth = gc.getFont().getSize() * text.length() * 0.6;
        gc.fillText(text, enduranceIndicator.x - textWidth / 2.0, enduranceIndicator.y);
        gc.setGlobalAlpha(1.0);
    }

    static void drawProjectile(GraphicsContext gc, OptimizedLayeredRenderer.ProjectileBatch p) {
        gc.setStroke(p.color());
        gc.setLineWidth(p.trailWidth());
        gc.setGlobalAlpha(0.5);
        gc.strokeLine(p.trailX1(), p.trailY1(), p.trailX2(), p.trailY2());
        gc.setGlobalAlpha(1.0);
        gc.setFill(p.color());
        gc.fillOval(p.cx() - p.w() /2, p.cy() - p.h() /2, p.w(), p.h());
    }

    static void drawHpBar(GraphicsContext gc, OptimizedLayeredRenderer.HpBarBatch h) {
        gc.setFill(ThemeManager.getInstance().getColorError());
        gc.fillRect(h.x(), h.y(), h.w(), h.h());
        gc.setFill(ThemeManager.getInstance().getColorSuccess());
        gc.fillRect(h.x(), h.y(), h.w() * h.percent(), h.h());
    }
}