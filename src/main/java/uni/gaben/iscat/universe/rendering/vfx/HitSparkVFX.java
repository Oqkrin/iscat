package uni.gaben.iscat.universe.rendering.vfx;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import uni.gaben.iscat.universe.effects.HitSpark;
import uni.gaben.iscat.universe.rendering.OptimizedLayeredRenderer;

public final class HitSparkVFX {

    private HitSparkVFX() {}

    public static void renderHitSpark(HitSpark model, OptimizedLayeredRenderer layers) {
        if (model == null || model.isExpired()) return;

        for (var particle : model.getParticles()) {
            if (particle instanceof HitSpark.ConfettiParticle c) {
                // Draw confetti as a filled rotated rectangle
                double w = c.getWidth();
                double h = c.getHeight() * c.getScaleY(); // scaleY flips for back side
                double cx = c.getX();
                double cy = c.getY();
                double angle = c.getRotation();
                Color color = c.getColor();
                // Use batched rect
                layers.addFilledRotatedRect(cx - w/2, cy - h/2, w, h, angle, color, 1.0);
            } else if (particle instanceof HitSpark.SequinParticle s) {
                // Draw sequin as a filled circle (batched oval)
                double r = s.getRadius();
                layers.addFilledOval(s.getX() - r, s.getY() - r, r*2, r*2, s.getColor(), 1.0);
            }
        }
    }

    public static void drawHitSpark(HitSpark model, GraphicsContext gc) {
        if (model == null || model.isExpired()) return;

        for (var particle : model.getParticles()) {
            if (particle instanceof HitSpark.ConfettiParticle c) {
                gc.save();
                gc.translate(c.getX(), c.getY());
                gc.rotate(c.getRotation());
                double w = c.getWidth();
                double h = c.getHeight() * c.getScaleY();
                gc.setFill(c.getColor());
                gc.fillRect(-w/2, -h/2, w, h);
                gc.restore();
            } else if (particle instanceof HitSpark.SequinParticle s) {
                double r = s.getRadius();
                gc.setFill(s.getColor());
                gc.fillOval(s.getX() - r, s.getY() - r, r*2, r*2);
            }
        }
    }
}