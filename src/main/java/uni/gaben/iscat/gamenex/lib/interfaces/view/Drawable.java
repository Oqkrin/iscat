package uni.gaben.iscat.gamenex.lib.interfaces.view;

import javafx.scene.canvas.GraphicsContext;
import uni.gaben.iscat.gamenex.lib.abstracts.AbstractEntityModel;

/**
 * Interfaccia generica per oggetti che possono essere disegnati sul Canvas.
 * Definisce il contratto tra il modello fisico dell'entità e il sistema di rendering.
 * @param <T> Il tipo di entità fisica che questa View è in grado di disegnare.
 */
public interface Drawable<T extends AbstractEntityModel> {
    /**
     * Esegue il rendering dell'entità sul contesto grafico fornito.
     * @param entity Il modello dell'entità da disegnare.
     * @param gc Il contesto grafico (Canvas) su cui eseguire le operazioni di disegno.
     */
    void draw(T entity, GraphicsContext gc);
}
