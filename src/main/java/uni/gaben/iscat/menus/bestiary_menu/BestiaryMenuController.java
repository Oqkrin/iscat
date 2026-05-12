package uni.gaben.iscat.menus.bestiary_menu;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import uni.gaben.iscat.IscatNavigator;
import uni.gaben.iscat.IscatScenes;

public class BestiaryMenuController {

    public BorderPane rootPane;

    @FXML
    private void handleBack(ActionEvent event) {
        IscatNavigator.getInstance().navigateTo(IscatScenes.MAIN_MENU);
    }
}
