package uni.gaben.iscat.screens.options;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.stage.Stage;
import uni.gaben.iscat.universe.UU;

public class OptionDisplayController {

    @FXML private Slider scaleSlider;
    @FXML private CheckBox checkFps;
    @FXML private CheckBox FullscreenCheck;
    @FXML private CheckBox DebugModeCheck;

    @FXML
    public void initialize() {
        scaleSlider.valueProperty().addListener((obs, old, val) ->
                UU.setUniverseScale(val.doubleValue()));
    }

    /**
     * Sincronizza lo stato della checkbox del Fullscreen con la finestra di JavaFX
     */
    public void bindFullscreenProperty(Stage stage) {
        if (stage != null && FullscreenCheck != null) {
            FullscreenCheck.selectedProperty().bind(stage.fullScreenProperty());
        }
    }

    @FXML
    void toggleFullscreen(ActionEvent event) {
        if (FullscreenCheck.getScene() != null) {
            Stage stage = (Stage) FullscreenCheck.getScene().getWindow();
            stage.setFullScreen(!stage.isFullScreen());
        }
    }

    @FXML void toggleFPSVisible(ActionEvent event) {}
    @FXML void toggleDebugMode(ActionEvent event) {}

    public javafx.scene.control.CheckBox getCheckFps() {
        return checkFps;
    }

    public javafx.scene.control.CheckBox getDebugModeCheck() {
        return DebugModeCheck;
    }
}