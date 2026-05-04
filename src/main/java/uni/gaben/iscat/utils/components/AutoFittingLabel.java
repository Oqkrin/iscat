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

import java.util.Locale;

public class AutoFittingLabel extends Label {

    private final DoubleProperty baseFontSizeProperty = new SimpleDoubleProperty(12.0);
    private final DoubleProperty manualLimitProperty = new SimpleDoubleProperty(-1.0);
    private final BooleanProperty autoFitToParent = new SimpleBooleanProperty(true);

    private final DoubleProperty parentWidthTracker = new SimpleDoubleProperty(-1.0);
    private DoubleBinding effectiveLimit;
    private final Text helper = new Text();

    public AutoFittingLabel(double baseSize, String cssClass) {
        this.baseFontSizeProperty.set(baseSize);
        if (cssClass != null) getStyleClass().add(cssClass);

        // Importante per permettere alla label di contrarsi
        setMinWidth(0);

        setupParentTracking();
        setupAutoFittingLogic();
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
        effectiveLimit = Bindings.createDoubleBinding(() -> {
            if (manualLimitProperty.get() > 0) return manualLimitProperty.get();
            if (autoFitToParent.get()) return parentWidthTracker.get();
            return -1.0;
        }, manualLimitProperty, autoFitToParent, parentWidthTracker);

        effectiveLimit.addListener((obs, oldVal, newVal) -> recalculateFontSize(newVal.doubleValue()));
        textProperty().addListener((obs, oldVal, newVal) -> recalculateFontSize(effectiveLimit.get()));
        baseFontSizeProperty.addListener((obs, oldVal, newVal) -> recalculateFontSize(effectiveLimit.get()));
        paddingProperty().addListener((obs, oldVal, newVal) -> recalculateFontSize(effectiveLimit.get()));

        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if(newScene != null) {
                applyCss();
                recalculateFontSize(effectiveLimit.get());
            }
        });
    }

    private void recalculateFontSize(double limit) {
        double base = baseFontSizeProperty.get();
        String text = getText();

        if (text == null || text.isEmpty() || limit <= 0) {
            applyFontSize(base);
            return;
        }

        double available = limit - (getPadding().getLeft() + getPadding().getRight());
        if (available <= 0) {
            applyFontSize(base);
            return;
        }

        available *= 0.98; // Safety margin

        Font currentFont = getFont();
        if(currentFont == null) currentFont = Font.getDefault();

        helper.setFont(new Font(currentFont.getName(), base));
        helper.setText(text);

        double textWidth = helper.getLayoutBounds().getWidth();

        if (textWidth <= available) {
            applyFontSize(base);
            return;
        }

        double targetSize = (available / textWidth) * base;
        targetSize = Math.max(targetSize, 2.0);

        helper.setFont(new Font(currentFont.getName(), targetSize));
        int safety = 0;
        while (helper.getLayoutBounds().getWidth() > available && targetSize > 2 && safety < 15) {
            targetSize -= 0.5;
            helper.setFont(new Font(currentFont.getName(), targetSize));
            safety++;
        }

        applyFontSize(targetSize);
    }

    private void applyFontSize(double size) {
        if (size > 0) {
            setStyle(String.format(Locale.US, "-fx-font-size: %.2fpx;", size));
        }
    }

    /**
     * Calcola la larghezza reale del testo renderizzato con il font size attuale.
     */
    public double getEffectiveTextWidth() {
        if (getText() == null || getText().isEmpty()) return 0;
        helper.setText(getText());
        // Usiamo la dimensione attuale del font (quella applicata via CSS/Style)
        helper.setFont(getFont());
        return helper.getLayoutBounds().getWidth();
    }

    public DoubleProperty limitProperty() { return manualLimitProperty; }
    public void setLimit(ObservableNumberValue limit) { this.manualLimitProperty.bind(limit); }
    public BooleanProperty autoFitToParentProperty() { return autoFitToParent; }
    public DoubleProperty baseFontSizeProperty() { return baseFontSizeProperty; }
    public void setBaseFontSize(double size) { this.baseFontSizeProperty.set(size); }
}