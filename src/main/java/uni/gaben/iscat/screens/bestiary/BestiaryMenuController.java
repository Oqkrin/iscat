package uni.gaben.iscat.screens.bestiary;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import uni.gaben.iscat.IscatNavigator;
import uni.gaben.iscat.screens.base.IscatMenuController;
import uni.gaben.iscat.view.AnimatedCanvas;
import uni.gaben.iscat.model.IscatViews;
import uni.gaben.iscat.utils.SessionManager;
import uni.gaben.iscat.screens.login.model.SessionUser;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Controller per la schermata del Bestiario.
 * Gestisce l'interfaccia grafica JavaFX per la selezione, la visualizzazione animata
 * dei render grafici (sprite) e la formattazione dei dati strutturali e di sessione dei nemici.
 */
public class BestiaryMenuController implements IscatMenuController {

    /**
     * Rappresenta le tre modalità (tab) di visualizzazione delle informazioni testuali.
     */
    private enum InfoMode {
        DESCRIPTION, STATS, EXTRA
    }

    private final BestiaryData bestiaryData = new BestiaryData();
    private Map<String, BestiaryData.Enemy> enemies = new LinkedHashMap<>();

    private static final double DISPLAY_SIZE = 160.0; // Dimensione canvas di anteprima principale
    private static final double ICON_SIZE = 32.0;     // Dimensione icone animate nei pulsanti della lista

    private String currentEnemyId = null;
    private InfoMode currentInfoMode = InfoMode.DESCRIPTION;

    @FXML private StackPane previewContainer;
    @FXML private Label skinNameLabel;
    @FXML private Label rightCardHeader;
    @FXML private TextArea description;
    @FXML private VBox enemyButtonsBox;

    @FXML private Button btnRandom;
    @FXML private Button btnDescription;
    @FXML private Button btnStats;
    @FXML private Button btnExtra;
    @FXML private Button btnBack;

    private StackPane contentRoot;
    private AnimatedCanvas previewCanvas;
    private final List<AnimatedCanvas> buttonCanvases = new ArrayList<>();

    /**
     * Inizializza i componenti grafici della UI, applica i font delle icone, registra
     * il listener sui dati di sessione ed esegue il primo caricamento del bestiario.
     */
    @FXML
    public void initialize() {
        previewCanvas = new AnimatedCanvas(DISPLAY_SIZE);
        previewContainer.getChildren().add(previewCanvas);

        description.setEditable(false);
        description.setWrapText(true);

        // Applicazione icone grafiche ai pulsanti di navigazione/tab
        applyIconButton(btnRandom,      "fas-dice");
        applyIconButton(btnDescription, "fas-book");
        applyIconButton(btnStats,       "fas-chart-bar");
        applyIconButton(btnExtra,       "fas-info-circle");
        applyIconButton(btnBack,        "fas-arrow-left");

        // Aggiorna la UI se si verifica un salvataggio in background o un cambio utente
        SessionManager.getInstance().saveDataProperty().addListener((obs, old, data) -> {
            refreshBestiary();
        });

        refreshBestiary();
        registerEscHandler();
    }

    /**
     * Recupera l'utente in sessione, ricarica le mappe dal database tramite BestiaryData
     * e rigenera i pulsanti, mantenendo la selezione sul nemico precedentemente visualizzato.
     */
    private void refreshBestiary() {
        int userId = 0;
        SessionUser user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            userId = user.id();
        }

        String previousSelection = currentEnemyId;

        // Caricamento dei record dal DB
        enemies = bestiaryData.loadEnemies(userId);
        createEnemyButtons();

