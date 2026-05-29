package uni.gaben.iscat.screens.options;

import de.androidpit.colorthief.ColorThief;
import javafx.animation.AnimationTimer;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OptionsMenuController implements IscatFxmlController {

    private StackPane contentRoot;

    @FXML private Slider BGMSlider;
    @FXML private Button DeleteAccountBtn;
    @FXML private Button ExitBtn;
    @FXML private CheckBox FullscreenCheck;
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
    @FXML private GridPane paletteHolder;          // now a GridPane
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

    // New fields for revised layout
    @FXML private StackPane imageArea;
    @FXML private Button pickImageBtn;              // placeholder button (no image)
    @FXML private Button changeImageBtn;            // small "Change" button next to carousel
    @FXML private HBox modeToggleBox;

    private final List<File> carouselImages = new ArrayList<>();
    private int currentIndex = -1;

    private Button selectedButton = null;
    private String selectedColumn = null;
    private AnimationTimer uiRainbowSyncTimer;

    private ColorPicker activePicker = null;
    private List<Color> currentPalette = new ArrayList<>();

    // Map to link picker to its custom box for active highlighting
    private final Map<ColorPicker, StackPane> pickerBoxes = new HashMap<>();

    @FXML
    public void initialize() {

        applyIconButton(ExitBtn,            "fas-sign-out-alt");
        applyIconButton(pickImageBtn,        "fas-image");
        applyIconButton(ResetAccountBtn,    "fas-history");
        applyIconButton(DeleteAccountBtn,   "fas-trash-alt");
        applyIconButton(resetControlsBtn,   "fas-sync-alt");

        // ── Audio Listeners ───────────────────────────────────────────────────
        masterSlider.valueProperty().addListener((obs, old, val) ->
                AudioManager.getInstance().setBgmVolume(val.doubleValue()));
        BGMSlider.valueProperty().addListener((obs, old, val) ->
                AudioManager.getInstance().setBgmVolume(val.doubleValue()));
        SFXSlider.valueProperty().addListener((obs, old, val) ->
                AudioManager.getInstance().setSfxVolume(val.doubleValue()));

        // ── Scale & Theme Adjustments ─────────────────────────────────────────
        scaleSlider.valueProperty().addListener((obs, old, val) ->
                UU.setUniverseScale(val.doubleValue()));
        refreshButtonLabels();
        syncColorPickersWithTheme();

        paneMaster.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleGlobalKeyPress);
                Stage stage = (Stage) newScene.getWindow();
                if (stage != null) {
                    FullscreenCheck.selectedProperty().bind(stage.fullScreenProperty());
                }
                applyManualColorChanges();
                if (ThemeManager.getInstance().isRainbowModeActive()) {
                    startUiSyncTimer();
                }
            }
        });

        // ── Image preview & placeholder button ───────────────────────────────
        themePreview.managedProperty().bind(themePreview.imageProperty().isNotNull());
        themePreview.visibleProperty().bind(themePreview.imageProperty().isNotNull());
        themePreview.fitWidthProperty().bind(theme.widthProperty());
        themePreview.fitHeightProperty().bind(theme.heightProperty()
                .multiply(ScalareAureo.IPHI_D * ScalareAureo.IPHI_D));

        // When no image: show placeholder button, hide image + change button
        // When image: hide placeholder, show image + change button
        themePreview.imageProperty().addListener((obs, oldImg, newImg) -> {
            boolean hasImage = (newImg != null);
            pickImageBtn.setVisible(!hasImage);
            changeImageBtn.setVisible(hasImage);
        });
        // initial state
        pickImageBtn.setVisible(true);
        changeImageBtn.setVisible(false);

        pickImageBtn.setOnAction(e -> onImagePick(null));
        changeImageBtn.setOnAction(e -> onImagePick(null));

        // ── Build custom colour pickers ──────────────────────────────────────
        buildCustomPickers();

        // ── Palette click targets (still use the hidden pickers) ─────────────
        setupPickerClickTarget(accentPrimary);
        setupPickerClickTarget(accentSecondary);
        setupPickerClickTarget(accentTernary);
        setupPickerClickTarget(bgPrimary);
    }

    // ── Custom picker construction ───────────────────────────────────────────
    private void buildCustomPickers() {
        buildCustomPicker(accentPrimary,  "Primary",    0, 0);
        buildCustomPicker(accentSecondary,"Secondary",  0, 1);
        buildCustomPicker(accentTernary,  "Tertiary",   1, 0);
        buildCustomPicker(bgPrimary,      "Background", 1, 1);
    }

    private void buildCustomPicker(ColorPicker picker, String role, int row, int col) {
        picker.setVisible(false);
        picker.setManaged(false);

        Rectangle rect = new Rectangle(60, 28);
        rect.setArcWidth(8);
        rect.setArcHeight(8);
        rect.fillProperty().bind(picker.valueProperty());

        Label roleLabel = new Label(role);
        roleLabel.setFont(Font.font("System", FontWeight.BOLD, 10));
        roleLabel.textFillProperty().bind(Bindings.createObjectBinding(() ->
                        picker.getValue().getBrightness() > 0.5 ? Color.BLACK : Color.WHITE,
                picker.valueProperty()));

        StackPane colorBox = new StackPane(rect, roleLabel);
        colorBox.getStyleClass().add("custom-color-box");
        colorBox.setOnMouseClicked(e -> setActivePicker(picker));

        Button arrowBtn = new Button("▼");
        arrowBtn.getStyleClass().add("arrow-button");
        arrowBtn.setOnAction(e -> picker.show());

        HBox widget = new HBox(2, colorBox, arrowBtn);
        widget.setAlignment(Pos.CENTER_LEFT);

        // Store for active highlighting
        pickerBoxes.put(picker, colorBox);

        // Update theme whenever the picker value changes
        picker.valueProperty().addListener((obs, oldVal, newVal) -> applyManualColorChanges());

        // Place widget in the grid
        GridPane grid = (GridPane) picker.getParent();
        GridPane.setConstraints(widget, col, row);
        grid.getChildren().add(widget);
    }

    // ── Active picker highlighting ───────────────────────────────────────────
    private void setupPickerClickTarget(ColorPicker picker) {
        picker.setOnMouseClicked(e -> setActivePicker(picker));
    }

    private void setActivePicker(ColorPicker picker) {
        // Remove highlight from all picker boxes
        for (StackPane box : pickerBoxes.values()) {
            box.getStyleClass().remove("picker-active");
        }
        // Highlight the selected one
        if (picker != null) {
            StackPane box = pickerBoxes.get(picker);
            if (box != null) {
                box.getStyleClass().add("picker-active");
            }
        }
        activePicker = picker;
    }

    // ── Key Bindings ─────────────────────────────────────────────────────────
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
        if (selectedButton == walkUp) selectedColumn = "WalkUp";
        else if (selectedButton == walkDown) selectedColumn = "WalkDown";
        else if (selectedButton == walkLeft) selectedColumn = "WalkLeft";
        else if (selectedButton == walkRight) selectedColumn = "WalkRight";
        else if (selectedButton == dash1) selectedColumn = "Dash1";
        else if (selectedButton == dash2) selectedColumn = "Dash2";
        else if (selectedButton == esc) selectedColumn = "PauseGame";
    }

    private void handleGlobalKeyPress(KeyEvent event) {
        if (selectedButton == null || selectedColumn == null) return;
        String pressedKey = event.getCode().toString();
        UserSettings settings = SessionManager.getInstance().getCurrentSettings();
        if (settings != null) {
            switch (selectedColumn) {
                case "WalkUp"    -> settings.setWalkUp(pressedKey);
                case "WalkDown"  -> settings.setWalkDown(pressedKey);
                case "WalkLeft"  -> settings.setWalkLeft(pressedKey);
                case "WalkRight" -> settings.setWalkRight(pressedKey);
                case "Dash1"     -> settings.setDash1(pressedKey);
                case "Dash2"     -> settings.setDash2(pressedKey);
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
        int uid = settings.getUserId();
        SettingsDAO.updateControl(uid, "WalkUp", "W");
        SettingsDAO.updateControl(uid, "WalkDown", "S");
        SettingsDAO.updateControl(uid, "WalkLeft", "A");
        SettingsDAO.updateControl(uid, "WalkRight", "D");
        SettingsDAO.updateControl(uid, "Dash1", "Space");
        SettingsDAO.updateControl(uid, "Dash2", "Middle Mouse");
        SettingsDAO.updateControl(uid, "PauseGame", "ESC");
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

    // ── Palette extraction and UI building ───────────────────────────────────
    private void applyTheme(File imageFile) {
        try {
            ThemeManager.getInstance().stopRainbowMode();
            if (uiRainbowSyncTimer != null) uiRainbowSyncTimer.stop();

            BufferedImage bufferedImage = ImageIO.read(imageFile);
            if (bufferedImage == null) {
                System.err.println("Unable to read image: " + imageFile.getAbsolutePath());
                return;
            }

            int[][] rawPalette = ColorThief.getPalette(bufferedImage, 16, 1, false);

            currentPalette.clear();
            for (int[] rgb : rawPalette) {
                currentPalette.add(Color.rgb(rgb[0], rgb[1], rgb[2]));
            }

            rebuildPaletteUI();
            assignPickersFromPalette();

            themePreview.setImage(new Image(imageFile.toURI().toString()));
            applyManualColorChanges();
        } catch (IOException e) {
            System.err.println("Theme Load Error: " + e.getMessage());
        }
    }

    private void rebuildPaletteUI() {
        paletteHolder.getChildren().clear();
        if (currentPalette.isEmpty()) return;

        final int COLUMNS = 8;
        int row = 0, col = 0;
        for (Color color : currentPalette) {
            Rectangle rect = new Rectangle(24, 24);
            rect.setFill(color);
            rect.getStyleClass().add("palette-swatch");
            Tooltip.install(rect, new Tooltip("Click to assign to selected picker"));

            rect.setOnMouseClicked(e -> {
                if (activePicker != null) {
                    activePicker.setValue(color);
                    applyManualColorChanges();
                }
            });

            paletteHolder.add(rect, col, row);
            col++;
            if (col == COLUMNS) {
                col = 0;
                row++;
            }
        }
    }

    private void assignPickersFromPalette() {
        if (currentPalette.isEmpty()) return;

        Color bg = pickBackground(currentPalette, lightModeCheck.isSelected());
        bgPrimary.setValue(bg);

        List<Color> accents = new ArrayList<>();
        for (Color c : currentPalette) {
            if (!equalsByRGB(c, bg)) {
                accents.add(c);
            }
        }
        if (!accents.isEmpty()) accentPrimary.setValue(accents.get(0));
        if (accents.size() >= 2) accentSecondary.setValue(accents.get(1));
        if (accents.size() >= 3) accentTernary.setValue(accents.get(2));
    }

    // ── Color utilities ──────────────────────────────────────────────────────
    private static boolean equalsByRGB(Color a, Color b) {
        return a.getRed() == b.getRed() &&
                a.getGreen() == b.getGreen() &&
                a.getBlue() == b.getBlue();
    }

    private Color pickBackground(List<Color> palette, boolean lightMode) {
        return palette.stream()
                .max(java.util.Comparator.comparingDouble(c -> lightMode ? luminance(c) : -luminance(c)))
                .orElse(lightMode ? Color.WHITE : Color.BLACK);
    }

    private double luminance(Color c) {
        double r = linearize(c.getRed());
        double g = linearize(c.getGreen());
        double b = linearize(c.getBlue());
        return 0.2126 * r + 0.7152 * g + 0.0722 * b;
    }

    private double linearize(double channel) {
        return (channel <= 0.03928) ? channel / 12.92 : Math.pow((channel + 0.055) / 1.055, 2.4);
    }

    // ── Toggles ──────────────────────────────────────────────────────────────
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
        File chosen = picker.showOpenDialog(paneMaster.getScene().getWindow());
        if (chosen != null) {
            carouselImages.add(chosen);
            currentIndex = carouselImages.size() - 1;
            applyTheme(chosen);
        }
    }

    @FXML
    void nextTheme(ActionEvent event) {
        if (!carouselImages.isEmpty()) {
            currentIndex = (currentIndex + 1) % carouselImages.size();
            applyTheme(carouselImages.get(currentIndex));
        }
    }

    @FXML
    void prevTheme(ActionEvent event) {
        if (!carouselImages.isEmpty()) {
            currentIndex = (currentIndex - 1 + carouselImages.size()) % carouselImages.size();
            applyTheme(carouselImages.get(currentIndex));
        }
    }

    @FXML
    void toggleFullscreen(ActionEvent event) {
        Stage stage = (Stage) paneMaster.getScene().getWindow();
        stage.setFullScreen(!stage.isFullScreen());
    }

    private String toHex(Color c) {
        return String.format("#%02x%02x%02x", (int)(c.getRed()*255), (int)(c.getGreen()*255), (int)(c.getBlue()*255));
    }

    @FXML void onPrimary(ActionEvent event)   { applyManualColorChanges(); }
    @FXML void onSecondary(ActionEvent event) { applyManualColorChanges(); }
    @FXML void onTernary(ActionEvent event)   { applyManualColorChanges(); }
    @FXML void onBgPrimary(ActionEvent event) { applyManualColorChanges(); }

    @Override public void setContentRoot(StackPane contentRoot) { this.contentRoot = contentRoot; }
    @FXML void toggleFPSVisible(ActionEvent event) {}
    @FXML void deleteAccount(ActionEvent event) {}
}