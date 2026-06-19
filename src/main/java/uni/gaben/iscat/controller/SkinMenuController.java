package uni.gaben.iscat.controller;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
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

    public static final double SKIN_TO_BUTTON_RATIO = 0.75;
    private static final int RANDOM_INDEX = -2;   // matches model constant

    @FXML public VBox skinsVbox;
    @FXML private GridPane skinGrid;
    @FXML private StackPane skinStackPane;
    @FXML private Button confirmBtn;
    // randomBtn removed from FXML – we'll use the grid cell instead
    @FXML private Button cancelBtn;
    @FXML private InfoCardController infoCardController;

    private final SkinGridModel gridModel = new SkinGridModel();
    private final Random rng = new Random();   // dedicated Random instance
    private StackPane contentRoot;
    private String selectedSkinPath;
    private String selectedSkinKey = "player1";

    private boolean rebuilding = false;

    @FXML
    public void initialize() {
        ComponentsUtils.applyIconButton(confirmBtn, "fas-check");
        ComponentsUtils.applyIconButton(cancelBtn, "fas-arrow-left");

        loadSkinsFromJson();

        gridModel.placementsProperty().addListener((obs, old, placements) -> rebuildGrid(placements));
        gridModel.selectedKeyProperty().addListener((obs, oldKey, newKey) -> {
            if (newKey != null && !newKey.equals(oldKey)) {
                refreshInfoZone();
            }
        });

        // Wait for the grid to have actual size before first rebuild
        skinGrid.widthProperty().addListener((obs, o, n) -> {
            if (n.doubleValue() > 0 && skinGrid.getHeight() > 0)
                rebuildGrid(gridModel.getPlacements());
        });
        skinGrid.heightProperty().addListener((obs, o, n) -> {
            if (n.doubleValue() > 0 && skinGrid.getWidth() > 0)
                rebuildGrid(gridModel.getPlacements());
        });

        // Column count adaptation when the whole view resizes
        skinStackPane.widthProperty().addListener((obs, o, n) -> {
            if (n.doubleValue() > 0) adjustColumnsAndRebuild();
        });

        preselectSkin();   // safe – rebuild will happen when size is ready

        registerEscHandler();
    }

    private void adjustColumnsAndRebuild() {
        double width = skinGrid.getWidth();
        if (width <= 0) return;
        int cols = Math.max(2, (int) (width / SkinGridModel.NCOL));   // roughly one column per 300px
        gridModel.setColumns(cols);
    }

    private void loadSkinsFromJson() {
        List<EntityRecord> skins = new ArrayList<>();
        Map<String, EntityRecord> globalCache = EntityFactory.getCache();
        globalCache.entrySet().stream()
                .filter(entry -> entry.getValue() != null)
                .sorted(Comparator.comparing(entry -> entry.getValue().bestiaryOrder())).forEach(entry -> {
                    String key = entry.getKey().toLowerCase().trim();
                    if (entry.getValue().player() != null || key.contains("player")) {
                        skins.add(entry.getValue());
                    }
                });
        gridModel.getSkins().setAll(skins);
    }

    private void rebuildGrid(ObservableList<SkinGridModel.SkinPlacement> placements) {
        if (rebuilding) return;
        if (placements.isEmpty() || skinGrid.getWidth() <= 0 || skinGrid.getHeight() <= 0) {
            return;   // wait for valid layout bounds
        }
        rebuilding = true;

        skinGrid.getChildren().clear();
        skinGrid.getColumnConstraints().clear();
        skinGrid.getRowConstraints().clear();

        int maxRow = 0, maxCol = 0;
        for (SkinGridModel.SkinPlacement p : placements) {
            int endRow = p.row() + p.rowSpan();
            int endCol = p.col() + p.colSpan();
            if (endRow > maxRow) maxRow = endRow;
            if (endCol > maxCol) maxCol = endCol;
        }

        double hGap = skinGrid.getHgap();
        double vGap = skinGrid.getVgap();
        double availWidth  = skinGrid.getWidth()  - (maxCol - 1) * hGap;
        double availHeight = skinGrid.getHeight() - (maxRow - 1) * vGap;
        double cellW = Math.max(1, availWidth / maxCol);
        double cellH = Math.max(1, availHeight / maxRow);
        double cellSize = Math.min(cellW, cellH);

        // Row & column constraints
        for (int r = 0; r < maxRow; r++) {
            RowConstraints row = new RowConstraints(cellSize, cellSize, cellSize);
            row.setFillHeight(true);
            skinGrid.getRowConstraints().add(row);
        }
        for (int c = 0; c < maxCol; c++) {
            ColumnConstraints col = new ColumnConstraints(cellSize, cellSize, cellSize);
            col.setFillWidth(true);
            skinGrid.getColumnConstraints().add(col);
        }

        for (SkinGridModel.SkinPlacement p : placements) {
            if (p.index() == RANDOM_INDEX) {
                // Random cell – interactive, styled with dice icon
                Button randBtn = createRandomCellButton(cellSize, cellSize);
                skinGrid.add(randBtn, p.col(), p.row(), p.colSpan(), p.rowSpan());
                GridPane.setHgrow(randBtn, Priority.ALWAYS);
                GridPane.setVgrow(randBtn, Priority.ALWAYS);
                continue;
            }

            double currentCellW = cellSize * p.colSpan() + (p.colSpan() - 1) * hGap;
            double currentCellH = cellSize * p.rowSpan() + (p.rowSpan() - 1) * vGap;
            Button btn = createSkinButton(p.record(), currentCellW, currentCellH);
            if (p.selected()) {
                btn.getStyleClass().add("selected-skin-button");
            }
            skinGrid.add(btn, p.col(), p.row(), p.colSpan(), p.rowSpan());
            GridPane.setHgrow(btn, Priority.ALWAYS);
            GridPane.setVgrow(btn, Priority.ALWAYS);
        }

        rebuilding = false;
    }

    private Button createRandomCellButton(double width, double height) {
        Button btn = new Button();
        btn.getStyleClass().add("skin-button");
        btn.setMinSize(width, height);
        btn.setPrefSize(width, height);
        btn.setMaxSize(width, height);
        ComponentsUtils.applyIconButton(btn, "fas-dice");
        btn.setOnAction(e -> selectRandom());
        btn.setFocusTraversable(false);
        return btn;
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
        gridModel.setSelectedKey(key);
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
        }
    }

    @FXML
    private void selectRandom() {
        List<EntityRecord> skins = gridModel.getSkins();
        if (skins.isEmpty()) return;
        EntityRecord randomSkin;
        do {
            randomSkin = skins.get(rng.nextInt(skins.size()));
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

    // ----- ESC handler & navigation (unchanged) -----
    @Override public void registerEscHandler() {
        skinStackPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.setOnKeyPressed(event -> {
                    if (event.getCode() == KeyCode.ESCAPE) handleBack();
                });
            }
        });
    }

    @FXML private void handleConfirm(ActionEvent event) {
        if (selectedSkinPath != null && selectedSkinKey != null) {
            SessionManager.setPlayerSkin(selectedSkinPath);
            SessionManager.setPlayerSkinKey(selectedSkinKey);
            SessionUser user = SessionManager.getInstance().getCurrentUser();
            if (user != null) {
                IscatDB.getInstance().executeAsync(() ->
                        IscatDB.getInstance().getSettingsDAO().updatePlayerSkin(user.id(), selectedSkinKey)
                );
            }
        }
        handleBack();
    }

    @FXML private void handleBack(ActionEvent event) { handleBack(); }
    @Override public Pane getRootPane() { return skinStackPane; }
    @Override public void setPointerToView(StackPane pointer) { this.contentRoot = pointer; }
}