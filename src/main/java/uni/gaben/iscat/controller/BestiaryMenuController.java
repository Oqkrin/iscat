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
import uni.gaben.iscat.universe.entity.EntityRecord;
import uni.gaben.iscat.utils.ComponentsUtils;
import uni.gaben.iscat.view.components.AnimatedCanvas;
import uni.gaben.iscat.utils.SessionManager;
import uni.gaben.iscat.model.user.SessionUser;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class BestiaryMenuController implements IscatMenuController {

    private enum InfoMode {
        DESCRIPTION, STATS, EXTRA
    }

    private enum CategoryMode {
        ENEMIES, PLAYERS
    }

    private final BestiaryModel bestiaryModel = new BestiaryModel();
    private Map<String, EntityRecord> rawEnemiesMap = new LinkedHashMap<>();
    private final Map<String, EntityRecord> filteredEnemies = new LinkedHashMap<>();

    private static final double DISPLAY_SIZE = 196.0;
    private static final double ICON_SIZE = 32.0;

    private String currentEnemyId = null;
    private InfoMode currentInfoMode = InfoMode.DESCRIPTION;
    private CategoryMode currentCategory = CategoryMode.ENEMIES;

    @FXML private StackPane previewContainer;
    @FXML private Label skinNameLabel;
    @FXML private Label rightCardHeader;
    @FXML private TextArea description;
    @FXML private VBox enemyButtonsBox;
    @FXML private Label listHeaderLabel;

    @FXML private Button btnToggleCategory;
    @FXML private Button btnRandom;
    @FXML private Button btnDescription;
    @FXML private Button btnStats;
    @FXML private Button btnExtra;
    @FXML private Button btnBack;

    private StackPane contentRoot;
    private AnimatedCanvas previewCanvas;
    private final List<AnimatedCanvas> buttonCanvases = new ArrayList<>();

    @FXML
    public void initialize() {
        previewCanvas = new AnimatedCanvas(DISPLAY_SIZE);
        previewContainer.getChildren().add(previewCanvas);

        description.setEditable(false);
        description.setWrapText(true);

        ComponentsUtils.applyIconButton(btnToggleCategory, "fas-exchange-alt");
        ComponentsUtils.applyIconButton(btnRandom,         "fas-dice");
        ComponentsUtils.applyIconButton(btnDescription,    "fas-book");
        ComponentsUtils.applyIconButton(btnStats,          "fas-chart-bar");
        ComponentsUtils.applyIconButton(btnExtra,          "fas-info-circle");
        ComponentsUtils.applyIconButton(btnBack,           "fas-arrow-left");

        SessionManager.getInstance().saveDataProperty().addListener((obs, old, data) -> {
            refreshBestiary();
        });

        refreshBestiary();
    }

    private void refreshBestiary() {
        int userId = 0;
        SessionUser user = SessionManager.getInstance().getCurrentUser();
        if (user != null) {
            userId = user.id();
        }

        String previousSelection = currentEnemyId;

        rawEnemiesMap = bestiaryModel.loadEnemies(userId);

        applyFilterAndRebuildUI();

        if (!filteredEnemies.isEmpty()) {
            if (previousSelection != null && filteredEnemies.containsKey(previousSelection)) {
                showEnemyById(previousSelection);
            } else {
                String firstId = filteredEnemies.keySet().iterator().next();
                showEnemyById(firstId);
            }
        }
    }

    private void applyFilterAndRebuildUI() {
        filteredEnemies.clear();

        for (Map.Entry<String, EntityRecord> entry : rawEnemiesMap.entrySet()) {
            String key = entry.getKey().toLowerCase().trim();
            boolean isPlayerEntity = key.contains("player") || (entry.getValue().identity() != null && entry.getValue().identity().name().toLowerCase().contains("player"));

            if (currentCategory == CategoryMode.PLAYERS && isPlayerEntity) {
                filteredEnemies.put(entry.getKey(), entry.getValue());
            } else if (currentCategory == CategoryMode.ENEMIES && !isPlayerEntity) {
                filteredEnemies.put(entry.getKey(), entry.getValue());
            }
        }

        if (currentCategory == CategoryMode.ENEMIES) {
            listHeaderLabel.setText("ENEMIES");
            btnToggleCategory.setText("CHANGE TO PLAYERS");
        } else {
            listHeaderLabel.setText("PLAYERS");
            btnToggleCategory.setText("CHANGE TO ENEMIES");
        }

        createEnemyButtons();
    }

    @FXML
    private void toggleCategory() {
        currentCategory = (currentCategory == CategoryMode.ENEMIES) ? CategoryMode.PLAYERS : CategoryMode.ENEMIES;
        applyFilterAndRebuildUI();

        if (!filteredEnemies.isEmpty()) {
            showEnemyById(filteredEnemies.keySet().iterator().next());
        } else {
            skinNameLabel.setText("NO ENTITIES FOUND");
            description.setText("");
        }
    }

    private void createEnemyButtons() {
        enemyButtonsBox.getChildren().clear();
        buttonCanvases.clear();

        for (EntityRecord enemy : filteredEnemies.values()) {
            String safeId = enemy.identity().entityKey().toLowerCase().trim();
            boolean unlocked = bestiaryModel.isUnlocked(safeId);

            String buttonText = unlocked ? enemy.identity().name() : "???";

            Button button = new Button(buttonText);
            button.setPrefWidth(250.0);
            button.setPrefHeight(42.0);
            button.setId(safeId);

            AnimatedCanvas iconCanvas = new AnimatedCanvas(ICON_SIZE);
            iconCanvas.setFrameDuration(0.20);

            if (unlocked && enemy.sprite() != null) {
                iconCanvas.loadSkin(enemy.identity().name(), enemy.sprite().frameW(), enemy.sprite().frameH());
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

    private void showEnemyById(String id) {
        if (id == null) return;

        String cleanId = id.toLowerCase().trim();
        EntityRecord enemy = filteredEnemies.get(cleanId);

        if (enemy == null) return;

        currentEnemyId = cleanId;

        boolean unlocked = bestiaryModel.isUnlocked(cleanId);
        String nameToShow = unlocked ? enemy.identity().name().toUpperCase() : "??? UNKNOWN ENTITY ???";
        skinNameLabel.setText(nameToShow);

        refreshInfoZone();

        previewCanvas.setFrameDuration(0.1);
        if (unlocked && enemy.sprite() != null) {
            previewCanvas.loadSkin(enemy.identity().name(), enemy.sprite().frameW(), enemy.sprite().frameH());
        } else {
            previewCanvas.loadSkin("/uni/gaben/iscat/sprites/enemies/unknown_enemy.png", 32, 32);
        }

        previewCanvas.resize(DISPLAY_SIZE);
        ComponentsUtils.playSpawnTween(previewContainer);
    }

    private void refreshInfoZone() {
        if (currentEnemyId == null) return;
        EntityRecord enemy = filteredEnemies.get(currentEnemyId);
        if (enemy == null) return;

        boolean unlocked = bestiaryModel.isUnlocked(currentEnemyId);
        int currentKills = bestiaryModel.getKillCount(currentEnemyId);

        if (!unlocked) {
            rightCardHeader.setText("LOCKED");
            description.setText("[ INFO NASCOSTE ]\n\nSconfiggi o sblocca questa entità per visualizzarla.");
            return;
        }

        switch (currentInfoMode) {
            case DESCRIPTION -> {
                rightCardHeader.setText("DESCRIPTION");
                description.setText(enemy.identity().description());
            }
            case STATS -> {
                rightCardHeader.setText("STATS");
                description.setText(String.format("""
                    STATISTICHE DI BASE
                    
                    ❤ Punti Vita: %.0f HP
                    ⚡ Velocità Massima: %.1f m/s
                    ✨ Ricompensa Esperienza: %.0f XP
                    📐 Scala Moltiplicatore: %.1fx
                    ⚓ Attrito Lineare: %.1f
                    ⚙ Massa: %.1f kg
                    💪 Forza Massima: %.1f N
                    """,
                        enemy.endurance() != null ? enemy.endurance().initLife() : 0, 
                        enemy.dynamics() != null ? enemy.dynamics().maxVelocity() : 0,
                        enemy.xp() != null ? enemy.xp().xpReward() : 0.0,
                        enemy.sprite() != null ? enemy.sprite().scale() : 1, 
                        enemy.physics() != null ? enemy.physics().linearDamping() : 0, 
                        enemy.physics() != null ? enemy.physics().mass() : 0, 
                        enemy.dynamics() != null ? enemy.dynamics().maxForce() : 0
                ));
            }
            case EXTRA -> {
                rightCardHeader.setText("EXTRA INFO");

                double cooldownSeconds = (enemy.dynamics() != null && enemy.dynamics().actionCooldownSec() > 0) ? enemy.dynamics().actionCooldownSec() : 0;

                description.setText(String.format("""
                    INFORMAZIONI EXTRA
                    
                    👁 Raggio di Avvistamento: %.1f unità
                    ⚔ Raggio di Combattimento: %.1f unità
                    🎯 Raggio Preferito: %.1f unità
                    ⏱ Cooldown Azione: %.1f secondi
                    🆔 ID : %s
                    📊 Totale Uccisi: %d
                    """,
                        0.0, 0.0, 0.0,
                        cooldownSeconds, enemy.identity().entityKey(), currentKills
                ));
            }
        }
    }

    @FXML private void showDescription() { currentInfoMode = InfoMode.DESCRIPTION; refreshInfoZone(); }
    @FXML private void showStats()       { currentInfoMode = InfoMode.STATS; refreshInfoZone(); }
    @FXML private void showExtra()       { currentInfoMode = InfoMode.EXTRA; refreshInfoZone(); }

    @FXML
    private void selectRandom() {
        var validIds = filteredEnemies.keySet().stream()
                .filter(id -> !filteredEnemies.get(id).identity().name().toUpperCase().equals(skinNameLabel.getText()))
                .toList();

        if (validIds.isEmpty()) return;

        String randomId = validIds.get(ThreadLocalRandom.current().nextInt(validIds.size()));
        showEnemyById(randomId);
    }

    @Override public void setContentRoot(StackPane contentRoot) { this.contentRoot = contentRoot; }
    @Override public Pane getRootPane() { return (Pane) btnBack.getParent().getParent(); }
    @FXML private void handleBack(ActionEvent event) { handleBack(); }
}