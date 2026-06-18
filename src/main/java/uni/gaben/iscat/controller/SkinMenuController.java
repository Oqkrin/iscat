package uni.gaben.iscat.controller;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import uni.gaben.iscat.controller.components.InfoCardController;
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

    @FXML private FlowPane skinFlow;
    @FXML private Label skinNameLabel;
    @FXML private StackPane skinStackPane;
    @FXML private VBox previewBox;       // kept for fallback, not used in FXML

    @FXML private Button confirmBtn;
    @FXML private Button randomBtn;
    @FXML private Button cancelBtn;

    @FXML private InfoCardController infoCardController;

    private StackPane contentRoot;
    private String selectedSkinPath;
    private String selectedSkinKey = "player1";

    private final List<AnimatedCanvas> buttonCanvases = new ArrayList<>();
    private final List<EntityRecord> playerSkins = new ArrayList<>();

    private static final double BASE_SIZE = 32.0;          // icon size
    private static final double BASE_BUTTON_SIZE = 80.0;   // default button size
    private static final double SELECTED_BUTTON_SIZE = BASE_BUTTON_SIZE * 4.0; // 320

    private boolean isScaling = false;
    private Button selectedButton;

    @FXML
    public void initialize() {
        ComponentsUtils.applyIconButton(confirmBtn, "fas-check");
        ComponentsUtils.applyIconButton(randomBtn,  "fas-dice");
        ComponentsUtils.applyIconButton(cancelBtn,  "fas-arrow-left");

        loadSkinsFromJson();
        populateFlow();

        // Preselect current skin
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
            if (skinNameLabel != null) skinNameLabel.setText("BATTLE SHIP");
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
        // This method can be left empty; it's kept for compatibility.
        // Sizes are fixed and animated via the selection mechanism.
    }

    private void populateFlow() {
        skinFlow.getChildren().clear();
        buttonCanvases.clear();

        for (EntityRecord skin : playerSkins) {
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
            btn.setMinSize(BASE_BUTTON_SIZE, BASE_BUTTON_SIZE);
            btn.setPrefSize(BASE_BUTTON_SIZE, BASE_BUTTON_SIZE);
            btn.setMaxSize(BASE_BUTTON_SIZE, BASE_BUTTON_SIZE);
            btn.setUserData(key);
            btn.setOnAction(e -> selectSkin(key, path, name));

            skinFlow.getChildren().add(btn);
        }
    }

    private void selectSkin(String key, String path, String name) {
        this.selectedSkinKey  = key;
        this.selectedSkinPath = path;
        if (skinNameLabel != null) skinNameLabel.setText(name.toUpperCase());

        refreshInfoZone();
        updateSelection(key);
        ComponentsUtils.playSpawnTween(skinFlow);
    }

    private void updateSelection(String selectedKey) {
        // Reset previous selection
        if (selectedButton != null) {
            animateButtonSize(selectedButton, BASE_BUTTON_SIZE);
            if (selectedButton.getGraphic() instanceof AnimatedCanvas canvas) {
                canvas.resize(BASE_SIZE);
            }
            selectedButton.getStyleClass().remove("selected-skin-button");
        }

        // Find and enlarge the new button
        for (Node node : skinFlow.getChildren()) {
            if (node instanceof Button btn && selectedKey.equals(btn.getUserData())) {
                selectedButton = btn;
                animateButtonSize(btn, SELECTED_BUTTON_SIZE);
                if (btn.getGraphic() instanceof AnimatedCanvas canvas) {
                    canvas.resize(BASE_SIZE * 4);
                }
                btn.getStyleClass().add("selected-skin-button");
                break;
            }
        }
    }

    private void animateButtonSize(Button btn, double targetSize) {
        // Allow the button to grow beyond its current constraints
        btn.setMinSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        btn.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        double startW = btn.getPrefWidth();
        double startH = btn.getPrefHeight();
        if (startW <= 0) startW = BASE_BUTTON_SIZE;
        if (startH <= 0) startH = BASE_BUTTON_SIZE;

        final double fromW = startW;
        final double fromH = startH;
        final double toW = targetSize;
        final double toH = targetSize;

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(btn.prefWidthProperty(), fromW),
                        new KeyValue(btn.prefHeightProperty(), fromH)
                ),
                new KeyFrame(Duration.millis(300),
                        new KeyValue(btn.prefWidthProperty(), toW, Interpolator.EASE_OUT),
                        new KeyValue(btn.prefHeightProperty(), toH, Interpolator.EASE_OUT)
                )
        );
        timeline.setOnFinished(e -> {
            // Lock the final size to avoid layout shifts after animation
            btn.setPrefWidth(toW);
            btn.setPrefHeight(toH);
            btn.setMinSize(targetSize, targetSize);
            btn.setMaxSize(targetSize, targetSize);
            // Force FlowPane to reflow
            skinFlow.requestLayout();
        });
        timeline.play();
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
        } else {
            infoCardController.updateEntityInfo(record);
        }
    }

    @Override
    public Pane getRootPane() { return skinStackPane; }

    @FXML
    private void handleBack(ActionEvent event) { handleBack(); }

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

    @Override
    public void setContentRoot(StackPane contentRoot) { this.contentRoot = contentRoot; }
}