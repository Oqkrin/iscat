package uni.gaben.iscat.iscat_game.lib.interfaces.view;

import javafx.scene.canvas.GraphicsContext;
import uni.gaben.iscat.iscat_game.lib.abstracts.AbstractEntityModel;
public interface Drawable<T extends AbstractEntityModel> {
    void draw(T entity, GraphicsContext gc);
}