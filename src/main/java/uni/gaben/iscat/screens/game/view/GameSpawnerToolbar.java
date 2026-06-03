package uni.gaben.iscat.screens.game.view;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.screens.game.controller.GameController;
import uni.gaben.iscat.universe.UniverseSpawnable;
import uni.gaben.iscat.universe.enemies.generic.GenericPhysicalEntitySettings;
import uni.gaben.iscat.utils.design.CssHelper;

import java.util.Set;

public class GameSpawnerToolbar extends StackPane {

    public final FlowPane spawnContainer;
    private final ScrollPane scroll;

    private static final Set<UniverseSpawnable> HIDDEN_SPAWNABLES = Set.of(
            UniverseSpawnable.PLAYER,
            UniverseSpawnable.PROJECTILE
    );

    public GameSpawnerToolbar(GameController controller) {

        VBox root = new VBox(8);
        root.setPadding(new Insets(8, 0, 8, 0));

        Label hardcodedLabel = sectionLabel("Hardcoded entities");

        spawnContainer = new FlowPane(10, 10);
        spawnContainer.setAlignment(Pos.BOTTOM_CENTER);
        spawnContainer.setPadding(new Insets(4, 20, 4, 20));

        for (UniverseSpawnable spawnable : UniverseSpawnable.values()) {
            if (HIDDEN_SPAWNABLES.contains(spawnable)) continue;
            Button b = createSmallButton(spawnable.name());
            b.setOnAction(e -> controller.debugSpawn(spawnable.name()));
            spawnContainer.getChildren().add(b);
        }

        Label genericLabel = sectionLabel("DATABASE LOADED");

        FlowPane genericContainer = new FlowPane(10, 10);
        genericContainer.setAlignment(Pos.BOTTOM_CENTER);
        genericContainer.setPadding(new Insets(4, 20, 4, 20));

        // Sostituisci il vecchio blocco Platform.runLater con questo:
        IscatDB.getInstance().queryAsync(() -> IscatDB.getInstance().getEnemyDAO().findAll())
                .thenAccept(enemies -> {
                    // Una volta letti i dati in background, torniamo sul thread UI per modificare i nodi grafici
                    Platform.runLater(() -> {
                        for (GenericPhysicalEntitySettings s : enemies) {
                            if (s == null || s.entityKey == null) continue;

                            Button b = createSmallButton(s.entityKey);
                            b.setTooltip(new javafx.scene.control.Tooltip(s.name));
                            b.setOnAction(e -> controller.debugSpawn(s.entityKey));
                            genericContainer.getChildren().add(b);
                        }
                    });
                }).exceptionally(ex -> {
                    // Opzionale ma consigliato: intercetta eventuali errori di lettura dal DB
                    System.err.println("[GameSpawnerToolbar] Errore nel caricamento dei nemici dal DB: " + ex.getMessage());
                    return null;
                });

        root.getChildren().addAll(hardcodedLabel, spawnContainer, genericLabel, genericContainer);

        scroll = new ScrollPane(root);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        getChildren().add(scroll);

        CssHelper.sfondoScuro(this);
        CssHelper.bordoArrotondato(this);
        CssHelper.ombra3(this);
        CssHelper.bordoPrimario(this);
        setStyle(getStyle() + "-fx-background-color: rgba(13,15,18,0.92); -fx-border-width: 1.5;");
        setVisible(false);
    }

    private Label sectionLabel(String text) {
        Label lbl = new Label(text);
        lbl.setPadding(new Insets(2, 20, 0, 20));
        CssHelper.testoSecondario(lbl);
        return lbl;
    }

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