package uni.gaben.iscat.screens.skin_selection;

import javafx.scene.layout.StackPane;
import uni.gaben.iscat.view.AbstractIscatStackPane;

public class SkinMenuView extends AbstractIscatStackPane {
    public SkinMenuView() {
        super(new StackPane(), true);
        initialize("/uni/gaben/iscat/fxml/skin_menu.fxml");
    }
}
