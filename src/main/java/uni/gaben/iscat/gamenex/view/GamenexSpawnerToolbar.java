package uni.gaben.iscat.gamenex.view;

import javafx.geometry.Insets;
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

    private final HBox spawnContainer;
    private final ScrollPane scroll;

    public GamenexSpawnerToolbar(GamenexController controller) {

        // ==================== SPAWN BUTTONS ====================
        scroll = new ScrollPane();
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setFitToHeight(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        spawnContainer = new HBox(12);
        spawnContainer.setAlignment(Pos.CENTER);
        spawnContainer.setStyle("-fx-padding: 10 20;");

        // SPAWNERS BUTTONS
        Button spawnAsteroidBtn = createSmallButton("ASTEROID");
        spawnAsteroidBtn.setOnAction(e -> controller.spawnAsteroid());
        
        Button spawnIscatBtn = createSmallButton("ISCAT MOB");
        spawnIscatBtn.setOnAction(e -> controller.spawnIscatMob());

        Button spawnHearthBtn = createSmallButton("HEARTH");
        spawnHearthBtn.setOnAction(e -> controller.spawnHearth());

        Button spawnEaterBtn = createSmallButton("EATER");
        spawnEaterBtn.setOnAction(e -> controller.spawnEater());

        spawnContainer.getChildren().addAll(spawnAsteroidBtn, spawnIscatBtn, spawnHearthBtn, spawnEaterBtn);

        scroll.setContent(spawnContainer);

        getChildren().addAll(scroll);

        // Stile generale della toolbar
        setMaxHeight(80);
        setMaxWidth(900);
        CssHelper.sfondoScuro(this);
        CssHelper.bordoArrotondato(this);
        CssHelper.ombra3(this);
        CssHelper.bordoPrimario(this);
        setStyle(getStyle() + "-fx-background-color: rgba(13, 15, 18, 0.92); -fx-border-width: 1.5;");
        setVisible(false);
    }

    /**
     * Helper per creare un bottone piccolo con lo stile del menu principale.
     */
    private Button createSmallButton(String text) {
        Button btn = new Button(text);
        btn.setPrefHeight(34);
        btn.setMinWidth(120);
        btn.setFocusTraversable(false);

        CssHelper.stilePulsanteMenu(btn);
        CssHelper.testoPrimario(btn);
        CssHelper.labelLarge(btn);

        btn.setStyle(btn.getStyle() + "-fx-padding: 0 15; -fx-font-size: 13px;");
        return btn;
    }

    public void setSpawnButtonsVisible(boolean visible) {
        setVisible(visible);
        scroll.setVisible(visible);
        spawnContainer.setVisible(visible);
    }

    public boolean isSpawnButtonsVisible() {
        return spawnContainer.isVisible();
    }
}
