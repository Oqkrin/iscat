package uni.gaben.iscat.universe.interfaces;

import javafx.scene.canvas.GraphicsContext;
import uni.gaben.iscat.universe.entity.AbstractEntityModel;
public interface Drawable<T extends AbstractEntityModel> {
    void draw(T entity, GraphicsContext gc);
}