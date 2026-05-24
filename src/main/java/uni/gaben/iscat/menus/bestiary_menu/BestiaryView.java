package uni.gaben.iscat.menus.bestiary_menu;

import javafx.scene.layout.StackPane;
import uni.gaben.iscat.utils.components.AbstractIscatStackPane;

public class BestiaryView extends AbstractIscatStackPane {
    public BestiaryView() {
        super(new StackPane(),true);
        initialize("/uni/gaben/iscat/fxml/bestiary_menu.fxml");
    }
}
