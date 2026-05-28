package uni.gaben.iscat.screens.main_menu;

import javafx.scene.layout.StackPane;
import uni.gaben.iscat.view.AbstractIscatStackPane;
public class MenuView extends AbstractIscatStackPane {
    public MenuView() {
        super(new StackPane(),true);
        initialize("/uni/gaben/iscat/fxml/main_menu.fxml");
    }
}
