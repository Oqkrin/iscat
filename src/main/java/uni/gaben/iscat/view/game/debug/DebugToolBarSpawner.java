package uni.gaben.iscat.view.game.debug;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import uni.gaben.iscat.IscatSettings;
import uni.gaben.iscat.controller.game.GameController;
import uni.gaben.iscat.universe.spawn.UniverseSpawnable;
import uni.gaben.iscat.universe.entities.parsed.EntityFactory;
import uni.gaben.iscat.universe.entities.parsed.EntityRecord;
import uni.gaben.iscat.utils.design.CssHelper;
import uni.gaben.iscat.utils.design.ScalareAureo;
import uni.gaben.iscat.view.components.AnimatedCanvas;

import java.io.InputStream;
import java.util.*;

/**
 * Interfaccia per la generazione forzata delle entità a schermo (Spawner) in modalità debug.
 * Suddivide e organizza le entità disponibili in categorie logiche (Speciali, Nemici, Giocatori, Personalizzati).
 */
public class DebugToolBarSpawner extends VBox {

    private static final Set<UniverseSpawnable> HIDDEN_SPAWNABLES = Set.of(
            UniverseSpawnable.PLAYER,
            UniverseSpawnable.PROJECTILE
    );
    public static final String HEART_PNG = "/uni/gaben/iscat/sprites/boosts/heart.png";
    public static final String BLACKHOLE_PNG = "/uni/gaben/iscat/sprites/other/blackhole.png";
    public static final String ASTEROID_PNG = "/uni/gaben/iscat/sprites/other/asteroid.png";

    private final GameController controller;
    // Cache per le immagini singole (hardcoded)
    private final Map<String, Image> imageCache = new HashMap<>();

