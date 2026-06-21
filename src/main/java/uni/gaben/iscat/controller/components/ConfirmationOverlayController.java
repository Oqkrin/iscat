package uni.gaben.iscat.controller.components;

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
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import uni.gaben.iscat.controller.interfaces.IscatMenuController;
import uni.gaben.iscat.utils.design.ScalareAureo;

import java.util.function.Consumer;

/**
 * Controller per l'overlay di conferma interattiva.
 * Gestisce dialoghi modali con supporto per testo, input testuale, password e keybind.
 */
public class ConfirmationOverlayController implements IscatMenuController {

    /** Tipo di input richiesto dall'overlay */
    public enum InputType {
        NONE,       // Nessun input, solo conferma/annulla
        NORMAL,     // Input testo normale
        PASSWORD,   // Input password (nascosto)
        KEYBIND     // Selezione tasto per keybind
    }

    @FXML private StackPane rootPane;
    @FXML private Label titleLabel;
    @FXML private Label descLabel;
    @FXML private TextField normalTextField;
    @FXML private PasswordField passwordTextField;
    @FXML private Button keybindBtn;
    @FXML private Button yesBtn;
    @FXML private Button noBtn;
    @FXML private VBox dialog;

    private Consumer<String> onConfirmWithInput;
    private Runnable onCancelCallback;
    private Runnable onKeybindTrigger;
    private InputType currentInputType = InputType.NONE;

