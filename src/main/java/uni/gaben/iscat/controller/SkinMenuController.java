package uni.gaben.iscat.controller;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.model.user.SessionUser;
import uni.gaben.iscat.universe.entities.EntityFactory;
import uni.gaben.iscat.universe.entities.EntityRecord;
import uni.gaben.iscat.utils.ComponentsUtils;
import uni.gaben.iscat.utils.SessionManager;
import uni.gaben.iscat.view.components.AnimatedCanvas;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SkinMenuController implements IscatMenuController {

    private enum InfoMode { DESCRIPTION, STATS, EXTRA }
    private InfoMode currentInfoMode = InfoMode.DESCRIPTION;

    @FXML private GridPane skinGrid;
    @FXML private StackPane previewContainer;
    @FXML private Label skinNameLabel;
    @FXML private StackPane skinStackPane;
    @FXML private VBox previewBox;

    @FXML private Button confirmBtn;
    @FXML private Button randomBtn;
    @FXML private Button cancelBtn;

    @FXML private Button btnDescription;
    @FXML private Button btnStats;
    @FXML private Button btnExtra;

    @FXML private InfoCardController infoCardController;

    private StackPane contentRoot;
    private String selectedSkinPath;
    private String selectedSkinKey = "player1";

    private AnimatedCanvas previewCanvas;
    private final List<AnimatedCanvas> buttonCanvases = new ArrayList<>();
    private final List<EntityRecord> playerSkins = new ArrayList<>();

    private static final double BASE_SIZE = 32.0;
    private boolean isScaling = false;

    @FXML
    public void initialize() {
        previewCanvas = new AnimatedCanvas(BASE_SIZE);
        previewContainer.getChildren().add(previewCanvas);

        ComponentsUtils.applyIconButton(confirmBtn, "fas-check");
        ComponentsUtils.applyIconButton(randomBtn,  "fas-dice");
        ComponentsUtils.applyIconButton(cancelBtn,  "fas-arrow-left");

        ComponentsUtils.applyIconButton(btnDescription, "fas-book");
        ComponentsUtils.applyIconButton(btnStats,       "fas-chart-bar");
        ComponentsUtils.applyIconButton(btnExtra,       "fas-info-circle");

        loadSkinsFromJson();
        populateGrid();

        // Preseleziona la skin attualmente salvata
        String currentKey = SessionManager.getPlayerSkinKey();
        EntityRecord current = EntityFactory.getCache().get(currentKey);
        if (current != null) {
            selectSkin(current.entityKey(), current.spritePath(), current.name());
        } else if (!playerSkins.isEmpty()) {
            EntityRecord first = playerSkins.get(0);
            selectSkin(first.entityKey(), first.spritePath(), first.name());
        } else {
            this.selectedSkinKey = "player1";
            this.selectedSkinPath = "/uni/gaben/iscat/sprites/players/player1.png";
            this.skinNameLabel.setText("BATTLE SHIP");
            previewCanvas.loadSkin(selectedSkinPath);
            refreshInfoZone();
        }

        ChangeListener<Number> sizeListener = (obs, oldVal, newVal) -> updateDynamicScaling();
        skinStackPane.widthProperty().addListener(sizeListener);
        skinStackPane.heightProperty().addListener(sizeListener);

        Platform.runLater(this::updateDynamicScaling);
        registerEscHandler();
    }

    private void loadSkinsFromJson() {
        playerSkins.clear();
        Map<String, EntityRecord> globalCache = EntityFactory.getCache();
        for (Map.Entry<String, EntityRecord> entry : globalCache.entrySet()) {
            EntityRecord record = entry.getValue();
            String key = entry.getKey().toLowerCase().trim();
            if (record.player() != null || key.contains("player")) {
                playerSkins.add(record);
            }
        }
    }

    private void updateDynamicScaling() {
        if (isScaling) return;
        double w = skinStackPane.getWidth();
        double h = skinStackPane.getHeight();
        if (w < 200 || h < 200) return;

        isScaling = true;
        double iconDim = getIconDim(w, h);
        double previewDim = iconDim * 3.5;
        double btnDim = iconDim + 30;

        for (Node node : skinGrid.getChildren()) {
            if (node instanceof Button btn && btn.getGraphic() instanceof AnimatedCanvas canvas) {
                btn.setMinSize(btnDim, btnDim);
                btn.setPrefSize(btnDim, btnDim);
                btn.setMaxSize(btnDim, btnDim);
                canvas.resize(iconDim);
            }
        }

        double fattoreScala = 1.25;
        skinGrid.setScaleX(fattoreScala);
        skinGrid.setScaleY(fattoreScala);

        previewCanvas.resize(previewDim);
        previewBox.setMinSize(previewDim + 100, previewDim + 150);
        previewBox.setPrefWidth(previewDim + 100);

        Platform.runLater(() -> isScaling = false);
    }

    private static double getIconDim(double w, double h) {
        double multiplier = Math.max(1.0, Math.min(w, h) / 400.0);
        multiplier = Math.round(multiplier * 5.0) / 5.0;
        return BASE_SIZE * multiplier;
    }

    private void populateGrid() {
        skinGrid.getChildren().clear();
        buttonCanvases.clear();
        double initialDim = 80.0;

        for (int i = 0; i < playerSkins.size(); i++) {
            EntityRecord skin = playerSkins.get(i);
            String key  = skin.entityKey();
            String path = skin.spritePath();
            String name = skin.name();

            AnimatedCanvas canvas = new AnimatedCanvas(BASE_SIZE);
            canvas.loadSkin(path, skin.frameW(), skin.frameH());
            buttonCanvases.add(canvas);

            Button btn = new Button();
            btn.getStyleClass().add("skin-button");
            btn.setGraphic(canvas);
            btn.setFocusTraversable(false);
            btn.setMinSize(initialDim, initialDim);
            btn.setPrefSize(initialDim, initialDim);
            btn.setMaxSize(initialDim, initialDim);
            btn.setOnAction(e -> selectSkin(key, path, name));

            skinGrid.add(btn, i % 3, i / 3);
        }
    }

    private void selectSkin(String key, String path, String name) {
        this.selectedSkinKey  = key;
        this.selectedSkinPath = path;
        this.skinNameLabel.setText(name.toUpperCase());

        EntityRecord record = EntityFactory.getCache().get(key);
        if (record != null) {
            previewCanvas.loadSkin(path, record.frameW(), record.frameH());
        } else {
            previewCanvas.loadSkin(path);
        }

        refreshInfoZone();
        ComponentsUtils.playSpawnTween(previewBox);
        updateDynamicScaling();
    }

    @FXML
    private void selectRandom() {
        if (playerSkins.isEmpty()) return;
        int idx;
        EntityRecord randomSkin;
        do {
            idx = java.util.concurrent.ThreadLocalRandom.current().nextInt(playerSkins.size());
            randomSkin = playerSkins.get(idx);
        } while (randomSkin.spritePath().equals(selectedSkinPath) && playerSkins.size() > 1);
        selectSkin(randomSkin.entityKey(), randomSkin.spritePath(), randomSkin.name());
    }

    private void refreshInfoZone() {
        if (selectedSkinKey == null || infoCardController == null) return;
        EntityRecord record = EntityFactory.getCache().get(selectedSkinKey);
        if (record == null) {
            infoCardController.updateInfo("N/A", "Nessun dato caricato per " + selectedSkinKey);
            return;
        }
        InfoCardController.InfoMode targetMode = InfoCardController.InfoMode.valueOf(currentInfoMode.name());
        infoCardController.updateEntityInfo(targetMode, record, 0);
    }

    @FXML private void showDescription() { currentInfoMode = InfoMode.DESCRIPTION; refreshInfoZone(); }
    @FXML private void showStats()       { currentInfoMode = InfoMode.STATS;        refreshInfoZone(); }
    @FXML private void showExtra()       { currentInfoMode = InfoMode.EXTRA;        refreshInfoZone(); }

    @Override public Pane getRootPane() { return skinStackPane; }
    @FXML private void handleBack(ActionEvent event) { handleBack(); }

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

    @Override public void setContentRoot(StackPane contentRoot) { this.contentRoot = contentRoot; }
}