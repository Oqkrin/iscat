package uni.gaben.iscat.menus.bestiary_menu;

import javafx.scene.SceneAntialiasing;
import javafx.scene.layout.StackPane;
import uni.gaben.iscat.IscatSceneAbstract;

public class BestiaryMenuScene extends IscatSceneAbstract {
    public BestiaryMenuScene() {
        super(new StackPane(), true, SceneAntialiasing.DISABLED);
        loadFxml("/uni/gaben/iscat/fxml/bestiary_menu.fxml");
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