    @FXML
    public void initialize() {
        // Configura visibilità e gestione tasti di scelta rapida (ENTER / ESC)
        if (rootPane != null) {
            rootPane.managedProperty().bind(rootPane.visibleProperty());
            rootPane.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                if (!rootPane.isVisible()) return;

                if (event.getCode() == KeyCode.ENTER) {
                    handleYes(new ActionEvent());
                    event.consume();
                } else if (event.getCode() == KeyCode.ESCAPE) {
                    handleBack();
                    event.consume();
                }
            });
        }

        // Binding visibilità per i campi input e pulsanti
        normalTextField.managedProperty().bind(normalTextField.visibleProperty());
        passwordTextField.managedProperty().bind(passwordTextField.visibleProperty());
        yesBtn.managedProperty().bind(yesBtn.visibleProperty());
        noBtn.managedProperty().bind(noBtn.visibleProperty());

        // Consente l'invio diretto premendo INVIO mentre si scrive nei campi di testo
        normalTextField.setOnAction(e -> handleYes(new ActionEvent()));
        passwordTextField.setOnAction(e -> handleYes(new ActionEvent()));

        // Configura pulsante keybind
        if (keybindBtn != null) {
            keybindBtn.managedProperty().bind(keybindBtn.visibleProperty());
            keybindBtn.setOnAction(e -> {
                if (onKeybindTrigger != null) onKeybindTrigger.run();
            });
        }

        // Ridimensionamento responsive del dialog basato sul modulo della sezione aurea
        if (dialog != null && rootPane != null) {
            var goldenScale = rootPane.widthProperty().multiply(ScalareAureo.IPHI_D * ScalareAureo.IPHI_D);
            var goldenHeight = rootPane.heightProperty().multiply(ScalareAureo.IPHI_D * ScalareAureo.IPHI_D);

            dialog.minWidthProperty().bind(goldenScale);
            dialog.maxWidthProperty().bind(goldenScale);
            dialog.minHeightProperty().bind(goldenHeight);
            dialog.maxHeightProperty().bind(goldenHeight);
        }
    }

    @Override
    public Pane getRootPane() { return rootPane; }

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
    public void setPointerToView(StackPane pointer) {
    }

    /** Mostra un overlay di conferma semplice. */
    public void ask(String title, String description, Runnable onConfirm) {
        askWithButtons(title, description, "SI", "NO", onConfirm, null);
    }

    /** Mostra un overlay con pulsanti di conferma personalizzati. */
    public void askWithButtons(String title, String description, String yesText, String noText, Runnable onConfirm) {
        askWithButtons(title, description, yesText, noText, onConfirm, null);
    }

    /** Mostra un overlay con pulsanti personalizzati e una callback di annullamento dedicata. */
    public void askWithButtons(String title, String description, String yesText, String noText,
                               Runnable onConfirm, Runnable onCancel) {
        this.onCancelCallback = onCancel;
        this.yesBtn.setText(yesText);
        this.yesBtn.setVisible(true);

        if (noText == null || noText.isEmpty()) {
            this.noBtn.setVisible(false);
        } else {
            this.noBtn.setText(noText);
            this.noBtn.setVisible(true);
        }

        askWithInput(title, description, InputType.NONE, text -> {
            if (onConfirm != null) onConfirm.run();
        });
    }

    /** Mostra l'overlay predisposto per la cattura di un nuovo comando di input (Keybind). */
    public void askForKeybind(String title, String description, Runnable onStartListening, Runnable onCancel) {
        this.onKeybindTrigger = onStartListening;
        this.onCancelCallback = onCancel;
        this.yesBtn.setVisible(false);
        this.noBtn.setText("ANNULLA");
        this.noBtn.setVisible(true);
        askWithInput(title, description, InputType.KEYBIND, text -> {});
    }

    /** Aggiorna dinamicamente il testo descrittivo del pulsante keybind durante l'ascolto. */
    public void setKeybindBtnText(String text) {
        if (keybindBtn != null) keybindBtn.setText(text);
    }

    /** Mostra l'overlay configurando i campi di testo o i pulsanti a seconda dell'InputType richiesto. */
    public void askWithInput(String title, String description, InputType inputType, Consumer<String> onConfirm) {
        this.onConfirmWithInput = onConfirm;
        this.currentInputType = inputType;
        if (inputType == InputType.NONE) this.onKeybindTrigger = null;

        this.titleLabel.setText(title.toUpperCase());
        this.descLabel.setText(description);

        if (inputType != InputType.NONE && inputType != InputType.KEYBIND) {
            this.yesBtn.setText("SÌ");
            this.noBtn.setText("NO");
            this.noBtn.setVisible(true);
        }

        normalTextField.clear();
        passwordTextField.clear();

        normalTextField.setVisible(inputType == InputType.NORMAL);
        passwordTextField.setVisible(inputType == InputType.PASSWORD);
        if (keybindBtn != null) keybindBtn.setVisible(inputType == InputType.KEYBIND);

        if (rootPane != null) {
            rootPane.setVisible(true);

            // Sposta il focus sul campo corretto in base all'esigenza attuale
            if (inputType == InputType.NORMAL) normalTextField.requestFocus();
            else if (inputType == InputType.PASSWORD) passwordTextField.requestFocus();
            else if (inputType == InputType.KEYBIND && keybindBtn != null) {
                keybindBtn.setText("CLICCA PER ASSEGNARE");
                keybindBtn.requestFocus();
            }

            // Animazione fluida di comparsa (FadeIn)
            rootPane.setOpacity(0.0);
            FadeTransition fadeInBg = new FadeTransition(Duration.millis(150), rootPane);
            fadeInBg.setToValue(1.0);
            fadeInBg.play();
            playSpawnTween(rootPane.getChildren().getFirst());
        }
    }

    @FXML
    private void handleYes(ActionEvent event) {
        String inputResult = "";
        if (currentInputType == InputType.NORMAL) inputResult = normalTextField.getText();
        else if (currentInputType == InputType.PASSWORD) inputResult = passwordTextField.getText();

        final String finalResult = inputResult;
        closeWithAnimation(() -> {
            if (onConfirmWithInput != null) onConfirmWithInput.accept(finalResult);
        });
    }

    @FXML
    private void handleNo(ActionEvent event) {
        closeWithAnimation(() -> {
            this.onConfirmWithInput = null;
            if (onCancelCallback != null) {
                onCancelCallback.run();
                onCancelCallback = null;
            }
        });
    }

    /** Chiude l'overlay applicando le transizioni di uscita combinate prima di distruggere o nascondere il nodo. */
    private void closeWithAnimation(Runnable onFinishedAction) {
        if (rootPane == null || !rootPane.isVisible()) {
            if (onFinishedAction != null) onFinishedAction.run();
            return;
        }

        Node container = rootPane.getChildren().getFirst();
        FadeTransition fadeOutBg = new FadeTransition(Duration.millis(150), rootPane);
        fadeOutBg.setToValue(0.0);

        playDespawnTween(container, () -> {
            rootPane.setVisible(false);
            rootPane.setOpacity(1.0);
            if (onFinishedAction != null) onFinishedAction.run();
        });

        fadeOutBg.play();
    }

    /** Esegue lo zoom in temporaneo (effetto spawn) per accentuare l'apertura del popup. */
    public void playSpawnTween(Node target) {
        target.setScaleX(0.8);
        target.setScaleY(0.8);
        ScaleTransition scale = new ScaleTransition(Duration.millis(150), target);
        scale.setToX(1.0);
        scale.setToY(1.0);
        scale.play();
    }

    /** Dissolve l'elemento rimpicciolendolo in concomitanza del fade out dello sfondo. */
    private void playDespawnTween(Node target, Runnable onFinished) {
        ScaleTransition scale = new ScaleTransition(Duration.millis(150), target);
        scale.setToX(0.8);
        scale.setToY(0.8);

        FadeTransition fade = new FadeTransition(Duration.millis(150), target);
        fade.setToValue(0.0);

        ParallelTransition parallel = new ParallelTransition(scale, fade);
        parallel.setOnFinished(e -> {
            target.setScaleX(1.0);
            target.setScaleY(1.0);
            target.setOpacity(1.0);
            onFinished.run();
        });
        parallel.play();
    }
}