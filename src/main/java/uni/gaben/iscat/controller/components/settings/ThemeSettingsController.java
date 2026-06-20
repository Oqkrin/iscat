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
 *
 * <p>Responsabile esclusivamente della creazione e gestione della UI.
 * Tutta la logica di business (estrazione colori, persistenza, stato) è delegata al {@link ThemeSettingsModel}.</p>
 *
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
    @FXML private Button addImageBtn;
    @FXML private Label paletteTip;

    private Pane paneMaster;
    private ThemeSettingsModel model;

    private final Map<ColorPicker, Label> pickerBoxes = new HashMap<>();
    private final ObjectProperty<Object> activePicker = new SimpleObjectProperty<>(null);

    /**
     * Inizializza il controller creando il modello e collegando la UI.
     */
    @FXML
    public void initialize() {
        model = new ThemeSettingsModel();

        // Carica i dati dal database
        model.loadFromDatabase();

        // Collega le proprietà del modello ai controlli JavaFX (bidirezionale)
        lightModeCheck.selectedProperty().bindBidirectional(model.lightModeProperty());
        rainbowModeCheck.selectedProperty().bindBidirectional(model.rainbowModeProperty());

        // All'avvio, sincronizza i picker con i colori del modello (che ha già i valori dal DB)
        accentPrimary.valueProperty().bindBidirectional(model.accentPrimaryProperty());
        accentSecondary.valueProperty().bindBidirectional(model.accentSecondaryProperty());
        accentTernary.valueProperty().bindBidirectional(model.accentTernaryProperty());
        bgPrimary.valueProperty().bindBidirectional(model.bgPrimaryProperty());

        // Costruisci i widget personalizzati per i color picker (nascosti)
        buildCustomPickers();

        // Configura i pulsanti
        addImageBtn.setOnAction(this::onImagePick);
        prevThemeBtn.setOnAction(e -> model.navigateCarousel(false));
        nextThemeBtn.setOnAction(e -> model.navigateCarousel(true));

        // Gestione dell'anteprima immagine
        themePreview.managedProperty().bind(themePreview.imageProperty().isNotNull());
        themePreview.visibleProperty().bind(themePreview.imageProperty().isNotNull());
        themePreview.fitWidthProperty().bind(imageArea.widthProperty().multiply(0.9));
        themePreview.fitHeightProperty().bind(imageArea.heightProperty().multiply(0.9));

        paletteTip.setVisible(false);
        paletteTip.visibleProperty().bind(addImageBtn.visibleProperty());

        // Clip arrotondata per l'anteprima
        Rectangle clip = new Rectangle();
        clip.setArcWidth(IscatSettings.STANDARD_UNIT);
        clip.setArcHeight(IscatSettings.STANDARD_UNIT);
        themePreview.layoutBoundsProperty().addListener((obs, old, newBounds) -> {
            clip.setWidth(newBounds.getWidth());
            clip.setHeight(newBounds.getHeight());
        });
        themePreview.setClip(clip);

        // Carica la palette e l'immagine se presenti in sessione (ereditate da SessionManager? meglio dal modello)
        // Se SessionManager conteneva dati persistenti, vanno spostati nel modello.
        // Per semplicità assumiamo che il modello ora sia l'unica fonte.
        if (!model.getCurrentPalette().isEmpty()) {
            rebuildPaletteUI();
        }
        File currentImg = model.getCurrentImage();
        if (currentImg != null) {
            themePreview.setImage(new Image(currentImg.toURI().toString()));
        }
        updateCarouselButtons();

        // Ascolto cambiamenti dell'immagine corrente
        model.currentImageIndexProperty().addListener((obs, oldIdx, newIdx) -> {
            File img = model.getCurrentImage();
            if (img != null) {
                themePreview.setImage(new Image(img.toURI().toString()));
            }
            updateCarouselButtons();
        });

        // Ricostruisci la palette quando cambia la lista dei colori (opzionale)
        // model.getCurrentPalette() non è osservabile; potremmo chiamare rebuildPaletteUI dopo ogni estrazione
    }

    /**
     * Inietta il pannello padre (necessario per ottenere la scena e applicare i temi CSS).
     */
    public void injectParentPane(Pane paneMaster) {
        this.paneMaster = paneMaster;
        // Se la modalità arcobaleno era attiva, avvia il timer di sincronizzazione UI
        if (model.rainbowModeProperty().get()) {
            model.startRainbowSyncTimer();
        }

        // Applica i colori del modello alla scena (richiede la scena)
        applyModelColorsToScene();
    }

    /**
     * Applica i colori attuali del modello alla scena tramite ThemeManager.
     * Da chiamare dopo che la scena è disponibile e dopo ogni cambiamento.
     */
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

    // ==================== Costruzione UI personalizzata ====================

    /**
     * Crea i widget personalizzati per i quattro ColorPicker (due file da due).
     */
    private void buildCustomPickers() {
        buildCustomPicker(accentPrimary, "Primary", pickerRow1);
        buildCustomPicker(accentSecondary, "Secondary", pickerRow1);
        buildCustomPicker(accentTernary, "Tertiary", pickerRow2);
        buildCustomPicker(bgPrimary, "Background", pickerRow2);
    }

    /**
     * Costruisce un singolo widget: rettangolo colorato + etichetta + pulsante freccia.
     */
    private void buildCustomPicker(ColorPicker picker, String role, HBox row) {
        // The real picker is invisible (just used for coordinate reference)
        picker.setOpacity(0);
        picker.setMouseTransparent(true);
        picker.setManaged(false);
        picker.showingProperty().addListener((obs, wasShowing, isShowing) -> {
            if (isShowing) {
                PopupWindow popup = getPopup(picker);
                if (popup != null && popup.getScene() != null) {
                    // Add your main CSS (adjust path to your actual file)
                    String css = getClass().getResource("/uni/gaben/iscat/styles/screens/settings-menu.css").toExternalForm();
                    if (!popup.getScene().getStylesheets().contains(css)) {
                        popup.getScene().getStylesheets().add(css);
                    }
                    // Optionally also add the specific color‑picker CSS if it’s separate
                    String pickerCss = getClass().getResource("/uni/gaben/iscat/styles/components/iscat-color-picker.css").toExternalForm();
                    if (!popup.getScene().getStylesheets().contains(pickerCss)) {
                        popup.getScene().getStylesheets().add(pickerCss);
                    }
                }
            }
        });

        // --- The colour swatch + role name ---
        Label colorLabel = new Label(role);
        colorLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
        colorLabel.setAlignment(Pos.CENTER);
        colorLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        // Bind the background colour to the picker value
        colorLabel.backgroundProperty().bind(
                Bindings.createObjectBinding(
                        () -> {
                            Color c = picker.getValue();
                            return new Background(new BackgroundFill(c, new CornerRadii(8), Insets.EMPTY));
                        },
                        picker.valueProperty()
                )
        );

        // Auto‑adjust text colour for contrast
        colorLabel.textFillProperty().bind(
                Bindings.createObjectBinding(
                        () -> picker.getValue().getBrightness() > 0.5 ? Color.BLACK : Color.WHITE,
                        picker.valueProperty()
                )
        );

        // Size and style
        double boxWidth = IscatSettings.STANDARD_UNIT * 5;
        double boxHeight = IscatSettings.STANDARD_UNIT * 2;
        colorLabel.setMinSize(boxWidth, boxHeight);
        colorLabel.setPrefSize(boxWidth, boxHeight);
        colorLabel.setMaxSize(boxWidth, boxHeight);
        colorLabel.getStyleClass().add("custom-color-box");
        colorLabel.setOnMouseClicked(e -> setActivePicker(picker));

        // --- Arrow button ---
        Button arrowBtn = new Button("▼");
        arrowBtn.getStyleClass().add("arrow-button");
        arrowBtn.setOnAction(e -> picker.show());
        // Make the button a little larger to match the box
        arrowBtn.setMinHeight(boxHeight);
        arrowBtn.setPrefHeight(boxHeight);
        arrowBtn.setMaxHeight(boxHeight);

        // --- Overlay for the invisible real picker (for correct popup position) ---
        StackPane overlay = new StackPane(colorLabel, picker);
        overlay.setAlignment(Pos.CENTER);
        overlay.setMinSize(boxWidth, boxHeight);
        overlay.setPrefSize(boxWidth, boxHeight);
        overlay.setMaxSize(boxWidth, boxHeight);

        // --- Final widget ---
        HBox widget = new HBox(4, overlay, arrowBtn);
        widget.setAlignment(Pos.CENTER_LEFT);

        pickerBoxes.put(picker, colorLabel);   // store the actual widget for active highlighting
        row.getChildren().add(widget);
    }

    /**
     * Evidenzia il picker selezionato e aggiorna il modello.
     */
    private void setActivePicker(ColorPicker picker) {
        pickerBoxes.values().forEach(box -> box.getStyleClass().remove("picker-active"));
        if (picker != null && pickerBoxes.containsKey(picker)) {
            pickerBoxes.get(picker).getStyleClass().add("picker-active");
        }
        activePicker.set(picker);
    }

    /**
     * Ricostruisce l'area dei cerchi colorati a partire dalla palette del modello.
     */
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

                if (activePicker.get() instanceof ColorPicker picker) {
                    picker.setValue(color);
                    // Il cambio di valore farà scattare il listener che chiama applyThemeToManager
                }
            });
            paletteHolder.getChildren().add(circle);
        }
    }

    // ==================== Azioni UI ====================

    @FXML
    void onImagePick(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        File chosen = chooser.showOpenDialog(paneMaster.getScene().getWindow());
        if (chosen != null) {
            model.addImageAndApply(chosen);
            themePreview.setImage(new Image(chosen.toURI().toString()));
            rebuildPaletteUI();
            updateCarouselButtons();
            applyModelColorsToScene();
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
        // Dopo il cambio potrebbe servire ricostruire la palette se c'è un'immagine
        if (!model.getCurrentPalette().isEmpty()) {
            rebuildPaletteUI();
        }
        applyModelColorsToScene();
    }

    /**
     * Aggiorna la visibilità dei pulsanti del carosello in base al numero di immagini.
     */
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

    /**
     * Helper to extract the PopupWindow from a ColorPicker (the popup with the colour grid).
     * Works for JavaFX 8+.
     */
    private PopupWindow getPopup(ColorPicker picker) {
        // The popup is hidden inside the skin; we can find it via lookup
        // or by accessing the skin's popup. A reliable way:
        if (picker.getSkin() != null) {
            // The skin is usually a ColorPickerSkin, which has a method getPopupContent()
            // but it's not public. Using reflection is overkill, so we can search the scene.
            // Alternatively, listen to the popup's ownerWindow.
            // Simplest: iterate over all open popups? Not ideal.
            // Better: use the internal property "popup" via reflection or rely on the fact that
            // the popup is a child of the picker's scene window? No, it's separate.
        }
        // For a simple solution, we can obtain the popup by checking the children of the
        // ColorPicker's scene's window? Not reliable.
        // Instead, we can use the fact that the popup is shown when isShowing is true,
        // and we can access it via `picker.getScene().getWindow()`? No.

        // A common workaround: after the popup is shown, we can find it by searching for
        // a PopupWindow whose owner is the picker's scene's window.
        // Let's use a straightforward, safe approach with Java 8+:
        for (Window window : Window.getWindows()) {
            if (window instanceof javafx.stage.PopupWindow popup && popup.isShowing()) {
                // Check if the popup's owner scene is the same as the picker's scene
                if (popup.getOwnerWindow() == picker.getScene().getWindow()) {
                    return popup;
                }
            }
        }
        return null;
    }

    public void loadAndApplySavedTheme() {
        model.loadFromDatabase();
    }
}