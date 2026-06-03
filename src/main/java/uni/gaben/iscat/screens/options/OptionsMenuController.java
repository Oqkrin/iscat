package uni.gaben.iscat.screens.options;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import uni.gaben.iscat.IscatNavigator;
import uni.gaben.iscat.model.IscatViews;
import uni.gaben.iscat.screens.base.IscatMenuController;
import uni.gaben.iscat.screens.confirmation_overlay.ConfirmationOverlayController;

public class OptionsMenuController implements IscatMenuController {

    @FXML private VBox paneMaster;
    @FXML private OptionDisplayController subDisplayController;
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
        if (subThemeController != null) subThemeController.injectParentPane(paneMaster);
        if (subAccountController != null) subAccountController.setConfirmOverlayController(confirmOverlayController);
        if (subKeybindsController != null) subKeybindsController.setConfirmOverlayController(confirmOverlayController);

        registerEscHandler();

        getRootPane().sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, e -> {
                    if (subKeybindsController != null && subKeybindsController.isListening()) {
                        boolean consumed = subKeybindsController.handleKeyPress(e);
                        if (consumed) e.consume();
                    }
                });

                newScene.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, e -> {
                    if (subKeybindsController != null && subKeybindsController.isListening()) {
                        boolean consumed = subKeybindsController.handleMousePress(e);
                        if (consumed) e.consume();
                    }
                });

                Stage stage = (Stage) newScene.getWindow();
                if (subDisplayController != null && stage != null) {
                    subDisplayController.bindFullscreenProperty(stage);
                }
                if (subThemeController != null) {
                    subThemeController.applyManualColorChanges();
                }
            }
        });
    }

    @Override
    public Pane getRootPane() { return paneMaster; }

    @Override
    public void handleBack() {
        if (confirmOverlay != null && confirmOverlay.isVisible()) {
            if (subKeybindsController != null && subKeybindsController.hasActiveSelection()) {
                subKeybindsController.clearSelection();
            }
            confirmOverlayController.handleBack();
            return;
        }
        if (subKeybindsController != null && subKeybindsController.hasActiveSelection()) {
            subKeybindsController.clearSelection();
            return;
        }
        if (customBackAction != null) customBackAction.run();
        else IscatNavigator.getInstance().navigateWithFade(IscatViews.MAIN_MENU);
    }

    public void initGameContext(uni.gaben.iscat.screens.game.controller.GameController gameController) {
        if (gameController == null || subDisplayController == null) return;

        // Allinea lo stato iniziale dei checkbox con lo stato del gioco
        subDisplayController.getCheckFps().setSelected(gameController.isFpsOn());
        subDisplayController.getDebugModeCheck().setSelected(gameController.isDebugModeOn());

        // Aggiunge listener reattivi per aggiornare il gioco in tempo reale al click
        subDisplayController.getCheckFps().selectedProperty().addListener((obs, oldV, newV) ->
                gameController.setShowFps(newV));

        subDisplayController.getDebugModeCheck().selectedProperty().addListener((obs, oldV, newV) ->
                gameController.setShowDebugMode(newV));
    }

    @FXML
    void handleBackAction(ActionEvent event) { handleBack(); }

    public void setCustomBackAction(Runnable customBackAction) { this.customBackAction = customBackAction; }
    @Override public void setContentRoot(StackPane contentRoot) { this.contentRoot = contentRoot; }
}