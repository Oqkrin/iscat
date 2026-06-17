package uni.gaben.iscat.universe.rendering;

import javafx.scene.canvas.GraphicsContext;
import uni.gaben.iscat.universe.entities.AbstractEntityModel;
public interface Renderable<T extends AbstractEntityModel> {
    void render(T entity, GraphicsContext gc);
}