    /**
     * Costruisce il pannello dello spawner raccogliendo le entità registrate e distribuendole nelle rispettive macro-sezioni.
     *
     * @param controller Il controller logico del gioco.
     * @param onBack     Callback per gestire il ritorno al menu precedente.
     */
    public DebugToolBarSpawner(GameController controller, Runnable onBack) {
        final double SU = IscatSettings.STANDARD_UNIT;

        setSpacing(SU / 2);
        setPadding(new Insets(SU / 2));
        setAlignment(Pos.TOP_CENTER);
        getStyleClass().add("debug-tool-bar");

        this.controller = controller;

        VBox mainCategoriesLayout = new VBox(SU / 2);
        mainCategoriesLayout.setPadding(new Insets(SU / 4, SU / 2, SU / 4, SU / 2));

        ScrollPane scroll = new ScrollPane();
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setFitToWidth(true);
        scroll.getStyleClass().add("debug-spawner-scroll");

        Button btnBack = new Button("← INDIETRO");
        btnBack.setFocusTraversable(false);
        CssHelper.stilePulsanteMenu(btnBack);
        CssHelper.testoSecondario(btnBack);
        btnBack.getStyleClass().add("debug-btn-back");
        btnBack.setPadding(new Insets(SU / 4, SU / 2, SU / 4, SU / 2));
        btnBack.setOnAction(e -> onBack.run());

        HBox topBar = new HBox(btnBack);
        topBar.setAlignment(Pos.TOP_LEFT);

        // Gather entities
        Map<String, EntityRecord> allUniqueEntities = getAllUniqueEntities();
        List<Map.Entry<String, EntityRecord>> specialList = new ArrayList<>();
        List<Map.Entry<String, EntityRecord>> enemiesList = new ArrayList<>();
        List<Map.Entry<String, EntityRecord>> playersList = new ArrayList<>();
        List<Map.Entry<String, EntityRecord>> customList = new ArrayList<>();

        for (Map.Entry<String, EntityRecord> entry : allUniqueEntities.entrySet()) {
            String lowerKey = entry.getKey().toLowerCase();
            EntityRecord record = entry.getValue();
            boolean isPlayer = record != null && record.player() != null;
            boolean isCustom = isCustom(entry.getKey());
            boolean isWormComponent = lowerKey.contains("head") || lowerKey.contains("body") || lowerKey.contains("tail");

            if (isCustom) {
                customList.add(entry);
                continue;
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

        @SuppressWarnings("SortedCollectionWithNonComparableElements")
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
        getChildren().addAll(topBar, scroll);
    }

    /** Recupera e unisce in una mappa ordinata tutte le entità univoche estratte dai file di configurazione e dall'enum di spawn. */
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

    /** Determina se la chiave passata fa riferimento a un'entità definita all'interno del percorso dei contenuti personalizzati. */
    private boolean isCustom(String key) {
        String origin = EntityFactory.getOriginPath(key);
        return origin != null && origin.toLowerCase().contains("custom");
    }

    /** Genera graficamente una macro-categoria con una griglia a 8 colonne per ospitare le schede di spawn. */
    private void creaSezioneCategoria(VBox parent, String titolo, List<Map.Entry<String, EntityRecord>> elementi) {
        if (elementi.isEmpty()) return;
        final double SU = IscatSettings.STANDARD_UNIT;

        VBox sectionBox = new VBox(SU / 4);
        sectionBox.setFillWidth(true);

        Label sectionTitle = new Label(titolo);
        CssHelper.testoPrimario(sectionTitle);
        sectionTitle.getStyleClass().add("debug-title");
        sectionTitle.setPadding(new Insets(SU / 2, 0, SU / 8, 0));
        sectionTitle.setMaxWidth(Double.MAX_VALUE);
        sectionTitle.setAlignment(Pos.CENTER_LEFT);

        GridPane grid = new GridPane();
        grid.setHgap(SU / 3);
        grid.setVgap(SU / 3);
        grid.setAlignment(Pos.TOP_LEFT);
        grid.setMaxWidth(Double.MAX_VALUE);

        int numCols = 8;
        for (int i = 0; i < numCols; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(100.0 / numCols);
            col.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(col);
        }

        int row = 0, col = 0;
        for (Map.Entry<String, EntityRecord> entry : elementi) {
            VBox card = createSquareSpawnCard(entry.getKey(), entry.getValue(), grid);
            grid.add(card, col, row);
            GridPane.setHgrow(card, Priority.ALWAYS);
            GridPane.setVgrow(card, Priority.ALWAYS);
            col++;
            if (col >= numCols) {
                col = 0;
                row++;
            }
        }

        sectionBox.getChildren().addAll(sectionTitle, grid);
        parent.getChildren().add(sectionBox);
    }

    /** Instanzia una scheda quadrata per il singolo elemento, configurando l'anteprima dello sprite (statico o animato) e il tooltip informativo. */
    private VBox createSquareSpawnCard(String key, EntityRecord entity, GridPane parentGrid) {
        final double SU = IscatSettings.STANDARD_UNIT;

        VBox card = new VBox(SU / 4);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(SU / 4));
        card.setId(key);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setMaxHeight(Double.MAX_VALUE);

        Button btnSquare = new Button();
        btnSquare.setFocusTraversable(false);
        CssHelper.stilePulsanteMenu(btnSquare);
        btnSquare.getStyleClass().add("debug-spawn-btn-square");
        btnSquare.setPadding(new Insets(SU / 4));
        btnSquare.setOnAction(e -> controller.debugSpawn(key));
        btnSquare.setMaxWidth(Double.MAX_VALUE);
        btnSquare.setMaxHeight(Double.MAX_VALUE);

        String lowerKey = key.toLowerCase();

        // --- 1) Gestione hardcoded (heart, blackhole, asteroid) con ImageView ---
        String hardcodedPath = null;
        if (lowerKey.contains("heart")) {
            hardcodedPath = HEART_PNG;
        } else if (lowerKey.contains("blackhole")) {
            hardcodedPath = BLACKHOLE_PNG;
        } else if (lowerKey.contains("asteroid")) {
            hardcodedPath = ASTEROID_PNG;
        }

        if (hardcodedPath != null) {
            // Carica l'immagine singola
            Image img = imageCache.computeIfAbsent(hardcodedPath, path -> {
                try (InputStream is = getClass().getResourceAsStream(path)) {
                    if (is != null) return new Image(is);
                } catch (Exception e) {
                    System.err.println("[DebugToolBar] Failed to load hardcoded image: " + path);
                }
                return null;
            });

            if (img != null && !img.isError()) {
                ImageView view = new ImageView(img);
                view.setPreserveRatio(true);
                view.setSmooth(false);
                // Bind dimensioni al 60% della larghezza della card
                view.fitWidthProperty().bind(card.widthProperty().multiply(0.6));
                view.fitHeightProperty().bind(view.fitWidthProperty());
                btnSquare.setGraphic(view);
            } else {
                // Fallback a testo
                Label textLabel = new Label("[+]");
                CssHelper.testoPrimario(textLabel);
                textLabel.getStyleClass().add("debug-spawn-fallback-text");
                textLabel.setAlignment(Pos.CENTER);
                btnSquare.setGraphic(textLabel);
            }
        } else {
            // --- 2) Entità normali → AnimatedCanvas ---
            AnimatedCanvas spriteCanvas = null;
            String spritePath = null;
            int frameW = 0, frameH = 0;

            if (entity != null && entity.spritePath() != null && !entity.spritePath().isBlank()) {
                spritePath = entity.spritePath();
                frameW = entity.frameW();
                frameH = entity.frameH();
            } else if (lowerKey.equals("worm") || lowerKey.equals("iscat_worm")) {
                // Worm usa la testa come icona
                Map<String, EntityRecord> cacheMap = EntityFactory.getCache();
                if (cacheMap.containsKey("iscat_worm_head")) {
                    EntityRecord head = cacheMap.get("iscat_worm_head");
                    if (head != null) {
                        spritePath = head.spritePath();
                        frameW = head.frameW();
                        frameH = head.frameH();
                    }
                }
            }

            if (spritePath != null && !spritePath.isBlank()) {
                try {
                    spriteCanvas = new AnimatedCanvas();
                    if (frameW > 0 && frameH > 0) {
                        spriteCanvas.loadSkin(spritePath, frameW, frameH);
                    } else {
                        // Fallback: carica come immagine singola? Meglio usare ImageView?
                        // Per sicurezza proviamo a caricare con 0,0 (potrebbe fallire)
                        spriteCanvas.loadSkin(spritePath, 0, 0);
                    }
                    spriteCanvas.setFrameDuration(0.1);
                } catch (Exception e) {
                    System.err.println("[DebugToolBar] Failed to load sprite: " + spritePath);
                    spriteCanvas = null;
                }
            }

            if (spriteCanvas != null) {
                btnSquare.setGraphic(spriteCanvas);
                AnimatedCanvas finalSpriteCanvas = spriteCanvas;
                card.widthProperty().addListener((observable, oldValue, newValue) ->
                        finalSpriteCanvas.resize(ScalareAureo.phiMinore(newValue.doubleValue()))
                );
            } else {
                // Fallback a testo
                Label textLabel = new Label("[+]");
                CssHelper.testoPrimario(textLabel);
                textLabel.getStyleClass().add("debug-spawn-fallback-text");
                textLabel.setAlignment(Pos.CENTER);
                btnSquare.setGraphic(textLabel);
            }
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
        nameLabel.setPadding(new Insets(SU / 8, 0, 0, 0));

        card.getChildren().addAll(btnSquare, nameLabel);
        return card;
    }
}