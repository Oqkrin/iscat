package uni.gaben.iscat.iscat_screens.options;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
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

import java.io.File;
import java.util.List;

public class OptionsMenuController implements IscatFxmlController {

    private StackPane contentRoot;

    @FXML private VBox mainOptions;
    @FXML private Slider BGMSlider;
    @FXML private Slider SFXSlider;
    @FXML private Slider scaleSlider;
    @FXML private Pane paneMaster;
    @FXML private Label skinNameLabel;
    @FXML private Label skinNameLabel1;
    @FXML private VBox controlsBox;

    @FXML private Button walkUp;
    @FXML private Button walkDown;
    @FXML private Button walkLeft;
    @FXML private Button walkRight;
    @FXML private Button dash1;
    @FXML private Button dash2;
    @FXML private Button esc;

    @FXML private Button ImagePicker;
    @FXML private ColorPicker accentPrimary;
    @FXML private ColorPicker accentSecondary;
    @FXML private ColorPicker accentTernary;
    @FXML private ColorPicker bgPrimary; // Fourth configuration node injected here

    private Button selectedButton = null;
    private String selectedColumn = null;

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

        // Global Key Listener Configuration
        paneMaster.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleGlobalKeyPress);
            }
        });
    }

    /**
     * Legge le impostazioni correnti dal SessionManager e imposta il testo corretto dei pulsanti.
     */
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

    /**
     * Azione FXML unica associata a TUTTI i pulsanti di controllo nel file .fxml.
     * Configura il rispettivo ID del pulsante e la colonna SQL corrispondente.
     */
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

    /**
     * Cattura la pressione fisica del tasto sulla tastiera
     */
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
    @FXML void resetAccount(ActionEvent event) {}

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

    private void toggleVisibilityMainOptions() {
        boolean show = !mainOptions.isVisible();
        mainOptions.setVisible(show);
        mainOptions.setManaged(show);
        if (show) mainOptions.toFront();
    }

    private void toggleVisibilityControls() {
        boolean show = !controlsBox.isVisible();
        controlsBox.setVisible(show);
        controlsBox.setManaged(show);
        if (show) controlsBox.toFront();
    }

    @FXML
    void updateScale(MouseEvent event) {
        UU.setUniverseScale(scaleSlider.getValue());
    }

    /**
     * Reads current running runtime values from ThemeManager and matches the
     * ColorPicker states to them.
     */
    private void syncColorPickersWithTheme() {
        ThemeManager tm = ThemeManager.getInstance();
        accentPrimary.setValue(tm.getAccentPrimary());
        accentSecondary.setValue(tm.getAccentSecondary());
        accentTernary.setValue(tm.getAccentTernary());
        bgPrimary.setValue(tm.getBgPrimary()); // Unified initialization value synchronizer
    }

    /**
     * Converts a JavaFX Color structure cleanly to a web-safe hex string (#RRGGBB).
     */
    private String toHex(Color color) {
        return String.format("#%02x%02x%02x",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255)
        );
    }

    /**
     * Collects all manual user color selections from the ColorPickers and flushes them
     * to the running theme.
     */
    private void applyManualColorChanges() {
        if (paneMaster.getScene() == null) return;

        List<String> hexPalette = List.of(
                toHex(accentPrimary.getValue()),
                toHex(accentSecondary.getValue()),
                toHex(accentTernary.getValue()),
                toHex(bgPrimary.getValue()) // Appends manual foundation layer selection cleanly
        );

        ThemeManager.getInstance().applyHexColorsTheme(paneMaster.getScene(), hexPalette, 0.2);
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
            ThemeManager.getInstance().applyDynamicImageTheme(paneMaster.getScene(), chosenImage, 0.8);
            syncColorPickersWithTheme();
        }
    }

    @FXML void onPrimary(ActionEvent event) { applyManualColorChanges(); }
    @FXML void onSecondary(ActionEvent event) { applyManualColorChanges(); }
    @FXML void onTernary(ActionEvent event) { applyManualColorChanges(); }
    @FXML void onBgPrimary(ActionEvent event) { applyManualColorChanges(); } // Added target capture action wrapper
}