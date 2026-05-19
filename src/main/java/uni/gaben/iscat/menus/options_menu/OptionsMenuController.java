package uni.gaben.iscat.menus.options_menu;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import uni.gaben.iscat.IscatFxmlController;
import uni.gaben.iscat.IscatNavigator;
import uni.gaben.iscat.IscatScenes;

public class OptionsMenuController implements IscatFxmlController {

    private StackPane contentRoot;

    @FXML
    private VBox mainOptions;

    @FXML
    private BorderPane rootPane;

    @FXML
    private Label skinNameLabel;

    @FXML
    private Label skinNameLabel1;

    @FXML
    private VBox controlsBox;

    @FXML
    void changeControl(ActionEvent event) {

    }

    @FXML
    void handleBack(ActionEvent event) {
        IscatNavigator.getInstance().navigateWithFade(IscatScenes.MAIN_MENU, contentRoot);
    }

    @FXML
    void resetControls(ActionEvent event) {
        //TODO RESET ALL BUTTONS TO THE DEFAULT OF INPUTMANAGER
    }

    @FXML
    void saveAndCloseControls(ActionEvent event) {
        //TODO SAVE NEW CONTROLS IN INPUTMANAGER
        toggleVisibilityMainOptions();
        toggleVisibilityControls();
    }

    @FXML
    void showControls(ActionEvent event) {
        toggleVisibilityMainOptions();
        toggleVisibilityControls();
    }

    @FXML
    void toggleFPSVisible(ActionEvent event) {

    }

    @FXML
    void toggleFullscreen(ActionEvent event) {

    }

    @FXML
    void updateBGMAudio(MouseEvent event) {

    }

    @FXML
    void updateSFXAudio(MouseEvent event) {

    }

    @Override
    public void setContentRoot(StackPane contentRoot) {
        this.contentRoot = contentRoot;
    }

    private void toggleVisibilityMainOptions() {
        boolean show = !mainOptions.isVisible();
        mainOptions.setVisible(show);
        mainOptions.setManaged(show);
        if (show) {
            mainOptions.toFront();
        }
    }

    private void toggleVisibilityControls() {
        boolean show = !controlsBox.isVisible();
        controlsBox.setVisible(show);
        controlsBox.setManaged(show);
        if (show) {
            controlsBox.toFront();
        }
    }
}
