package uni.gaben.iscat.universe.lib.interfaces.view;

import javafx.scene.canvas.GraphicsContext;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
public interface Drawable<T extends AbstractEntityModel> {
    void draw(T entity, GraphicsContext gc);
}