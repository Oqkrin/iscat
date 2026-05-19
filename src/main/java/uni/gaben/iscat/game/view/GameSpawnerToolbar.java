package uni.gaben.iscat.game.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import uni.gaben.iscat.game.controller.GameController;
import uni.gaben.iscat.game.universe.UniverseSpawnable;

import uni.gaben.iscat.utils.design.CssHelper;

import java.util.Set;

/**
 * Barra degli strumenti inferiore per lo spawning rapido di entità.
 * Permette di generare asteroidi o nemici con un click.
 */
public class GameSpawnerToolbar extends StackPane {

    public final FlowPane spawnContainer;
    private final ScrollPane scroll;

    // Spawn button che non dovrebbero esistere
    private static final Set<UniverseSpawnable> HIDDEN_SPAWNABLES = Set.of(
            UniverseSpawnable.PLAYER,
            UniverseSpawnable.WORM_HEAD,
            UniverseSpawnable.WORM_BODY,
            UniverseSpawnable.WORM_TAIL,
            UniverseSpawnable.PROJECTILE
    );


    public GameSpawnerToolbar(GameController controller) {

        // ==================== SPAWN BUTTONS ====================
        scroll = new ScrollPane();
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        // FlowPane: va a capo automaticamente, nessuna riga/colonna fissa
        spawnContainer = new FlowPane(10, 10); // hgap, vgap
        spawnContainer.setAlignment(Pos.CENTER);
        spawnContainer.setPadding(new Insets(10, 20, 10, 20));

        // SPAWNERS BUTTONS
        for (UniverseSpawnable spawnable : UniverseSpawnable.values()) {
            if (HIDDEN_SPAWNABLES.contains(spawnable)) continue;
            Button b = createSmallButton(spawnable.name());
            b.setOnAction(e -> controller.debugSpawn(spawnable.name()));
            spawnContainer.getChildren().add(b);
        }

        scroll.setContent(spawnContainer);
        getChildren().addAll(scroll);

        // Style
        setMaxWidth(900);
        setMaxHeight(160);
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
}
