package uni.gaben.iscat.universe.rendering;

import javafx.scene.canvas.GraphicsContext;
import uni.gaben.iscat.universe.entity.GameEntity;
public interface Renderable<T extends GameEntity> {
    void render(T entity, GraphicsContext gc);
}
