package uni.gaben.iscat.gamenex.lib.interfaces.view;

import javafx.scene.canvas.GraphicsContext;
import uni.gaben.iscat.gamenex.lib.abstracts.AbstractEntityModel;
public interface Drawable<T extends AbstractEntityModel> {
    void draw(T entity, GraphicsContext gc);
}