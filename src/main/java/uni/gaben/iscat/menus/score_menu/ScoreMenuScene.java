package uni.gaben.iscat.menus.score_menu;

import javafx.scene.SceneAntialiasing;
import javafx.scene.layout.StackPane;
import uni.gaben.iscat.IscatSceneAbstract;

public class ScoreMenuScene extends IscatSceneAbstract {

    public ScoreMenuScene() {
        super(new StackPane(), true, SceneAntialiasing.DISABLED);
        initialize("/uni/gaben/iscat/fxml/score_menu.fxml");
    }
    @Override protected void initStyles()        {}
    @Override protected void initNodes()         {}
    @Override protected void initLayout()        {}
    @Override protected void initBindings()      {}
    @Override protected void initEventHandlers() {}

    @Override
    public void onShow() {
        if (getStarryBackground() != null) {
            getStarryBackground().setFollowMouse(true);
            setOnMouseMoved(e -> getStarryBackground().updateMousePosition(e.getSceneX(), e.getSceneY()));
            fadeIn();
        }
    }
}
