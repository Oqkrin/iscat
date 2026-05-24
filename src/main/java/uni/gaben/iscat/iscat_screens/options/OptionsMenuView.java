package uni.gaben.iscat.iscat_screens.options;

import javafx.scene.layout.StackPane;
import uni.gaben.iscat.iscat_m_view_c.AbstractIscatStackPane;

public class OptionsMenuView extends AbstractIscatStackPane {
    public OptionsMenuView() {
        super(new StackPane(), true);
        initialize("/uni/gaben/iscat/fxml/options_menu.fxml");
    }
}
