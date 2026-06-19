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
 * <p>
 * Questa barra supporta il ridimensionamento dinamico, il caricamento di
 * texture personalizzate e la gestione manuale di coordinate di viewport
 * specifiche per entità con fogli di sprite complessi.
 * </p>
 * * @author gaben
 * @version 1.2
 */
public class DebugToolBarSpawner extends VBox {

    /**
     * Insieme di entità che devono essere nascoste dall'interfaccia di spawn
     * poiché non ha senso generarle manualmente (es. il giocatore stesso o i proiettili).
     */
    private static final Set<UniverseSpawnable> HIDDEN_SPAWNABLES = Set.of(
            UniverseSpawnable.PLAYER,
            UniverseSpawnable.PROJECTILE
    );

    /** Cache interna per memorizzare le immagini caricate ed evitare letture IO ridondanti. */
    private final Map<String, Image> imageCache = new HashMap<>();

    /** Contenitore a flusso dinamico che ospita le schede quadrate di ogni entità. */
    private final FlowPane spawnContainer;

    /** Pannello di scorrimento verticale che racchiude la lista delle entità. */
    private final ScrollPane scroll;

    /** Riferimento al controller principale del gioco per inviare le richieste di spawn. */
    private final GameController controller;

    /**
     * Costruisce una nuova istanza di {@code DebugToolBarSpawner}.
     * Configura il layout per allinearsi perfettamente alle proporzioni degli altri pannelli di debug.
     *
     * @param controller Il controller logico del gioco.
     * @param onBack     Azione di callback da eseguire quando viene premuto il pulsante "Indietro".
     */
    public DebugToolBarSpawner(GameController controller, Runnable onBack) {
        super(12);
        setPadding(new Insets(12, 20, 20, 20));
        setAlignment(Pos.TOP_CENTER);

        this.controller = controller;
        this.spawnContainer = new FlowPane(15, 15);
        this.scroll = new ScrollPane();

        // Configurazione pulsante di ritorno
        Button btnBack = new Button("← INDIETRO");
        btnBack.setFocusTraversable(false);
        CssHelper.stilePulsanteMenu(btnBack);
        CssHelper.testoSecondario(btnBack);
        btnBack.setStyle(btnBack.getStyle() + "; -fx-font-size: 11px; -fx-padding: 4 12;");
        btnBack.setOnAction(e -> onBack.run());

        HBox topBar = new HBox(btnBack);
        topBar.setAlignment(Pos.TOP_LEFT);

        // Titolo della sezione
        Label mainTitle = new Label("SPAWNABLE LIST (CLICK TO GENERATE)");
        CssHelper.testoPrimario(mainTitle);
        mainTitle.setStyle(mainTitle.getStyle() + "; -fx-font-weight: bold; -fx-font-size: 14px;");

        spawnContainer.setAlignment(Pos.TOP_LEFT);
        spawnContainer.setPadding(new Insets(10, 0, 10, 0));

        Map<String, EntityRecord> cacheMap = EntityFactory.getCache();

        // 1. Popolamento tramite l'enum standard UniverseSpawnable
        for (UniverseSpawnable spawnable : UniverseSpawnable.values()) {
            if (HIDDEN_SPAWNABLES.contains(spawnable)) continue;
            EntityRecord record = cacheMap != null ? cacheMap.get(spawnable.name().toLowerCase()) : null;
            if (record == null && cacheMap != null) {
                record = cacheMap.get(spawnable.name());
            }
            spawnContainer.getChildren().add(createSquareSpawnCard(spawnable.name(), record));
        }

        // 2. Popolamento per le entità custom registrate nella factory non presenti nell'enum
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

        // Configurazione del contenitore di scorrimento per preservare le proporzioni verticali (Y)
        scroll.setContent(spawnContainer);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-padding: 0;");

        // Forza il ScrollPane a espandersi occupando lo spazio Y stanziato nella GameView
        scroll.setFitToHeight(true);
        scroll.prefHeightProperty().bind(spawnContainer.heightProperty());

        VBox.setVgrow(scroll, Priority.ALWAYS);
        getChildren().addAll(topBar, mainTitle, scroll);
    }

    /**
     * Crea un box grafico verticale contenente il pulsante di generazione (con icona o texture)
     * e l'etichetta testuale descrittiva dell'entità corrente.
     * Gestisce i casi speciali come Worm, Heart, Blackhole, Asteroid e Iscat Master.
     *
     * @param key    La chiave univoca identificativa del comando di spawn.
     * @param record Il record contenente i dati di inizializzazione e di configurazione della sprite.
     * @return Un {@link VBox} formattato rappresentante la tessera di spawn.
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
        btnSquare.setStyle(btnSquare.getStyle() + "; -fx-background-radius: 8; -fx-border-radius: 8;");
        btnSquare.setOnAction(e -> controller.debugSpawn(key));

        String targetSpritePath = (record != null) ? record.spritePath() : null;
        boolean isTextFallback = false;

        // --- GESTIONE DEI CASI SPECIALI HARDCODED ---
        String lowerKey = key.toLowerCase();
        if (lowerKey.contains("worm")) {
            // Reindirizza la ricerca grafica alla testa del verme per coerenza estetica
            Map<String, EntityRecord> cacheMap = EntityFactory.getCache();
            if (cacheMap != null && cacheMap.containsKey("iscat_worm_head")) {
                record = cacheMap.get("iscat_worm_head");
                targetSpritePath = record.spritePath();
            }
        } else if (lowerKey.contains("heart")) {
            // Percorso fisso dell'asset del cuore curativo
            targetSpritePath = "/uni/gaben/iscat/sprites/boosts/heart.png";
        } else if (lowerKey.contains("blackhole")) {
            // Percorso fisso per l'immagine del buco nero
            targetSpritePath = "/uni/gaben/iscat/sprites/other/blackhole.png";
        } else if (lowerKey.contains("asteroid")) {
            // Percorso fisso per l'immagine statica dell'asteroide
            targetSpritePath = "/uni/gaben/iscat/sprites/other/asteroid.png";
        }

        // Caricamento e rendering dell'immagine dell'entità
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
                        // Caso speciale: ISCAT MASTER richiede la colonna 0, riga 1 dello spritesheet
                        if (lowerKey.contains("master")) {
                            view.setViewport(new Rectangle2D(0, record.frameH(), record.frameW(), record.frameH()));
                        } else {
                            // Viewport standard (colonna 0, riga 0)
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

        // Testo di fallback sicuro qualora gli asset PNG mancassero o fallissero il caricamento
        if (isTextFallback) {
            Label textLabel = new Label("[+]");
            CssHelper.testoPrimario(textLabel);
            textLabel.setStyle(textLabel.getStyle() + "; -fx-font-size: 16px; -fx-font-weight: bold;");
            textLabel.setAlignment(Pos.CENTER);
            btnSquare.setGraphic(textLabel);
        }

        // Configurazione delle informazioni di riepilogo al passaggio del mouse
        if (record != null && record.name() != null) {
            btnSquare.setTooltip(new Tooltip(record.name() + "\nHP: " + record.initLife() + "\nXP: " + record.xpReward()));
        }

        // Pulizia e formattazione del nome visualizzato sotto il pulsante
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