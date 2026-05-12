package uni.gaben.iscat.menus.options_menu;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import uni.gaben.iscat.IscatNavigator;
import uni.gaben.iscat.IscatScenes;

public class OptionsMenuController {

    public BorderPane rootPane;

    @FXML
    private void handleBack(ActionEvent event) {
        IscatNavigator.getInstance().navigateTo(IscatScenes.MAIN_MENU);
    }
    @FXML
    private void handleConfirm(ActionEvent event) {
        IscatNavigator.getInstance().navigateTo(IscatScenes.MAIN_MENU);
    }


}
