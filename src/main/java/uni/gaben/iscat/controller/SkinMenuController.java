package uni.gaben.iscat.controller;

import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.util.Duration;
import uni.gaben.iscat.controller.components.InfoCardController;
import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.model.SkinGridModel;
import uni.gaben.iscat.model.user.SessionUser;
import uni.gaben.iscat.universe.entities.EntityFactory;
import uni.gaben.iscat.universe.entities.EntityRecord;
import uni.gaben.iscat.utils.ComponentsUtils;
import uni.gaben.iscat.utils.SessionManager;
import uni.gaben.iscat.view.components.AnimatedCanvas;

import java.util.*;

public class SkinMenuController implements IscatMenuController {

    public static final double SKIN_TO_BUTTON_RATIO = 0.75; // Lowered slightly to ensure no padding clip
    @FXML public VBox skinsVbox;
    @FXML private GridPane skinGrid;
    @FXML private Label skinNameLabel;
    @FXML private StackPane skinStackPane;
    @FXML private Button confirmBtn;
    @FXML private Button randomBtn;
    @FXML private Button cancelBtn;
    @FXML private InfoCardController infoCardController;

    private final SkinGridModel gridModel = new SkinGridModel();
    private StackPane contentRoot;
    private String selectedSkinPath;
    private String selectedSkinKey = "player1";

    @FXML
    public void initialize() {
        ComponentsUtils.applyIconButton(confirmBtn, "fas-check");
        ComponentsUtils.applyIconButton(randomBtn,  "fas-dice");
        ComponentsUtils.applyIconButton(cancelBtn,  "fas-arrow-left");

        loadSkinsFromJson();

        gridModel.placementsProperty().addListener((obs, old, placements) -> rebuildGrid(placements));
        gridModel.selectedKeyProperty().addListener((obs, oldKey, newKey) -> {
            if (newKey != null && !newKey.equals(oldKey)) {
                rebuildGrid(gridModel.getPlacements());
                refreshInfoZone();
            }
        });

        // Listen directly to window height to shrink/grow dynamically
        skinStackPane.heightProperty().addListener((obs, old, newVal) -> rebuildGrid(gridModel.getPlacements()));
        skinStackPane.widthProperty().addListener((obs, old, newVal) -> rebuildGrid(gridModel.getPlacements()));

        // Run later to guarantee initial layout bounds are determined
        Platform.runLater(() -> {
            preselectSkin();
            rebuildGrid(gridModel.getPlacements());
        });

        registerEscHandler();
    }

    private void loadSkinsFromJson() {
        List<EntityRecord> skins = new ArrayList<>();
        Map<String, EntityRecord> globalCache = EntityFactory.getCache();
        for (Map.Entry<String, EntityRecord> entry : globalCache.entrySet()) {
            EntityRecord record = entry.getValue();
            String key = entry.getKey().toLowerCase().trim();
            if (record.player() != null || key.contains("player")) {
                skins.add(record);
            }
        }
        gridModel.getSkins().setAll(skins);
        gridModel.setColumns(4);
    }

    private void rebuildGrid(ObservableList<SkinGridModel.SkinPlacement> placements) {
        skinGrid.getChildren().clear();
        skinGrid.getColumnConstraints().clear();
        skinGrid.getRowConstraints().clear();

        if (placements.isEmpty()) return;

        int maxRow = 0, maxCol = 0;
        for (SkinGridModel.SkinPlacement p : placements) {
            int endRow = p.row + p.rowSpan;
            int endCol = p.col + p.colSpan;
            if (endRow > maxRow) maxRow = endRow;
            if (endCol > maxCol) maxCol = endCol;
        }

        // Calculate maximum vertical clearance space avoiding overflow
        double squareCellSize = getSquareCellSize(maxCol);

        // Apply matching constraints to enforce square grids
        for (int r = 0; r < maxRow; r++) {
            RowConstraints row = new RowConstraints();
            row.setPrefHeight(squareCellSize);
            row.setMinHeight(squareCellSize);
            row.setMaxHeight(squareCellSize);
            row.setFillHeight(true);
            skinGrid.getRowConstraints().add(row);
        }

        for (int c = 0; c < maxCol; c++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPrefWidth(squareCellSize);
            col.setMinWidth(squareCellSize);
            col.setMaxWidth(squareCellSize);
            col.setFillWidth(true);
            skinGrid.getColumnConstraints().add(col);
        }

        for (SkinGridModel.SkinPlacement p : placements) {
            double currentCellW = squareCellSize * p.colSpan;
            double currentCellH = squareCellSize * p.rowSpan;

            Button btn = createSkinButton(p.record, currentCellW, currentCellH);

            skinGrid.add(btn, p.col, p.row, p.colSpan, p.rowSpan);
            GridPane.setHgrow(btn, Priority.ALWAYS);
            GridPane.setVgrow(btn, Priority.ALWAYS);

            if (p.selected) {
                btn.getStyleClass().add("selected-skin-button");
                ScaleTransition pop = new ScaleTransition(Duration.millis(180), btn);
                pop.setToX(1.02);
                pop.setToY(1.02);
                pop.play();
            }
        }
    }

