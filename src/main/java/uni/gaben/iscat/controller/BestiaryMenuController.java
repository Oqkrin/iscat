package uni.gaben.iscat.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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
    @FXML private VBox enemyButtonsBox;
    @FXML private Label listHeaderLabel;

    @FXML private Button btnToggleCategory;
    @FXML private Button btnRandom;
    @FXML private Button btnDescription;
    @FXML private Button btnStats;
    @FXML private Button btnExtra;
    @FXML private Button btnBack;

    @FXML private InfoCardController infoCardController;

    private StackPane contentRoot;
    private AnimatedCanvas previewCanvas;
    private final List<AnimatedCanvas> buttonCanvases = new ArrayList<>();

    @FXML
    public void initialize() {
        previewCanvas = new AnimatedCanvas(DISPLAY_SIZE);
        previewContainer.getChildren().add(previewCanvas);

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

    /**
     * Verifica in modo centralizzato se l'entità corrisponde a una navicella del giocatore.
     */
    private boolean isPlayer(String key, EntityRecord record) {
        if (key == null) return false;
        String lowerKey = key.toLowerCase().trim();
        return lowerKey.contains("player")
                || (record != null && record.name() != null && record.name().toLowerCase().contains("player"))
                || (record != null && record.player() != null);
    }

    private void applyFilterAndRebuildUI() {
        filteredEnemies.clear();

        for (Map.Entry<String, EntityRecord> entry : rawEnemiesMap.entrySet()) {
            boolean isPlayerEntity = isPlayer(entry.getKey(), entry.getValue());

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
            if (infoCardController != null) {
                infoCardController.updateInfo("", "");
            }
        }
    }

    private void createEnemyButtons() {
        enemyButtonsBox.getChildren().clear();
        buttonCanvases.clear();

        for (EntityRecord enemy : filteredEnemies.values()) {
            String safeId = enemy.entityKey().toLowerCase().trim();

            // Se è un player, ignoriamo il database dei salvataggi: è sempre sbloccato.
            boolean unlocked = isPlayer(safeId, enemy) || bestiaryModel.isUnlocked(safeId);

            String buttonText = unlocked ? enemy.name() : "???";

            Button button = new Button(buttonText);
            button.setPrefWidth(250.0);
            button.setPrefHeight(42.0);
            button.setId(safeId);

            AnimatedCanvas iconCanvas = new AnimatedCanvas(ICON_SIZE);
            iconCanvas.setFrameDuration(0.20);

            if (unlocked) {
                iconCanvas.loadSkin(enemy.spritePath(), enemy.frameW(), enemy.frameH());
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

        boolean unlocked = isPlayer(cleanId, enemy) || bestiaryModel.isUnlocked(cleanId);
        String nameToShow = unlocked ? enemy.name().toUpperCase() : "??? UNKNOWN ENTITY ???";
        skinNameLabel.setText(nameToShow);

        refreshInfoZone();

        previewCanvas.setFrameDuration(0.1);
        if (unlocked) {
            previewCanvas.loadSkin(enemy.spritePath(), enemy.frameW(), enemy.frameH());
        } else {
            previewCanvas.loadSkin("/uni/gaben/iscat/sprites/enemies/unknown_enemy.png", 32, 32);
        }

        previewCanvas.resize(DISPLAY_SIZE);
        ComponentsUtils.playSpawnTween(previewContainer);
    }

    private void refreshInfoZone() {
        if (currentEnemyId == null || infoCardController == null) return;
        EntityRecord enemy = filteredEnemies.get(currentEnemyId);
        if (enemy == null) return;

        boolean unlocked = isPlayer(currentEnemyId, enemy) || bestiaryModel.isUnlocked(currentEnemyId);
        int currentKills = bestiaryModel.getKillCount(currentEnemyId);

        if (!unlocked) {
            infoCardController.updateInfo("LOCKED", "[ INFO NASCOSTE ]\n\nSconfiggi o sblocca questa entità per visualizzarla.");
            return;
        }

        switch (currentInfoMode) {
            case DESCRIPTION -> {
                infoCardController.updateInfo("DESCRIPTION", enemy.description());
            }
            case STATS -> {
                String statsText = String.format("""
                    STATISTICHE DI BASE
                    
                    ❤ Punti Vita: %.0f HP
                    ⚡ Velocità Massima: %.1f m/s
                    ✨ Ricompensa Esperienza: %d XP
                    📐 Scala Moltiplicatore: %.1fx
                    ⚓ Attrito Lineare: %.1f
                    ⚙ Massa: %.1f kg
                    💪 Forza Massima: %.1f N
                    """,
                        enemy.initLife(), enemy.maxVelocity(), enemy.xpReward(),
                        enemy.scale(), enemy.linearDamping(), enemy.mass(), enemy.maxForce()
                );
                infoCardController.updateInfo("STATS", statsText);
            }
            case EXTRA -> {
                double cooldownSeconds = (enemy.actionCooldownSec() > 0) ? enemy.actionCooldownSec() : (enemy.actionCooldownSec() / 1000.0);

                String extraText = isPlayer(currentEnemyId, enemy) ?
                        String.format("""
                    INFORMAZIONI ABILITÀ
                    
                    ⏱ Cooldown Attacco Base: %.2f secondi
                    🆔 ID Skin : %s
                    """, enemy.actionCooldownSec(), enemy.entityKey())
                        :
                        String.format("""
                    INFORMAZIONI EXTRA
                    
                    👁 Raggio di Avvistamento: %.1f unità
                    ⚔ Raggio di Combattimento: %.1f unità
                    🎯 Raggio Preferito: %.1f unità
                    ⏱ Cooldown Azione: %.1f secondi
                    🆔 ID : %s
                    📊 Totale Uccisi: %d
                    """,
                                enemy.detectionRange(), enemy.combatRange(), enemy.preferredRange(),
                                cooldownSeconds, enemy.entityKey(), currentKills);

                infoCardController.updateInfo("EXTRA INFO", extraText);
            }
        }
    }

    @FXML private void showDescription() { currentInfoMode = InfoMode.DESCRIPTION; refreshInfoZone(); }
    @FXML private void showStats()       { currentInfoMode = InfoMode.STATS; refreshInfoZone(); }
    @FXML private void showExtra()       { currentInfoMode = InfoMode.EXTRA; refreshInfoZone(); }

    @FXML
    private void selectRandom() {
        var validIds = filteredEnemies.keySet().stream()
                .filter(id -> !filteredEnemies.get(id).name().toUpperCase().equals(skinNameLabel.getText()))
                .toList();

        if (validIds.isEmpty()) return;

        String randomId = validIds.get(ThreadLocalRandom.current().nextInt(validIds.size()));
        showEnemyById(randomId);
    }

    @Override public void setContentRoot(StackPane contentRoot) { this.contentRoot = contentRoot; }
    @Override public Pane getRootPane() { return (Pane) btnBack.getParent().getParent(); }
    @FXML private void handleBack(ActionEvent event) { handleBack(); }
}