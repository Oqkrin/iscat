package uni.gaben.iscat.controller.menus;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
import uni.gaben.iscat.IscatNavigator;
import uni.gaben.iscat.IscatSettings;
import uni.gaben.iscat.controller.components.settings.*;
import uni.gaben.iscat.controller.interfaces.IscatMenuController;
import uni.gaben.iscat.model.IscatViews;
import uni.gaben.iscat.controller.components.ConfirmationOverlayController;

public class SettingsMenuController implements IscatMenuController {

    @FXML private VBox paneMaster;

    @FXML private VBox tabMainSettings;
    @FXML private VBox tabControls;
    @FXML private VBox tabTheme;
    @FXML private VBox tabAccount;

    @FXML private Button mainSettingsButton;
    @FXML private Button controlsButton;
    @FXML private Button themeButton;
    @FXML private Button accountButton;
    @FXML private Button exitBtn;

    @FXML private DisplaySettingsController subDisplayController;
    @FXML private AudioSettingsController subAudioController;
    @FXML private ThemeSettingsController subThemeController;
    @FXML private KeybindsSettingsController subKeybindsController;
    @FXML private AccountSettingsController subAccountController;
    @FXML private StackPane confirmOverlay;
    @FXML private ConfirmationOverlayController confirmOverlayController;

    private Runnable customBackAction = null;
    private StackPane contentRoot;

    @FXML
    public void initialize() {
        setupButtons();

        if (exitBtn != null) {
            FontIcon backIcon = new FontIcon("fas-arrow-left");
            backIcon.setIconSize((int) IscatSettings.STANDARD_UNIT);
            exitBtn.setGraphic(backIcon);
            exitBtn.setText("MAIN MENU");
            exitBtn.setContentDisplay(ContentDisplay.LEFT);
            exitBtn.setGraphicTextGap(IscatSettings.STANDARD_UNIT);
        }

        if (subDisplayController != null) subDisplayController.setConfirmOverlayController(confirmOverlayController);
        if (subThemeController != null) subThemeController.injectParentPane(paneMaster);
        if (subAccountController != null) subAccountController.setConfirmOverlayController(confirmOverlayController);
        if (subKeybindsController != null) subKeybindsController.setConfirmOverlayController(confirmOverlayController);

        registerEscHandler();
        syncAllProperties();

        switchTab(tabMainSettings);

        getRootPane().sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
                    if (subKeybindsController != null && subKeybindsController.isListening()) {
                        boolean consumed = subKeybindsController.handleKeyPress(e);
                        if (consumed) e.consume();
                    }
                });

                newScene.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
                    if (subKeybindsController != null && subKeybindsController.isListening()) {
                        boolean consumed = subKeybindsController.handleMousePress(e);
                        if (consumed) e.consume();
                    }
                });

                syncAllProperties();
            }
        });
    }

    private void setupButtons() {
        setSquareButton(mainSettingsButton, "fas-sliders-h", "SETTINGS");
        setSquareButton(controlsButton,     "fas-keyboard",  "CONTROLS");
        setSquareButton(themeButton,        "fas-palette",   "THEME");
        setSquareButton(accountButton,      "fas-user-cog",  "ACCOUNT");
    }

    private void setSquareButton(Button btn, String iconCode, String labelText) {
        if (btn == null) return;

        FontIcon icon = new FontIcon(iconCode);
        icon.setIconSize((int) (IscatSettings.STANDARD_UNIT*2));
        icon.getStyleClass().add("button-icon");

        btn.setText(labelText);
        btn.setGraphic(icon);

        btn.setContentDisplay(ContentDisplay.TOP);
        btn.setGraphicTextGap(IscatSettings.STANDARD_UNIT);
    }

    @FXML private void showMainSettings() { switchTab(tabMainSettings); }
    @FXML private void showControls()     { switchTab(tabControls);     }
    @FXML private void showTheme()        { switchTab(tabTheme);        }
    @FXML private void showAccount()      { switchTab(tabAccount);      }

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

    /**
     * Inizializza il contesto quando i settaggi vengono aperti a partita in corso.
     */
    public void initGameContext(uni.gaben.iscat.controller.game.GameController gameController) {
        if (gameController == null || subDisplayController == null) return;

        subDisplayController.getCheckFps().setSelected(gameController.isFpsOn());
        subDisplayController.getDebugModeCheck().setSelected(gameController.isDebugModeOn());

        subDisplayController.getCheckFps().selectedProperty().addListener((obs, oldV, newV) ->
                gameController.setShowFps(newV));

        gameController.debugModeProperty().addListener((obs, oldV, newV) -> {
            if (subDisplayController.getDebugModeCheck().isSelected() != newV) {
                subDisplayController.getDebugModeCheck().setSelected(newV);
            }
        });
    }

    @FXML void handleBackAction(ActionEvent event) { handleBack(); }
    public void setCustomBackAction(Runnable customBackAction) { this.customBackAction = customBackAction; }
    @Override public void setPointerToView(StackPane pointer) { this.contentRoot = pointer; }
}