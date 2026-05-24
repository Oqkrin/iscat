package uni.gaben.iscat.iscat_m_view_c;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.*;
import javafx.beans.value.ObservableNumberValue;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * Label che adatta automaticamente la dimensione del font allo spazio disponibile.
 * Previene flickering usando binding espliciti invece di CSS inline.
 * 
 * <p>Caratteristiche:
 * <ul>
 *   <li>Ridimensionamento automatico basato su larghezza disponibile</li>
 *   <li>Supporto per limite manuale o automatico dal parent</li>
 *   <li>Misurazione efficiente tramite nodo Text invisibile</li>
 *   <li>Raffinamento iterativo per compensare kerning non lineare</li>
 * </ul>
 * 
 * <p>Esempio d'uso:
 * <pre>{@code
 * AutoFittingLabel label = new AutoFittingLabel(24.0, "Miracode", "my-style");
 * label.setText("Testo che si adatta");
 * label.setLimit(parentWidth); // Opzionale: limite manuale
 * }</pre>
 */
public class AutoFittingLabel extends Label {

    // --- Proprietà ---
    private final DoubleProperty baseFontSize = new SimpleDoubleProperty(12.0);
    private final StringProperty fontFamily = new SimpleStringProperty("System");
    private final DoubleProperty manualLimit = new SimpleDoubleProperty(-1.0);
    private final BooleanProperty autoFitToParent = new SimpleBooleanProperty(true);

    // --- Tracker interni ---
    private final DoubleProperty parentWidthTracker = new SimpleDoubleProperty(-1.0);
    private DoubleBinding effectiveLimit;
    
    /**
     * Nodo invisibile per misurare dimensioni testo senza triggerare
     * ricalcoli pesanti del layout della Label.
     */
    private final Text measurementHelper = new Text();

    /**
     * Costruisce una AutoFittingLabel con dimensione e famiglia font specificate.
     * 
     * @param baseSize Dimensione target del font (es. 24.0)
     * @param family   Famiglia del font (es. "Miracode")
     * @param cssClass Classe CSS opzionale da applicare
     */
    public AutoFittingLabel(double baseSize, String family, String cssClass) {
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
        paddingProperty().addListener((obs, old, val) -> recalc.run());

        // Inizializza quando la scena è pronta
        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                applyCss();
                recalc.run();
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

        double paddingOffset = getPadding().getLeft() + getPadding().getRight();
        double available = (limit - paddingOffset) * 0.98; // Margine 2%

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
