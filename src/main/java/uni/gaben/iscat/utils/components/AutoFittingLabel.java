package uni.gaben.iscat.utils.components;

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
 * Una Label "intelligente" che ridimensiona il proprio font per adattarsi allo spazio disponibile.
 * Risolve i problemi di flickering evitando il CSS inline e usando binding espliciti per la famiglia del font.
 */
public class AutoFittingLabel extends Label {

    // --- Proprietà Core ---
    private final DoubleProperty baseFontSize = new SimpleDoubleProperty(12.0);
    private final StringProperty fontFamily = new SimpleStringProperty("System");
    private final DoubleProperty manualLimit = new SimpleDoubleProperty(-1.0);
    private final BooleanProperty autoFitToParent = new SimpleBooleanProperty(true);

    // --- Tracker Interni ---
    private final DoubleProperty parentWidthTracker = new SimpleDoubleProperty(-1.0);
    private DoubleBinding effectiveLimit;

    /**
     * Nodo invisibile usato per misurare le dimensioni del testo senza triggerare
     * ricalcoli pesanti del layout della Label reale.
     */
    private final Text measurementHelper = new Text();

    /**
     * @param baseSize Dimensione target del font (es. TipografiaAurea.HEADLINE[LARGE])
     * @param family   Nome della famiglia del font (es. "Miracode")
     * @param cssClass Classe CSS da applicare (opzionale)
     */
    public AutoFittingLabel(double baseSize, String family, String cssClass) {
        this.baseFontSize.set(baseSize);
        this.fontFamily.set(family);

        if (cssClass != null) {
            getStyleClass().add(cssClass);
        }

        // Setup strutturale
        setMinWidth(0);
        setPrefWidth(USE_COMPUTED_SIZE);
        setMaxWidth(Double.MAX_VALUE);

        setupParentTracking();
        setupAutoFittingLogic();

        // Applichiamo il font iniziale
        applyFontSize(baseSize);
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

    private void setupAutoFittingLogic() {
        // Calcola il limite di larghezza effettivo (manuale o basato sul parent)
        effectiveLimit = Bindings.createDoubleBinding(() -> {
            double manual = manualLimit.get();
            if (manual > 0) return manual;
            if (autoFitToParent.get()) return parentWidthTracker.get();
            return -1.0;
        }, manualLimit, autoFitToParent, parentWidthTracker);

        // Trigger ricalcolo: ogni volta che cambia il testo, la dimensione base, la famiglia o lo spazio
        Runnable recalculate = () -> recalculateFontSize(effectiveLimit.get());

        effectiveLimit.addListener((obs, old, val) -> recalculate.run());
        textProperty().addListener((obs, old, val) -> recalculate.run());
        baseFontSize.addListener((obs, old, val) -> recalculate.run());
        fontFamily.addListener((obs, old, val) -> recalculate.run());
        paddingProperty().addListener((obs, old, val) -> recalculate.run());

        // Sync iniziale quando la scena è pronta
        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                applyCss();
                recalculate.run();
            }
        });
    }

    private void recalculateFontSize(double limit) {
        double base = baseFontSize.get();
        String family = fontFamily.get();
        String text = getText();

        if (text == null || text.isEmpty() || limit <= 0) {
            applyFontSize(base);
            return;
        }

        double paddingOffset = getPadding().getLeft() + getPadding().getRight();
        double available = (limit - paddingOffset) * 0.98; // Margine di sicurezza del 2%

        if (available <= 0) {
            applyFontSize(base);
            return;
        }

        // Misurazione preliminare con il font base
        measurementHelper.setFont(Font.font(family, base));
        measurementHelper.setText(text);
        double textWidth = measurementHelper.getLayoutBounds().getWidth();

        // Se ci sta già, usiamo la dimensione base
        if (textWidth <= available) {
            applyFontSize(base);
            return;
        }

        // Calcolo proporzionale della taglia ideale
        double targetSize = (available / textWidth) * base;
        targetSize = Math.max(targetSize, 4.0); // Non scendiamo sotto i 4px per leggibilità

        // Raffinamento iterativo (per compensare kerning non lineare)
        measurementHelper.setFont(Font.font(family, targetSize));
        int safetyCounter = 0;
        while (measurementHelper.getLayoutBounds().getWidth() > available && targetSize > 4 && safetyCounter < 10) {
            targetSize -= 0.5;
            measurementHelper.setFont(Font.font(family, targetSize));
            safetyCounter++;
        }

        applyFontSize(targetSize);
    }

    /**
     * Applica il font size usando il metodo setFont() invece dello stile CSS,
     * evitando invalidazioni globali dello stage.
     */
    private void applyFontSize(double size) {
        if (size > 0) {
            setFont(Font.font(fontFamily.get(), size));
        }
    }

    public StringProperty fontFamilyProperty() { return fontFamily; }
    public void setFontFamily(String family) { this.fontFamily.set(family); }
    public String getFontFamily() { return fontFamily.get(); }

    public DoubleProperty baseFontSizeProperty() { return baseFontSize; }
    public void setBaseFontSize(double size) { this.baseFontSize.set(size); }
    public double getBaseFontSize() { return baseFontSize.get(); }

    public void setLimit(ObservableNumberValue limit) { this.manualLimit.bind(limit); }
    public DoubleProperty manualLimitProperty() { return manualLimit; }

    public BooleanProperty autoFitToParentProperty() { return autoFitToParent; }
}