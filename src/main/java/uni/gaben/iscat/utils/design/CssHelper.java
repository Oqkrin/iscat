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
 * @see GeometriaAurea
 */
public final class CssHelper {
    
    private CssHelper() {}
    
    // ============================================
    // TIPOGRAFIA
    // ============================================
    
    /**
     * Applica tipografia display large (~94px).
     * Per testi hero, numerali grandi.
     */
    public static void displayLarge(Node node) {
        node.getStyleClass().add("display-large");
    }
    
    /**
     * Applica tipografia display medium (~58px).
     */
    public static void displayMedium(Node node) {
        node.getStyleClass().add("display-medium");
    }
    
    /**
     * Applica tipografia display small (~36px).
     */
    public static void displaySmall(Node node) {
        node.getStyleClass().add("display-small");
    }
    
    /**
     * Applica tipografia headline large (~36px).
     * Per testo breve ad alta enfasi.
     */
    public static void headlineLarge(Node node) {
        node.getStyleClass().add("headline-large");
    }
    
    /**
     * Applica tipografia headline medium (~23px).
     */
    public static void headlineMedium(Node node) {
        node.getStyleClass().add("headline-medium");
    }
    
    /**
     * Applica tipografia headline small (~23px).
     */
    public static void headlineSmall(Node node) {
        node.getStyleClass().add("headline-small");
    }
    
    /**
     * Applica tipografia title large (~23px).
     * Per enfasi media, testo relativamente breve.
     */
    public static void titleLarge(Node node) {
        node.getStyleClass().add("title-large");
    }
    
    /**
     * Applica tipografia title medium (14px).
     */
    public static void titleMedium(Node node) {
        node.getStyleClass().add("title-medium");
    }
    
    /**
     * Applica tipografia title small (~9px).
     */
    public static void titleSmall(Node node) {
        node.getStyleClass().add("title-small");
    }
    
    /**
     * Applica tipografia body large (~23px).
     * Per testo lungo, leggibile.
     */
    public static void bodyLarge(Node node) {
        node.getStyleClass().add("body-large");
    }
    
    /**
     * Applica tipografia body medium (14px).
     * Body standard.
     */
    public static void bodyMedium(Node node) {
        node.getStyleClass().add("body-medium");
    }
    
    /**
     * Applica tipografia body small (~9px).
     */
    public static void bodySmall(Node node) {
        node.getStyleClass().add("body-small");
    }
    
    /**
     * Applica tipografia label large (14px).
     * Per testo piccolo utilitario, bottoni, caption.
     */
    public static void labelLarge(Node node) {
        node.getStyleClass().add("label-large");
    }
    
    /**
     * Applica tipografia label medium (~9px).
     */
    public static void labelMedium(Node node) {
        node.getStyleClass().add("label-medium");
    }
    
    /**
     * Applica tipografia label small (~5px).
     */
    public static void labelSmall(Node node) {
        node.getStyleClass().add("label-small");
    }
    
    // ============================================
    // COLORI TESTO
    // ============================================
    
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
     * Applica colore testo accento (magenta).
     */
    public static void testoAccento(Node node) {
        node.getStyleClass().add("testo-accento");
    }
    
    /**
     * Applica colore testo successo (verde).
     */
    public static void testoSuccesso(Node node) {
        node.getStyleClass().add("testo-successo");
    }
    
    /**
     * Applica colore testo pericolo (rosso).
     */
    public static void testoPericolo(Node node) {
        node.getStyleClass().add("testo-pericolo");
    }
    
    /**
     * Applica colore testo avviso (oro).
     */
    public static void testoAvviso(Node node) {
        node.getStyleClass().add("testo-avviso");
    }
    
    /**
     * Applica colore testo info (azzurro).
     */
    public static void testoInfo(Node node) {
        node.getStyleClass().add("testo-info");
    }
    
    /**
     * Applica colore testo dim (grigio medio).
     */
    public static void testoDim(Node node) {
        node.getStyleClass().add("testo-dim");
    }
    
    // ============================================
    // COLORI SFONDO
    // ============================================
    
    /**
     * Applica sfondo primario.
     */
    public static void sfondoPrimario(Node node) {
        node.getStyleClass().add("sfondo-primario");
    }
    
    /**
     * Applica sfondo secondario.
     */
    public static void sfondoSecondario(Node node) {
        node.getStyleClass().add("sfondo-secondario");
    }
    
    /**
     * Applica sfondo scuro.
     */
    public static void sfondoScuro(Node node) {
        node.getStyleClass().add("sfondo-scuro");
    }
    
    /**
     * Applica sfondo chiaro.
     */
    public static void sfondoChiaro(Node node) {
        node.getStyleClass().add("sfondo-chiaro");
    }
    
    // ============================================
    // SPAZIATURA
    // ============================================
    
    /**
     * Applica padding micro (~3.8px).
     */
    public static void paddingMicro(Node node) {
        node.getStyleClass().add("padding-micro");
    }
    
    /**
     * Applica padding extra small (~6.2px).
     */
    public static void paddingXs(Node node) {
        node.getStyleClass().add("padding-xs");
    }
    
    /**
     * Applica padding small (10px).
     */
    public static void paddingSm(Node node) {
        node.getStyleClass().add("padding-sm");
    }
    
    /**
     * Applica padding medium (~16px).
     */
    public static void paddingMd(Node node) {
        node.getStyleClass().add("padding-md");
    }
    
