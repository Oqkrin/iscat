package uni.gaben.iscat.game.lib.interfaces.view;

import javafx.scene.canvas.GraphicsContext;
import uni.gaben.iscat.game.lib.abstracts.AbstractEntityModel;
public interface Drawable<T extends AbstractEntityModel> {
    void draw(T entity, GraphicsContext gc);
}