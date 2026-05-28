package uni.gaben.iscat.iscat_screens.main_menu;

import javafx.scene.layout.StackPane;
import uni.gaben.iscat.iscat_m_view_c.AbstractIscatStackPane;
public class MenuView extends AbstractIscatStackPane {
    public MenuView() {
        super(new StackPane(),true);
        initialize("/uni/gaben/iscat/fxml/main_menu.fxml");
    }
}
