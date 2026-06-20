// HitSparkVFX.java
package uni.gaben.iscat.universe.rendering.vfx;

import javafx.scene.paint.Color;
import uni.gaben.iscat.universe.effects.HitSpark;
import uni.gaben.iscat.universe.rendering.OptimizedLayeredRenderer;

public final class HitSparkVFX {

    private HitSparkVFX() {}

    public static void renderHitSpark(HitSpark model, OptimizedLayeredRenderer layers) {
        if (model == null || model.isExpired()) return;

        for (var particle : model.getParticles()) {
            if (particle instanceof HitSpark.ConfettiParticle c) {
                // Rotated rectangle with fade
                double w = c.getWidth();
                double h = c.getHeight();
                double cx = c.getX();
                double cy = c.getY();
                double angle = c.getRotation();
                Color color = c.getColor().deriveColor(1, 1, 1, c.getAlpha());
                layers.addFilledRotatedRect(cx - w/2, cy - h/2, w, h, angle, color, 1.0);

            } else if (particle instanceof HitSpark.SequinParticle s) {
                // Circle with trail
                double r = s.getSize();
                double alpha = s.getAlpha();
                Color color = s.getColor().deriveColor(1, 1, 1, alpha);
                layers.addFilledOval(s.getX() - r, s.getY() - r, r*2, r*2, color, 1.0);

                // Draw faint trail (line from previous positions)
                if (s.hasTrail()) {
                    double[] tx = s.getTrailX();
                    double[] ty = s.getTrailY();
                    // Draw two trail segments with decreasing alpha
                    for (int i = 0; i < 2; i++) {
                        int idx = (s.trailIdx - i - 1 + 3) % 3;
                        int next = (idx + 1) % 3;
                        double alphaTrail = alpha * (0.2 - i * 0.08);
                        if (alphaTrail > 0.01) {
                            Color trailColor = color.deriveColor(1, 1, 1, alphaTrail);
                            layers.addLine(tx[idx], ty[idx], tx[next], ty[next],
                                    0.5, trailColor, 1.0);
                        }
                    }
                }
            }
        }
    }
}