package uni.gaben.iscat.gamenex.view;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import uni.gaben.iscat.gamenex.controller.GamenexController;
import uni.gaben.iscat.utils.design.TipografiaAurea;

import uni.gaben.iscat.utils.design.CssHelper;

/**
 * Barra degli strumenti inferiore per lo spawning rapido di entità.
 * Permette di generare asteroidi o nemici con un click.
 */
public class GamenexSpawnerToolbar extends StackPane {

    /**
     * Crea la barra di spawning.
     * @param controller Il controller di Gamenex per gestire le richieste di spawn.
     */
    public GamenexSpawnerToolbar(GamenexController controller) {
        // ScrollPane per permettere la navigazione orizzontale se ci sono molte icone
        ScrollPane scroll = new ScrollPane();
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setFitToHeight(true);
        
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        HBox container = new HBox(12);
        container.setAlignment(Pos.CENTER);
        container.setStyle("-fx-padding: 10 20;");

        Button spawnAsteroidBtn = createSmallButton("ASTEROID");
        spawnAsteroidBtn.setOnAction(e -> controller.spawnAsteroid());
        
        Button spawnIscatBtn = createSmallButton("ISCAT MOB");
        spawnIscatBtn.setOnAction(e -> controller.spawnIscatMob());

        container.getChildren().addAll(spawnAsteroidBtn, spawnIscatBtn);
        
        scroll.setContent(container);
        
        // Layout: centrato in basso, dimensioni contenute
        getChildren().add(scroll);
        setMaxHeight(60);
        setMaxWidth(650);
        
        // Applica lo stile del Main Menu al contenitore tramite CssHelper
        CssHelper.sfondoScuro(this);
        CssHelper.bordoArrotondato(this);
        CssHelper.ombra3(this);
        CssHelper.bordoPrimario(this); // Aggiunge un sottile bordo neon
        
        // Ulteriore fine-tuning per l'effetto vetro scuro del menu
        setStyle(getStyle() + "-fx-background-color: rgba(13, 15, 18, 0.95); -fx-border-width: 1.5;");
    }

    /**
     * Helper per creare un bottone piccolo con lo stile del menu principale.
     */
    private Button createSmallButton(String text) {
        Button btn = new Button(text);
        btn.setPrefHeight(34);
        btn.setMinWidth(120);
        btn.setFocusTraversable(false); // IMPORTANTE: evita che il bottone rubi lo Spacebar
        
        // Applica stili dal design system
        CssHelper.stilePulsanteMenu(btn);
        CssHelper.testoPrimario(btn);
        CssHelper.labelLarge(btn);
        
        // Sovrascriviamo leggermente le dimensioni del pulsante menu standard per la toolbar
        btn.setStyle(btn.getStyle() + "-fx-padding: 0 15; -fx-font-size: 13px;");
        
        return btn;
    }
}
