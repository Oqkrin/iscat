package uni.gaben.iscat.utils.components;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.*;
import javafx.beans.value.ObservableNumberValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.Locale;

public class AutoFittingLabel extends Label {

    private final DoubleProperty baseFontSize = new SimpleDoubleProperty(12.0);
    private final DoubleProperty manualLimit = new SimpleDoubleProperty(-1.0);
    private final BooleanProperty autoFitToParent = new SimpleBooleanProperty(true);

    // We don't need a binding for the effective size, we just need a standard property
    // that we update when necessary.
    private final DoubleProperty parentWidthTracker = new SimpleDoubleProperty(-1.0);
    private final Text helper = new Text();

    public AutoFittingLabel(double baseSize, String cssClass) {
        this.baseFontSize.set(baseSize);
        if (cssClass != null) getStyleClass().add(cssClass);

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
        DoubleBinding effectiveLimit = Bindings.createDoubleBinding(() -> {
            if (manualLimit.get() > 0) return manualLimit.get();
            if (autoFitToParent.get()) return parentWidthTracker.get();
            return -1.0;
        }, manualLimit, autoFitToParent, parentWidthTracker);

        // We use a listener instead of a binding to have strict control over when the font updates
        // We do NOT listen to fontProperty() here to avoid loops.
        effectiveLimit.addListener((obs, oldVal, newVal) -> recalculateFontSize(newVal.doubleValue()));
        textProperty().addListener((obs, oldVal, newVal) -> recalculateFontSize(effectiveLimit.get()));
        baseFontSize.addListener((obs, oldVal, newVal) -> recalculateFontSize(effectiveLimit.get()));
        paddingProperty().addListener((obs, oldVal, newVal) -> recalculateFontSize(effectiveLimit.get()));

        // Listen to scene property. When the label is added to a scene, we wait for layout
        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if(newScene != null) {
                // applyCss() forces the CSS engine to resolve styles immediately
                // This ensures getFont() returns the CSS-defined font (family/weight)
                // BEFORE we do our first calculation.
                applyCss();
                recalculateFontSize(effectiveLimit.get());
            }
        });
    }

    private void recalculateFontSize(double limit) {
        double base = baseFontSize.get();
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

        // 2% safety margin prevents ellipsis due to anti-aliasing / rounding
        available *= 0.98;

        Font currentFont = getFont();
        // Fallback in case font is somehow null
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

        // Final proportional correction if still slightly over
        double finalWidth = helper.getLayoutBounds().getWidth();
        if (finalWidth > available && targetSize > 2) {
            targetSize = targetSize * (available / finalWidth);
            targetSize = Math.max(targetSize, 2.0);
        }

        applyFontSize(targetSize);
    }

    private void applyFontSize(double size) {
        if (size > 0) {
            setStyle(String.format(Locale.US, "-fx-font-size: %.2fpx;", size));
        }
    }

    public void setLimit(ObservableNumberValue limit) {
        this.manualLimit.bind(limit);
    }

    public BooleanProperty autoFitToParentProperty() {
        return autoFitToParent;
    }
}