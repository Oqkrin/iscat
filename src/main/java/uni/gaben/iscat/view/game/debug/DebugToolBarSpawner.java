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
import uni.gaben.iscat.IscatSettings;
import uni.gaben.iscat.controller.game.GameController;
import uni.gaben.iscat.universe.spawn.UniverseSpawnable;
import uni.gaben.iscat.universe.entities.parsed.EntityFactory;
import uni.gaben.iscat.universe.entities.parsed.EntityRecord;
import uni.gaben.iscat.utils.design.CssHelper;

import java.io.InputStream;
import java.util.*;

public class DebugToolBarSpawner extends VBox {

    private static final Set<UniverseSpawnable> HIDDEN_SPAWNABLES = Set.of(
            UniverseSpawnable.PLAYER,
            UniverseSpawnable.PROJECTILE
    );
    public static final String HEART_PNG = "/uni/gaben/iscat/sprites/boosts/heart.png";
    public static final String BLACKHOLE_PNG = "/uni/gaben/iscat/sprites/other/blackhole.png";
    public static final String ASTEROID_PNG = "/uni/gaben/iscat/sprites/other/asteroid.png";

    private final Map<String, Image> imageCache = new HashMap<>();
    private final GameController controller;

    public DebugToolBarSpawner(GameController controller, Runnable onBack) {
        final double SU = IscatSettings.STANDARD_UNIT;

        setSpacing(SU);
        setPadding(new Insets(SU, SU, SU, SU));
        setAlignment(Pos.TOP_CENTER);
        getStyleClass().add("debug-tool-bar");

        this.controller = controller;

        VBox mainCategoriesLayout = new VBox(SU);
        mainCategoriesLayout.setPadding(new Insets(SU /2, 0, SU /2, 0));

        ScrollPane scroll = new ScrollPane();
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("debug-spawner-scroll");

        // Back button
        Button btnBack = new Button("← INDIETRO");
        btnBack.setFocusTraversable(false);
        CssHelper.stilePulsanteMenu(btnBack);
        CssHelper.testoSecondario(btnBack);
        btnBack.getStyleClass().add("debug-btn-back");
        btnBack.setPadding(new Insets(SU /4, SU /2, SU /4, SU /2));
        btnBack.setOnAction(e -> onBack.run());

        HBox topBar = new HBox(btnBack);
        topBar.setAlignment(Pos.TOP_LEFT);

        Label mainTitle = new Label("SPAWNABLE LIST (CLICK TO GENERATE)");
        CssHelper.testoPrimario(mainTitle);
        mainTitle.getStyleClass().add("debug-main-title");
        mainTitle.setPadding(new Insets(SU /2, 0, SU /4, 0));

        // Gather entities
        Map<String, EntityRecord> allUniqueEntities = getAllUniqueEntities();
        List<Map.Entry<String, EntityRecord>> specialList = new ArrayList<>();
        List<Map.Entry<String, EntityRecord>> enemiesList = new ArrayList<>();
        List<Map.Entry<String, EntityRecord>> playersList = new ArrayList<>();
        List<Map.Entry<String, EntityRecord>> customList  = new ArrayList<>();

        for (Map.Entry<String, EntityRecord> entry : allUniqueEntities.entrySet()) {
            String lowerKey = entry.getKey().toLowerCase();
            EntityRecord record = entry.getValue();
            boolean isPlayer = record != null && record.player() != null;
            boolean isCustom = isCustom(entry.getKey());
            boolean isWormComponent = lowerKey.contains("head") || lowerKey.contains("body") || lowerKey.contains("tail");

            if (isCustom) {
                customList.add(entry);
                continue; // separate category
            }
            if (isPlayer) {
                playersList.add(entry);
            } else if ((lowerKey.contains("worm") && !isWormComponent) || lowerKey.contains("asteroid") ||
                    lowerKey.contains("blackhole") || lowerKey.contains("heart")) {
                specialList.add(entry);
            } else {
                enemiesList.add(entry);
            }
        }

        Comparator<Map.Entry<String, EntityRecord>> comp = (e1, e2) -> {
            Integer o1 = (e1.getValue() != null) ? e1.getValue().bestiaryOrder() : null;
            Integer o2 = (e2.getValue() != null) ? e2.getValue().bestiaryOrder() : null;
            if (o1 == null && o2 == null) return 0;
            if (o1 == null) return 1;
            if (o2 == null) return -1;
            return Integer.compare(o1, o2);
        };
        enemiesList.sort(comp);
        playersList.sort(comp);
        customList.sort(Map.Entry.comparingByKey());

        creaSezioneCategoria(mainCategoriesLayout, "★ SPECIAL ★", specialList);
        creaSezioneCategoria(mainCategoriesLayout, "⚔ ENEMIES ⚔", enemiesList);
        creaSezioneCategoria(mainCategoriesLayout, "✈ PLAYERS ✈", playersList);
        creaSezioneCategoria(mainCategoriesLayout, "⚙ CUSTOM ⚙", customList);

        scroll.setContent(mainCategoriesLayout);
        scroll.setFitToHeight(true);
        scroll.prefHeightProperty().bind(mainCategoriesLayout.heightProperty());

        VBox.setVgrow(scroll, Priority.ALWAYS);
        getChildren().addAll(topBar, mainTitle, scroll);
    }

