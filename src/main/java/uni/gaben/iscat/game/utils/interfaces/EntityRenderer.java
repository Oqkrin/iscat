package uni.gaben.iscat.game.utils.interfaces;

import javafx.scene.canvas.GraphicsContext;

/**
 * Renderer per un tipo specifico di entità.
 * Ogni implementazione sa come disegnare un solo tipo — nessuna logica di gioco.
 *
 * @param <T> il tipo di entità da disegnare
 */
public interface EntityRenderer<T> {

    /**
     * Disegna l'entità sul GraphicsContext fornito.
     * @param gc  il contesto grafico del canvas
     * @param entity l'entità da disegnare
     */
    void draw(GraphicsContext gc, T entity);
}
