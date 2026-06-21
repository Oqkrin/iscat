package uni.gaben.iscat.utils;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

/**
 * Classe di utilità per il debugging grafico dell'interfaccia utente.
 * Mette a disposizione strumenti visivi per analizzare la disposizione dei componenti
 * all'interno dello Scene Graph.
 */
public final class Debug {

    /**
     * Costruttore privato per impedire l'istanziamento della classe utility.
     */
    private Debug() {
        /* classe utility */
    }

    /**
     * Evidenzia visivamente i confini di tutti i componenti grafici di tipo {@link Region}
     * all'interno dello Scene Graph, partendo da un nodo radice.
     * <p>
     * Il metodo naviga l'albero dei componenti in modo <b>ricorsivo</b>, applicando a ciascuna
     * regione trovata un bordo dorato e tratteggiato. Questo permette di identificare
     * istantaneamente le dimensioni, gli allineamenti e il posizionamento di layout complessi
     * (come VBox, HBox, AnchorPane o pulsanti).
     * </p>
     */
    public static void addDebugBorderToGraph(Parent root) {
        // Definiamo uno stroke riutilizzabile
        Border debugBorder = new Border(new BorderStroke(
                Color.GOLD,
                BorderStrokeStyle.DASHED,
                CornerRadii.EMPTY,
                new BorderWidths(0.8)
        ));

        for (Node n : root.getChildrenUnmodifiable()) {
            if (n instanceof Region r) {
                // Usiamo setBorder invece di setStyle!
                r.setBorder(debugBorder);

                // Se è un contenitore, scendiamo ricorsivamente
                addDebugBorderToGraph(r);
            }
        }
    }
}