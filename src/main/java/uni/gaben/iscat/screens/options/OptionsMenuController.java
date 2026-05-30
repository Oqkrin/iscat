package uni.gaben.iscat.screens.options;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import uni.gaben.iscat.IscatNavigator;
import uni.gaben.iscat.controller.IscatFxmlController;
import uni.gaben.iscat.model.IscatViews;
import uni.gaben.iscat.screens.confirmation_overlay.ConfirmationOverlayController;
import uni.gaben.iscat.universe.UU;

public class OptionsMenuController implements IscatFxmlController {

    @FXML private VBox paneMaster;
    @FXML private CheckBox FullscreenCheck;
    @FXML private Slider scaleSlider;

    @FXML private OptionAudioController subAudioController;
    @FXML private OptionThemeController subThemeController;
    @FXML private OptionKeybindsController subKeybindsController;
    @FXML private OptionAccountController subAccountController;

    @FXML private StackPane confirmOverlay;
    @FXML private ConfirmationOverlayController confirmOverlayController;

    private Runnable customBackAction = null;
    private StackPane contentRoot;

    @FXML
    public void initialize() {
        scaleSlider.valueProperty().addListener((obs, old, val) -> UU.setUniverseScale(val.doubleValue()));

        if (subThemeController != null) subThemeController.injectParentPane(paneMaster);
        if (subAccountController != null) subAccountController.setConfirmOverlayController(confirmOverlayController);

        paneMaster.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleGlobalKeyPress);
                Stage stage = (Stage) newScene.getWindow();
                if (stage != null && FullscreenCheck != null) {
                    FullscreenCheck.selectedProperty().bind(stage.fullScreenProperty());
                }
                if (subThemeController != null) subThemeController.applyManualColorChanges();
            }
        });
    }

    private void handleGlobalKeyPress(KeyEvent event) {
        if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
            if (subKeybindsController != null && subKeybindsController.hasActiveSelection()) {
                subKeybindsController.clearSelection();
            } else {
                handleBack(null);
            }
            event.consume();
            return;
        }

        if (subKeybindsController != null && subKeybindsController.handleKeyPress(event)) {
            event.consume();
        }
    }

    @FXML
    void handleBack(ActionEvent event) {
        if (customBackAction != null) customBackAction.run();
        else IscatNavigator.getInstance().navigateWithFade(IscatViews.MAIN_MENU);
    }

    public void setCustomBackAction(Runnable customBackAction) { this.customBackAction = customBackAction; }
    @Override public void setContentRoot(StackPane contentRoot) { this.contentRoot = contentRoot; }
    @FXML void toggleFullscreen(ActionEvent event) { Stage stage = (Stage) paneMaster.getScene().getWindow(); stage.setFullScreen(!stage.isFullScreen()); }
    @FXML void toggleFPSVisible(ActionEvent event) {}
    @FXML void toggleDebugMode(ActionEvent event) {}
}