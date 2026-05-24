package uni.gaben.iscat.iscat_screens.scores;

import javafx.scene.layout.StackPane;
import uni.gaben.iscat.iscat_m_view_c.AbstractIscatStackPane;

public class ScoreMenuView extends AbstractIscatStackPane {
    public ScoreMenuView() {
        super(new StackPane(), true);
        initialize("/uni/gaben/iscat/fxml/score_menu.fxml");
    }
}
