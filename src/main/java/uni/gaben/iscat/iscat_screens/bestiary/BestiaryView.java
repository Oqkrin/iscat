package uni.gaben.iscat.iscat_screens.bestiary;

import javafx.scene.layout.StackPane;
import uni.gaben.iscat.iscat_m_view_c.AbstractIscatStackPane;

public class BestiaryView extends AbstractIscatStackPane {
    public BestiaryView() {
        super(new StackPane(),true);
        initialize("/uni/gaben/iscat/fxml/bestiary_menu.fxml");
    }
}
