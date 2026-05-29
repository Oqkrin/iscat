package uni.gaben.iscat.screens.options;

import javafx.animation.AnimationTimer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import uni.gaben.iscat.database.sqlite.ScoreDAO;
import uni.gaben.iscat.utils.AudioManager;
import uni.gaben.iscat.controller.IscatFxmlController;
import uni.gaben.iscat.IscatNavigator;
import uni.gaben.iscat.model.IscatViews;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.screens.login.model.UserSettings;
import uni.gaben.iscat.database.sqlite.SettingsDAO;
import uni.gaben.iscat.utils.SessionManager;
import uni.gaben.iscat.utils.design.ScalareAureo;
import uni.gaben.iscat.utils.theme.ThemeManager;
import uni.gaben.iscat.utils.theme.DynamicColors;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class OptionsMenuController implements IscatFxmlController {

    private StackPane contentRoot;

    @FXML private Slider BGMSlider;
    @FXML private Button DeleteAccountBtn;
    @FXML private Button ExitBtn;
    @FXML private CheckBox FullscreenCheck;
    @FXML private Button ImagePicker;
    @FXML private Button ResetAccountBtn;
    @FXML private Slider SFXSlider;
    @FXML private ColorPicker accentPrimary;
    @FXML private ColorPicker accentSecondary;
    @FXML private ColorPicker accentTernary;
    @FXML private VBox account;
    @FXML private VBox audio;
    @FXML private ColorPicker bgPrimary;
    @FXML private CheckBox checkFps;
    @FXML private Button dash1;
    @FXML private Button dash2;
    @FXML private VBox display;
    @FXML private Button esc;
    @FXML private VBox keybinds;
    @FXML private CheckBox lightModeCheck;
    @FXML private HBox paletteHolder;
    @FXML private CheckBox rainbowModeCheck;
    @FXML private Slider masterSlider;
    @FXML private VBox paneMaster;
    @FXML private Button resetControlsBtn;
    @FXML private Slider scaleSlider;
    @FXML private VBox theme;
    @FXML private ImageView themePreview;
    @FXML private Button walkDown;
    @FXML private Button walkLeft;
    @FXML private Button walkRight;
    @FXML private Button walkUp;

    private final List<File> carouselImages = new ArrayList<>();
    private int currentIndex = -1;

    private Button selectedButton = null;
    private String selectedColumn = null;
    private AnimationTimer uiRainbowSyncTimer;

    @FXML
    public void initialize() {
        // ── Audio Listeners ───────────────────────────────────────────────────
        masterSlider.valueProperty().addListener((obs, old, val) -> AudioManager.getInstance().setBgmVolume(val.doubleValue()));
        BGMSlider.valueProperty().addListener((obs, old, val) -> AudioManager.getInstance().setBgmVolume(val.doubleValue()));
        SFXSlider.valueProperty().addListener((obs, old, val) -> AudioManager.getInstance().setSfxVolume(val.doubleValue()));

        // ── Scale & Theme Adjustments ─────────────────────────────────────────
        scaleSlider.valueProperty().addListener((obs, old, val) -> UU.setUniverseScale(val.doubleValue()));
        refreshButtonLabels();
        syncColorPickersWithTheme();
        paneMaster.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventFilter(
                        KeyEvent.KEY_PRESSED,
                        this::handleGlobalKeyPress
                );
                Stage stage = (Stage) newScene.getWindow();
                if (stage != null) {
                    FullscreenCheck.selectedProperty()
                            .bind(stage.fullScreenProperty());
                }
                applyManualColorChanges();
                if (ThemeManager.getInstance().isRainbowModeActive()) {
                    startUiSyncTimer();
                }
            }
        });

        // ── Dynamic Responsive Layout Controls ───────────────────────────────
        themePreview.managedProperty().bind(themePreview.imageProperty().isNotNull());
        themePreview.visibleProperty().bind(themePreview.imageProperty().isNotNull());

        themePreview.fitWidthProperty().bind(theme.widthProperty());
        themePreview.fitHeightProperty().bind(theme.heightProperty().multiply(ScalareAureo.IPHI_D*ScalareAureo.IPHI_D));

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

    @FXML
    void changeControl(ActionEvent event) {
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
            }
            SettingsDAO.updateControl(settings.getUserId(), selectedColumn, pressedKey);
        }
        selectedButton.setText(pressedKey);
        selectedButton = null;
        selectedColumn = null;
        event.consume();
    }

    @FXML
    void handleBack(ActionEvent event) {
        if (uiRainbowSyncTimer != null) uiRainbowSyncTimer.stop();
        IscatNavigator.getInstance().navigateWithFade(IscatViews.MAIN_MENU);
    }

    @FXML
    void resetControls(ActionEvent event) {
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

    @FXML
    void resetAccount(ActionEvent event) {
        UserSettings settings = SessionManager.getInstance().getCurrentSettings();
        if (settings == null) return;
        int userId = settings.getUserId();
        ScoreDAO.reset(userId);
        SessionManager.getInstance().setCurrentSaveData(ScoreDAO.load(userId));
        AudioManager.getInstance().playSFX("laugh");
    }

    private void startUiSyncTimer() {
        if (uiRainbowSyncTimer != null) uiRainbowSyncTimer.stop();
        uiRainbowSyncTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                Color c = ThemeManager.getInstance().getAccentPrimary();
                accentPrimary.setValue(c);
                accentSecondary.setValue(c);
                accentTernary.setValue(c);
            }
        };
        uiRainbowSyncTimer.start();
    }

    // ── Theme Management ─────────────────────────────────────────────────────
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

            List<java.awt.Color> palette = DynamicColors.getPaletteForFile(imageFile, 16, lightModeCheck.isSelected());

            paletteHolder.getChildren().clear();
            paletteHolder.setSpacing(16);
            paletteHolder.setAlignment(Pos.CENTER);
            for (java.awt.Color color : palette) {
                Circle circle = new Circle(8);
                circle.setFill(toJfx(color));
                paletteHolder.getChildren().add(circle);
            }

            if (palette.size() >= 4) {
                accentPrimary.setValue(toJfx(palette.get(0)));
                accentSecondary.setValue(toJfx(palette.get(1)));
                accentTernary.setValue(toJfx(palette.get(2)));
                bgPrimary.setValue(toJfx(palette.get(3)));
                themePreview.setImage(new Image(imageFile.toURI().toString()));
                applyManualColorChanges();
            }
        } catch (Exception e) {
            System.err.println("Theme Load Error: " + e.getMessage());
        }
    }

    @FXML
    void toggleThemeMode(ActionEvent event) {
        if (!carouselImages.isEmpty() && currentIndex >= 0) {
            applyTheme(carouselImages.get(currentIndex));
        } else {
            boolean isLight = lightModeCheck.isSelected();
            Color cp = accentPrimary.getValue();
            bgPrimary.setValue(Color.hsb(cp.getHue(), cp.getSaturation() * 0.1, isLight ? 0.95 : 0.05));
            applyManualColorChanges();
        }
    }

    @FXML
    void toggleRainbowMode(ActionEvent event) {
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

    @FXML
    void onImagePick(ActionEvent event) {
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

    @FXML
    void toggleFullscreen(ActionEvent event) {
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