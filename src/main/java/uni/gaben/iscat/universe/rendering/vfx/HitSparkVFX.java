// HitSparkVFX.java
package uni.gaben.iscat.universe.rendering.vfx;

import javafx.scene.paint.Color;
import uni.gaben.iscat.universe.effects.HitSpark;
import uni.gaben.iscat.universe.rendering.OptimizedLayeredRenderer;

public final class HitSparkVFX {

    private HitSparkVFX() {}

    public static void renderHitSpark(HitSpark model, OptimizedLayeredRenderer layers) {
        if (model == null || model.isExpired()) return;

        // ---- Particles ----
        for (HitSpark.SparkParticle p : model.getParticles()) {
            if (p.isDead()) continue;
            double r = p.getRadius();
            double alpha = p.getAlpha();
            Color color = p.getColor().deriveColor(1, 1, 1, alpha);
            layers.addFilledOval(p.getX() - r, p.getY() - r, r * 2, r * 2, color, 1.0);
        }

        // ---- Shockwave circle ----
        HitSpark.ShockwaveCircle sw = model.getShockwave();
        if (!sw.isDead() && sw.getAlpha() > 0.01) {
            double cx = sw.getCenterX();
            double cy = sw.getCenterY();
            double radius = sw.getRadius();
            double lineWidth = sw.getLineWidth();
            double alpha = sw.getAlpha();
            Color color = Color.WHITE.deriveColor(1, 1, 1, alpha);
            // Use a stroked oval with no fill
            layers.addStrokedOval(cx - radius, cy - radius, radius * 2, radius * 2, color, lineWidth);
        }
    }
}