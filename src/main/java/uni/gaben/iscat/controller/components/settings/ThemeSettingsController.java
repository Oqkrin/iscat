package uni.gaben.iscat.controller.components.settings;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
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
import javafx.stage.PopupWindow;
import javafx.stage.Window;
import uni.gaben.iscat.IscatSettings;
import uni.gaben.iscat.model.settings.ThemeSettingsModel;
import uni.gaben.iscat.utils.theme.ThemeManager;

import java.io.File;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller per la scheda delle impostazioni del tema.
 * Gestisce la UI dei selettori colore personalizzati, del carosello di immagini e delle tavolozze.
 */
public class ThemeSettingsController {

    @FXML private ColorPicker accentPrimary, accentSecondary, accentTernary, bgPrimary;
    @FXML private CheckBox lightModeCheck, rainbowModeCheck;
    @FXML private HBox paletteHolder, pickerRow1, pickerRow2;

    @FXML private VBox theme;
    @FXML private StackPane imageArea;
    @FXML private ImageView themePreview;
    @FXML private Button prevThemeBtn, nextThemeBtn;
    @FXML private Button pickImageBtn;
    @FXML private Button restoreBtn;
    @FXML private Button addImageBtn;
    @FXML private Label paletteTip;

    private Pane paneMaster;
    private ThemeSettingsModel model;

    private final Map<ColorPicker, Label> pickerBoxes = new HashMap<>();
    private final ObjectProperty<ColorPicker> activePicker = new SimpleObjectProperty<>(null);

    @FXML
    public void initialize() {
        model = new ThemeSettingsModel();
        model.loadFromDatabase();

        // Binding proprietà booleane
        lightModeCheck.selectedProperty().bindBidirectional(model.lightModeProperty());
        rainbowModeCheck.selectedProperty().bindBidirectional(model.rainbowModeProperty());

        // Binding bidirezionale dei ColorPicker
        accentPrimary.valueProperty().bindBidirectional(model.accentPrimaryProperty());
        accentSecondary.valueProperty().bindBidirectional(model.accentSecondaryProperty());
        accentTernary.valueProperty().bindBidirectional(model.accentTernaryProperty());
        bgPrimary.valueProperty().bindBidirectional(model.bgPrimaryProperty());

        // Listeners reattivi sui cambi di colore - Aggiornano solo se NON siamo in rainbow mode
        accentPrimary.valueProperty().addListener((obs, oldV, newV) -> {
            if (!rainbowModeCheck.isSelected()) applyModelColorsToScene();
        });
        accentSecondary.valueProperty().addListener((obs, oldV, newV) -> {
            if (!rainbowModeCheck.isSelected()) applyModelColorsToScene();
        });
        accentTernary.valueProperty().addListener((obs, oldV, newV) -> {
            if (!rainbowModeCheck.isSelected()) applyModelColorsToScene();
        });
        bgPrimary.valueProperty().addListener((obs, oldV, newV) -> {
            if (!rainbowModeCheck.isSelected()) applyModelColorsToScene();
        });

        // Configurazione componenti grafici aggiuntivi
        buildCustomPickers();

        addImageBtn.setOnAction(this::onImagePick);
        pickImageBtn.setOnAction(this::onImagePick);
        prevThemeBtn.setOnAction(e -> model.navigateCarousel(false));
        nextThemeBtn.setOnAction(e -> model.navigateCarousel(true));

        themePreview.managedProperty().bind(themePreview.imageProperty().isNotNull());
        themePreview.visibleProperty().bind(themePreview.imageProperty().isNotNull());
        themePreview.fitWidthProperty().bind(imageArea.widthProperty().multiply(0.9));
        themePreview.fitHeightProperty().bind(imageArea.heightProperty().multiply(0.9));

        paletteTip.setVisible(false);
        paletteTip.visibleProperty().bind(addImageBtn.visibleProperty());

        Rectangle clip = new Rectangle();
        clip.setArcWidth(IscatSettings.STANDARD_UNIT);
        clip.setArcHeight(IscatSettings.STANDARD_UNIT);
        themePreview.layoutBoundsProperty().addListener((obs, old, newBounds) -> {
            clip.setWidth(newBounds.getWidth());
            clip.setHeight(newBounds.getHeight());
        });
        themePreview.setClip(clip);

        if (!model.getCurrentPalette().isEmpty()) {
            rebuildPaletteUI();
        }
        File currentImg = model.getCurrentImage();
        if (currentImg != null) {
            themePreview.setImage(new Image(currentImg.toURI().toString()));
        }
        updateCarouselButtons();

        // Ascolto carosello
        model.currentImageIndexProperty().addListener((obs, oldIdx, newIdx) -> {
            File img = model.getCurrentImage();
            if (img != null) {
                themePreview.setImage(new Image(img.toURI().toString()));
            }
            updateCarouselButtons();
        });
    }

