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
import javafx.scene.layout.Priority;
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
 * Pannello dedicato alla generazione visiva e al monitoraggio di nemici,
 * oggetti speciali o entità hardcoded all'interno del mondo attivo.
 */
public class DebugToolBarSpawner extends VBox {

    private static final Set<UniverseSpawnable> HIDDEN_SPAWNABLES = Set.of(
            UniverseSpawnable.PLAYER,
            UniverseSpawnable.PROJECTILE
    );

    private final Map<String, Image> imageCache = new HashMap<>();
    private final GameController controller;

    public DebugToolBarSpawner(GameController controller, Runnable onBack) {
        super(12);
        setPadding(new Insets(12, 20, 20, 20));
        setAlignment(Pos.TOP_CENTER);

        this.controller = controller;
        FlowPane spawnContainer = new FlowPane(15, 15);
        ScrollPane scroll = new ScrollPane();

        Button btnBack = new Button("← INDIETRO");
        btnBack.setFocusTraversable(false);
        CssHelper.stilePulsanteMenu(btnBack);
        CssHelper.testoSecondario(btnBack);
        btnBack.getStyleClass().add("debug-btn-back");
        btnBack.setOnAction(e -> onBack.run());

        HBox topBar = new HBox(btnBack);
        topBar.setAlignment(Pos.TOP_LEFT);

        Label mainTitle = new Label("SPAWNABLE LIST (CLICK TO GENERATE)");
        CssHelper.testoPrimario(mainTitle);
        mainTitle.getStyleClass().add("debug-main-title");

        spawnContainer.setAlignment(Pos.TOP_LEFT);
        spawnContainer.setPadding(new Insets(10, 0, 10, 0));

        Map<String, EntityRecord> cacheMap = EntityFactory.getCache();

        for (UniverseSpawnable spawnable : UniverseSpawnable.values()) {
            if (HIDDEN_SPAWNABLES.contains(spawnable)) continue;
            EntityRecord record = cacheMap.get(spawnable.name().toLowerCase());
            if (record == null) {
                record = cacheMap.get(spawnable.name());
            }
            spawnContainer.getChildren().add(createSquareSpawnCard(spawnable.name(), record));
        }

        if (!cacheMap.isEmpty()) {
            for (EntityRecord record : cacheMap.values()) {
                if (record == null || record.entityKey() == null) continue;
                boolean giaAggiunto = spawnContainer.getChildren().stream()
                        .anyMatch(node -> node.getId() != null && node.getId().equals(record.entityKey()));

                if (!giaAggiunto) {
                    spawnContainer.getChildren().add(createSquareSpawnCard(record.entityKey(), record));
                }
            }
        }

        scroll.setContent(spawnContainer);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setFitToWidth(true);
        // Pulito da stili inline, ora usa la classe CSS specifica
        scroll.getStyleClass().add("debug-spawner-scroll");

        scroll.setFitToHeight(true);
        scroll.prefHeightProperty().bind(spawnContainer.heightProperty());

        VBox.setVgrow(scroll, Priority.ALWAYS);
        getChildren().addAll(topBar, mainTitle, scroll);
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
        // Assegniamo la classe CSS per gestire i bordi arrotondati
        btnSquare.getStyleClass().add("debug-spawn-btn-square");
        btnSquare.setOnAction(e -> controller.debugSpawn(key));

        String targetSpritePath = (record != null) ? record.spritePath() : null;
        boolean isTextFallback = false;

        String lowerKey = key.toLowerCase();
        if (lowerKey.contains("worm")) {
            Map<String, EntityRecord> cacheMap = EntityFactory.getCache();
            if (cacheMap.containsKey("iscat_worm_head")) {
                record = cacheMap.get("iscat_worm_head");
                targetSpritePath = record.spritePath();
            }
        } else if (lowerKey.contains("heart")) {
            targetSpritePath = "/uni/gaben/iscat/sprites/boosts/heart.png";
        } else if (lowerKey.contains("blackhole")) {
            targetSpritePath = "/uni/gaben/iscat/sprites/other/blackhole.png";
        } else if (lowerKey.contains("asteroid")) {
            targetSpritePath = "/uni/gaben/iscat/sprites/other/asteroid.png";
        }

        if (targetSpritePath != null && !targetSpritePath.isBlank()) {
            try {
                String path = targetSpritePath.startsWith("/") ? targetSpritePath : "/" + targetSpritePath;
                Image spriteImg = imageCache.computeIfAbsent(path, p -> {
                    try (InputStream is = getClass().getResourceAsStream(p)) {
                        if (is != null) return new Image(is);
                    } catch (Exception ex) {
                        System.err.println("[DebugToolBar] Impossibile caricare asset override: " + p);
                    }
                    return null;
                });

                if (spriteImg != null && !spriteImg.isError()) {
                    ImageView view = new ImageView(spriteImg);

                    boolean isHardcoded = lowerKey.contains("heart") || lowerKey.contains("blackhole") || lowerKey.contains("asteroid");

                    if (record != null && record.frameW() > 0 && record.frameH() > 0 && !isHardcoded) {
                        if (lowerKey.contains("master")) {
                            view.setViewport(new Rectangle2D(0, record.frameH(), record.frameW(), record.frameH()));
                        } else {
                            view.setViewport(new Rectangle2D(0, 0, record.frameW(), record.frameH()));
                        }
                    }

                    view.setFitWidth(42);
                    view.setFitHeight(42);
                    view.setPreserveRatio(true);
                    view.setSmooth(true);
                    btnSquare.setGraphic(view);
                } else {
                    isTextFallback = true;
                }
            } catch (Exception e) {
                isTextFallback = true;
            }
        } else {
            isTextFallback = true;
        }

        if (isTextFallback) {
            Label textLabel = new Label("[+]");
            CssHelper.testoPrimario(textLabel);
            textLabel.getStyleClass().add("debug-spawn-fallback-text");
            textLabel.setAlignment(Pos.CENTER);
            btnSquare.setGraphic(textLabel);
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
        nameLabel.getStyleClass().add("debug-spawn-card-label");

        card.getChildren().addAll(btnSquare, nameLabel);
        return card;
    }
}