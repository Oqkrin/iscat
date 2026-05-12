package uni.gaben.iscat.skin_menu;

import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import uni.gaben.iscat.IscatNavigator;
import uni.gaben.iscat.IscatScenes;
import uni.gaben.iscat.gamenex.universe.player.PlayerSettings;

import java.util.Objects;

public class SkinMenuController {

    @FXML private GridPane skinGrid;
    @FXML private ImageView skinPreview;
    @FXML private Label skinNameLabel;
    @FXML private BorderPane rootPane;
    @FXML private VBox previewBox;

    private String selectedSkinPath;
    private static final int TOTAL_SKINS = 9;
    private static final double BASE_SIZE = 32.0; // Dimensione nativa della tua sprite

    @FXML
    public void initialize() {
        // Popoliamo la griglia inizialmente
        populateGrid();

        // Seleziona la prima skin di default
        selectSkin("/uni/gaben/iscat/sprites/player1.png", "Goblin Standard");

        // Listener per scalare i bottoni e la preview per multipli di 2
        ChangeListener<Number> sizeListener = (obs, oldVal, newVal) -> updateDynamicScaling();

        rootPane.widthProperty().addListener(sizeListener);
        rootPane.heightProperty().addListener(sizeListener);
    }

    private void updateDynamicScaling() {
        double w = rootPane.getWidth();
        double h = rootPane.getHeight();

        if (w <= 0 || h <= 0) return;

        double iconDim = getIconDim(w, h);
        double previewDim = iconDim * 3.5; // Preview sensibilmente più

        for (Node node : skinGrid.getChildren()) {
            if (node instanceof Button btn && btn.getGraphic() instanceof ImageView iv) {
                String path = (String) iv.getUserData();
                if (path != null) {
                    // RESET TOTALE: permette al bottone di rimpicciolirsi senza limiti
                    btn.setMinSize(0, 0);

                    iv.setImage(loadPixelImage(path, iconDim));

                    // Usiamo prefSize per suggerire la dimensione, ma senza bloccare il layout
                    btn.setPrefSize(iconDim + 30, iconDim + 30);
                    btn.setMaxSize(iconDim + 30, iconDim + 30);
                }
            }
        }

        if (selectedSkinPath != null) {
            skinPreview.setImage(loadPixelImage(selectedSkinPath, previewDim));
            // Reset preview fit
            skinPreview.setFitWidth(previewDim);
            skinPreview.setFitHeight(previewDim);
        }

        // Libera la previewBox
        previewBox.setMinSize(0, 0);
        previewBox.setPrefWidth(previewDim + 100);
    }

    private static double getIconDim(double w, double h) {
        double minDim = Math.min(w, h);

        // Calcoliamo un multiplier double basato sulla dimensione minima
        // Usiamo uno step di 400.0 come riferimento per la dimensione "standard"
        // Esempio: se minDim è 600, multiplier = 1.5. Se è 800, multiplier = 2.0
        double multiplier = Math.max(1.0, minDim / 400.0);

        // Arrotondiamo a step di 0.2 per mantenere una certa stabilità visiva (1.0, 1.2, 1.4...)
        multiplier = Math.round(multiplier * 5.0) / 5.0;

        return BASE_SIZE * multiplier;
    }

    private void populateGrid() {
        skinGrid.getChildren().clear();
        for (int i = 0; i < TOTAL_SKINS; i++) {
            int num = i + 1;
            String path = "/uni/gaben/iscat/sprites/player" + num + ".png";

            Button btn = new Button();
            btn.getStyleClass().add("skin-button");

            ImageView icon = new ImageView();
            icon.setUserData(path); // Fondamentale per ricaricare l'immagine nel listener
            btn.setGraphic(icon);

            btn.setOnAction(e -> selectSkin(path, "GOBLIN #" + num));
            skinGrid.add(btn, i % 3, i / 3);
        }
    }

    private void selectSkin(String path, String name) {
        this.selectedSkinPath = path;
        this.skinNameLabel.setText(name.toUpperCase());
        updateDynamicScaling(); // Applica subito lo scaling corretto
    }

    private Image loadPixelImage(String path, double size) {
        // smooth = false disattiva l'interpolazione bilineare (Pixel Art pura)
        return new Image(Objects.requireNonNull(getClass().getResourceAsStream(path)),
                size, size, true, false);
    }

    @FXML private void handleConfirm(ActionEvent event) {
        if (selectedSkinPath != null) {
            PlayerSettings.setPlayerSkin(selectedSkinPath);
            IscatNavigator.getInstance().navigateTo(IscatScenes.MENU);
        }
    }

    @FXML private void handleBack(ActionEvent event) {
        IscatNavigator.getInstance().navigateTo(IscatScenes.MENU);
    }
}