    /**
     * Applica padding large (~26px).
     */
    public static void paddingLg(Node node) {
        node.getStyleClass().add("padding-lg");
    }
    
    /**
     * Applica padding extra large (~42px).
     */
    public static void paddingXl(Node node) {
        node.getStyleClass().add("padding-xl");
    }
    
    /**
     * Applica spacing micro (~3.8px) - per VBox/HBox.
     */
    public static void spacingMicro(Node node) {
        node.getStyleClass().add("spacing-micro");
    }
    
    /**
     * Applica spacing extra small (~6.2px).
     */
    public static void spacingXs(Node node) {
        node.getStyleClass().add("spacing-xs");
    }
    
    /**
     * Applica spacing small (10px).
     */
    public static void spacingSm(Node node) {
        node.getStyleClass().add("spacing-sm");
    }
    
    /**
     * Applica spacing medium (~16px).
     */
    public static void spacingMd(Node node) {
        node.getStyleClass().add("spacing-md");
    }
    
    /**
     * Applica spacing large (~26px).
     */
    public static void spacingLg(Node node) {
        node.getStyleClass().add("spacing-lg");
    }
    
    /**
     * Applica spacing extra large (~42px).
     */
    public static void spacingXl(Node node) {
        node.getStyleClass().add("spacing-xl");
    }
    
    // ============================================
    // BORDI
    // ============================================
    
    /**
     * Applica bordo standard.
     */
    public static void bordo(Node node) {
        node.getStyleClass().add("bordo");
    }
    
    /**
     * Applica bordo primario (verde neon).
     */
    public static void bordoPrimario(Node node) {
        node.getStyleClass().add("bordo-primario");
    }
    
    /**
     * Applica bordo arrotondato (raggio normale ~10px).
     */
    public static void bordoArrotondato(Node node) {
        node.getStyleClass().add("bordo-arrotondato");
    }
    
    /**
     * Applica bordo circolare (50%).
     */
    public static void bordoCircolare(Node node) {
        node.getStyleClass().add("bordo-circolare");
    }
    
    // ============================================
    // OMBRE E GLOW
    // ============================================
    
    /**
     * Applica ombra livello 1 (sottile).
     */
    public static void ombra1(Node node) {
        node.getStyleClass().add("ombra-1");
    }
    
    /**
     * Applica ombra livello 2.
     */
    public static void ombra2(Node node) {
        node.getStyleClass().add("ombra-2");
    }
    
    /**
     * Applica ombra livello 3 (media).
     */
    public static void ombra3(Node node) {
        node.getStyleClass().add("ombra-3");
    }
    
    /**
     * Applica ombra livello 4.
     */
    public static void ombra4(Node node) {
        node.getStyleClass().add("ombra-4");
    }
    
    /**
     * Applica ombra livello 5 (profonda).
     */
    public static void ombra5(Node node) {
        node.getStyleClass().add("ombra-5");
    }
    
    /**
     * Applica glow primario (verde neon).
     */
    public static void glowPrimario(Node node) {
        node.getStyleClass().add("glow-primario");
    }
    
    /**
     * Applica glow secondario (blu elettrico).
     */
    public static void glowSecondario(Node node) {
        node.getStyleClass().add("glow-secondario");
    }
    
    /**
     * Applica glow pericolo (rosso).
     */
    public static void glowPericolo(Node node) {
        node.getStyleClass().add("glow-pericolo");
    }
    
    /**
     * Applica glow successo (verde).
     */
    public static void glowSuccesso(Node node) {
        node.getStyleClass().add("glow-successo");
    }
    
    // ============================================
    // STILI COMPLETI COMPONENTI
    // ============================================
    
    /**
     * Applica stile completo pulsante menu.
     * Include dimensioni, colori, tipografia, ombre.
     */
    public static void stilePulsanteMenu(Node button) {
        button.getStyleClass().add("pulsante-menu");
    }
    
    /**
     * Applica stile completo pulsante primario.
     */
    public static void stilePulsantePrimario(Node button) {
        button.getStyleClass().addAll("pulsante-menu", "primario");
    }
    
    /**
     * Applica stile completo pulsante pericolo.
     */
    public static void stilePulsantePericolo(Node button) {
        button.getStyleClass().addAll("pulsante-menu", "pericolo");
    }
    
    /**
     * Applica stile contenitore pulsanti.
     */
    public static void contenitorePulsanti(Node container) {
        container.getStyleClass().add("contenitore-pulsanti");
    }
    
    // ============================================
    // UTILITY MULTIPLE CLASSI
    // ============================================
    
    /**
     * Applica multiple classi CSS contemporaneamente.
     * @param node nodo target
     * @param classes classi CSS da applicare
     */
    public static void applicaClassi(Node node, String... classes) {
        node.getStyleClass().addAll(classes);
    }
    
    /**
     * Rimuove tutte le classi CSS dal nodo.
     * @param node nodo target
     */
    public static void rimuoviTutteClassi(Node node) {
        node.getStyleClass().clear();
    }
    
    /**
     * Sostituisce tutte le classi CSS con nuove classi.
     * @param node nodo target
     * @param classes nuove classi CSS
     */
    public static void sostituisciClassi(Node node, String... classes) {
        node.getStyleClass().clear();
        node.getStyleClass().addAll(classes);
    }
}
