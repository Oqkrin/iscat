package uni.gaben.iscat.screens.confirmation_overlay;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import uni.gaben.iscat.screens.base.IscatMenuController;

import java.util.function.Consumer;

public class ConfirmationOverlayController implements IscatMenuController {

    public enum InputType {
        NONE, NORMAL, PASSWORD
    }

    @FXML private StackPane rootPane;
    @FXML private Label titleLabel;
    @FXML private Label descLabel;

    @FXML private TextField normalTextField;
    @FXML private PasswordField passwordTextField;

    @FXML private Button yesBtn;
    @FXML private Button noBtn;

    private StackPane contentRoot;
    private Consumer<String> onConfirmWithInput;
    private InputType currentInputType = InputType.NONE;

    @FXML
    public void initialize() {
        if (rootPane != null) {
            rootPane.managedProperty().bind(rootPane.visibleProperty());

            rootPane.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (!rootPane.isVisible()) return;

                if (event.getCode() == KeyCode.ENTER) {
                    handleYes(new ActionEvent());
                    event.consume();
                }
            });
        }

        normalTextField.managedProperty().bind(normalTextField.visibleProperty());
        passwordTextField.managedProperty().bind(passwordTextField.visibleProperty());

        yesBtn.managedProperty().bind(yesBtn.visibleProperty());
        noBtn.managedProperty().bind(noBtn.visibleProperty());
    }

    @Override
    public Pane getRootPane() {
        return rootPane;
    }

    @Override
    public void handleBack() {
        if (rootPane != null && !rootPane.isVisible()) return;

        if (noBtn.isVisible()) {
            handleNo(new ActionEvent());
        } else {
            handleYes(new ActionEvent());
        }
    }

    @Override
    public void setContentRoot(StackPane contentRoot) {
        this.contentRoot = contentRoot;
    }

    public void ask(String title, String description, Runnable onConfirm) {
        askWithButtons(title, description, "SÌ", "NO", onConfirm);
    }

    public void askWithButtons(String title,
                               String description,
                               String yesText,
                               String noText,
                               Runnable onConfirm) {

        this.yesBtn.setText(yesText);

        if (noText == null || noText.isEmpty()) {
            this.noBtn.setVisible(false);
        } else {
            this.noBtn.setText(noText);
            this.noBtn.setVisible(true);
        }

        askWithInput(
                title,
                description,
                InputType.NONE,
                text -> onConfirm.run()
        );
    }

    public void askWithInput(String title,
                             String description,
                             InputType inputType,
                             Consumer<String> onConfirm) {

        this.titleLabel.setText(title.toUpperCase());
        this.descLabel.setText(description);
        this.onConfirmWithInput = onConfirm;
        this.currentInputType = inputType;

        if (inputType != InputType.NONE) {
            this.yesBtn.setText("SÌ");
            this.noBtn.setText("NO");
            this.noBtn.setVisible(true);
        }

        normalTextField.clear();
        passwordTextField.clear();

        normalTextField.setVisible(inputType == InputType.NORMAL);
        passwordTextField.setVisible(inputType == InputType.PASSWORD);

        if (rootPane != null) {
            rootPane.setVisible(true);

            if (inputType == InputType.NORMAL) {
                normalTextField.requestFocus();
            } else if (inputType == InputType.PASSWORD) {
                passwordTextField.requestFocus();
            }

            rootPane.setOpacity(0.0);

            FadeTransition fadeInBg =
                    new FadeTransition(Duration.millis(150), rootPane);
            fadeInBg.setToValue(1.0);
            fadeInBg.play();

            playSpawnTween(rootPane.getChildren().get(0));
        }
    }

    @FXML
    private void handleYes(ActionEvent event) {

        String inputResult = "";

        if (currentInputType == InputType.NORMAL) {
            inputResult = normalTextField.getText();
        } else if (currentInputType == InputType.PASSWORD) {
            inputResult = passwordTextField.getText();
        }

        final String finalResult = inputResult;

        closeWithAnimation(() -> {
            if (onConfirmWithInput != null) {
                onConfirmWithInput.accept(finalResult);
            }
        });
    }

    @FXML
    private void handleNo(ActionEvent event) {
        closeWithAnimation(() ->
                this.onConfirmWithInput = null
        );
    }

    private void closeWithAnimation(Runnable onFinishedAction) {

        if (rootPane == null || !rootPane.isVisible()) {
            if (onFinishedAction != null) {
                onFinishedAction.run();
            }
            return;
        }

        Node container = rootPane.getChildren().get(0);

        FadeTransition fadeOutBg =
                new FadeTransition(Duration.millis(150), rootPane);
        fadeOutBg.setToValue(0.0);

        playDespawnTween(container, () -> {

            rootPane.setVisible(false);
            rootPane.setOpacity(1.0);

            if (onFinishedAction != null) {
                onFinishedAction.run();
            }
        });

        fadeOutBg.play();
    }

    //TODO DA SPOSTARE
    private void playDespawnTween(Node target,
                                  Runnable onFinished) {

        ScaleTransition scale =
                new ScaleTransition(Duration.millis(150), target);

        scale.setToX(0.8);
        scale.setToY(0.8);

        FadeTransition fade =
                new FadeTransition(Duration.millis(150), target);

        fade.setToValue(0.0);

        ParallelTransition parallel =
                new ParallelTransition(scale, fade);

        parallel.setOnFinished(e -> {

            target.setScaleX(1.0);
            target.setScaleY(1.0);
            target.setOpacity(1.0);

            onFinished.run();
        });

        parallel.play();
    }
}