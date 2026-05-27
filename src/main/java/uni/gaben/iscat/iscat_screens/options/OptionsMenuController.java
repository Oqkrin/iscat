package uni.gaben.iscat.iscat_screens.options;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import uni.gaben.iscat.utils.AudioManager;
import uni.gaben.iscat.iscat_mv_controller.IscatFxmlController;
import uni.gaben.iscat.IscatNavigator;
import uni.gaben.iscat.iscat_model_vc.IscatViews;
import uni.gaben.iscat.universe.UU;

public class OptionsMenuController implements IscatFxmlController {

    private StackPane contentRoot;

    @FXML
    private VBox mainOptions;

    @FXML
    private Slider BGMSlider;

    @FXML
    private Slider SFXSlider;

    @FXML
    private BorderPane rootPane;

    @FXML
    private Label skinNameLabel;

    @FXML
    private Label skinNameLabel1;

    @FXML
    private VBox controlsBox;

    @FXML Button walkUp;
    @FXML Button walkDown;
    @FXML Button walkLeft;
    @FXML Button dash1;
    @FXML Button dash2;
    @FXML Button esc;



    @FXML
    public void initialize(){
        BGMSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            AudioManager.getInstance().setBgmVolume(newValue.doubleValue());
        });

        SFXSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            AudioManager.getInstance().setSfxVolume(newValue.doubleValue());
        });
    }

    @FXML
    private Slider scaleSlider;

    @FXML
    void changeControl(ActionEvent event) {

    }

    @FXML
    void handleBack(ActionEvent event) {
        IscatNavigator.getInstance().navigateWithFade(IscatViews.MAIN_MENU);
    }

    @FXML
    void resetControls(ActionEvent event) {
        //TODO RESET ALL BUTTONS TO THE DEFAULT
    }

    @FXML
    void saveAndCloseControls(ActionEvent event) {
        //TODO SAVE NEW CONTROLS
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
    void deleteAccount(ActionEvent event) {

    }

    @FXML
    void resetAccount(ActionEvent event) {

    }

    @FXML
    void toggleFullscreen(ActionEvent event) {
        if (rootPane != null && rootPane.getScene() != null) {
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setFullScreen(!stage.isFullScreen());
        }
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

    @FXML
    void updateScale(MouseEvent event) {

        UU.setUniverseScale(scaleSlider.getValue());

    }
}
