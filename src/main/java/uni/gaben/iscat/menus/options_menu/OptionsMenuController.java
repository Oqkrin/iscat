package uni.gaben.iscat.menus.options_menu;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import uni.gaben.iscat.IscatFxmlController;
import uni.gaben.iscat.IscatNavigator;
import uni.gaben.iscat.IscatScenes;

public class OptionsMenuController implements IscatFxmlController {
    @FXML private BorderPane rootPane;

    private StackPane contentRoot;

    @Override
    public void setContentRoot(StackPane contentRoot) {
        this.contentRoot = contentRoot;
    }

    @FXML
    private void handleBack(ActionEvent event) {
        if (contentRoot != null) {
            IscatNavigator.getInstance().navigateWithFade(IscatScenes.MAIN_MENU, contentRoot);
        } else {
            IscatNavigator.getInstance().navigateTo(IscatScenes.MAIN_MENU);
        }
    }
}