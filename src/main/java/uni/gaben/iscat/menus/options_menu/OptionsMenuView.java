package uni.gaben.iscat.menus.options_menu;

import javafx.scene.layout.StackPane;
import uni.gaben.iscat.utils.components.AbstractIscatStackPane;

public class OptionsMenuView extends AbstractIscatStackPane {
    public OptionsMenuView() {
        super(new StackPane(), true);
        initialize("/uni/gaben/iscat/fxml/options_menu.fxml");
    }
}
