package uni.gaben.iscat.universe.rendering;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.*;
import javafx.scene.paint.Color;
import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.Shockwave;
import uni.gaben.iscat.universe.Thrust;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.entity.AbstractEntityModel;
import uni.gaben.iscat.universe.entity.LifeDeath;
import uni.gaben.iscat.universe.entity.player.PlayerSettings;
import uni.gaben.iscat.utils.theme.ThemeManager;
import uni.gaben.iscat.utils.design.ScalareAureo;

import java.util.Random;

/**
 * Stateless renderer for generic visual effects (thrust, explosions, …).
 * Each method expects the {@link GraphicsContext} to already be translated
 * to the entity’s position and rotated to its facing direction.
 */
public final class VFXRenderer {

    private static final Random RANDOM = new Random();
    private static final Effect thrustEffect = new Glow();
    private static final Effect shockwaveEffect = new GaussianBlur();


    private VFXRenderer() {}

    /**
     * Draws an engine thrust effect based on the data stored in a {@link Thrust}.
     *
     * @param gc     graphics context already positioned and rotated at the entity’s pivot
     * @param thrust the thrust parameters (intensity, drift, dimensions)
     */
    public static void drawThrust(GraphicsContext gc, Thrust thrust) {
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
        gc.setGlobalBlendMode(BlendMode.ADD);
        gc.setEffect(thrustEffect);

        for (int i = 0; i < particleCount; i++) {
            double distRatio = RANDOM.nextDouble();

            // Cone spread
            double spreadX = w * (0.15 + Math.pow(distRatio, 1.5) * PlayerSettings.THRUST_SPREAD_X_FACTOR);

            // Whip curve from lateral drift
            double whipX = 0;
            if (distRatio > 0.15) {
                double curveRatio = (distRatio - 0.15) / 0.85;
                whipX = drift.x * Math.pow(curveRatio, 5) * (w * 2);
            }

            double offsetX = (RANDOM.nextGaussian() * 0.22) * spreadX + whipX;
            double offsetY = (h / 2) + (distRatio * maxThrustHeight)
                    + drift.y * distRatio * (h * 0.1);
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

    /**
     * Chromatic transition purely driven by the theme accent colour.
     * (Originally from {@code PlayerView}.)
     */
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
            return Color.color(
                    accent.getRed(),
                    accent.getGreen(),
                    accent.getBlue(),
                    alpha * 0.85
            );
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

    public static void drawDebugCollision(AbstractEntityModel e, GraphicsContext gc) {
        double cx = UU.mToPx(e.getTransform().getTranslationX());
        double cy = UU.mToPx(e.getTransform().getTranslationY());
        double w  = e.getWidthPx();
        double h  = e.getHeightPx();
        gc.save();
        gc.translate(cx, cy);
        gc.rotate(Math.toDegrees(e.getTransform().getRotationAngle() + RenderingSettings.BASE_ROTRAD_OFFSET));
        gc.setStroke(Color.LIME);
        gc.setLineWidth(1.5);
        if (e.getFixtureCount() > 0 && e.getFixture(0).getShape() instanceof org.dyn4j.geometry.Circle) {
            double r = w / 2;
            gc.strokeOval(-r, -r, w, h);
        } else {
            gc.strokeRect(-w / 2, -h / 2, w, h);
        }
        gc.setStroke(Color.RED);
        gc.strokeLine(0, 0, w / 2, 0);
        gc.restore();
    }

    public static void drawHpBar(LifeDeath entity, GraphicsContext gc, double w, double h) {
        double barX = -w / 2;
        double barY = -h / 2 - PlayerSettings.HP_BAR_OFFSET_Y;

        gc.setFill(ThemeManager.getInstance().getColorError());
        gc.fillRect(barX, barY, w, PlayerSettings.HP_BAR_HEIGHT);

        gc.setFill(ThemeManager.getInstance().getColorSuccess());
        double percent = entity.getLife() / entity.getMaxLife();
        gc.fillRect(barX, barY, w * percent, PlayerSettings.HP_BAR_HEIGHT);
    }

    // =========================================================================
    // 1. PRE-ALLOCATED COLOR CONSTANTS (Opaque bases to avoid runtime object creation)
    // =========================================================================
    private static final Color COLOR_WHITE          = Color.WHITE;
    private static final Color COLOR_AURA_DARK      = Color.rgb(20, 15, 30);
    private static final Color COLOR_SHOCK_PURPLE   = Color.rgb(170, 110, 255);
    private static final Color COLOR_OUTER_PURPLE   = Color.rgb(160, 100, 255);
    private static final Color COLOR_INNER_PURPLE   = Color.rgb(180, 110, 255);
    private static final Color COLOR_PARTICLE_GLOW  = Color.rgb(220, 180, 255);
    private static final Color COLOR_CORE_DARK      = Color.rgb(45, 20, 70);

    // =========================================================================
    // 2. REUSABLE PERFORMANCE BUFFERS & TRIG LOOKUP TABLES (LUT)
    // =========================================================================
    private static final int MAX_SEGMENTS = 64;
    private static final double[] X_BUFFER = new double[MAX_SEGMENTS];
    private static final double[] Y_BUFFER = new double[MAX_SEGMENTS];

    private static final double[] COS_TABLE = new double[MAX_SEGMENTS];
    private static final double[] SIN_TABLE = new double[MAX_SEGMENTS];
    static {
        // Pre-calculate unit circle coordinates to bypass Math.cos/sin inside loops
        for (int i = 0; i < MAX_SEGMENTS; i++) {
            double angle = (Math.PI * 2 * i) / MAX_SEGMENTS;
            COS_TABLE[i] = Math.cos(angle);
            SIN_TABLE[i] = Math.sin(angle);
        }
    }

    // =========================================================================
    // OPTIMIZED SHOCKWAVE (Zero allocations)
    // =========================================================================
    public static void drawShockwave(GraphicsContext gc, Shockwave shockwave) {
        gc.save();
        gc.setEffect(shockwaveEffect);
        double radius = shockwave.getRadius();
        double alpha  = shockwave.getAlpha();
        double d = radius * 2;
        double baseLineWidth = shockwave.getLineWidth();

        // Capture parent layer alpha to compound cleanly
        double parentAlpha = gc.getGlobalAlpha();

        // Re-use pre-allocated white reference for both paint targets
        gc.setFill(COLOR_WHITE);
        gc.setStroke(COLOR_WHITE);

        // Pass 1: Core Fill
        gc.setGlobalAlpha(parentAlpha * alpha * 0.15);
        gc.fillOval(-radius, -radius, d, d);

        // Pass 2: Soft Outer Ring
        gc.setGlobalAlpha(parentAlpha * alpha * 0.3);
        gc.setLineWidth(baseLineWidth * 3.5);
        gc.strokeOval(-radius, -radius, d, d);

        // Pass 3: Mid Highlight Ring
        gc.setGlobalAlpha(parentAlpha * alpha * 0.6);
        gc.setLineWidth(baseLineWidth * 1.8);
        gc.strokeOval(-radius, -radius, d, d);

        // Pass 4: Sharp Vector Core Edge
        gc.setGlobalAlpha(parentAlpha * alpha);
        gc.setLineWidth(baseLineWidth);
        gc.strokeOval(-radius, -radius, d, d);

        // Restore baseline global alpha state
        gc.setGlobalAlpha(parentAlpha);
        gc.restore();
    }

    // =========================================================================
    // OPTIMIZED BLACK HOLE (No array allocations, fully unrolled math)
    // =========================================================================
    public static void drawBlackHole(GraphicsContext gc, Shockwave shockwave) {
        double radius = shockwave.getRadius();
        double alpha = shockwave.getAlpha();
        double time = System.currentTimeMillis() * 0.002;
        double lineWidth = shockwave.getLineWidth();

        gc.save();
        gc.setGlobalBlendMode(BlendMode.SCREEN);
        double parentAlpha = gc.getGlobalAlpha();

        // ---------------------------------------------------------------------
        // DARK AURA
        // ---------------------------------------------------------------------
        gc.setGlobalAlpha(parentAlpha * alpha * 0.45);
        gc.setFill(COLOR_AURA_DARK);
        gc.fillOval(-radius * 1.15, -radius * 1.15, radius * 2.3, radius * 2.3);

        double t = time * 4.2;

        // ---------------------------------------------------------------------
        // OUTER COLLAPSING WAVES (OUT → IN)
        // ---------------------------------------------------------------------
        int waveCount = 6;
        gc.setStroke(COLOR_SHOCK_PURPLE);
        gc.setLineWidth(lineWidth * 1.1);

        for (int w = 0; w < waveCount; w++) {
            double waveProgress = (time * 0.8 + w * 0.18) % 1.0;
            double waveRadius = radius * (1.0 - waveProgress);
            double waveAlpha = alpha * (1.0 - waveProgress) * 0.55;

            gc.setGlobalAlpha(parentAlpha * waveAlpha);

            for (int i = 0; i < MAX_SEGMENTS; i++) {
                // Keep structural angular lookups but project points using the precalculated circle table
                double angle = (Math.PI * 2 * i) / MAX_SEGMENTS;
                double noise = Math.sin(angle * 10 + t * 3.5) * 6.0
                        + Math.cos(angle * 6 - t * 2.8) * 4.0;

                double r = waveRadius + noise;
                X_BUFFER[i] = COS_TABLE[i] * r;
                Y_BUFFER[i] = SIN_TABLE[i] * r;
            }
            gc.strokePolygon(X_BUFFER, Y_BUFFER, MAX_SEGMENTS);
        }

        // ---------------------------------------------------------------------
        // MAIN OUTER WAVY RING (THINNER)
        // ---------------------------------------------------------------------
        gc.setStroke(COLOR_OUTER_PURPLE);
        gc.setLineWidth(lineWidth * 1.6);
        gc.setGlobalAlpha(parentAlpha * alpha * 0.95);

        double rotationOffset = t * 0.35;
        for (int i = 0; i < MAX_SEGMENTS; i++) {
            double angle = (Math.PI * 2 * i) / MAX_SEGMENTS;
            double wave = Math.sin(angle * 8 + t * 6.0) * radius * 0.04
                    + Math.cos(angle * 5 - t * 4.5) * radius * 0.02;

            double r = radius + wave;
            double finalAngle = angle + rotationOffset;

            X_BUFFER[i] = Math.cos(finalAngle) * r;
            Y_BUFFER[i] = Math.sin(finalAngle) * r;
        }
        gc.strokePolygon(X_BUFFER, Y_BUFFER, MAX_SEGMENTS);

        // ---------------------------------------------------------------------
        // INNER RING
        // ---------------------------------------------------------------------
        double innerRadius = radius * (0.72 + Math.sin(time * 3) * 0.02);
        gc.setStroke(COLOR_INNER_PURPLE);
        gc.setLineWidth(lineWidth * 1.2);
        gc.setGlobalAlpha(parentAlpha * alpha * 0.75);
        gc.strokeOval(-innerRadius, -innerRadius, innerRadius * 2, innerRadius * 2);

        // ---------------------------------------------------------------------
        // WHITE PARTICLES
        // ---------------------------------------------------------------------
        int particleCount = 28;
        double inverseParticleCount = (Math.PI * 2) / particleCount;

        for (int i = 0; i < particleCount; i++) {
            double angle = (inverseParticleCount * i) + time + Math.sin(time + i) * 0.5;
            double movement = (Math.sin(time * 2 + i * 1.7) + 1) * 0.5;
            double distance = radius * (0.95 - movement * 0.85);

            double px = Math.cos(angle) * distance;
            double py = Math.sin(angle) * distance;
            double size = 3.5 + Math.sin(time * 4 + i) * 1.5;

            // Base Core Point
            gc.setGlobalAlpha(parentAlpha * alpha);
            gc.setFill(COLOR_WHITE);
            gc.fillOval(px - size * 0.5, py - size * 0.5, size, size);

            // Halo Core Pass
            gc.setGlobalAlpha(parentAlpha * alpha * 0.22);
            gc.setFill(COLOR_PARTICLE_GLOW);
            gc.fillOval(px - size, py - size, size * 2, size * 2);
        }

        // ---------------------------------------------------------------------
        // SINGULARITY CORE
        // ---------------------------------------------------------------------
        double coreRadius = radius * (0.16 + Math.sin(time * 5) * 0.01);
        gc.setGlobalAlpha(parentAlpha * alpha * 0.95);
        gc.setFill(COLOR_CORE_DARK);
        gc.fillOval(-coreRadius, -coreRadius, coreRadius * 2, coreRadius * 2);

        double centerGlow = coreRadius * 0.45;
        gc.setGlobalAlpha(parentAlpha * alpha * 0.35);
        gc.setFill(COLOR_WHITE);
        gc.fillOval(-centerGlow, -centerGlow, centerGlow * 2, centerGlow * 2);

        gc.setGlobalBlendMode(BlendMode.SRC_OVER);
        gc.restore();
    }
}