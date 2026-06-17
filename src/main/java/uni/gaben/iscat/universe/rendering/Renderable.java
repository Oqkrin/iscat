package uni.gaben.iscat.universe.rendering;

import javafx.scene.canvas.GraphicsContext;
import uni.gaben.iscat.universe.entities.AbstractPhysicalEntityModel;
public interface Renderable<T extends AbstractPhysicalEntityModel> {
    void render(T entity, GraphicsContext gc);
}