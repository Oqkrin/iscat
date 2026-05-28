package uni.gaben.iscat.screens.scores;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import uni.gaben.iscat.controller.IscatFxmlController;
import uni.gaben.iscat.IscatNavigator;
import uni.gaben.iscat.model.IscatViews;

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