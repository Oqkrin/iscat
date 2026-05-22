package uni.gaben.iscat.menus.skin_menu;

import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import uni.gaben.iscat.IscatFxmlController;
import uni.gaben.iscat.IscatNavigator;
import uni.gaben.iscat.IscatScenes;
import uni.gaben.iscat.game.universe.player.PlayerSettings;
import uni.gaben.iscat.utils.components.AnimatedCanvas;

import java.util.ArrayList;
import java.util.List;

public class SkinMenuController implements IscatFxmlController {

    @FXML private GridPane skinGrid;
    @FXML private Pane previewContainer;
    @FXML private Label skinNameLabel;
    @FXML private BorderPane rootPane;
    @FXML private VBox previewBox;

    private StackPane contentRoot;
    private String selectedSkinPath;
    private AnimatedCanvas previewCanvas;
    private final List<AnimatedCanvas> buttonCanvases = new ArrayList<>();

    private static final int TOTAL_SKINS = 9;
    private static final double BASE_SIZE = 32.0;

    private static final String[] SKIN_NAMES = {
            "Battle Ship", "Simple Battle Ship", "Friendly Ship",
            "Iscat Traitor", "Cubism Lover", "Space Goblin",
            "Phantom", "Radar", "NO NAME"
    };

    @FXML
    public void initialize() {
        previewCanvas = new AnimatedCanvas(BASE_SIZE);
        previewContainer.getChildren().add(previewCanvas);

        populateGrid();
        selectSkin("/uni/gaben/iscat/sprites/players/player1.png", SKIN_NAMES[0]);

        ChangeListener<Number> sizeListener = (obs, oldVal, newVal) -> updateDynamicScaling();
        rootPane.widthProperty().addListener(sizeListener);
        rootPane.heightProperty().addListener(sizeListener);
    }

    private void updateDynamicScaling() {
        double w = rootPane.getWidth();
        double h = rootPane.getHeight();
        if (w <= 0 || h <= 0) return;

        double iconDim = getIconDim(w, h);
        double previewDim = iconDim * 3.5;

        for (Node node : skinGrid.getChildren()) {
            if (node instanceof Button btn && btn.getGraphic() instanceof AnimatedCanvas canvas) {
                btn.setMinSize(0, 0);
                btn.setPrefSize(iconDim + 30, iconDim + 30);
                btn.setMaxSize(iconDim + 30, iconDim + 30);
                canvas.resize(iconDim);
            }
        }

        previewCanvas.resize(previewDim);
        previewBox.setMinSize(0, 0);
        previewBox.setPrefWidth(previewDim + 100);
    }

    private static double getIconDim(double w, double h) {
        double multiplier = Math.max(1.0, Math.min(w, h) / 400.0);
        multiplier = Math.round(multiplier * 5.0) / 5.0;
        return BASE_SIZE * multiplier;
    }

    private void populateGrid() {
        skinGrid.getChildren().clear();
        buttonCanvases.clear();

        for (int i = 0; i < TOTAL_SKINS; i++) {
            int num = i + 1;
            String path = "/uni/gaben/iscat/sprites/players/player" + num + ".png";
            String name = SKIN_NAMES[i];

            AnimatedCanvas canvas = new AnimatedCanvas(BASE_SIZE);
            canvas.loadSkin(path);
            buttonCanvases.add(canvas);

            Button btn = new Button();
            btn.getStyleClass().add("skin-button");
            btn.setGraphic(canvas);
            btn.setFocusTraversable(false);
            btn.setOnAction(e -> selectSkin(path, name));
            skinGrid.add(btn, i % 3, i / 3);
        }
    }

    private void selectSkin(String path, String name) {
        this.selectedSkinPath = path;
        this.skinNameLabel.setText(name.toUpperCase());
        previewCanvas.loadSkin(path);
        updateDynamicScaling();
    }

    private void stopAll() {
        buttonCanvases.forEach(AnimatedCanvas::stop);
        previewCanvas.stop();
    }

    @FXML
    private void handleConfirm(ActionEvent event) {
        stopAll();
        if (selectedSkinPath != null) {
            PlayerSettings.setPlayerSkin(selectedSkinPath);
        }
        IscatNavigator.getInstance().navigateWithFade(IscatScenes.MAIN_MENU, contentRoot);
    }

    @FXML
    private void handleBack(ActionEvent event) {
        stopAll();
        IscatNavigator.getInstance().navigateWithFade(IscatScenes.MAIN_MENU, contentRoot);
    }

    @Override
    public void setContentRoot(StackPane contentRoot) {
        this.contentRoot = contentRoot;
    }
}