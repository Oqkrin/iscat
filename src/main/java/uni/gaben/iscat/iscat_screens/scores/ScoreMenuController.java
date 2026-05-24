package uni.gaben.iscat.iscat_screens.scores;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import uni.gaben.iscat.iscat_mv_controller.IscatFxmlController;
import uni.gaben.iscat.IscatNavigator;
import uni.gaben.iscat.iscat_model_vc.IscatViews;

public class ScoreMenuController implements IscatFxmlController {
    private StackPane contentRoot;

    @Override
    public void setContentRoot(StackPane contentRoot) {
        this.contentRoot = contentRoot;
    }

    @FXML
    private void handleBack(ActionEvent event) {
        IscatNavigator.getInstance().navigateWithFade(IscatViews.MAIN_MENU);
    }
}