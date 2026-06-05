package uni.gaben.iscat.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import uni.gaben.iscat.IscatNavigator;
import uni.gaben.iscat.controller.components.options.*;
import uni.gaben.iscat.model.IscatViews;
import uni.gaben.iscat.controller.components.ConfirmationOverlayController;
import uni.gaben.iscat.utils.ComponentsUtils;

public class OptionsMenuController implements IscatMenuController {

    @FXML private VBox paneMaster;

    @FXML private VBox tabMainSettings;
    @FXML private VBox tabControls;
    @FXML private VBox tabTheme;
    @FXML private VBox tabAccount;

    @FXML private Button btnMainSettings;
    @FXML private Button btnControls;
    @FXML private Button btnTheme;
    @FXML private Button btnAccount;
    @FXML private Button ExitBtn;

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
        ComponentsUtils.applyIconButton(btnMainSettings, "fas-sliders-h");
        ComponentsUtils.applyIconButton(btnControls,     "fas-keyboard");
        ComponentsUtils.applyIconButton(btnTheme,        "fas-palette");
        ComponentsUtils.applyIconButton(btnAccount,      "fas-user-cog");
        ComponentsUtils.applyIconButton(ExitBtn,         "fas-arrow-left");

        if (subThemeController != null) subThemeController.injectParentPane(paneMaster);
        if (subAccountController != null) subAccountController.setConfirmOverlayController(confirmOverlayController);
        if (subKeybindsController != null) subKeybindsController.setConfirmOverlayController(confirmOverlayController);

        registerEscHandler();
        syncAllProperties();

        switchTab(tabMainSettings);

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

                syncAllProperties();
            }
        });
    }


    @FXML private void showMainSettings() { switchTab(tabMainSettings); }
    @FXML private void showControls()     { switchTab(tabControls);     }
    @FXML private void showTheme()        { switchTab(tabTheme);        }
    @FXML private void showAccount()      { switchTab(tabAccount);      }

    /**
     * Rende visibile solo il contenitore passato, nascondendo tutti gli altri.
     */
    private void switchTab(VBox activeTab) {
        tabMainSettings.setVisible(tabMainSettings == activeTab);
        tabControls.setVisible(tabControls == activeTab);
        tabTheme.setVisible(tabTheme == activeTab);
        tabAccount.setVisible(tabAccount == activeTab);

        if (activeTab != tabControls && subKeybindsController != null) {
            subKeybindsController.clearSelection();
        }
    }

    public void syncAllProperties() {
        if (subAudioController != null) subAudioController.bindAudioProperties();
        if (subDisplayController != null && getRootPane().getScene() != null) {
            Stage stage = (Stage) getRootPane().getScene().getWindow();
            if (stage != null) subDisplayController.bindDisplayProperties(stage);
        }
        if (subThemeController != null && getRootPane().getScene() != null) {
            subThemeController.loadAndApplySavedTheme();
        }
    }

    @Override public Pane getRootPane() { return paneMaster; }

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

    public void initGameContext(uni.gaben.iscat.controller.game.GameController gameController) {
        if (gameController == null || subDisplayController == null) return;
        subDisplayController.getCheckFps().setSelected(gameController.isFpsOn());
        subDisplayController.getDebugModeCheck().setSelected(gameController.isDebugModeOn());
        subDisplayController.getCheckFps().selectedProperty().addListener((obs, oldV, newV) -> gameController.setShowFps(newV));
        subDisplayController.getDebugModeCheck().selectedProperty().addListener((obs, oldV, newV) -> gameController.setShowDebugMode(newV));
    }

    @FXML void handleBackAction(ActionEvent event) { handleBack(); }
    public void setCustomBackAction(Runnable customBackAction) { this.customBackAction = customBackAction; }
    @Override public void setContentRoot(StackPane contentRoot) { this.contentRoot = contentRoot; }
}