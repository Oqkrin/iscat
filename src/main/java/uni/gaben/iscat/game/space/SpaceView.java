package uni.gaben.iscat.game.space;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * Disegna il campo stellato di sfondo.
 */
public class SpaceView {

    public void draw(GraphicsContext gc, SpaceModel space) {
        gc.setFill(Color.WHITE);
        for (StarModel star : space.stars) {
            double size = star.getSize();
            gc.fillRect(star.getX(), star.getY(), size, size);
        }
    }
}