    public void injectParentPane(Pane paneMaster) {
        this.paneMaster = paneMaster;
        if (model.rainbowModeProperty().get()) {
            model.startRainbowSyncTimer();
        }
        applyModelColorsToScene();
    }

    private void applyModelColorsToScene() {
        if (paneMaster == null || paneMaster.getScene() == null) return;
        List<String> hexPalette = List.of(
                ThemeSettingsModel.toHex(accentPrimary.getValue()),
                ThemeSettingsModel.toHex(accentSecondary.getValue()),
                ThemeSettingsModel.toHex(accentTernary.getValue()),
                ThemeSettingsModel.toHex(bgPrimary.getValue())
        );
        ThemeManager.getInstance().applyHexColorsTheme(paneMaster.getScene(), hexPalette, 0.2);
    }

    private void buildCustomPickers() {
        buildCustomPicker(accentPrimary, "Primary", pickerRow1);
        buildCustomPicker(accentSecondary, "Secondary", pickerRow1);
        buildCustomPicker(accentTernary, "Tertiary", pickerRow2);
        buildCustomPicker(bgPrimary, "Background", pickerRow2);
    }

    private void buildCustomPicker(ColorPicker picker, String role, HBox row) {
        picker.setOpacity(0);
        picker.setMouseTransparent(true);
        picker.setManaged(false);
        picker.showingProperty().addListener((obs, wasShowing, isShowing) -> {
            if (isShowing) {
                PopupWindow popup = getPopup(picker);
                if (popup != null && popup.getScene() != null) {
                    String css = getClass().getResource("/uni/gaben/iscat/styles/screens/settings-menu.css").toExternalForm();
                    if (!popup.getScene().getStylesheets().contains(css)) {
                        popup.getScene().getStylesheets().add(css);
                    }
                    String pickerCss = getClass().getResource("/uni/gaben/iscat/styles/components/iscat-color-picker.css").toExternalForm();
                    if (!popup.getScene().getStylesheets().contains(pickerCss)) {
                        popup.getScene().getStylesheets().add(pickerCss);
                    }
                }
            }
        });

        Label colorLabel = new Label(role);
        colorLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        colorLabel.setAlignment(Pos.CENTER);
        colorLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        colorLabel.backgroundProperty().bind(
                Bindings.createObjectBinding(
                        () -> new Background(new BackgroundFill(picker.getValue(), new CornerRadii(8), Insets.EMPTY)),
                        picker.valueProperty()
                )
        );

        colorLabel.textFillProperty().bind(
                Bindings.createObjectBinding(
                        () -> picker.getValue().getBrightness() > 0.5 ? Color.BLACK : Color.WHITE,
                        picker.valueProperty()
                )
        );

        double boxWidth = IscatSettings.STANDARD_UNIT * 5;
        double boxHeight = IscatSettings.STANDARD_UNIT * 2;
        colorLabel.setMinSize(boxWidth, boxHeight);
        colorLabel.setPrefSize(boxWidth, boxHeight);
        colorLabel.setMaxSize(boxWidth, boxHeight);
        colorLabel.getStyleClass().add("custom-color-box");
        colorLabel.setOnMouseClicked(e -> setActivePicker(picker));

        Button arrowBtn = new Button("▼");
        arrowBtn.getStyleClass().add("arrow-button");
        arrowBtn.setOnAction(e -> picker.show());
        arrowBtn.setMinHeight(boxHeight);
        arrowBtn.setPrefHeight(boxHeight);
        arrowBtn.setMaxHeight(boxHeight);

        StackPane overlay = new StackPane(colorLabel, picker);
        overlay.setAlignment(Pos.CENTER);
        overlay.setMinSize(boxWidth, boxHeight);
        overlay.setPrefSize(boxWidth, boxHeight);
        overlay.setMaxSize(boxWidth, boxHeight);

        HBox widget = new HBox(4, overlay, arrowBtn);
        widget.setAlignment(Pos.CENTER_LEFT);

        pickerBoxes.put(picker, colorLabel);
        row.getChildren().add(widget);
    }

