package uni.gaben.iscat.utils.components;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.*;
import javafx.beans.value.ObservableNumberValue;
import javafx.geometry.Insets;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * Failure
 * TextField che adatta automaticamente la dimensione del font allo spazio disponibile.
 * Mantiene tutte le funzionalità native di TextField (cursore, selezione, placeholder)
 * aggiungendo il ridimensionamento automatico del testo.
 * 
 * <p>Caratteristiche:
 * <ul>
 *   <li>Ridimensionamento automatico basato su larghezza disponibile</li>
 *   <li>Supporto placeholder nativo ({@link #setPromptText(String)})</li>
 *   <li>Cursore e selezione testo nativi</li>
 *   <li>Nessun flickering grazie a binding espliciti</li>
 * </ul>
 */
public class AutoFittingTextField extends TextField {

    // --- Proprietà ---
    private final DoubleProperty baseFontSize = new SimpleDoubleProperty(12.0);
    private final StringProperty fontFamily = new SimpleStringProperty("System");
    private final DoubleProperty manualLimit = new SimpleDoubleProperty(-1.0);
    private final BooleanProperty autoFitToParent = new SimpleBooleanProperty(true);

    // --- Tracker interni ---
    private final DoubleProperty parentWidthTracker = new SimpleDoubleProperty(-1.0);
    private DoubleBinding effectiveLimit;
    private final Text measurementHelper = new Text();

    /**
     * Costruisce un AutoFittingTextField con dimensione e famiglia font specificate.
     * 
     * @param baseSize Dimensione target del font (es. 24.0)
     * @param family   Famiglia del font (es. "Miracode")
     * @param cssClass Classe CSS opzionale da applicare
     */
    public AutoFittingTextField(double baseSize, String family, String cssClass) {
        this.baseFontSize.set(baseSize);
        this.fontFamily.set(family);

        if (cssClass != null) {
            getStyleClass().add(cssClass);
        }

        setupLayout();
        setupParentTracking();
        setupAutoFitting();
        applyFontSize(baseSize);
    }

    private void setupLayout() {
        setMinWidth(0);
        setPrefWidth(USE_COMPUTED_SIZE);
        setMaxWidth(Double.MAX_VALUE);
    }

    private void setupParentTracking() {
        parentProperty().addListener((obs, oldP, newP) -> {
            parentWidthTracker.unbind();
            if (newP instanceof Region region) {
                parentWidthTracker.bind(Bindings.createDoubleBinding(() -> {
                    if (region.getWidth() <= 0) return -1.0;
                    Insets insets = region.getPadding();
                    return region.getWidth() - insets.getLeft() - insets.getRight();
                }, region.widthProperty(), region.paddingProperty()));
            }
        });
    }

    private void setupAutoFitting() {
        // Limite effettivo: manuale o parent width
        effectiveLimit = Bindings.createDoubleBinding(() -> {
            double manual = manualLimit.get();
            if (manual > 0) return manual;
            if (autoFitToParent.get()) return parentWidthTracker.get();
            return -1.0;
        }, manualLimit, autoFitToParent, parentWidthTracker);

        // Ricalcola quando cambiano testo, dimensione base, famiglia o spazio
        Runnable recalc = () -> recalculateFontSize(effectiveLimit.get());
        
        effectiveLimit.addListener((obs, old, val) -> recalc.run());
        textProperty().addListener((obs, old, val) -> recalc.run());
        baseFontSize.addListener((obs, old, val) -> recalc.run());
        fontFamily.addListener((obs, old, val) -> recalc.run());

        // Inizializza quando la scena è pronta
        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                applyCss();
                recalc.run();
                setupCaretScaling();
            }
        });
    }

    /**
     * Configura il caret per scalare con il font.
     * Il caret di TextField è un Path che va ridimensionato manualmente.
     */
    private void setupCaretScaling() {
        // Cerca il caret nel skin del TextField
        skinProperty().addListener((obs, oldSkin, newSkin) -> {
            if (newSkin != null) {
                // Il caret è un Path dentro il TextInputControl
                javafx.scene.Node caretNode = lookup(".caret");
                if (caretNode instanceof javafx.scene.shape.Path caret) {
                    // Scala il caret in base alla dimensione del font
                    fontProperty().addListener((o, old, newFont) -> {
                        if (newFont != null) {
                            double fontSize = newFont.getSize();
                            // Scala il caret proporzionalmente al font
                            caret.setScaleY(fontSize / 12.0); // 12 è la dimensione base
                            caret.setStrokeWidth(Math.max(1.5, fontSize / 10.0));
                        }
                    });
                    
                    // Applica subito la scala iniziale
                    if (getFont() != null) {
                        double fontSize = getFont().getSize();
                        caret.setScaleY(fontSize / 12.0);
                        caret.setStrokeWidth(Math.max(1.5, fontSize / 10.0));
                    }
                }
            }
        });
    }

    private void recalculateFontSize(double limit) {
        double base = baseFontSize.get();
        String family = fontFamily.get();
        String text = getText();

        // Se vuoto o nessun limite, usa dimensione base
        if (text == null || text.isEmpty() || limit <= 0) {
            applyFontSize(base);
            return;
        }

        double available = limit * 0.98; // Margine 2%

        if (available <= 0) {
            applyFontSize(base);
            return;
        }

        // Misura larghezza con font base
        measurementHelper.setFont(Font.font(family, base));
        measurementHelper.setText(text);
        double textWidth = measurementHelper.getLayoutBounds().getWidth();

        // Se ci sta, usa dimensione base
        if (textWidth <= available) {
            applyFontSize(base);
            return;
        }

        // Calcola dimensione proporzionale
        double targetSize = (available / textWidth) * base;
        targetSize = Math.max(targetSize, 4.0); // Minimo 4px

        // Raffinamento iterativo per kerning non lineare
        measurementHelper.setFont(Font.font(family, targetSize));
        int iterations = 0;
        while (measurementHelper.getLayoutBounds().getWidth() > available && targetSize > 4 && iterations < 10) {
            targetSize -= 0.5;
            measurementHelper.setFont(Font.font(family, targetSize));
            iterations++;
        }

        applyFontSize(targetSize);
    }

    /**
     * Applica la dimensione del font evitando CSS inline per prevenire flickering.
     */
    private void applyFontSize(double size) {
        if (size > 0) {
            setFont(Font.font(fontFamily.get(), size));
        }
    }

    // --- Proprietà pubbliche ---

    public DoubleProperty baseFontSizeProperty() { return baseFontSize; }
    public void setBaseFontSize(double size) { this.baseFontSize.set(size); }
    public double getBaseFontSize() { return baseFontSize.get(); }

    public StringProperty fontFamilyProperty() { return fontFamily; }
    public void setFontFamily(String family) { this.fontFamily.set(family); }
    public String getFontFamily() { return fontFamily.get(); }

    public DoubleProperty manualLimitProperty() { return manualLimit; }
    public void setLimit(ObservableNumberValue limit) { this.manualLimit.bind(limit); }
    public void setLimit(double limit) { this.manualLimit.set(limit); }

    public BooleanProperty autoFitToParentProperty() { return autoFitToParent; }
    public void setAutoFitToParent(boolean value) { this.autoFitToParent.set(value); }
}
