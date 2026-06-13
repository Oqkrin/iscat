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
import uni.gaben.iscat.universe.entity.hardcoded.player.PlayerSettings;
import uni.gaben.iscat.utils.ComponentsUtils;
import uni.gaben.iscat.view.components.AnimatedCanvas;
import java.util.ArrayList;
import java.util.List;

public class SkinMenuController implements IscatMenuController {

    @FXML private GridPane skinGrid;
    @FXML private StackPane previewContainer;
    @FXML private Label skinNameLabel;
    @FXML private StackPane skinStackPane;
    @FXML private VBox previewBox;

    @FXML private Button confirmBtn;
    @FXML private Button randomBtn;
    @FXML private Button cancelBtn;

    private StackPane contentRoot;
    private String selectedSkinPath;

    private String selectedSkinKey = "player1";

    private AnimatedCanvas previewCanvas;
    private final List<AnimatedCanvas> buttonCanvases = new ArrayList<>();

    private static final int TOTAL_SKINS = 9;
    private static final double BASE_SIZE = 32.0;

    private boolean isScaling = false;

    private static final String[] SKIN_NAMES = {
            "Battle Ship", "Simple Battle Ship", "Friendly Ship",
            "Iscat Traitor", "Cubism Lover", "Space Goblin",
            "Phantom", "Radar", "PARADOX"
    };

    @FXML
    public void initialize() {
        previewCanvas = new AnimatedCanvas(BASE_SIZE);
        previewContainer.getChildren().add(previewCanvas);

        ComponentsUtils.applyIconButton(confirmBtn, "fas-check");
        ComponentsUtils.applyIconButton(randomBtn,  "fas-dice");
        ComponentsUtils.applyIconButton(cancelBtn,  "fas-arrow-left");

        populateGrid();

        // Stato iniziale basato su player1
        this.selectedSkinKey = "player1";
        this.selectedSkinPath = "/uni/gaben/iscat/sprites/players/player1.png";
        this.skinNameLabel.setText(SKIN_NAMES[0].toUpperCase());
        previewCanvas.loadSkin(selectedSkinPath);

        ChangeListener<Number> sizeListener = (obs, oldVal, newVal) -> updateDynamicScaling();
        skinStackPane.widthProperty().addListener(sizeListener);
        skinStackPane.heightProperty().addListener(sizeListener);

        Platform.runLater(this::updateDynamicScaling);

        registerEscHandler();
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

        for (int i = 0; i < TOTAL_SKINS; i++) {
            int num = i + 1;
            String key = "player" + num;
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

            // Passiamo anche la chiave del player all'azione di selezione
            btn.setOnAction(e -> selectSkin(key, path, name));

            skinGrid.add(btn, i % 3, i / 3);
        }
    }

    private void selectSkin(String key, String path, String name) {
        this.selectedSkinKey = key;
        this.selectedSkinPath = path;
        this.skinNameLabel.setText(name.toUpperCase());
        previewCanvas.loadSkin(path);
        ComponentsUtils.playSpawnTween(previewBox);
        updateDynamicScaling();
    }

    @FXML
    private void selectRandom() {
        int idx;
        String key;
        String path;
        do {
            idx = java.util.concurrent.ThreadLocalRandom.current().nextInt(TOTAL_SKINS);
            key = "player" + (idx + 1);
            path = "/uni/gaben/iscat/sprites/players/player" + (idx + 1) + ".png";
        } while (path.equals(selectedSkinPath));

        selectSkin(key, path, SKIN_NAMES[idx]);
    }

    @Override
    public Pane getRootPane() { return skinStackPane; }

    @FXML
    private void handleBack(ActionEvent event) { handleBack(); }

    @FXML
    private void handleConfirm(ActionEvent event) {
        if (selectedSkinPath != null && selectedSkinKey != null) {
            // Salva i dati globali nei settings del Player
            PlayerSettings.setPlayerSkin(selectedSkinPath);
            PlayerSettings.setPlayerSkinKey(selectedSkinKey);

            // Sincronizzazione con il database
            //TODO
            try {
                System.out.println("[SkinMenu] Salvata skin nel Database: " + selectedSkinKey);
            } catch (Exception ex) {
                System.err.println("[SkinMenu] Impossibile salvare la skin nel DB: " + ex.getMessage());
            }
        }
        handleBack();
    }

    @Override
    public void setContentRoot(StackPane contentRoot) {
        this.contentRoot = contentRoot;
    }
}