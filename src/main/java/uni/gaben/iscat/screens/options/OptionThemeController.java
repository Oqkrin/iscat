package uni.gaben.iscat.screens.options;

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
import uni.gaben.iscat.utils.AudioManager;
import uni.gaben.iscat.utils.design.ScalareAureo;
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
    @FXML private ImageView themePreview;
    @FXML private Button prevThemeBtn, nextThemeBtn;

    private final List<File> carouselImages = new ArrayList<>();
    private int currentIndex = -1;
    private AnimationTimer uiRainbowSyncTimer;
    private ColorPicker activePicker = null;
    private final List<Color> currentPalette = new ArrayList<>();
    private final Map<ColorPicker, StackPane> pickerBoxes = new HashMap<>();
    private Pane paneMaster;
    private boolean isUpdatingProgrammatically = false;

    @FXML
    public void initialize() {
        syncColorPickersWithTheme();
        buildCustomPickers();

        themePreview.managedProperty().bind(themePreview.imageProperty().isNotNull());
        themePreview.visibleProperty().bind(themePreview.imageProperty().isNotNull());
        themePreview.fitWidthProperty().bind(theme.widthProperty());
        themePreview.fitHeightProperty().bind(theme.heightProperty().multiply(ScalareAureo.IPHI_D * ScalareAureo.IPHI_D));
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
        pickerBoxes.put(picker, colorBox);

        row.getChildren().add(widget);
    }

    private void setActivePicker(ColorPicker picker) {
        pickerBoxes.values().forEach(box -> box.getStyleClass().remove("picker-active"));
        if (picker != null && pickerBoxes.containsKey(picker)) {
            pickerBoxes.get(picker).getStyleClass().add("picker-active");
        }
        activePicker = picker;
    }

    public void applyManualColorChanges() {
        if (paneMaster == null || paneMaster.getScene() == null) return;
        ThemeManager.getInstance().stopRainbowMode();
        if (uiRainbowSyncTimer != null) uiRainbowSyncTimer.stop();

        List<String> hexPalette = List.of(toHex(accentPrimary.getValue()), toHex(accentSecondary.getValue()), toHex(accentTernary.getValue()), toHex(bgPrimary.getValue()));
        ThemeManager.getInstance().applyHexColorsTheme(paneMaster.getScene(), hexPalette, 0.2);
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
            updateCarouselButtons();
        }
    }

    @FXML void nextTheme(ActionEvent event) { if (!carouselImages.isEmpty()) { currentIndex = (currentIndex + 1) % carouselImages.size(); applyTheme(carouselImages.get(currentIndex)); } }
    @FXML void prevTheme(ActionEvent event) { if (!carouselImages.isEmpty()) { currentIndex = (currentIndex - 1 + carouselImages.size()) % carouselImages.size(); applyTheme(carouselImages.get(currentIndex)); } }

    private void applyTheme(File imageFile) {
        try {
            ThemeManager.getInstance().stopRainbowMode();
            if (uiRainbowSyncTimer != null) uiRainbowSyncTimer.stop();
            BufferedImage bufferedImage = ImageIO.read(imageFile);
            if (bufferedImage == null) return;

            int[][] rawPalette = ColorThief.getPalette(bufferedImage, 16, 1, false);
            currentPalette.clear();
            for (int[] rgb : rawPalette) currentPalette.add(Color.rgb(rgb[0], rgb[1], rgb[2]));

            rebuildPaletteUI();
            assignPickersFromPalette();
            themePreview.setImage(new Image(imageFile.toURI().toString()));
            applyManualColorChanges();
            updateCarouselButtons();
        } catch (IOException e) { System.err.println(e.getMessage()); }
    }

    private void rebuildPaletteUI() {
        paletteHolder.getChildren().clear();
        if (currentPalette.isEmpty()) return;
        List<Color> sorted = currentPalette.stream().sorted(Comparator.comparingDouble(this::luminance)).toList();
        double diameter = Math.max(8, Math.min(24, (theme.getWidth() - 28 - (5 * (sorted.size() - 1))) / sorted.size()));

        for (Color color : sorted) {
            Circle circle = new Circle(diameter / 2.0, color);
            circle.getStyleClass().add("palette-swatch");
            circle.setOnMouseClicked(e -> { if (activePicker != null) { activePicker.setValue(color); applyManualColorChanges(); } });
            paletteHolder.getChildren().add(circle);
        }
    }

    private void assignPickersFromPalette() {
        if (currentPalette.isEmpty()) return;
        Color bg = currentPalette.stream().max(Comparator.comparingDouble(c -> lightModeCheck.isSelected() ? luminance(c) : -luminance(c))).orElse(Color.BLACK);

        isUpdatingProgrammatically = true;
        bgPrimary.setValue(bg);
        List<Color> accents = currentPalette.stream().filter(c -> !c.equals(bg)).toList();
        if (!accents.isEmpty()) accentPrimary.setValue(accents.get(0));
        if (accents.size() >= 2) accentSecondary.setValue(accents.get(1));
        if (accents.size() >= 3) accentTernary.setValue(accents.get(2));
        isUpdatingProgrammatically = false;
    }

    private void startUiSyncTimer() {
        if (uiRainbowSyncTimer != null) uiRainbowSyncTimer.stop();
        uiRainbowSyncTimer = new AnimationTimer() {
            @Override public void handle(long now) {
                Color c = ThemeManager.getInstance().getAccentPrimary();

                isUpdatingProgrammatically = true;
                accentPrimary.setValue(c);
                accentSecondary.setValue(c);
                accentTernary.setValue(c);
                isUpdatingProgrammatically = false;
            }
        };
        uiRainbowSyncTimer.start();
    }

    @FXML void toggleThemeMode(ActionEvent event) { toggleThemeModeLogic(); }
    private void toggleThemeModeLogic() {
        if (!carouselImages.isEmpty() && currentIndex >= 0) { applyTheme(carouselImages.get(currentIndex)); }
        else {
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
    private void updateCarouselButtons() { boolean show = carouselImages.size() > 1; prevThemeBtn.setVisible(show); nextThemeBtn.setVisible(show); }
    private String toHex(Color c) { return String.format("#%02x%02x%02x", (int)(c.getRed()*255), (int)(c.getGreen()*255), (int)(c.getBlue()*255)); }
}