    private static Map<String, EntityRecord> getAllUniqueEntities() {
        Map<String, EntityRecord> cacheMap = EntityFactory.getCache();
        Map<String, EntityRecord> all = new LinkedHashMap<>();
        for (UniverseSpawnable sp : UniverseSpawnable.values()) {
            if (HIDDEN_SPAWNABLES.contains(sp)) continue;
            EntityRecord r = cacheMap.get(sp.name().toLowerCase());
            if (r == null) r = cacheMap.get(sp.name());
            all.put(sp.name(), r);
        }
        for (EntityRecord r : cacheMap.values()) {
            if (r == null || r.entityKey() == null) continue;
            if (!all.containsKey(r.entityKey())) {
                all.put(r.entityKey(), r);
            }
        }
        return all;
    }

    private boolean isCustom(String key) {
        String origin = EntityFactory.getOriginPath(key);
        return origin != null && origin.toLowerCase().contains("custom");
    }

    private void creaSezioneCategoria(VBox parent, String titolo, List<Map.Entry<String, EntityRecord>> elementi) {
        if (elementi.isEmpty()) return;
        final double SU = IscatSettings.STANDARD_UNIT;

        VBox sectionBox = new VBox(SU/3); // 6
        Label sectionTitle = new Label(titolo);
        CssHelper.testoPrimario(sectionTitle);
        sectionTitle.getStyleClass().add("debug-title");
        sectionTitle.setPadding(new Insets(SU /2, 0, SU /8, 0)); // 10,0,2,0

        FlowPane flowPane = new FlowPane(SU, SU); // 15
        flowPane.setAlignment(Pos.TOP_LEFT);

        for (Map.Entry<String, EntityRecord> entry : elementi) {
            flowPane.getChildren().add(createSquareSpawnCard(entry.getKey(), entry.getValue()));
        }

        sectionBox.getChildren().addAll(sectionTitle, flowPane);
        parent.getChildren().add(sectionBox);
    }

    private VBox createSquareSpawnCard(String key, EntityRecord entity) {
        final double SU = IscatSettings.STANDARD_UNIT;
        final double IMG_SIZE = SU * 2; // 32

        VBox card = new VBox(SU /2); // 8
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(SU /2)); // 8
        card.setId(key);

        Button btnSquare = new Button();
        btnSquare.setFocusTraversable(false);
        CssHelper.stilePulsanteMenu(btnSquare);
        btnSquare.getStyleClass().add("debug-spawn-btn-square");
        btnSquare.setPadding(new Insets(SU /4)); // 4
        btnSquare.setOnAction(e -> controller.debugSpawn(key));

        // Determine sprite path & fallback logic
        String targetSpritePath = (entity != null) ? entity.spritePath() : null;
        boolean isTextFallback = false;
        String lowerKey = key.toLowerCase();

        if (lowerKey.equals("worm") || lowerKey.equals("iscat_worm")) {
            Map<String, EntityRecord> cacheMap = EntityFactory.getCache();
            if (cacheMap.containsKey("iscat_worm_head")) {
                entity = cacheMap.get("iscat_worm_head");
                targetSpritePath = entity.spritePath();
            }
        } else if (lowerKey.contains("heart")) {
            targetSpritePath = HEART_PNG;
        } else if (lowerKey.contains("blackhole")) {
            targetSpritePath = BLACKHOLE_PNG;
        } else if (lowerKey.contains("asteroid")) {
            targetSpritePath = ASTEROID_PNG;
        }

        if (targetSpritePath != null && !targetSpritePath.isBlank()) {
            try {
                String path = targetSpritePath.startsWith("/") ? targetSpritePath : "/" + targetSpritePath;
                Image spriteImg = imageCache.computeIfAbsent(path, p -> {
                    try (InputStream is = getClass().getResourceAsStream(p)) {
                        if (is != null) return new Image(is);
                    } catch (Exception ex) {
                        System.err.println("[DebugToolBar] Cannot load: " + p);
                    }
                    return null;
                });

                if (spriteImg != null && !spriteImg.isError()) {
                    ImageView view = new ImageView(spriteImg);
                    boolean isHardcoded = lowerKey.contains("heart") || lowerKey.contains("blackhole") || lowerKey.contains("asteroid");
                    if (entity != null && entity.frameW() > 0 && entity.frameH() > 0 && !isHardcoded) {
                        if (lowerKey.contains("master")) {
                            view.setViewport(new Rectangle2D(0, entity.frameH(), entity.frameW(), entity.frameH()));
                        } else {
                            view.setViewport(new Rectangle2D(0, 0, entity.frameW(), entity.frameH()));
                        }
                    }
                    view.setFitWidth(IMG_SIZE);
                    view.setFitHeight(IMG_SIZE);
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

        if (entity != null && entity.name() != null) {
            btnSquare.setTooltip(new Tooltip(entity.name() + "\nHP: " + entity.initLife() + "\nXP: " + entity.xpReward()));
        }

        String cleanName = key.replace("_", " ").replace("-", " ").toUpperCase();
        Label nameLabel = new Label(cleanName);
        nameLabel.setAlignment(Pos.CENTER);
        nameLabel.setWrapText(true);
        CssHelper.testoPrimario(nameLabel);
        nameLabel.getStyleClass().add("debug-spawn-card-label");
        nameLabel.setMaxWidth(Double.MAX_VALUE);
        nameLabel.setPadding(new Insets(SU /8, 0, 0, 0)); // 2

        card.getChildren().addAll(btnSquare, nameLabel);
        return card;
    }
}