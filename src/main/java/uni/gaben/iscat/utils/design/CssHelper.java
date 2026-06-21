package uni.gaben.iscat.utils.design;

import javafx.scene.Node;

/**
 * Helper per applicare design system via CSS classes.
 * Bridge tra design system Java e CSS.
 * 
 * <p>Questo helper fornisce metodi convenienti per applicare
 * le classi CSS definite in {@code iscat-base.css} ai nodi JavaFX.
 * 
 * <p><b>Esempio d'uso:</b>
 * <pre>{@code
 * Label title = new Label("Titolo");
 * CssHelper.headlineLarge(title);
 * CssHelper.testoPrimario(title);
 * CssHelper.ombra3(title);
 * }</pre>
 * 
 * @see ScalareAureo
 * @see TipografiaAurea
 */
public final class CssHelper {
    
    private CssHelper() {}
    
    /**
     * Applica tipografia label large (14px).
     * Per testo piccolo utilitario, bottoni, caption.
     */
    public static void labelLarge(Node node) {
        node.getStyleClass().add("label-large");
    }
    
    /**
     * Applica colore testo primario (verde neon aureo).
     */
    public static void testoPrimario(Node node) {
        node.getStyleClass().add("testo-primario");
    }
    
    /**
     * Applica colore testo secondario (blu elettrico).
     */
    public static void testoSecondario(Node node) {
        node.getStyleClass().add("testo-secondario");
    }

    /**
     * Applica stile completo pulsante menu.
     * Include dimensioni, colori, tipografia, ombre.
     */
    public static void stilePulsanteMenu(Node button) {
        button.getStyleClass().add("pulsante-menu");
    }
}
