package uni.gaben.iscat.controller.components.options;

import de.androidpit.colorthief.ColorThief;
import javafx.animation.AnimationTimer;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import uni.gaben.iscat.database.IscatDB;
import uni.gaben.iscat.database.dao.SettingsDAO;
import uni.gaben.iscat.model.user.UserSettings;
import uni.gaben.iscat.utils.AudioManager;
import uni.gaben.iscat.utils.SessionManager;
import uni.gaben.iscat.utils.theme.ThemeManager;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class OptionThemeController {
    @FXML private ColorPicker accentPrimary, accentSecondary, accentTernary, bgPrimary;
    @FXML private CheckBox lightModeCheck, rainbowModeCheck;
    @FXML private HBox paletteHolder, pickerRow1, pickerRow2;

    @FXML private VBox theme;

    @FXML private StackPane imageArea;
    @FXML private ImageView themePreview;
    @FXML private Button prevThemeBtn, nextThemeBtn;

    @FXML private Button pickImageBtn;
    @FXML private Button addImageBtn;

    private Pane paneMaster;
    private boolean isUpdatingProgrammatically = false;

    @FXML
    public void initialize() {
        SessionManager session = SessionManager.getInstance();

        session.pickerBoxes.clear();

        lightModeCheck.setSelected(session.isLightModeSelected);
        rainbowModeCheck.setSelected(ThemeManager.getInstance().isRainbowModeActive());

        syncColorPickersWithTheme();
        buildCustomPickers();

        if (addImageBtn != null) {
            addImageBtn.setOnAction(this::onImagePick);
        }

        if (session.activePicker != null) {
            setActivePicker(session.activePicker);
        }

        if (!session.currentPalette.isEmpty()) {
            rebuildPaletteUI();
        }

        if (!session.carouselImages.isEmpty() && session.currentIndex >= 0 && session.currentIndex < session.carouselImages.size()) {
            themePreview.setImage(new Image(session.carouselImages.get(session.currentIndex).toURI().toString()));
        }
        updateCarouselButtons();

        themePreview.managedProperty().bind(themePreview.imageProperty().isNotNull());
        themePreview.visibleProperty().bind(themePreview.imageProperty().isNotNull());

        themePreview.setFitWidth(320);
        themePreview.setFitHeight(180);

        Rectangle clip = new Rectangle();
        clip.setArcWidth(16);
        clip.setArcHeight(16);
        themePreview.layoutBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
            clip.setWidth(newBounds.getWidth());
            clip.setHeight(newBounds.getHeight());
        });
        themePreview.setClip(clip);
    }

    public void injectParentPane(Pane paneMaster) {
        this.paneMaster = paneMaster;
        if (ThemeManager.getInstance().isRainbowModeActive()) {
            startUiSyncTimer();
        }
    }

    private void buildCustomPickers() {
        buildCustomPicker(accentPrimary, "Primary", pickerRow1);
        buildCustomPicker(accentSecondary, "Secondary", pickerRow1);
        buildCustomPicker(accentTernary, "Tertiary", pickerRow2);
        buildCustomPicker(bgPrimary, "Background", pickerRow2);
    }

    private void buildCustomPicker(ColorPicker picker, String role, HBox row) {
        picker.setVisible(false);
        picker.setManaged(false);
        picker.valueProperty().addListener((obs, old, newVal) -> {
            if (!isUpdatingProgrammatically) {
                applyManualColorChanges();
            }
        });

        Rectangle rect = new Rectangle(60, 28);
        rect.setArcWidth(8); rect.setArcHeight(8);
        rect.fillProperty().bind(picker.valueProperty());

        Label roleLabel = new Label(role);
        roleLabel.setFont(Font.font("System", FontWeight.BOLD, 10));
        roleLabel.textFillProperty().bind(Bindings.createObjectBinding(() ->
                picker.getValue().getBrightness() > 0.5 ? Color.BLACK : Color.WHITE, picker.valueProperty()));

        StackPane colorBox = new StackPane(rect, roleLabel);
        colorBox.getStyleClass().add("custom-color-box");
        colorBox.setOnMouseClicked(e -> setActivePicker(picker));

        Button arrowBtn = new Button("▼");
        arrowBtn.getStyleClass().add("arrow-button");
        arrowBtn.setOnAction(e -> picker.show());

        HBox widget = new HBox(2, colorBox, arrowBtn);
        widget.setAlignment(Pos.CENTER_LEFT);

        SessionManager.getInstance().pickerBoxes.put(picker, colorBox);
        row.getChildren().add(widget);
    }

    private void setActivePicker(ColorPicker picker) {
        SessionManager.getInstance().pickerBoxes.values().forEach(box -> box.getStyleClass().remove("picker-active"));
        if (picker != null && SessionManager.getInstance().pickerBoxes.containsKey(picker)) {
            SessionManager.getInstance().pickerBoxes.get(picker).getStyleClass().add("picker-active");
        }
        SessionManager.getInstance().activePicker = picker;
    }

    public void applyManualColorChanges() {
        if (paneMaster == null || paneMaster.getScene() == null) return;
        ThemeManager.getInstance().stopRainbowMode();
        if (SessionManager.getInstance().uiRainbowSyncTimer != null) SessionManager.getInstance().uiRainbowSyncTimer.stop();
        rainbowModeCheck.setSelected(false);

        String hexPrimary = toHex(accentPrimary.getValue());
        String hexSecondary = toHex(accentSecondary.getValue());
        String hexTertiary = toHex(accentTernary.getValue());
        String hexBg = toHex(bgPrimary.getValue());

        UserSettings settings = SessionManager.getInstance().getCurrentSettings();

        if (settings != null) {
            settings.setPrimaryTheme(hexPrimary);
            settings.setSecondaryTheme(hexSecondary);
            settings.setTertiaryTheme(hexTertiary);
            settings.setBackgroundTheme(hexBg);

            IscatDB.getInstance().executeAsync(() -> {
                try {
                    SettingsDAO dao = IscatDB.getInstance().getSettingsDAO();
                    dao.updateThemeSetting(settings.getUserId(), "PrimaryTheme", hexPrimary);
                    dao.updateThemeSetting(settings.getUserId(), "SecondaryTheme", hexSecondary);
                    dao.updateThemeSetting(settings.getUserId(), "TertiaryTheme", hexTertiary);
                    dao.updateThemeSetting(settings.getUserId(), "BackgroundTheme", hexBg);
                } catch (Exception e) {
                    System.err.println("[ISCAT ERROR] Errore di scrittura nel Database: " + e.getMessage());
                }
            });
        }

        List<String> hexPalette = List.of(hexPrimary, hexSecondary, hexTertiary, hexBg);
        ThemeManager.getInstance().applyHexColorsTheme(paneMaster.getScene(), hexPalette, 0.2);
    }

    @FXML
    void toggleRainbowMode(ActionEvent event) {
        if (paneMaster.getScene() == null) return;
        boolean isActiveNow;

        if (ThemeManager.getInstance().isRainbowModeActive()) {
            ThemeManager.getInstance().stopRainbowMode();
            if (SessionManager.getInstance().uiRainbowSyncTimer != null) SessionManager.getInstance().uiRainbowSyncTimer.stop();
            syncColorPickersWithTheme();
            AudioManager.getInstance().playSFX("laugh");
            rainbowModeCheck.setSelected(false);
            isActiveNow = false;
        } else {
            AudioManager.getInstance().playSFX("rainbow");
            ThemeManager.getInstance().startRainbowMode(paneMaster.getScene());
            startUiSyncTimer();
            rainbowModeCheck.setSelected(true);
            isActiveNow = true;
        }

        UserSettings settings = SessionManager.getInstance().getCurrentSettings();
        if (settings != null) {
            int numericValue = isActiveNow ? 1 : 0;
            settings.setRainbowMode(numericValue);

            IscatDB.getInstance().executeAsync(() -> {
                try {
                    SettingsDAO dao = IscatDB.getInstance().getSettingsDAO();
                    dao.updateThemeSetting(settings.getUserId(), "RainbowMode", String.valueOf(numericValue));
                } catch (Exception e) {
                    System.err.println("[ISCAT ERROR] Impossibile salvare lo stato RainbowMode: " + e.getMessage());
                }
            });
        }
    }

    public void loadAndApplySavedTheme() {
        SessionManager session = SessionManager.getInstance();
        UserSettings settings = session.getCurrentSettings();

        isUpdatingProgrammatically = true;

        if (settings != null) {
            boolean dbLightMode = (settings.getLightmode() == 1);
            boolean dbRainbowMode = (settings.getRainbowMode() == 1);

            lightModeCheck.setSelected(dbLightMode);
            session.isLightModeSelected = dbLightMode;
            rainbowModeCheck.setSelected(dbRainbowMode);

            if (dbRainbowMode && !ThemeManager.getInstance().isRainbowModeActive() && paneMaster != null && paneMaster.getScene() != null) {
                ThemeManager.getInstance().startRainbowMode(paneMaster.getScene());
                startUiSyncTimer();
            }
        }

        if (settings != null && settings.getPrimaryTheme() != null && !settings.getPrimaryTheme().equalsIgnoreCase("#FFFFFF")) {
            accentPrimary.setValue(Color.web(settings.getPrimaryTheme()));
            accentSecondary.setValue(Color.web(settings.getSecondaryTheme()));
            accentTernary.setValue(Color.web(settings.getTertiaryTheme()));
            bgPrimary.setValue(Color.web(settings.getBackgroundTheme()));

            List<String> dbPalette = List.of(settings.getPrimaryTheme(), settings.getSecondaryTheme(), settings.getTertiaryTheme(), settings.getBackgroundTheme());
            if (paneMaster != null && paneMaster.getScene() != null) {
                ThemeManager.getInstance().applyHexColorsTheme(paneMaster.getScene(), dbPalette, 0.0);
            }
        } else {
            syncColorPickersWithTheme();
        }
        isUpdatingProgrammatically = false;
    }

    @FXML
    void onImagePick(ActionEvent event) {
        FileChooser picker = new FileChooser();
        File chosen = picker.showOpenDialog(paneMaster.getScene().getWindow());
        if (chosen != null) {
            SessionManager.getInstance().carouselImages.add(chosen);
            SessionManager.getInstance().currentIndex = SessionManager.getInstance().carouselImages.size() - 1;
            applyTheme(chosen);
            updateCarouselButtons();
        }
    }

    @FXML void nextTheme(ActionEvent event) { if (!SessionManager.getInstance().carouselImages.isEmpty()) { SessionManager.getInstance().currentIndex = (SessionManager.getInstance().currentIndex + 1) % SessionManager.getInstance().carouselImages.size(); applyTheme(SessionManager.getInstance().carouselImages.get(SessionManager.getInstance().currentIndex)); } }
    @FXML void prevTheme(ActionEvent event) { if (!SessionManager.getInstance().carouselImages.isEmpty()) { SessionManager.getInstance().currentIndex = (SessionManager.getInstance().currentIndex - 1 + SessionManager.getInstance().carouselImages.size()) % SessionManager.getInstance().carouselImages.size(); applyTheme(SessionManager.getInstance().carouselImages.get(SessionManager.getInstance().currentIndex)); } }

    private void applyTheme(File imageFile) {
        try {
            ThemeManager.getInstance().stopRainbowMode();
            if (SessionManager.getInstance().uiRainbowSyncTimer != null) SessionManager.getInstance().uiRainbowSyncTimer.stop();
            rainbowModeCheck.setSelected(false);

            BufferedImage bufferedImage = ImageIO.read(imageFile);
            if (bufferedImage == null) return;

            int[][] rawPalette = ColorThief.getPalette(bufferedImage, 16, 1, false);
            SessionManager.getInstance().currentPalette.clear();
            for (int[] rgb : rawPalette) SessionManager.getInstance().currentPalette.add(Color.rgb(rgb[0], rgb[1], rgb[2]));

            rebuildPaletteUI();
            assignPickersFromPalette();
            themePreview.setImage(new Image(imageFile.toURI().toString()));
            applyManualColorChanges();
            updateCarouselButtons();
        } catch (IOException e) { System.err.println(e.getMessage()); }
    }

    private void rebuildPaletteUI() {
        paletteHolder.getChildren().clear();
        if (SessionManager.getInstance().currentPalette.isEmpty()) return;
        List<Color> sorted = SessionManager.getInstance().currentPalette.stream().sorted(Comparator.comparingDouble(this::luminance)).toList();

        // Calcolo sicuro del diametro: usiamo una misura base fissa di 300px (la larghezza della card dei controlli meno i padding)
        double targetWidth = 300.0;
        double diameter = Math.max(8, Math.min(24, (targetWidth - (5 * (sorted.size() - 1))) / sorted.size()));

        for (Color color : sorted) {
            Circle circle = new Circle(diameter / 2.0, color);
            circle.getStyleClass().add("palette-swatch");
            circle.setOnMouseClicked(e -> {
                if (SessionManager.getInstance().activePicker != null) {
                    SessionManager.getInstance().activePicker.setValue(color);
                    applyManualColorChanges();
                }
            });
            paletteHolder.getChildren().add(circle);
        }
    }

    private void assignPickersFromPalette() {
        if (SessionManager.getInstance().currentPalette.isEmpty()) return;
        Color bg = SessionManager.getInstance().currentPalette.stream().max(Comparator.comparingDouble(c -> lightModeCheck.isSelected() ? luminance(c) : -luminance(c))).orElse(Color.BLACK);

        isUpdatingProgrammatically = true;
        bgPrimary.setValue(bg);
        List<Color> accents = SessionManager.getInstance().currentPalette.stream().filter(c -> !c.equals(bg)).toList();
        if (!accents.isEmpty()) accentPrimary.setValue(accents.get(0));
        if (accents.size() >= 2) accentSecondary.setValue(accents.get(1));
        if (accents.size() >= 3) accentTernary.setValue(accents.get(2));
        isUpdatingProgrammatically = false;
    }

    private void startUiSyncTimer() {
        if (SessionManager.getInstance().uiRainbowSyncTimer != null) SessionManager.getInstance().uiRainbowSyncTimer.stop();
        SessionManager.getInstance().uiRainbowSyncTimer = new AnimationTimer() {
            @Override public void handle(long now) {
                Color c = ThemeManager.getInstance().getAccentPrimary();

                isUpdatingProgrammatically = true;
                accentPrimary.setValue(c);
                accentSecondary.setValue(c);
                accentTernary.setValue(c);
                isUpdatingProgrammatically = false;
            }
        };
        SessionManager.getInstance().uiRainbowSyncTimer.start();
    }

    @FXML
    void toggleThemeMode(ActionEvent event) {
        boolean isLightSelected = lightModeCheck.isSelected();
        SessionManager.getInstance().isLightModeSelected = isLightSelected;

        UserSettings settings = SessionManager.getInstance().getCurrentSettings();
        if (settings != null) {
            int numericValue = isLightSelected ? 1 : 0;
            settings.setLightmode(numericValue);

            IscatDB.getInstance().executeAsync(() -> {
                try {
                    SettingsDAO dao = IscatDB.getInstance().getSettingsDAO();
                    dao.updateThemeSetting(settings.getUserId(), "Lightmode", String.valueOf(numericValue));
                } catch (Exception e) {
                    System.err.println("[ISCAT ERROR] Errore salvataggio Lightmode: " + e.getMessage());
                }
            });
        }

        toggleThemeModeLogic();
    }

    private void toggleThemeModeLogic() {
        if (!SessionManager.getInstance().carouselImages.isEmpty() && SessionManager.getInstance().currentIndex >= 0) {
            applyTheme(SessionManager.getInstance().carouselImages.get(SessionManager.getInstance().currentIndex));
        } else {
            Color cp = accentPrimary.getValue();
            bgPrimary.setValue(Color.hsb(cp.getHue(), cp.getSaturation() * 0.1, lightModeCheck.isSelected() ? 0.95 : 0.05));
            applyManualColorChanges();
        }
    }

    private void syncColorPickersWithTheme() {
        ThemeManager tm = ThemeManager.getInstance();
        isUpdatingProgrammatically = true;
        accentPrimary.setValue(tm.getAccentPrimary());
        accentSecondary.setValue(tm.getAccentSecondary());
        accentTernary.setValue(tm.getAccentTernary());
        bgPrimary.setValue(tm.getBgPrimary());
        isUpdatingProgrammatically = false;
    }

    private double luminance(Color c) { return 0.2126 * lin(c.getRed()) + 0.7152 * lin(c.getGreen()) + 0.0722 * lin(c.getBlue()); }
    private double lin(double ch) { return (ch <= 0.03928) ? ch / 12.92 : Math.pow((ch + 0.055) / 1.055, 2.4); }

    private void updateCarouselButtons() {
        int totalImages = SessionManager.getInstance().carouselImages.size();
        boolean hasImages = totalImages > 0;
        boolean showArrows = totalImages > 1;

        prevThemeBtn.setVisible(showArrows);
        prevThemeBtn.setManaged(showArrows);
        nextThemeBtn.setVisible(showArrows);
        nextThemeBtn.setManaged(showArrows);

        if (addImageBtn != null) {
            addImageBtn.setVisible(hasImages);
            addImageBtn.setManaged(hasImages);
        }
        if (pickImageBtn != null) {
            pickImageBtn.setVisible(!hasImages);
            pickImageBtn.setManaged(!hasImages);
        }
    }

    private String toHex(Color c) { return String.format("#%02x%02x%02x", (int)(c.getRed()*255), (int)(c.getGreen()*255), (int)(c.getBlue()*255)); }
}