    private double getSquareCellSize(int maxCol) {
        return (skinsVbox.getWidth()-64) / maxCol;
    }

    private Button createSkinButton(EntityRecord skin, double width, double height) {
        String key = skin.entityKey();
        String path = skin.spritePath();

        double baseSize = Math.min(width, height);
        AnimatedCanvas canvas = new AnimatedCanvas(baseSize);
        canvas.loadSkin(path, skin.frameW(), skin.frameH());

        Button btn = new Button();
        btn.getStyleClass().add("skin-button");
        btn.setGraphic(canvas);
        btn.setFocusTraversable(false);
        btn.setUserData(key);

        // Force rigorous layout boundaries
        btn.setMinSize(width, height);
        btn.setPrefSize(width, height);
        btn.setMaxSize(width, height);

        btn.widthProperty().addListener((obs, old, newVal) -> {
            double size = Math.min(newVal.doubleValue(), btn.getHeight());
            canvas.resize(size * SKIN_TO_BUTTON_RATIO);
        });
        btn.heightProperty().addListener((obs, old, newVal) -> {
            double size = Math.min(btn.getWidth(), newVal.doubleValue());
            canvas.resize(size * SKIN_TO_BUTTON_RATIO);
        });

        canvas.resize(baseSize * SKIN_TO_BUTTON_RATIO);
        btn.setOnAction(e -> selectSkin(key, skin.spritePath(), skin.name()));

        return btn;
    }

    private void selectSkin(String key, String path, String name) {
        this.selectedSkinKey = key;
        this.selectedSkinPath = path;
        if (skinNameLabel != null) {
            skinNameLabel.setText(name.toUpperCase());
        }
        gridModel.setSelectedKey(key);
        refreshInfoZone();
        ComponentsUtils.playSpawnTween(skinGrid);
    }

    private void preselectSkin() {
        String currentKey = SessionManager.getPlayerSkinKey();
        EntityRecord current = EntityFactory.getCache().get(currentKey);
        if (current != null) {
            selectSkin(current.entityKey(), current.spritePath(), current.name());
        } else if (!gridModel.getSkins().isEmpty()) {
            EntityRecord first = gridModel.getSkins().getFirst();
            selectSkin(first.entityKey(), first.spritePath(), first.name());
        } else {
            this.selectedSkinKey = "player1";
            this.selectedSkinPath = "/uni/gaben/iscat/sprites/players/player1.png";
            if (skinNameLabel != null) skinNameLabel.setText("BATTLE SHIP");
            refreshInfoZone();
        }
    }

    @FXML
    private void selectRandom() {
        List<EntityRecord> skins = gridModel.getSkins();
        if (skins.isEmpty()) return;
        int idx;
        EntityRecord randomSkin;
        do {
            idx = java.util.concurrent.ThreadLocalRandom.current().nextInt(skins.size());
            randomSkin = skins.get(idx);
        } while (randomSkin.spritePath().equals(selectedSkinPath) && skins.size() > 1);
        selectSkin(randomSkin.entityKey(), randomSkin.spritePath(), randomSkin.name());
    }

    private void refreshInfoZone() {
        if (selectedSkinKey == null || infoCardController == null) return;
        EntityRecord record = EntityFactory.getCache().get(selectedSkinKey);
        if (record == null) {
            infoCardController.updateInfo("N/A", "Nessun dato caricato per " + selectedSkinKey);
        } else {
            infoCardController.updateEntityInfo(record);
        }
    }

    public void registerEscHandler() {
        skinStackPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnKeyPressed(event -> {
                    if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                        handleBack();
                    }
                });
            }
        });
    }

    @FXML
    private void handleConfirm(ActionEvent event) {
        if (selectedSkinPath != null && selectedSkinKey != null) {
            SessionManager.setPlayerSkin(selectedSkinPath);
            SessionManager.setPlayerSkinKey(selectedSkinKey);

            SessionUser user = SessionManager.getInstance().getCurrentUser();
            if (user != null) {
                IscatDB.getInstance().executeAsync(() ->
                        IscatDB.getInstance().getSettingsDAO().updatePlayerSkin(user.id(), selectedSkinKey)
                );
            }
            System.out.println("[SkinMenu] Salvata skin: " + selectedSkinKey);
        }
        handleBack();
    }

    @FXML private void handleBack(ActionEvent event) { handleBack(); }
    @Override public Pane getRootPane() { return skinStackPane; }
    @Override public void setContentRoot(StackPane contentRoot) { this.contentRoot = contentRoot; }
}