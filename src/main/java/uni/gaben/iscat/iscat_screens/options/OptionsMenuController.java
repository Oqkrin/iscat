package uni.gaben.iscat.iscat_screens.options;

import javafx.animation.AnimationTimer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import uni.gaben.iscat.utils.AudioManager;
import uni.gaben.iscat.iscat_mv_controller.IscatFxmlController;
import uni.gaben.iscat.IscatNavigator;
import uni.gaben.iscat.iscat_model_vc.IscatViews;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.iscat_screens.login.model.UserSettings;
import uni.gaben.iscat.database.sqlite.SettingsDAO;
import uni.gaben.iscat.utils.SessionManager;
import uni.gaben.iscat.utils.theme.ThemeManager;
import uni.gaben.iscat.utils.theme.DynamicColors;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OptionsMenuController implements IscatFxmlController {

    private StackPane contentRoot;

    @FXML private VBox mainOptions;
    @FXML private Slider BGMSlider;
    @FXML private Slider BGMSlider1;
    @FXML private Slider SFXSlider;
    @FXML private Slider scaleSlider;
    @FXML private AnchorPane paneMaster;
    @FXML private Label skinNameLabel, skinNameLabel1, skinNameLabel2, skinNameLabel11;
    @FXML private VBox controlsBox;

    @FXML private Button walkUp, walkDown, walkLeft, walkRight, dash1, dash2, esc;
    @FXML private Button ImagePicker;
    @FXML private CheckBox lightModeCheck;
    @FXML private ColorPicker accentPrimary, accentSecondary, accentTernary, bgPrimary;

    @FXML private ImageView themePreview;
    private final List<File> carouselImages = new ArrayList<>();
    private int currentIndex = -1;

    private Button selectedButton = null;
    private String selectedColumn = null;

    // Rainbow feature
    private AnimationTimer uiRainbowSyncTimer;

    @FXML
    public void initialize() {
        // ── Audio Controls ────────────────────────────────────────────────────
        BGMSlider1.valueProperty().addListener((obs, old, val) -> AudioManager.getInstance().setBgmVolume(val.doubleValue()));
        BGMSlider.valueProperty().addListener((obs, old, val) -> AudioManager.getInstance().setBgmVolume(val.doubleValue()));
        SFXSlider.valueProperty().addListener((obs, old, val) -> AudioManager.getInstance().setSfxVolume(val.doubleValue()));

        // ── UI/Scale Initialization ───────────────────────────────────────────
        scaleSlider.valueProperty().addListener((obs, old, val) -> UU.setUniverseScale(val.doubleValue()));
        refreshButtonLabels();
        syncColorPickersWithTheme();

        // ── Scene Attachment ──────────────────────────────────────────────────
        paneMaster.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleGlobalKeyPress);
                applyManualColorChanges(); // Safe to apply now

                // If rainbow mode was active globally, re-start the sync timer
                if (ThemeManager.getInstance().isRainbowModeActive()) {
                    startUiSyncTimer();
                }
            }
        });

        themePreview.managedProperty().bind(themePreview.imageProperty().isNotNull());
        themePreview.visibleProperty().bind(themePreview.imageProperty().isNotNull());

        paneMaster.widthProperty().addListener((obs, old, newWidth)
                -> themePreview.setFitWidth(newWidth.doubleValue() / 3.0));

    }

    // ── Key Bindings ──────────────────────────────────────────────────────────
    private void refreshButtonLabels() {
        UserSettings settings = SessionManager.getInstance().getCurrentSettings();
        if (settings == null) return;
        walkUp.setText(settings.getWalkUp());
        walkDown.setText(settings.getWalkDown());
        walkLeft.setText(settings.getWalkLeft());
        walkRight.setText(settings.getWalkRight());
        dash1.setText(settings.getDash1());
        dash2.setText(settings.getDash2());
        esc.setText(settings.getPauseGame());
    }

    @FXML void changeControl(ActionEvent event) {
        if (selectedButton != null) refreshButtonLabels();
        selectedButton = (Button) event.getSource();
        selectedButton.setText("[ PREMI UN TASTO ]");
        if      (selectedButton == walkUp)    selectedColumn = "WalkUp";
        else if (selectedButton == walkDown)  selectedColumn = "WalkDown";
        else if (selectedButton == walkLeft)  selectedColumn = "WalkLeft";
        else if (selectedButton == walkRight) selectedColumn = "WalkRight";
        else if (selectedButton == dash1)     selectedColumn = "Dash1";
        else if (selectedButton == dash2)     selectedColumn = "Dash2";
        else if (selectedButton == esc)       selectedColumn = "PauseGame";
    }

    private void handleGlobalKeyPress(KeyEvent event) {
        if (selectedButton == null || selectedColumn == null) return;
        String pressedKey = event.getCode().toString();
        UserSettings settings = SessionManager.getInstance().getCurrentSettings();
        if (settings != null) {
            switch (selectedColumn) {
                case "WalkUp" -> settings.setWalkUp(pressedKey);
                case "WalkDown" -> settings.setWalkDown(pressedKey);
                case "WalkLeft" -> settings.setWalkLeft(pressedKey);
                case "WalkRight" -> settings.setWalkRight(pressedKey);
                case "Dash1" -> settings.setDash1(pressedKey);
                case "Dash2" -> settings.setDash2(pressedKey);
                case "PauseGame" -> settings.setPauseGame(pressedKey);
            }
            SettingsDAO.updateControl(settings.getUserId(), selectedColumn, pressedKey);
        }
        selectedButton.setText(pressedKey);
        selectedButton = null;
        selectedColumn = null;
        event.consume();
    }

    @FXML void handleBack(ActionEvent event) {
        if (uiRainbowSyncTimer != null) uiRainbowSyncTimer.stop();
        IscatNavigator.getInstance().navigateWithFade(IscatViews.MAIN_MENU);
    }

    @FXML void resetControls(ActionEvent event) {
        UserSettings settings = SessionManager.getInstance().getCurrentSettings();
        if (settings == null) return;
        SettingsDAO.updateControl(settings.getUserId(), "WalkUp", "W");
        SettingsDAO.updateControl(settings.getUserId(), "WalkDown", "S");
        SettingsDAO.updateControl(settings.getUserId(), "WalkLeft", "A");
        SettingsDAO.updateControl(settings.getUserId(), "WalkRight", "D");
        SettingsDAO.updateControl(settings.getUserId(), "Dash1", "Space");
        SettingsDAO.updateControl(settings.getUserId(), "Dash2", "Middle Mouse");
        SettingsDAO.updateControl(settings.getUserId(), "PauseGame", "ESC");
        refreshButtonLabels();
    }

    // ── Rainbow/Account Logic ─────────────────────────────────────────────
    @FXML void resetAccount(ActionEvent event) {
        if (paneMaster.getScene() == null) return;
        if (ThemeManager.getInstance().isRainbowModeActive()) {
            ThemeManager.getInstance().stopRainbowMode();
            if (uiRainbowSyncTimer != null) uiRainbowSyncTimer.stop();
            syncColorPickersWithTheme();
            AudioManager.getInstance().playSFX("laugh");
        } else {
            AudioManager.getInstance().playSFX("rainbow");
            ThemeManager.getInstance().startRainbowMode(paneMaster.getScene());
            startUiSyncTimer();
        }
    }

    private void startUiSyncTimer() {
        if (uiRainbowSyncTimer != null) uiRainbowSyncTimer.stop();
        uiRainbowSyncTimer = new AnimationTimer() {
            @Override public void handle(long now) {
                Color c = ThemeManager.getInstance().getAccentPrimary();
                accentPrimary.setValue(c);
                accentSecondary.setValue(c);
                accentTernary.setValue(c);
            }
        };
        uiRainbowSyncTimer.start();
    }

    // ── Theme / Color Logic ───────────────────────────────────────────────
    private void applyManualColorChanges() {
        if (paneMaster == null || paneMaster.getScene() == null) return;
        ThemeManager.getInstance().stopRainbowMode();
        if (uiRainbowSyncTimer != null) uiRainbowSyncTimer.stop();

        List<String> hexPalette = List.of(
                toHex(accentPrimary.getValue()),
                toHex(accentSecondary.getValue()),
                toHex(accentTernary.getValue()),
                toHex(bgPrimary.getValue()));
        ThemeManager.getInstance().applyHexColorsTheme(paneMaster.getScene(), hexPalette, 0.2);
    }

    private void syncColorPickersWithTheme() {
        ThemeManager tm = ThemeManager.getInstance();
        accentPrimary.setValue(tm.getAccentPrimary());
        accentSecondary.setValue(tm.getAccentSecondary());
        accentTernary.setValue(tm.getAccentTernary());
        bgPrimary.setValue(tm.getBgPrimary());
    }

    private void applyTheme(File imageFile) {
        try {
            ThemeManager.getInstance().stopRainbowMode();
            if (uiRainbowSyncTimer != null) uiRainbowSyncTimer.stop();

            // The utility now handles the caching logic internally
            List<java.awt.Color> palette = DynamicColors.getPaletteForFile(imageFile, 4, lightModeCheck.isSelected());

            if (palette.size() >= 4) {
                accentPrimary.setValue(toJfx(palette.get(0)));
                accentSecondary.setValue(toJfx(palette.get(1)));
                accentTernary.setValue(toJfx(palette.get(2)));
                bgPrimary.setValue(toJfx(palette.get(3)));
                themePreview.setImage(new Image(imageFile.toURI().toString()));
                applyManualColorChanges();
            }
        } catch (Exception e) { System.err.println("Theme Load Error: " + e.getMessage()); }
    }

    // ── Boilerplate Helpers ───────────────────────────────────────────────
    @FXML void toggleThemeMode(ActionEvent event) {
        if (!carouselImages.isEmpty() && currentIndex >= 0) applyTheme(carouselImages.get(currentIndex));
        else {
            boolean isLight = lightModeCheck.isSelected();
            Color cp = accentPrimary.getValue();
            bgPrimary.setValue(Color.hsb(cp.getHue(), cp.getSaturation() * 0.1, isLight ? 0.95 : 0.05));
            applyManualColorChanges();
        }
    }

    @FXML void onImagePick(ActionEvent event) {
        FileChooser picker = new FileChooser();
        File chosen = picker.showOpenDialog((Stage) paneMaster.getScene().getWindow());
        if (chosen != null) {
            carouselImages.add(chosen);
            currentIndex = carouselImages.size() - 1;
            applyTheme(chosen);
        }
    }

    @FXML void nextTheme(ActionEvent event) { if (!carouselImages.isEmpty()) applyTheme(carouselImages.get(currentIndex = (currentIndex + 1) % carouselImages.size())); }
    @FXML void prevTheme(ActionEvent event) { if (!carouselImages.isEmpty()) applyTheme(carouselImages.get(currentIndex = (currentIndex - 1 + carouselImages.size()) % carouselImages.size())); }

    @FXML void toggleFullscreen(ActionEvent event) {
        Stage stage = (Stage) paneMaster.getScene().getWindow();
        stage.setFullScreen(!stage.isFullScreen());
    }

    private String toHex(Color c) { return String.format("#%02x%02x%02x", (int)(c.getRed()*255), (int)(c.getGreen()*255), (int)(c.getBlue()*255)); }
    private Color toJfx(java.awt.Color a) { return new Color(a.getRed()/255.0, a.getGreen()/255.0, a.getBlue()/255.0, 1.0); }

    @FXML void onPrimary(ActionEvent event) { applyManualColorChanges(); }
    @FXML void onSecondary(ActionEvent event) { applyManualColorChanges(); }
    @FXML void onTernary(ActionEvent event) { applyManualColorChanges(); }
    @FXML void onBgPrimary(ActionEvent event) { applyManualColorChanges(); }

    @Override public void setContentRoot(StackPane contentRoot) { this.contentRoot = contentRoot; }
    @FXML void toggleFPSVisible(ActionEvent event) {}
    @FXML void deleteAccount(ActionEvent event) {}
}