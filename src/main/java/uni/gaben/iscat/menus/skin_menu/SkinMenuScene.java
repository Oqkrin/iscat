package uni.gaben.iscat.menus.skin_menu;

import javafx.scene.SceneAntialiasing;
import javafx.scene.layout.StackPane;
import uni.gaben.iscat.IscatSceneAbstract;

public class SkinMenuScene extends IscatSceneAbstract {
    public SkinMenuScene() {
        super(new StackPane(), true, SceneAntialiasing.DISABLED);
        loadFxml("/uni/gaben/iscat/fxml/skin_menu.fxml");
    }
    @Override protected void initStyles()        {}
    @Override protected void initNodes()         {}
    @Override protected void initLayout()        {}
    @Override protected void initBindings()      {}
    @Override protected void initEventHandlers() {}

    @Override
    public void onShow() {
        if (getStarryBackground() != null) getStarryBackground().setFollowMouse(true);
    }
}
