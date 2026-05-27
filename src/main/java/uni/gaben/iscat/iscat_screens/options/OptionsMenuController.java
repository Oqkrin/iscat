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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
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
    @FXML private Pane paneMaster;
    @FXML private Label skinNameLabel;
    @FXML private Label skinNameLabel1;
    @FXML private Label skinNameLabel2;
    @FXML private Label skinNameLabel11;
    @FXML private VBox controlsBox;

    @FXML private Button walkUp;
    @FXML private Button walkDown;
    @FXML private Button walkLeft;
    @FXML private Button walkRight;
    @FXML private Button dash1;
    @FXML private Button dash2;
    @FXML private Button esc;

    @FXML private Button ImagePicker;
    @FXML private CheckBox lightModeCheck;
    @FXML private ColorPicker accentPrimary;
    @FXML private ColorPicker accentSecondary;
    @FXML private ColorPicker accentTernary;
    @FXML private ColorPicker bgPrimary;

    // Theme Carousel Elements
    @FXML private ImageView themePreview;
    private List<File> carouselImages = new ArrayList<>();
    private int currentIndex = -1;

    private Button selectedButton = null;
    private String selectedColumn = null;

    // Timer locale per aggiornare i ColorPicker nell'interfaccia delle opzioni
    private AnimationTimer uiRainbowSyncTimer;

    @FXML
    public void initialize() {
        // Audio Controls
        BGMSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            AudioManager.getInstance().setBgmVolume(newValue.doubleValue());
        });

        SFXSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            AudioManager.getInstance().setSfxVolume(newValue.doubleValue());
        });

        // UI Initialization Sync
        refreshButtonLabels();
        syncColorPickersWithTheme();

        // Configurazione del listener della scena per catturare l'attivazione dell'arcobaleno
        paneMaster.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleGlobalKeyPress);

                // Se era già attivo globalmente da un gameplay precedente, aggancia il sync
                if (ThemeManager.getInstance().isRainbowModeActive()) {
                    startUiSyncTimer();
                }
            }
        });
    }

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
        if (selectedButton != null) {
            refreshButtonLabels();
        }

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
                case "PauseGame" -> settings.setPauseGame(pressedKey);
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
        if (uiRainbowSyncTimer != null) {
            uiRainbowSyncTimer.stop();
        }
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

        settings.setWalkUp("W");
        settings.setWalkDown("S");
        settings.setWalkLeft("A");
        settings.setWalkRight("D");
        settings.setDash1("Space");
        settings.setDash2("Middle Mouse");
        settings.setPauseGame("ESC");

        refreshButtonLabels();
    }

    @FXML void toggleFPSVisible(ActionEvent event) {}
    @FXML void deleteAccount(ActionEvent event) {}

    @FXML
    void resetAccount(ActionEvent event) {
        if (paneMaster.getScene() == null) return;
        if (ThemeManager.getInstance().isRainbowModeActive()) {
            ThemeManager.getInstance().stopRainbowMode();
            if (uiRainbowSyncTimer != null) {
                uiRainbowSyncTimer.stop();
            }
            syncColorPickersWithTheme();
            AudioManager.getInstance().playSFX("laugh");
        } else {
            AudioManager.getInstance().playSFX("rainbow");
            ThemeManager.getInstance().startRainbowMode(paneMaster.getScene());
            startUiSyncTimer();
        }
    }

    private void startUiSyncTimer() {
        if (uiRainbowSyncTimer != null) {
            uiRainbowSyncTimer.stop();
        }

        uiRainbowSyncTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                // Allinea i selettori grafici delle opzioni (Primary, Secondary, Tertiary)
                Color currentArcobaleno = ThemeManager.getInstance().getAccentPrimary();
                accentPrimary.setValue(currentArcobaleno);
                accentSecondary.setValue(currentArcobaleno);
                accentTernary.setValue(currentArcobaleno);
            }
        };
        uiRainbowSyncTimer.start();
    }

    @FXML
    void toggleFullscreen(ActionEvent event) {
        if (paneMaster != null && paneMaster.getScene() != null) {
            Stage stage = (Stage) paneMaster.getScene().getWindow();
            stage.setFullScreen(!stage.isFullScreen());
        }
    }

    @Override
    public void setContentRoot(StackPane contentRoot) {
        this.contentRoot = contentRoot;
    }

    @FXML
    void updateScale(MouseEvent event) {
        UU.setUniverseScale(scaleSlider.getValue());
    }

    private void syncColorPickersWithTheme() {
        ThemeManager tm = ThemeManager.getInstance();
        accentPrimary.setValue(tm.getAccentPrimary());
        accentSecondary.setValue(tm.getAccentSecondary());
        accentTernary.setValue(tm.getAccentTernary());
        bgPrimary.setValue(tm.getBgPrimary());
    }

    private String toHex(Color color) {
        return String.format("#%02x%02x%02x",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255)
        );
    }

    private void applyManualColorChanges() {
        if (paneMaster.getScene() == null) return;

        // Disattiva la RainbowMode se l'utente sceglie un colore manualmente
        ThemeManager.getInstance().stopRainbowMode();
        if (uiRainbowSyncTimer != null) {
            uiRainbowSyncTimer.stop();
        }

        List<String> hexPalette = List.of(
                toHex(accentPrimary.getValue()),
                toHex(accentSecondary.getValue()),
                toHex(accentTernary.getValue()),
                toHex(bgPrimary.getValue())
        );

        ThemeManager.getInstance().applyHexColorsTheme(paneMaster.getScene(), hexPalette, 0.2);
    }

    // --- Dynamic Theme Integration & Carousel ---

    @FXML
    void toggleThemeMode(ActionEvent event) {
        ThemeManager.getInstance().stopRainbowMode();
        if (uiRainbowSyncTimer != null) uiRainbowSyncTimer.stop();

        if (!carouselImages.isEmpty() && currentIndex >= 0) {
            applyTheme(carouselImages.get(currentIndex));
        } else {
            boolean isLight = lightModeCheck.isSelected();
            Color currentPrimary = accentPrimary.getValue();

            double targetBrightness = isLight ? 0.95 : 0.05;
            Color fallbackBg = Color.hsb(currentPrimary.getHue(), currentPrimary.getSaturation() * 0.1, targetBrightness);

            bgPrimary.setValue(fallbackBg);
            applyManualColorChanges();
        }
    }

    @FXML
    void onImagePick(ActionEvent event) {
        FileChooser imagePicker = new FileChooser();
        imagePicker.setTitle("Select Custom Theme Image Source");
        imagePicker.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.bmp")
        );

        Stage currentStage = (Stage) paneMaster.getScene().getWindow();
        File chosenImage = imagePicker.showOpenDialog(currentStage);

        if (chosenImage != null) {
            carouselImages.add(chosenImage);
            currentIndex = carouselImages.size() - 1;
            applyTheme(chosenImage);
        }
    }

    private void applyTheme(File imageFile) {
        try {
            ThemeManager.getInstance().stopRainbowMode();
            if (uiRainbowSyncTimer != null) uiRainbowSyncTimer.stop();

            boolean isLightMode = lightModeCheck.isSelected();
            List<java.awt.Color> palette = DynamicColors.getTopDistinctColors(
                    ImageIO.read(imageFile), 4, isLightMode
            );

            if (palette.size() >= 4) {
                accentPrimary.setValue(toJfx(palette.get(0)));
                accentSecondary.setValue(toJfx(palette.get(1)));
                accentTernary.setValue(toJfx(palette.get(2)));
                bgPrimary.setValue(toJfx(palette.get(3)));

                themePreview.setImage(new Image(imageFile.toURI().toString()));
                applyManualColorChanges();
            }
        } catch (IOException e) {
            System.err.println("Failed to load theme image: " + e.getMessage());
        }
    }

    @FXML
    void nextTheme(ActionEvent event) {
        if (carouselImages.isEmpty()) return;
        currentIndex = (currentIndex + 1) % carouselImages.size();
        applyTheme(carouselImages.get(currentIndex));
    }

    @FXML
    void prevTheme(ActionEvent event) {
        if (carouselImages.isEmpty()) return;
        currentIndex = (currentIndex - 1 + carouselImages.size()) % carouselImages.size();
        applyTheme(carouselImages.get(currentIndex));
    }

    private Color toJfx(java.awt.Color awt) {
        return new Color(
                awt.getRed() / 255.0,
                awt.getGreen() / 255.0,
                awt.getBlue() / 255.0,
                1.0
        );
    }

    @FXML void onPrimary(ActionEvent event) { applyManualColorChanges(); }
    @FXML void onSecondary(ActionEvent event) { applyManualColorChanges(); }
    @FXML void onTernary(ActionEvent event) { applyManualColorChanges(); }
    @FXML void onBgPrimary(ActionEvent event) { applyManualColorChanges(); }
}