package uni.gaben.iscat.controller;

import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
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
    @FXML private Label skinNameLabel;   // still used to show selected skin name
    @FXML private StackPane skinStackPane;
    @FXML private VBox previewBox;       // will be removed from FXML, but keep for fallback

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
    private static final double SELECTED_SCALE = 4.0;      // how many times larger

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
            // fallback: just show name, no preview
            if (skinNameLabel != null) skinNameLabel.setText("BATTLE SHIP");
            refreshInfoZone();
        }

        // Dynamic scaling listener for window resize (optional)
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
        // Adjust icon sizes based on container width – we'll keep it simple;
        // we can set a fixed size and rely on the 4x scale.
        // This method can be removed or simplified.
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
        // Optional: play a spawn tween on the info card or flow
        ComponentsUtils.playSpawnTween(skinFlow);
    }

    private void updateSelection(String selectedKey) {
        // Reset previous selection
        if (selectedButton != null) {
            ScaleTransition reset = new ScaleTransition(Duration.millis(200), selectedButton);
            reset.setToX(1.0);
            reset.setToY(1.0);
            reset.play();
            // Also reset the canvas size to base
            if (selectedButton.getGraphic() instanceof AnimatedCanvas canvas) {
                canvas.resize(BASE_SIZE);
            }
            selectedButton.getStyleClass().remove("selected-skin-button");
        }

        // Find and scale up the new button
        for (Node node : skinFlow.getChildren()) {
            if (node instanceof Button btn && selectedKey.equals(btn.getUserData())) {
                selectedButton = btn;
                // Grow the button itself
                ScaleTransition grow = new ScaleTransition(Duration.millis(300), btn);
                grow.setToX(SELECTED_SCALE);
                grow.setToY(SELECTED_SCALE);
                grow.play();
                // Also resize the canvas inside to 4x base size
                if (btn.getGraphic() instanceof AnimatedCanvas canvas) {
                    canvas.resize(BASE_SIZE * SELECTED_SCALE);
                }
                btn.getStyleClass().add("selected-skin-button");
                break;
            }
        }
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