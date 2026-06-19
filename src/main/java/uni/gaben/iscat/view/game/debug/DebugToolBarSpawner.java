package uni.gaben.iscat.view.game.debug;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import uni.gaben.iscat.controller.game.GameController;
import uni.gaben.iscat.universe.spawn.UniverseSpawnable;
import uni.gaben.iscat.universe.entities.EntityFactory;
import uni.gaben.iscat.universe.entities.EntityRecord;
import uni.gaben.iscat.utils.design.CssHelper;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Pannello dedicato alla generazione visiva di nemici o oggetti nel mondo attivo.
 */
public class DebugToolBarSpawner extends VBox {

    private static final Set<UniverseSpawnable> HIDDEN_SPAWNABLES = Set.of(
            UniverseSpawnable.PLAYER,
            UniverseSpawnable.PROJECTILE
    );

    private final Map<String, Image> imageCache = new HashMap<>();
    private final FlowPane spawnContainer;
    private final ScrollPane scroll;
    private final GameController controller;

    public DebugToolBarSpawner(GameController controller, Runnable onBack) {
        super(8);
        this.controller = controller;
        this.spawnContainer = new FlowPane(15, 15);
        this.scroll = new ScrollPane();

        // Pulsante Indietro
        Button btnBack = new Button("← INDIETRO");
        btnBack.setFocusTraversable(false);
        CssHelper.stilePulsanteMenu(btnBack);
        CssHelper.testoSecondario(btnBack);
        btnBack.setStyle(btnBack.getStyle() + "; -fx-font-size: 11px; -fx-padding: 4 12;");
        btnBack.setOnAction(e -> onBack.run());

        HBox topBar = new HBox(btnBack);
        topBar.setPadding(new Insets(10, 20, 5, 20));
        getChildren().add(topBar);

        spawnContainer.setAlignment(Pos.TOP_LEFT);
        spawnContainer.setPadding(new Insets(4, 20, 15, 20));

        Map<String, EntityRecord> cacheMap = EntityFactory.getCache();

        for (UniverseSpawnable spawnable : UniverseSpawnable.values()) {
            if (HIDDEN_SPAWNABLES.contains(spawnable)) continue;
            EntityRecord record = cacheMap != null ? cacheMap.get(spawnable.name().toLowerCase()) : null;
            if (record == null && cacheMap != null) {
                record = cacheMap.get(spawnable.name());
            }
            spawnContainer.getChildren().add(createSquareSpawnCard(spawnable.name(), record));
        }

        if (cacheMap != null && !cacheMap.isEmpty()) {
            for (EntityRecord record : cacheMap.values()) {
                if (record == null || record.entityKey() == null) continue;
                boolean giaAggiunto = spawnContainer.getChildren().stream()
                        .anyMatch(node -> node.getId() != null && node.getId().equals(record.entityKey()));

                if (!giaAggiunto) {
                    spawnContainer.getChildren().add(createSquareSpawnCard(record.entityKey(), record));
                }
            }
        }

        VBox scrollContent = new VBox(8);
        Label sectionTitle = new Label("SPAWNABLE LIST (CLICK TO GENERATE)");
        CssHelper.testoSecondario(sectionTitle);
        sectionTitle.setStyle(sectionTitle.getStyle() + "; -fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #8a94a6; -fx-padding: 5 20 0 20;");

        scrollContent.getChildren().addAll(sectionTitle, spawnContainer);

        scroll.setContent(scrollContent);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        getChildren().add(scroll);
    }

    private VBox createSquareSpawnCard(String key, EntityRecord record) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(90);
        card.setId(key);

        Button btnSquare = new Button();
        btnSquare.setMinSize(70, 70);
        btnSquare.setMaxSize(70, 70);
        btnSquare.setFocusTraversable(false);
        CssHelper.stilePulsanteMenu(btnSquare);
        btnSquare.setStyle(btnSquare.getStyle() + "; -fx-background-radius: 8; -fx-border-radius: 8;");
        btnSquare.setOnAction(e -> controller.debugSpawn(key));

        if (record != null && record.spritePath() != null && !record.spritePath().isBlank()) {
            try {
                String path = record.spritePath().startsWith("/") ? record.spritePath() : "/" + record.spritePath();
                Image spriteImg = imageCache.computeIfAbsent(path, p -> {
                    try (InputStream is = getClass().getResourceAsStream(p)) {
                        if (is != null) return new Image(is);
                    } catch (Exception ex) {
                        System.err.println("[DebugToolBar] Impossibile caricare asset: " + p);
                    }
                    return null;
                });

                if (spriteImg != null && !spriteImg.isError()) {
                    ImageView view = new ImageView(spriteImg);
                    if (record.frameW() > 0 && record.frameH() > 0) {
                        view.setViewport(new Rectangle2D(0, 0, record.frameW(), record.frameH()));
                    }
                    view.setFitWidth(42);
                    view.setFitHeight(42);
                    view.setPreserveRatio(true);
                    view.setSmooth(true);
                    btnSquare.setGraphic(view);
                } else {
                    btnSquare.setText("👾");
                }
            } catch (Exception e) {
                btnSquare.setText("👾");
            }
        } else {
            btnSquare.setText("➕");
        }

        if (record != null && record.name() != null) {
            btnSquare.setTooltip(new Tooltip(record.name() + "\nHP: " + record.initLife() + "\nXP: " + record.xpReward()));
        }

        String cleanName = key.replace("_", " ").replace("-", " ").toUpperCase();
        Label nameLabel = new Label(cleanName);
        nameLabel.setAlignment(Pos.CENTER);
        nameLabel.setPrefWidth(85);
        nameLabel.setWrapText(true);
        CssHelper.testoPrimario(nameLabel);
        nameLabel.setStyle(nameLabel.getStyle() + "; -fx-font-size: 10px; -fx-text-alignment: center; -fx-opacity: 0.85;");

        card.getChildren().addAll(btnSquare, nameLabel);
        return card;
    }
}