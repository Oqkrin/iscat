package uni.gaben.iscat.game.components.entities.player;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import uni.gaben.iscat.game.utils.interfaces.EntityRenderer;
import uni.gaben.iscat.game.utils.settings.VisualSettings;

import java.util.Objects;

/**
 * Disegna il giocatore: sprite ruotato verso la direzione corrente.
 */
public class PlayerView implements EntityRenderer<PlayerModel> {

    private static final double TILE_SIZE    = VisualSettings.DIMENSIONE_TILE;
    private static final double NORTH_OFFSET = VisualSettings.OFFSET_NORD_SPRITE;

    private final Image sprite = new Image(
            Objects.requireNonNull(
                    PlayerView.class.getResourceAsStream(
                            "/uni/gaben/iscat/sprites/battle_ship_1.png")));

    @Override
    public void draw(GraphicsContext gc, PlayerModel p) {
        double cx = p.getX() + TILE_SIZE / 2.0;
        double cy = p.getY() + TILE_SIZE / 2.0;

        gc.save();
        gc.translate(cx, cy);
        gc.rotate(p.getDirectionAngle() + NORTH_OFFSET);
        gc.drawImage(sprite, -TILE_SIZE / 2.0, -TILE_SIZE / 2.0, TILE_SIZE, TILE_SIZE);
        gc.restore();
    }
}
