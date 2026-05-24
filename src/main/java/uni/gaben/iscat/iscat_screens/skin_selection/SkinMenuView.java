package uni.gaben.iscat.iscat_screens.skin_selection;

import javafx.scene.layout.StackPane;
import uni.gaben.iscat.iscat_m_view_c.AbstractIscatStackPane;

public class SkinMenuView extends AbstractIscatStackPane {
    public SkinMenuView() {
        super(new StackPane(), true);
        initialize("/uni/gaben/iscat/fxml/skin_menu.fxml");
    }
}
