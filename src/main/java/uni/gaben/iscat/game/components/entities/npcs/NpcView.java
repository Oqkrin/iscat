package uni.gaben.iscat.game.components.entities.npcs;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import uni.gaben.iscat.game.utils.interfaces.EntityRenderer;
import uni.gaben.iscat.game.utils.settings.VisualSettings;

/**
 * Renderer di fallback per nemici senza sprite dedicato.
 * Disegna un cerchio rosso.
 */
public class NpcView implements EntityRenderer<NpcModel> {

    private static final double SIZE = VisualSettings.DIMENSIONE_TILE;

    @Override
    public void draw(GraphicsContext gc, NpcModel enemy) {
        gc.setFill(Color.RED);
        gc.fillOval(enemy.getX(), enemy.getY(), SIZE, SIZE);
    }
}
