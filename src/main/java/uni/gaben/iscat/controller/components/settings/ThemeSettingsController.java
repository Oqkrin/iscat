package uni.gaben.iscat.controller.components.settings;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
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
import uni.gaben.iscat.IscatSettings;
import uni.gaben.iscat.model.settings.ThemeSettingsModel;
import uni.gaben.iscat.utils.SessionManager;
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

    private final Map<Object, StackPane> pickerBoxes = new HashMap<>();
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
        // Il picker originale rimane visibile (per avere coordinate valide) ma invisibile all'utente
        picker.setOpacity(0);
        picker.setMouseTransparent(true);
        picker.setManaged(false);
        // Non chiamare picker.setVisible(false)!

        // Rettangolo colorato
        Rectangle rect = new Rectangle(60, 28);
        rect.setArcWidth(8);
        rect.setArcHeight(8);
        rect.fillProperty().bind(picker.valueProperty());

        // Etichetta ruolo
        Label roleLabel = new Label(role);
        roleLabel.setFont(Font.font("System", FontWeight.BOLD, 10));
        roleLabel.textFillProperty().bind(Bindings.createObjectBinding(() ->
                        picker.getValue().getBrightness() > 0.5 ? Color.BLACK : Color.WHITE,
                picker.valueProperty()));

        // Box colorato con etichetta
        StackPane colorBox = new StackPane(rect, roleLabel);
        colorBox.getStyleClass().add("custom-color-box");
        colorBox.setOnMouseClicked(e -> setActivePicker(picker));

        // Pulsante freccia (ora punta a picker.show())
        Button arrowBtn = new Button("+");
        arrowBtn.getStyleClass().add("arrow-button");
        arrowBtn.setOnAction(e -> picker.show());

        // StackPane che contiene sia il colorBox sia il picker invisibile
        StackPane overlay = new StackPane(colorBox, picker);
        overlay.setAlignment(Pos.CENTER);
        // Fissa le dimensioni minime per evitare che collassi
        overlay.setMinSize(IscatSettings.STANDARD_UNIT*7, IscatSettings.STANDARD_UNIT*3);
        overlay.setPrefSize(IscatSettings.STANDARD_UNIT*7, IscatSettings.STANDARD_UNIT*3);
        overlay.setMaxSize(IscatSettings.STANDARD_UNIT*7, IscatSettings.STANDARD_UNIT*3);

        HBox widget = new HBox(IscatSettings.STANDARD_UNIT/3, overlay, arrowBtn);
        widget.setAlignment(Pos.CENTER_LEFT);

        pickerBoxes.put(picker, colorBox);
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

    public void loadAndApplySavedTheme() {
        model.loadFromDatabase();
    }
}