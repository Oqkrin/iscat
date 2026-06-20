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
import java.util.*;

/**
 * Pannello dedicato alla generazione visiva e al monitoraggio di nemici,
 * oggetti speciali o entità all'interno del mondo attivo.
 */
public class DebugToolBarSpawner extends VBox {

    private static final Set<UniverseSpawnable> HIDDEN_SPAWNABLES = Set.of(
            UniverseSpawnable.PLAYER,
            UniverseSpawnable.PROJECTILE
    );

    private final Map<String, Image> imageCache = new HashMap<>();
    private final GameController controller;

    /**
     * Costruisce il pannello di controllo dello spawner suddividendo le entità
     * in tre categorie distinte e ordinandole.
     *
     * @param controller Il controller del gioco corrente
     * @param onBack     L'azione da eseguire per tornare al menu precedente
     */
    public DebugToolBarSpawner(GameController controller, Runnable onBack) {
        super(12);
        setPadding(new Insets(12, 20, 20, 20));
        setAlignment(Pos.TOP_CENTER);
        getStyleClass().add("debug-tool-bar");

        this.controller = controller;

        VBox mainCategoriesLayout = new VBox(20);
        mainCategoriesLayout.setPadding(new Insets(10, 0, 10, 0));

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

        Map<String, EntityRecord> cacheMap = EntityFactory.getCache();
        Map<String, EntityRecord> allUniqueEntities = new LinkedHashMap<>();

        for (UniverseSpawnable spawnable : UniverseSpawnable.values()) {
            if (HIDDEN_SPAWNABLES.contains(spawnable)) continue;
            EntityRecord record = cacheMap.get(spawnable.name().toLowerCase());
            if (record == null) {
                record = cacheMap.get(spawnable.name());
            }
            allUniqueEntities.put(spawnable.name(), record);
        }

        for (EntityRecord record : cacheMap.values()) {
            if (record == null || record.entityKey() == null) continue;
            if (!allUniqueEntities.containsKey(record.entityKey())) {
                allUniqueEntities.put(record.entityKey(), record);
            }
        }

        List<Map.Entry<String, EntityRecord>> specialList = new ArrayList<>();
        List<Map.Entry<String, EntityRecord>> enemiesList = new ArrayList<>();
        List<Map.Entry<String, EntityRecord>> playersList = new ArrayList<>();

        for (Map.Entry<String, EntityRecord> entry : allUniqueEntities.entrySet()) {
            String lowerKey = entry.getKey().toLowerCase();
            EntityRecord record = entry.getValue();

            boolean isWormComponent = lowerKey.contains("head") || lowerKey.contains("body") || lowerKey.contains("tail");

            if ((lowerKey.contains("worm") && !isWormComponent) || lowerKey.contains("asteroid") ||
                    lowerKey.contains("blackhole") || lowerKey.contains("heart")) {
                specialList.add(entry);
            }
            else if (record != null && record.player() != null) {
                playersList.add(entry);
            }
            else {
                enemiesList.add(entry);
            }
        }

        Comparator<Map.Entry<String, EntityRecord>> bestiaryComparator = (e1, e2) -> {
            Integer o1 = (e1.getValue() != null) ? e1.getValue().bestiaryOrder() : null;
            Integer o2 = (e2.getValue() != null) ? e2.getValue().bestiaryOrder() : null;
            if (o1 == null && o2 == null) return 0;
            if (o1 == null) return 1;
            if (o2 == null) return -1;
            return Integer.compare(o1, o2);
        };

        enemiesList.sort(bestiaryComparator);
        playersList.sort(bestiaryComparator);

        creaSezioneCategoria(mainCategoriesLayout, "★ SPECIAL ★", specialList);
        creaSezioneCategoria(mainCategoriesLayout, "⚔ ENEMIES ⚔", enemiesList);
        creaSezioneCategoria(mainCategoriesLayout, "✈ PLAYERS ✈", playersList);

        scroll.setContent(mainCategoriesLayout);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("debug-spawner-scroll");
        scroll.setFitToHeight(true);
        scroll.prefHeightProperty().bind(mainCategoriesLayout.heightProperty());

        VBox.setVgrow(scroll, Priority.ALWAYS);
        getChildren().addAll(topBar, mainTitle, scroll);
    }

    /**
     * Crea un blocco grafico completo di titolo intestazione e griglia per una categoria.
     *
     * @param parent   Il contenitore verticale di destinazione
     * @param titolo   Il testo visualizzato come intestazione di sezione
     * @param elementi La lista di entità appartenenti alla categoria
     */
    private void creaSezioneCategoria(VBox parent, String titolo, List<Map.Entry<String, EntityRecord>> elementi) {
        if (elementi.isEmpty()) return;

        VBox sectionBox = new VBox(6);
        Label sectionTitle = new Label(titolo);
        CssHelper.testoPrimario(sectionTitle);
        sectionTitle.getStyleClass().add("debug-title");
        sectionTitle.setPadding(new Insets(10, 0, 2, 0));

        FlowPane flowPane = new FlowPane(15, 15);
        flowPane.setAlignment(Pos.TOP_LEFT);

        for (Map.Entry<String, EntityRecord> entry : elementi) {
            flowPane.getChildren().add(createSquareSpawnCard(entry.getKey(), entry.getValue()));
        }

        sectionBox.getChildren().addAll(sectionTitle, flowPane);
        parent.getChildren().add(sectionBox);
    }

    /**
     * Genera la singola tessera quadrata interattiva con l'anteprima dell'entità sponabile.
     *
     * @param key    La chiave univoca di registrazione dell'entità
     * @param record Il record di configurazione statica dei parametri dell'entità
     * @return Il nodo grafico VBox completo pronto per la visualizzazione nella griglia
     */
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
        btnSquare.getStyleClass().add("debug-spawn-btn-square");
        btnSquare.setOnAction(e -> controller.debugSpawn(key));

        String targetSpritePath = (record != null) ? record.spritePath() : null;
        boolean isTextFallback = false;

        String lowerKey = key.toLowerCase();

        if (lowerKey.equals("worm") || lowerKey.equals("iscat_worm")) {
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