    private void setActivePicker(ColorPicker picker) {
        pickerBoxes.values().forEach(box -> box.getStyleClass().remove("picker-active"));
        if (picker != null && pickerBoxes.containsKey(picker)) {
            pickerBoxes.get(picker).getStyleClass().add("picker-active");
        }
        activePicker.set(picker);
    }

    private void rebuildPaletteUI() {
        paletteHolder.getChildren().clear();
        List<Color> palette = model.getCurrentPalette();
        if (palette.isEmpty()) return;

        List<Color> sorted = palette.stream()
                .sorted(Comparator.comparingDouble(ThemeSettingsModel::luminance))
                .toList();

        double diameter = Math.max(
                (imageArea.getWidth() - (paletteHolder.getPadding().getLeft() +
                        paletteHolder.getPadding().getRight() +
                        paletteHolder.getSpacing() * (sorted.size() - 1))) / sorted.size(),
                24
        );

        for (Color color : sorted) {
            Circle circle = new Circle(diameter / 2.0, color);
            circle.getStyleClass().add("palette-swatch");
            circle.setOnMouseClicked(e -> {
                ColorPicker picker = activePicker.get();
                if (picker != null) {
                    picker.setValue(color);
                }
            });
            paletteHolder.getChildren().add(circle);
        }
    }

    @FXML
    void onImagePick(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        File chosen = chooser.showOpenDialog(paneMaster.getScene().getWindow());
        if (chosen != null) {
            model.addImageAndApply(chosen);
            themePreview.setImage(new Image(chosen.toURI().toString()));
            rebuildPaletteUI();
            updateCarouselButtons();
            if (!rainbowModeCheck.isSelected()) {
                applyModelColorsToScene();
            }
        }
    }

    @FXML
    void toggleRainbowMode(ActionEvent event) {
        boolean active = rainbowModeCheck.isSelected();
        model.setRainbowMode(active);
        if (paneMaster != null && paneMaster.getScene() != null) {
            if (active) {
                ThemeManager.getInstance().startRainbowMode(paneMaster.getScene());
                model.startRainbowSyncTimer();
            } else {
                ThemeManager.getInstance().stopRainbowMode();
                model.stopRainbowSyncTimer();
                applyModelColorsToScene();
            }
        }
    }

    @FXML
    void toggleThemeMode(ActionEvent event) {
        boolean light = lightModeCheck.isSelected();
        model.setLightMode(light);

        if (!model.getCurrentPalette().isEmpty()) {
            rebuildPaletteUI();
        }

        if (paneMaster != null && paneMaster.getScene() != null && !rainbowModeCheck.isSelected()) {
            applyModelColorsToScene();
        }
    }

    private void updateCarouselButtons() {
        int total = model.getCarouselSize();
        boolean hasImages = total > 0;
        boolean showArrows = total > 1;

        prevThemeBtn.setVisible(showArrows);
        prevThemeBtn.setManaged(showArrows);
        nextThemeBtn.setVisible(showArrows);
        nextThemeBtn.setManaged(showArrows);

        addImageBtn.setVisible(hasImages);
        addImageBtn.setManaged(hasImages);
        pickImageBtn.setVisible(!hasImages);
        pickImageBtn.setManaged(!hasImages);
    }

    private PopupWindow getPopup(ColorPicker picker) {
        for (Window window : Window.getWindows()) {
            if (window instanceof PopupWindow popup && popup.isShowing()) {
                if (popup.getOwnerWindow() == picker.getScene().getWindow()) {
                    return popup;
                }
            }
        }
        return null;
    }

    public void loadAndApplySavedTheme() {
        model.loadFromDatabase();
        if (!model.rainbowModeProperty().get()) {
            applyModelColorsToScene();
        }
    }

    @FXML
    void onRestoreDefaults(ActionEvent event) {
        ThemeManager.getInstance().stopRainbowMode();
        model.restoreDefaultTheme();
        paletteHolder.getChildren().clear();
        themePreview.setImage(null);
        updateCarouselButtons();
        applyModelColorsToScene();
    }
}