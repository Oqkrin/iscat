package uni.gaben.iscat.screens.scores;

import javafx.scene.layout.StackPane;
import uni.gaben.iscat.view.AbstractIscatStackPane;

public class ScoreMenuView extends AbstractIscatStackPane {
    public ScoreMenuView() {
        super(new StackPane(), true);
        initialize("/uni/gaben/iscat/fxml/score_menu.fxml");
    }
}
