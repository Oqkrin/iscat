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
 * Controller per la gestione dell'interfaccia utente dedicata alla personalizzazione del tema visivo.
 * Coordina l'interazione tra i selettori colore (ColorPicker), il sistema di carosello per gli sfondi,
 * l'estrazione di palette e le modalità speciali Rainbow e Light Mode.
 */
public class ThemeSettingsController {

    /** Selettori nativi JavaFX per la gestione dei colori di accento e dello sfondo. */
    @FXML private ColorPicker accentPrimary, accentSecondary, accentTernary, bgPrimary;

    /** Caselle di controllo per l'attivazione della Light Mode e della Rainbow Mode. */
    @FXML private CheckBox lightModeCheck, rainbowModeCheck;

    /** Contenitori orizzontali per l'allineamento dei widget di selezione colore e dei cerchi della palette. */
    @FXML private HBox paletteHolder, pickerRow1, pickerRow2;

    /** Contenitore radice del componente della vista. */
    @FXML private VBox theme;

    /** Area di posizionamento della preview dell'immagine di sfondo. */
    @FXML private StackPane imageArea;

    /** Visualizzatore grafico dell'immagine attiva estratta dal carosello. */
    @FXML private ImageView themePreview;

    /** Pulsanti di navigazione per scorrere gli elementi presenti all'interno del carosello. */
    @FXML private Button prevThemeBtn, nextThemeBtn;

    /** Pulsante iniziale posizionato al centro per l'apertura del selettore di immagini. */
    @FXML private Button pickImageBtn;

    /** Pulsante per il ripristino istantaneo di tutte le impostazioni cromatiche di default. */
    @FXML private Button restoreBtn;

    /** Pulsante secondario per aggiungere ulteriori immagini all'elenco esistente. */
    @FXML private Button addImageBtn;

    /** Etichetta di testo informativa per guidare l'utente nell'utilizzo dei campioni colore generati. */
    @FXML private Label paletteTip;

    /** Riferimento al pannello contenitore di livello superiore all'interno del quale viene iniettata la scena. */
    private Pane paneMaster;

    /** Istanza del modello di business associata alla logica di sincronizzazione e persistenza del tema. */
    private ThemeSettingsModel model;

    /** Mappa di associazione per rintracciare l'etichetta testuale personalizzata legata a ciascun ColorPicker nativo. */
    private final Map<ColorPicker, Label> pickerBoxes = new HashMap<>();

    /** Proprietà che memorizza il selettore colore attualmente attivo e selezionato dall'utente. */
    private final ObjectProperty<ColorPicker> activePicker = new SimpleObjectProperty<>(null);

    /**
     * Inizializza i componenti della vista FXML. Configura i binding bidirezionali,
     * i listener reattivi sulle proprietà del modello, le maschere di ritaglio geometriche
     * e le impostazioni di ridimensionamento automatico delle immagini.
     */
    @FXML
    public void initialize() {
        model = new ThemeSettingsModel();
        model.loadFromDatabase();

        lightModeCheck.selectedProperty().bindBidirectional(model.lightModeProperty());
        rainbowModeCheck.selectedProperty().bindBidirectional(model.rainbowModeProperty());

        accentPrimary.valueProperty().bindBidirectional(model.accentPrimaryProperty());
        accentSecondary.valueProperty().bindBidirectional(model.accentSecondaryProperty());
        accentTernary.valueProperty().bindBidirectional(model.accentTernaryProperty());
        bgPrimary.valueProperty().bindBidirectional(model.bgPrimaryProperty());

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

        model.currentImageIndexProperty().addListener((obs, oldIdx, newIdx) -> {
            File img = model.getCurrentImage();
            if (img != null) {
                themePreview.setImage(new Image(img.toURI().toString()));
            }
            updateCarouselButtons();
        });
    }

    /**
     * Inietta il riferimento al pannello contenitore master radice. Se la modalità arcobaleno
     * è attiva, avvia i timer grafici per l'aggiornamento real-time dei colori.
     * * @param paneMaster Il contenitore grafico padre da associare.
     */
    public void injectParentPane(Pane paneMaster) {
        this.paneMaster = paneMaster;
        if (model.rainbowModeProperty().get()) {
            model.startRainbowSyncTimer();
        }
        applyModelColorsToScene();
    }

