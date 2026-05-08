package uni.gaben.iscat.game.enemies;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import uni.gaben.iscat.game.entities.EntityRenderer;
import uni.gaben.iscat.game.settings.VisualSettings;

/**
 * Renderer di fallback per nemici senza sprite dedicato.
 * Disegna un cerchio rosso.
 */
public class EnemyView implements EntityRenderer<EnemyModel> {

    private static final double SIZE = VisualSettings.DIMENSIONE_TILE;

    @Override
    public void draw(GraphicsContext gc, EnemyModel enemy) {
        gc.setFill(Color.RED);
        gc.fillOval(enemy.getX(), enemy.getY(), SIZE, SIZE);
    }
}
