package uni.gaben.iscat.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import uni.gaben.iscat.model.BestiaryModel;
import uni.gaben.iscat.universe.entity.GenericEntitySettings;
import uni.gaben.iscat.utils.ComponentsUtils;
import uni.gaben.iscat.view.components.AnimatedCanvas;
import uni.gaben.iscat.utils.SessionManager;
import uni.gaben.iscat.model.user.SessionUser;

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

    private final BestiaryModel bestiaryModel = new BestiaryModel();
    private Map<String, GenericEntitySettings> enemies = new LinkedHashMap<>();

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
        ComponentsUtils.applyIconButton(btnRandom,      "fas-dice");
        ComponentsUtils.applyIconButton(btnDescription, "fas-book");
        ComponentsUtils.applyIconButton(btnStats,       "fas-chart-bar");
        ComponentsUtils.applyIconButton(btnExtra,       "fas-info-circle");
        ComponentsUtils.applyIconButton(btnBack,        "fas-arrow-left");

        // Aggiorna la UI se si verifica un salvataggio in background o un cambio utente
        SessionManager.getInstance().saveDataProperty().addListener((obs, old, data) -> {
            refreshBestiary();
        });

        refreshBestiary();
        registerEscHandler();
    }

    /**
     * Recupera l'utente in sessione, ricarica le mappe dal database tramite BestiaryModel
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
        enemies = bestiaryModel.loadEnemies(userId);
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

        for (GenericEntitySettings enemy : enemies.values()) {
            String safeId = enemy.entityKey.toLowerCase().trim();
            boolean unlocked = bestiaryModel.isUnlocked(safeId);

            String buttonText = unlocked ? enemy.name : "???";

            Button button = new Button(buttonText);
            button.setPrefWidth(250.0);
            button.setPrefHeight(42.0);
            button.setId(safeId);

            AnimatedCanvas iconCanvas = new AnimatedCanvas(ICON_SIZE);
            iconCanvas.setFrameDuration(0.20);

            // Caricamento skin differenziato in base allo stato di sblocco
            if (unlocked) {
                iconCanvas.loadSkin(enemy.spritePath, enemy.frameW, enemy.frameH);
            } else {
                iconCanvas.loadSkin("/uni/gaben/iscat/sprites/enemies/unknown_enemy.png", 32, 32);
                button.setStyle("-fx-opacity: 0.75;");
            }

            buttonCanvases.add(iconCanvas);
            button.setGraphic(iconCanvas);
            button.setGraphicTextGap(14.0);

            ComponentsUtils.setupButtonHoverTween(button);
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
        GenericEntitySettings enemy = enemies.get(cleanId);

        if (enemy == null) {
            System.err.println("ERRORE BESTIARIO: Impossibile trovare il nemico con l'EntityKey '" + cleanId + "'!");
            return;
        }

        currentEnemyId = cleanId;

        boolean unlocked = bestiaryModel.isUnlocked(cleanId);
        String nameToShow = unlocked ? enemy.name.toUpperCase() : "??? UNKNOWN ENTITY ???";
        skinNameLabel.setText(nameToShow);

        refreshInfoZone();

        previewCanvas.setFrameDuration(0.20);
        if (unlocked) {
            previewCanvas.loadSkin(enemy.spritePath, enemy.frameW, enemy.frameH);
        } else {
            previewCanvas.loadSkin("/uni/gaben/iscat/sprites/enemies/unknown_enemy.png", 32, 32);
        }

        previewCanvas.resize(DISPLAY_SIZE);
        ComponentsUtils.playSpawnTween(previewContainer); // Effetto transizione grafica d'ingresso
    }

    /**
     * Formatta e renderizza il testo all'interno della TextArea principale.
     * Se l'entità è bloccata, maschera tutte le schede mostrando un avviso crittografato.
     * Se è sbloccata, mostra i dati strutturali o i contatori di morte legati alla mappa extra.
     */
    private void refreshInfoZone() {
        if (currentEnemyId == null) return;
        GenericEntitySettings enemy = enemies.get(currentEnemyId);
        if (enemy == null) return;

        boolean unlocked = bestiaryModel.isUnlocked(currentEnemyId);
        int currentKills = bestiaryModel.getKillCount(currentEnemyId);

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
                description.setText(enemy.description);
            }
            case STATS -> {
                rightCardHeader.setText("STATS");
                description.setText(String.format("""
                    STATISTICHE DI BASE
                    
                    ❤ Punti Vita: %.0f HP
                    ⚡ Velocità Massima: %.1f m/s
                    ✨ Ricompensa Esperienza: %d XP
                    📐 Scala Moltiplicatore: %.1fx
                    ⚓ Attrito Lineare: %.1f
                    ⚙ Massa: %.1f kg
                    💪 Forza Massima: %.1f N
                    """,
                        enemy.initLife, enemy.maxVelocity, enemy.xpReward,
                        enemy.scale, enemy.linearDamping, enemy.mass, enemy.maxForce
                ));
            }
            case EXTRA -> {
                rightCardHeader.setText("EXTRA INFO");
                description.setText(String.format("""
                    INFORMAZIONI EXTRA
                    
                    👁 Raggio di Avvistamento: %.1f unità
                    ⚔ Raggio di Combattimento: %.1f unità
                    🎯 Raggio Preferito: %.1f unità
                    ⏱ Cooldown Azione: %.1f secondi
                    🆔 ID : %s
                    📊 Counter Morti: %d
                    """,
                        enemy.detectionRange, enemy.combatRange, enemy.preferredRange,
                        enemy.actionCooldownMS/1000, enemy.entityKey, currentKills
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
                .filter(id -> !enemies.get(id).name.toUpperCase().equals(skinNameLabel.getText()))
                .toList();

        if (validIds.isEmpty()) return;

        String randomId = validIds.get(ThreadLocalRandom.current().nextInt(validIds.size()));
        showEnemyById(randomId);
    }

    @Override public void setContentRoot(StackPane contentRoot) { this.contentRoot = contentRoot; }
    @Override public Pane getRootPane() { return (Pane) btnBack.getParent().getParent(); }
    @FXML private void handleBack(ActionEvent event) { handleBack(); }
}