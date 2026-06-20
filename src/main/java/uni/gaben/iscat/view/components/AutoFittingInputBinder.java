package uni.gaben.iscat.view.components;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextInputControl;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class AutoFittingInputBinder {

    private final TextInputControl inputField;
    private final String fontFamily;
    private final double baseFontSize;
    private final double minFontSize;
    private final int maxInputLength;
    private final DoubleProperty customLimit = new SimpleDoubleProperty(-1.0);

    private static final int SHRINK_THRESHOLD = 15;
    private final Text measurementHelper = new Text();

    public AutoFittingInputBinder(TextInputControl inputField, String fontFamily, double baseFontSize, double minFontSize, int maxInputLength) {
        this.inputField = inputField;
        this.fontFamily = fontFamily;
        this.baseFontSize = baseFontSize;
        this.minFontSize = minFontSize;
        this.maxInputLength = maxInputLength;

        setupConstraints();
        setupLengthFilter();
        setupTriggers();
    }

    private void setupConstraints() {
        inputField.setMinWidth(TextInputControl.USE_COMPUTED_SIZE);
        inputField.setMaxWidth(Double.MAX_VALUE);
    }

    private void setupLengthFilter() {
        inputField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.length() > maxInputLength) {
                inputField.setText(oldVal);
            }
        });
    }

    private void setupTriggers() {
        inputField.textProperty().addListener((obs, old, val) -> recalc());
        customLimit.addListener((obs, old, val) -> recalc());
    }

    private void recalc() {
        Platform.runLater(() -> {
            String text = inputField.getText();
            if (text == null) text = "";

            if (inputField instanceof PasswordField) {
                text = "●".repeat(text.length());
            }

            double paddingOffset = 36.0;

            measurementHelper.setFont(Font.font(fontFamily, baseFontSize));
            measurementHelper.setText("WWWWWWWWWW");
            double minAllowedFieldWidth = measurementHelper.getLayoutBounds().getWidth() + paddingOffset;

            if (text.isEmpty()) {
                inputField.setPrefWidth(minAllowedFieldWidth);
                applyFont(baseFontSize);
                return;
            }

            double startingFontSize = baseFontSize;
            if (text.length() > SHRINK_THRESHOLD) {
                double overflowRatio = (double) (text.length() - SHRINK_THRESHOLD) / (maxInputLength - SHRINK_THRESHOLD);
                startingFontSize = baseFontSize - (overflowRatio * (baseFontSize - minFontSize) * 0.5);
                startingFontSize = Math.max(startingFontSize, minFontSize);
            }

            measurementHelper.setFont(Font.font(fontFamily, startingFontSize));
            measurementHelper.setText(text);
            double requiredTextWidth = measurementHelper.getLayoutBounds().getWidth();

            double desiredFieldWidth = Math.max(requiredTextWidth + paddingOffset, minAllowedFieldWidth);

            double maxAllowedLimit = customLimit.get();

            if (maxAllowedLimit > 0 && desiredFieldWidth > maxAllowedLimit) {
                inputField.setPrefWidth(maxAllowedLimit);

                double available = maxAllowedLimit - paddingOffset;
                double targetSize = Math.max((available / requiredTextWidth) * startingFontSize, minFontSize);

                measurementHelper.setFont(Font.font(fontFamily, targetSize));
                int iterations = 0;
                while (measurementHelper.getLayoutBounds().getWidth() > available
                        && targetSize > minFontSize
                        && iterations < 12) {
                    targetSize -= 0.5;
                    measurementHelper.setFont(Font.font(fontFamily, targetSize));
                    iterations++;
                }
                applyFont(Math.max(targetSize, minFontSize));
            } else {
                inputField.setPrefWidth(desiredFieldWidth);
                applyFont(startingFontSize);
            }
        });
    }

    private void applyFont(double size) {
        if (size > 0) {
            inputField.setFont(Font.font(fontFamily, size));
            inputField.requestLayout();
        }
    }

    public void bindLimit(javafx.beans.value.ObservableNumberValue limitSource) {
        customLimit.bind(limitSource);
    }
}