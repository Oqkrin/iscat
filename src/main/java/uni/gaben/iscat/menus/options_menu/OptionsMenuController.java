package uni.gaben.iscat.menus.options_menu;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import uni.gaben.iscat.IscatFxmlController;
import uni.gaben.iscat.IscatNavigator;
import uni.gaben.iscat.IscatScenes;
import javafx.scene.input.MouseEvent;

public class OptionsMenuController implements IscatFxmlController{

    private StackPane contentRoot;

    @FXML
    private VBox previewBox;

    @FXML
    private BorderPane rootPane;

    @FXML
    private Label skinNameLabel;

    @FXML
    private Label skinNameLabel1;

    @FXML
    private void handleBack(ActionEvent event) {
        if (contentRoot != null) {
            IscatNavigator.getInstance().navigateWithFade(IscatScenes.MAIN_MENU, contentRoot);
        } else {
            IscatNavigator.getInstance().navigateTo(IscatScenes.MAIN_MENU);
        }
    }

    @FXML
    void show_controls(ActionEvent event) {

    }

    @FXML
    void toggleFPSVisible(ActionEvent event) {

    }

    @FXML
    void toggleFullscreen(ActionEvent event) {

    }

    @FXML
    void updateBGMAudio(MouseEvent event) {
        System.out.println("AIDOP");
    }

    @FXML
    void updateSFXAudio(MouseEvent event) {
        System.out.println("AIDOP");
    }

    @Override
    public void setContentRoot(StackPane contentRoot) {
        this.contentRoot = contentRoot;
    }
}
