package uni.gaben.iscat.universe.rendering;

import javafx.scene.canvas.GraphicsContext;
import uni.gaben.iscat.universe.entities.AbstractPhysicalEntityModel;

/**
 * Contratto di rendering accoppiato per entità fisiche generiche (Render Strategy).
 *
 * @param <T> Il tipo specifico di entità che estende il modello fisico astratto.
 */
public interface Renderable<T extends AbstractPhysicalEntityModel> {

    /**
     * Esegue il disegno dell'entità sul contesto grafico specificato.
     *
     * @param entity L'istanza del modello dell'entità da renderizzare.
     * @param gc     Il contesto grafico (GraphicsContext) del canvas di destinazione.
     */
    void render(T entity, GraphicsContext gc);
}