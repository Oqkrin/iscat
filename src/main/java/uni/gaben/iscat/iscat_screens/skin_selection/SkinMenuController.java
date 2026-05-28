package uni.gaben.iscat.iscat_screens.skin_selection;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import uni.gaben.iscat.iscat_mv_controller.IscatFxmlController;
import uni.gaben.iscat.IscatNavigator;
import uni.gaben.iscat.iscat_model_vc.IscatViews;
import uni.gaben.iscat.universe.player.PlayerSettings;
import uni.gaben.iscat.iscat_m_view_c.AnimatedCanvas;

import java.util.ArrayList;
import java.util.List;

public class SkinMenuController implements IscatFxmlController {

    @FXML private GridPane skinGrid;
    @FXML private StackPane previewContainer;
    @FXML private Label skinNameLabel;
    @FXML private StackPane skinStackPane;
    @FXML private VBox previewBox;

    private StackPane contentRoot;
    private String selectedSkinPath;
    private AnimatedCanvas previewCanvas;
    private final List<AnimatedCanvas> buttonCanvases = new ArrayList<>();

    private static final int TOTAL_SKINS = 9;
    private static final double BASE_SIZE = 32.0;

    private boolean isScaling = false;

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

        this.selectedSkinPath = "/uni/gaben/iscat/sprites/players/player1.png";
        this.skinNameLabel.setText(SKIN_NAMES[0].toUpperCase());
        previewCanvas.loadSkin(selectedSkinPath);

        ChangeListener<Number> sizeListener = (obs, oldVal, newVal) -> updateDynamicScaling();
        skinStackPane.widthProperty().addListener(sizeListener);
        skinStackPane.heightProperty().addListener(sizeListener);

        // Calcola lo scaling dinamico iniziale dopo che la finestra è pronta e stabile
        Platform.runLater(this::updateDynamicScaling);
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

        // Rilascia il blocco alla fine dell'evento di rendering corrente
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

            btn.setMinSize(initialDim, initialDim);
            btn.setPrefSize(initialDim, initialDim);
            btn.setMaxSize(initialDim, initialDim);

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

    @FXML
    private void selectRandom() {
        int idx;
        String path;
        do {
            idx = java.util.concurrent.ThreadLocalRandom.current().nextInt(TOTAL_SKINS);
            path = "/uni/gaben/iscat/sprites/players/player" + (idx + 1) + ".png";
        } while (path.equals(selectedSkinPath));

        selectSkin(path, SKIN_NAMES[idx]);
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
        IscatNavigator.getInstance().navigateWithFade(IscatViews.MAIN_MENU);
    }

    @FXML
    private void handleBack(ActionEvent event) {
        stopAll();
        IscatNavigator.getInstance().navigateWithFade(IscatViews.MAIN_MENU);
    }

    @Override
    public void setContentRoot(StackPane contentRoot) {
        this.contentRoot = contentRoot;
    }
}