package uni.gaben.iscat.menus.score_menu;

import javafx.scene.layout.StackPane;
import uni.gaben.iscat.utils.components.AbstractIscatStackPane;

public class ScoreMenuView extends AbstractIscatStackPane {
    public ScoreMenuView() {
        super(new StackPane(), true);
        initialize("/uni/gaben/iscat/fxml/score_menu.fxml");
    }
}
