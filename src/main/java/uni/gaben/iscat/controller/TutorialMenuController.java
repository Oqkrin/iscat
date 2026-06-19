package uni.gaben.iscat.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import uni.gaben.iscat.utils.ComponentsUtils;

/**
 * Controller per la schermata dei comandi e del tutorial di gioco (Iscat Tutorial).
 */
public class TutorialMenuController implements IscatMenuController {

    @FXML private BorderPane rootPane;
    @FXML private Button backBtn;

    private StackPane contentRoot;

    @FXML
    public void initialize() {
        ComponentsUtils.applyIconButton(backBtn, "fas-arrow-left");
    }

    @FXML
    private void handleBackAction(ActionEvent event) {
        handleBack();
    }

    @Override
    public void setPointerToView(StackPane pointer) {
        this.contentRoot = pointer;
    }

    @Override
    public Pane getRootPane() {
        return rootPane;
    }
}