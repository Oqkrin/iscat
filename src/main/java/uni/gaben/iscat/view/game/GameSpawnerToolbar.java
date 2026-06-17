package uni.gaben.iscat.view.game;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import uni.gaben.iscat.controller.game.GameController;
import uni.gaben.iscat.universe.spawn.UniverseSpawnable;
import uni.gaben.iscat.universe.entities.EntityFactory;
import uni.gaben.iscat.universe.entities.EntityRecord;
import uni.gaben.iscat.utils.design.CssHelper;

import java.util.Map;
import java.util.Set;

/**
 * Debug toolbar that lets the developer spawn any entity into the active universe.
 * Only shown when debug mode is enabled in {@link GameView}.
 */
public class GameSpawnerToolbar extends StackPane {

    private static final Set<UniverseSpawnable> HIDDEN_SPAWNABLES = Set.of(
            UniverseSpawnable.PLAYER,
            UniverseSpawnable.PROJECTILE
    );

    private final FlowPane spawnContainer;
    private final ScrollPane scroll;

    public GameSpawnerToolbar(GameController controller) {
        spawnContainer = new FlowPane(10, 10);
        scroll         = new ScrollPane();
        buildNodes(controller);
        applyStyles();
    }

    // -------------------------------------------------------------------------
    // Build
    // -------------------------------------------------------------------------

    private void buildNodes(GameController controller) {
        VBox root = new VBox(8);
        root.setPadding(new Insets(8, 0, 8, 0));

        // --- Hardcoded entities (enum-driven) ---
        spawnContainer.setAlignment(Pos.BOTTOM_CENTER);
        spawnContainer.setPadding(new Insets(4, 20, 4, 20));

        for (UniverseSpawnable spawnable : UniverseSpawnable.values()) {
            if (HIDDEN_SPAWNABLES.contains(spawnable)) continue;
            Button b = createSpawnButton(spawnable.name());
            b.setOnAction(e -> controller.debugSpawn(spawnable.name()));
            spawnContainer.getChildren().add(b);
        }

        FlowPane genericContainer = new FlowPane(10, 10);
        genericContainer.setAlignment(Pos.BOTTOM_CENTER);
        genericContainer.setPadding(new Insets(4, 20, 4, 20));

        Map<String, EntityRecord> jsonEnemies = EntityFactory.getCache();

        if (jsonEnemies != null && !jsonEnemies.isEmpty()) {
            for (EntityRecord s : jsonEnemies.values()) {
                if (s == null || s.entityKey() == null) continue;

                Button b = createSpawnButton(s.entityKey());
                b.setTooltip(new javafx.scene.control.Tooltip(s.name()));
                b.setOnAction(e -> controller.debugSpawn(s.entityKey()));
                genericContainer.getChildren().add(b);
            }
        } else {
            System.err.println("[GameSpawnerToolbar] Nessuna entità trovata nella cache JSON!");
        }

        root.getChildren().addAll(
                sectionLabel("Hardcoded entities"), spawnContainer,
                sectionLabel("JSON Loaded (Runtime)"), genericContainer
        );

        scroll.setContent(root);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        getChildren().add(scroll);
    }

    private void applyStyles() {
        CssHelper.sfondoScuro(this);
        CssHelper.bordoArrotondato(this);
        CssHelper.ombra3(this);
        CssHelper.bordoPrimario(this);
        // Fine-tuned background and border — extends the CSS class defaults
        setStyle(getStyle() + "-fx-background-color: rgba(13,15,18,0.92); -fx-border-width: 1.5;");
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private Label sectionLabel(String text) {
        Label lbl = new Label(text);
        lbl.setPadding(new Insets(2, 20, 0, 20));
        CssHelper.testoSecondario(lbl);
        return lbl;
    }

    private Button createSpawnButton(String text) {
        String formattedText = text.replace("_", " ")
                .replace("-", " ")
                .toUpperCase();

        Button btn = new Button(formattedText);
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