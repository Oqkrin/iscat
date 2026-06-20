package uni.gaben.iscat.controller.menus;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import uni.gaben.iscat.controller.components.InfoCardController;
import uni.gaben.iscat.controller.interfaces.IscatMenuController;
import uni.gaben.iscat.model.BestiaryModel;
import uni.gaben.iscat.universe.entities.EntityFactory;
import uni.gaben.iscat.universe.entities.EntityRecord;
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

    private enum CategoryMode {
        ENEMIES, PLAYERS
    }

    private final BestiaryModel bestiaryModel = new BestiaryModel();
    private Map<String, EntityRecord> rawEnemiesMap = new LinkedHashMap<>();
    private final Map<String, EntityRecord> filteredEnemies = new LinkedHashMap<>();

    private static final double DISPLAY_SIZE = 196.0;
    private static final double ICON_SIZE = 32.0;

    private String currentEnemyId = null;
    private CategoryMode currentCategory = CategoryMode.ENEMIES;

    @FXML private StackPane previewContainer;
    @FXML private Label skinNameLabel;
    @FXML private VBox enemyButtonsBox;
    @FXML private Label listHeaderLabel;

    @FXML private Button btnToggleCategory;
    @FXML private Button btnRandom;
    @FXML private Button btnBack;

    @FXML private InfoCardController infoCardController;

    private StackPane viewRoot;
    private AnimatedCanvas previewCanvas;
    private final List<AnimatedCanvas> buttonCanvases = new ArrayList<>();

    @FXML
    public void initialize() {
        Platform.runLater(EntityFactory::ensureCacheLoaded);
        previewCanvas = new AnimatedCanvas(DISPLAY_SIZE);
        previewContainer.getChildren().add(previewCanvas);

        ComponentsUtils.applyIconButton(btnToggleCategory, "fas-exchange-alt");
        ComponentsUtils.applyIconButton(btnRandom,         "fas-dice");
        ComponentsUtils.applyIconButton(btnBack,           "fas-arrow-left");

        SessionManager.getInstance().saveDataProperty().addListener((obs, old, data) -> {
            refreshBestiary();
        });

        refreshBestiary();
        registerEscHandler();
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

    /**
     * Filtra le entità in base alla categoria attiva e le ORDINA secondo 'bestiaryOrder'.
     */
    private void applyFilterAndRebuildUI() {
        filteredEnemies.clear();

        // Estrarre i record che corrispondono ai filtri di categoria correnti
        List<EntityRecord> tempList = new ArrayList<>();
        for (Map.Entry<String, EntityRecord> entry : rawEnemiesMap.entrySet()) {
            boolean isPlayerEntity = isPlayer(entry.getKey(), entry.getValue());

            if (currentCategory == CategoryMode.PLAYERS && isPlayerEntity) {
                tempList.add(entry.getValue());
            } else if (currentCategory == CategoryMode.ENEMIES && !isPlayerEntity) {
                tempList.add(entry.getValue());
            }
        }

        // Ordinamento effettivo tramite bestiaryOrder
        tempList.sort((a, b) -> {
            int orderA = (a.bestiaryOrder() != null) ? a.bestiaryOrder() : 0;
            int orderB = (b.bestiaryOrder() != null) ? b.bestiaryOrder() : 0;
            return Integer.compare(orderA, orderB);
        });

        // Ripopolare la LinkedHashMap (che preserva l'ordine di inserimento)
        for (EntityRecord record : tempList) {
            filteredEnemies.put(record.entityKey().toLowerCase().trim(), record);
        }

        // Aggiornamento etichette UI
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

        if (!unlocked) {
            infoCardController.updateInfo("LOCKED", "[ INFO NASCOSTE ]\n\nSconfiggi o sblocca questa entità per visualizzarla.");
            return;
        }

        infoCardController.updateEntityInfo(enemy);
    }

    @FXML
    private void selectRandom() {
        var validIds = filteredEnemies.keySet().stream()
                .filter(id -> !filteredEnemies.get(id).name().toUpperCase().equals(skinNameLabel.getText()))
                .toList();

        if (validIds.isEmpty()) return;

        String randomId = validIds.get(ThreadLocalRandom.current().nextInt(validIds.size()));
        showEnemyById(randomId);
    }

    @Override public void setPointerToView(StackPane pointer) { this.viewRoot = pointer; }
    @Override public Pane getRootPane() { return (Pane) btnBack.getParent().getParent(); }
    @FXML private void handleBack(ActionEvent event) { handleBack(); }
}