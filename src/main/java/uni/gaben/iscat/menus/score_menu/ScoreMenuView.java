package uni.gaben.iscat.menus.score_menu;

import javafx.scene.layout.StackPane;
import uni.gaben.iscat.AbstractIscatStackPane;

public class ScoreMenuView extends AbstractIscatStackPane {
    public ScoreMenuView() {
        super(new StackPane(), true);
        initialize("/uni/gaben/iscat/fxml/score_menu.fxml");
    }
    @Override protected void initStyles()        {}
    @Override protected void initNodes()         {}
    @Override protected void initLayout()        {}
    @Override protected void initBindings()      {}
    @Override protected void initEventHandlers() {}

}