        // Ripristino selezione o fallback sul primo elemento della mappa
        if (!enemies.isEmpty()) {
            if (previousSelection != null && enemies.containsKey(previousSelection)) {
                showEnemyById(previousSelection);
            } else {
                String firstEnemyId = enemies.keySet().iterator().next();
                showEnemyById(firstEnemyId);
            }
        }
    }

    /**
     * Genera dinamicamente la lista verticale di pulsanti nel VBox.
     * Applica maschere oscurate ("???") e sprite generici se l'entità risulta ancora bloccata.
     */
    private void createEnemyButtons() {
        enemyButtonsBox.getChildren().clear();
        buttonCanvases.clear();

        for (BestiaryData.Enemy enemy : enemies.values()) {
            String safeId = enemy.entityKey().toLowerCase().trim();
            boolean unlocked = bestiaryData.isUnlocked(safeId);

            String buttonText = unlocked ? enemy.name() : "???";

            Button button = new Button(buttonText);
            button.setPrefWidth(250.0);
            button.setPrefHeight(42.0);
            button.setId(safeId);

            AnimatedCanvas iconCanvas = new AnimatedCanvas(ICON_SIZE);
            iconCanvas.setFrameDuration(0.20);

            // Caricamento skin differenziato in base allo stato di sblocco
            if (unlocked) {
                iconCanvas.loadSkin(enemy.sprite(), enemy.frameW(), enemy.frameH());
            } else {
                iconCanvas.loadSkin("/uni/gaben/iscat/sprites/enemies/unknown_enemy.png", 32, 32);
                button.setStyle("-fx-opacity: 0.75;");
            }

            buttonCanvases.add(iconCanvas);
            button.setGraphic(iconCanvas);
            button.setGraphicTextGap(14.0);

            setupButtonHoverTween(button);
            button.setOnAction(e -> showEnemyById(safeId));

            enemyButtonsBox.getChildren().add(button);
        }
    }

    /**
     * Aggiorna il pannello centrale di anteprima caricando lo sprite animato su scala
     * maggiore ed innesca l'aggiornamento dei dettagli descrittivi.
     *
     * @param id La chiave del nemico selezionato.
     */
    private void showEnemyById(String id) {
        if (id == null) return;

        String cleanId = id.toLowerCase().trim();
        BestiaryData.Enemy enemy = enemies.get(cleanId);

        if (enemy == null) {
            System.err.println("ERRORE BESTIARIO: Impossibile trovare il nemico con l'EntityKey '" + cleanId + "'!");
            return;
        }

        currentEnemyId = cleanId;

        boolean unlocked = bestiaryData.isUnlocked(cleanId);
        String nameToShow = unlocked ? enemy.name().toUpperCase() : "??? UNKNOWN ENTITY ???";
        skinNameLabel.setText(nameToShow);

        refreshInfoZone();

        previewCanvas.setFrameDuration(0.20);
        if (unlocked) {
            previewCanvas.loadSkin(enemy.sprite(), enemy.frameW(), enemy.frameH());
        } else {
            previewCanvas.loadSkin("/uni/gaben/iscat/sprites/enemies/unknown_enemy.png", 32, 32);
        }

        previewCanvas.resize(DISPLAY_SIZE);
        playSpawnTween(previewContainer); // Effetto transizione grafica d'ingresso
    }

    /**
     * Formatta e renderizza il testo all'interno della TextArea principale.
     * Se l'entità è bloccata, maschera tutte le schede mostrando un avviso crittografato.
     * Se è sbloccata, mostra i dati strutturali o i contatori di morte legati alla mappa extra.
     */
    private void refreshInfoZone() {
        if (currentEnemyId == null) return;
        BestiaryData.Enemy enemy = enemies.get(currentEnemyId);
        if (enemy == null) return;

        boolean unlocked = bestiaryData.isUnlocked(currentEnemyId);
        int currentKills = bestiaryData.getExtraKillCounts().getOrDefault(currentEnemyId, 0);

        // Blocco di protezione per mostri non ancora affrontati/sconfitti
        if (!unlocked) {
            rightCardHeader.setText("LOCKED");
            description.setText("[ INFO NASCOSTE ]\n\nSconfiggi questo nemico per sbloccarlo.");
            return;
        }

        // Switch di smistamento in base alla scheda (tab) attualmente attiva
        switch (currentInfoMode) {
            case DESCRIPTION -> {
                rightCardHeader.setText("DESCRIPTION");
                description.setText(enemy.description());
            }
            case STATS -> {
                rightCardHeader.setText("STATS");
                description.setText(String.format("""
                    STATISTICHE DI BASE
                    
                    ❤ Punti Vita: %d HP
                    ⚡ Velocità Massima: %d m/s
                    ✨ Ricompensa Esperienza: %d XP
                    📐 Scala Moltiplicatore: %.1fx
                    ⚓ Attrito Lineare: %.1f
                    """,
                        enemy.initLife(), enemy.maxVelocity(), enemy.xpReward(),
                        enemy.scale(), enemy.linearDamping()
                ));
            }
            case EXTRA -> {
                rightCardHeader.setText("EXTRA INFO");
                description.setText(String.format("""
                    INFORMAZIONI EXTRA
                    
                    🧠 Profilo Comportamento IA: %s
                    👁 Raggio di Avvistamento: %d unità
                    ⚔ Raggio di Combattimento: %d unità
                    ⏱ Cooldown Attacco: %d secondi
                    🆔 ID : %s
                    📊 Counter Morti: %d
                    """,
                        enemy.behaviorType() != null ? enemy.behaviorType().toUpperCase() : "STANDARD",
                        enemy.detectionRange(), enemy.combatRange(), enemy.fireCooldownS(),
                        enemy.entityKey(), currentKills
                ));
            }
        }
    }

    @FXML private void showDescription() { currentInfoMode = InfoMode.DESCRIPTION; refreshInfoZone(); }
    @FXML private void showStats()       { currentInfoMode = InfoMode.STATS; refreshInfoZone(); }
    @FXML private void showExtra()       { currentInfoMode = InfoMode.EXTRA; refreshInfoZone(); }

    /**
     * Seleziona in modo pseudo-casuale un nemico differente da quello attualmente visualizzato.
     */
    @FXML
    private void selectRandom() {
        var validIds = enemies.keySet().stream()
                .filter(id -> !enemies.get(id).name().toUpperCase().equals(skinNameLabel.getText()))
                .toList();

        if (validIds.isEmpty()) return;

        String randomId = validIds.get(ThreadLocalRandom.current().nextInt(validIds.size()));
        showEnemyById(randomId);
    }

    @Override public void setContentRoot(StackPane contentRoot) { this.contentRoot = contentRoot; }
    @Override public Pane getRootPane() { return (Pane) btnBack.getParent().getParent(); }
    @Override public void handleBack() { IscatNavigator.getInstance().navigateWithFade(IscatViews.MAIN_MENU); }
    @FXML private void handleBack(ActionEvent event) { handleBack(); }
}