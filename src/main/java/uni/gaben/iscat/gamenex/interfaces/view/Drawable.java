package uni.gaben.iscat.gamenex.interfaces.view;

import javafx.scene.canvas.GraphicsContext;
import uni.gaben.iscat.gamenex.interfaces.model.AbstractEntityModel;

public interface Drawable<T extends AbstractEntityModel> {
    void render(T entity, GraphicsContext gc);
}