    /**
     * Estrae i codici colore esadecimali memorizzati nel modello e li applica ai fogli di stile
     * CSS agganciati alla finestra principale tramite l'interfaccia singleton del {@link ThemeManager}.
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

    /**
     * Avvia il processo di generazione grafica di tutti e quattro i selettori colore personalizzati.
     */
    private void buildCustomPickers() {
        buildCustomPicker(accentPrimary, "Primary", pickerRow1);
        buildCustomPicker(accentSecondary, "Secondary", pickerRow1);
        buildCustomPicker(accentTernary, "Tertiary", pickerRow2);
        buildCustomPicker(bgPrimary, "Background", pickerRow2);
    }

    /**
     * Trasforma l'aspetto visivo di un selettore colore nativo mascherandolo sotto forma di blocco di testo
     * interattivo con colorazione dinamica adattiva dello sfondo, e vi applica fogli di stile esterni CSS personalizzati.
     * * @param picker Il componente {@link ColorPicker} nativo da configurare.
     * @param role   L'etichetta testuale descrittiva del ruolo del colore.
     * @param row    Il contenitore orizzontale in cui innestare il widget finale.
     */
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

    /**
     * Gestisce e aggiorna l'evidenziazione visiva del bordo del selettore colore attualmente attivo nella UI.
     * * @param picker L'istanza del componente {@link ColorPicker} da rendere attiva e selezionata.
     */
    private void setActivePicker(ColorPicker picker) {
        pickerBoxes.values().forEach(box -> box.getStyleClass().remove("picker-active"));
        if (picker != null && pickerBoxes.containsKey(picker)) {
            pickerBoxes.get(picker).getStyleClass().add("picker-active");
        }
        activePicker.set(picker);
    }

    /**
     * Ricostruisce dinamicamente i cerchi colorati di anteprima all'interno della barra della palette estratta.
     * Calcola le dimensioni dei singoli campioni e ne organizza la disposizione ordinandoli per luminanza.
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
                ColorPicker picker = activePicker.get();
                if (picker != null) {
                    picker.setValue(color);
                }
            });
            paletteHolder.getChildren().add(circle);
        }
    }

    /**
     * Gestisce l'evento di pressione dei pulsanti per la selezione delle immagini di sfondo locali via {@link FileChooser}.
     * * @param event L'evento di azione scatenato dal click sul componente grafico.
     */
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

    /**
     * Alterna lo stato di attivazione della Rainbow Mode sincronizzando di conseguenza il ciclo del manager grafico.
     * * @param event L'evento di selezione scatenato dalla casella di controllo.
     */
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

    /**
     * Gestisce l'attivazione o la disattivazione della Light Mode, avviando l'aggiornamento dei colori dello sfondo.
     * * @param event L'evento di selezione associato alla checkbox della Light Mode.
     */
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

    /**
     * Monitora la quantità complessiva delle immagini caricate per nascondere o mostrare selettivamente
     * i pulsanti di navigazione destra/sinistra e per alternare la visibilità dei selettori principali.
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
     * Isola ed individua l'istanza corretta della finestra Popup associata al ColorPicker nativo in primo piano.
     *
     * @param picker Il componente per cui rintracciare la finestra di dialogo.
     * @return L'oggetto {@link PopupWindow} se presente e visibile, altrimenti {@code null}.
     */
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

    /**
     * Carica dal database e imposta sulla scena attiva i colori e i parametri salvati nell'ultima sessione utente.
     */
    public void loadAndApplySavedTheme() {
        model.loadFromDatabase();
        if (!model.rainbowModeProperty().get()) {
            applyModelColorsToScene();
        }
    }

    /**
     * Interrompe le animazioni attive e ripristina istantaneamente l'interfaccia utente
     * e la palette cromatico-visiva ai valori iniziali originali dell'applicazione.
     * * @param event L'evento scatenato dalla pressione del tasto RESTORE.
     */
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