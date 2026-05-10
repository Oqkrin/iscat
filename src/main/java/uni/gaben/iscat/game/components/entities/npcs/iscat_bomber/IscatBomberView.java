package uni.gaben.iscat.game.components.entities.npcs.iscat_bomber;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import uni.gaben.iscat.game.utils.interfaces.Drawable;
import uni.gaben.iscat.game.utils.settings.VisualSettings;

import java.util.Objects;

/**
 * Disegna l'IscatBomberModel: sprite ruotato verso la direzione corrente.
 */
public class IscatBomberView implements Drawable<IscatBomberModel> {

    private static final double TILE_SIZE    = VisualSettings.DIMENSIONE_TILE;
    private static final double NORTH_OFFSET = VisualSettings.OFFSET_NORD_SPRITE;

    private final Image sprite = new Image(
            Objects.requireNonNull(
                    IscatBomberView.class.getResourceAsStream(
                            "/uni/gaben/iscat/sprites/IscatBomber.png")));

    @Override
    public void draw(GraphicsContext gc, IscatBomberModel bomber) {
        double cx = bomber.getX() + TILE_SIZE / 2.0;
        double cy = bomber.getY() + TILE_SIZE / 2.0;

        gc.save();
        gc.translate(cx, cy);
        gc.rotate(bomber.getDirectionAngle() + NORTH_OFFSET);
        gc.drawImage(sprite, -TILE_SIZE / 2.0, -TILE_SIZE / 2.0, TILE_SIZE, TILE_SIZE);
        gc.restore();
    }
}
