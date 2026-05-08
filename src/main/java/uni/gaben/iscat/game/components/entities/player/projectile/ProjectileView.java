package uni.gaben.iscat.game.components.entities.player.projectile;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import uni.gaben.iscat.game.utils.interfaces.EntityRenderer;

/**
 * Disegna un proiettile: cerchio bianco con bagliore semitrasparente.
 */
public class ProjectileView implements EntityRenderer<ProjectileModel> {

    private static final double RADIUS = ProjectileModel.RADIUS;

    @Override
    public void draw(GraphicsContext gc, ProjectileModel p) {
        double cx = p.getX();
        double cy = p.getY();

        // Core
        gc.setFill(Color.WHITE);
        gc.fillOval(cx - RADIUS, cy - RADIUS, RADIUS * 2, RADIUS * 2);

        // Glow
        gc.setGlobalAlpha(0.4);
        gc.fillOval(cx - RADIUS * 1.5, cy - RADIUS * 1.5, RADIUS * 3, RADIUS * 3);
        gc.setGlobalAlpha(1.0);
    }
}
