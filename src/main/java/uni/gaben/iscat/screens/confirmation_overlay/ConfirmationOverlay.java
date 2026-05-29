package uni.gaben.iscat.screens.confirmation_overlay;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class ConfirmationOverlay {

    @FXML private StackPane rootPane;
    @FXML private Label titleLabel;
    @FXML private Label descLabel;
    @FXML private Button yesBtn;
    @FXML private Button noBtn;

    private Runnable onConfirmAction;

    @FXML
    public void initialize() {
        if (rootPane != null) {
            rootPane.managedProperty().bind(rootPane.visibleProperty());
        }
    }

    /**
     * Configura e mostra l'overlay di conferma.
     */
    public void ask(String title, String description, Runnable onConfirm) {
        this.titleLabel.setText(title.toUpperCase());
        this.descLabel.setText(description);
        this.onConfirmAction = onConfirm;

        if (rootPane != null) {
            rootPane.setVisible(true);
        }
    }

    @FXML
    private void handleYes(ActionEvent event) {
        if (rootPane != null) rootPane.setVisible(false);
        if (onConfirmAction != null) {
            onConfirmAction.run();
        }
    }

    @FXML
    private void handleNo(ActionEvent event) {
        if (rootPane != null) rootPane.setVisible(false);
        this.onConfirmAction = null;
    }
}