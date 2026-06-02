package uni.gaben.iscat.universe.rendering.vfx;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.paint.Color;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.lib.interfaces.model.LifeDeath;
import uni.gaben.iscat.universe.player.PlayerSettings;
import uni.gaben.iscat.universe.rendering.RenderingSettings;
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

    private VFXRenderer() {}

    /**
     * Draws an engine thrust effect based on the data stored in a {@link ThrustModel}.
     *
     * @param gc     graphics context already positioned and rotated at the entity’s pivot
     * @param thrust the thrust parameters (intensity, drift, dimensions)
     */
    public static void drawThrust(GraphicsContext gc, ThrustModel thrust) {
        if (thrust == null || !thrust.isActive() || thrust.getIntensity() < 0.01) return;

        double intensity = Math.min(thrust.getIntensity(), 1.0);
        double w = thrust.getShipWidth();
        double h = thrust.getShipHeight();

        org.dyn4j.geometry.Vector2 drift = thrust.getLocalDrift();

        int particleCount = (int) (PlayerSettings.THRUST_MIN_PARTICLES
                + intensity * PlayerSettings.THRUST_EXTRA_PARTICLES);
        double maxThrustHeight = ScalareAureo.phiMaggiore(h) * 1.2;

        Color accent = ThemeManager.getInstance().getAccentPrimary();

        gc.save();
        gc.setGlobalBlendMode(BlendMode.ADD);

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
        double barWidth = w;
        double barX = -barWidth / 2;
        double barY = -h / 2 - PlayerSettings.HP_BAR_OFFSET_Y;

        gc.setFill(ThemeManager.getInstance().getColorError());
        gc.fillRect(barX, barY, barWidth, PlayerSettings.HP_BAR_HEIGHT);

        gc.setFill(ThemeManager.getInstance().getColorSuccess());
        double percent = entity.getLife() / entity.getMaxLife();
        gc.fillRect(barX, barY, barWidth * percent, PlayerSettings.HP_BAR_HEIGHT);
    }

    public static void drawShockwave(GraphicsContext gc,
                                     ShockwaveModel shockwave) {
        double radius = shockwave.getRadius();
        double alpha  = shockwave.getAlpha();
        double d = radius * 2;

        gc.setFill(Color.rgb(255,255,255, alpha * 0.15));
        gc.fillOval(-radius, -radius, d, d);

        gc.setStroke(Color.rgb(255,255,255, alpha * 0.3));
        gc.setLineWidth(shockwave.getLineWidth() * 3.5);
        gc.strokeOval(-radius, -radius, d, d);

        gc.setStroke(Color.rgb(255,255,255, alpha * 0.6));
        gc.setLineWidth(shockwave.getLineWidth() * 1.8);
        gc.strokeOval(-radius, -radius, d, d);

        gc.setStroke(Color.rgb(255,255,255, alpha));
        gc.setLineWidth(shockwave.getLineWidth());
        gc.strokeOval(-radius, -radius, d, d);
    }
}