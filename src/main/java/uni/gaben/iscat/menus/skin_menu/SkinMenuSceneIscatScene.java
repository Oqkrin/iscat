package uni.gaben.iscat.menus.skin_menu;

import javafx.scene.SceneAntialiasing;
import javafx.scene.layout.StackPane;
import uni.gaben.iscat.AbstractIscatScene;

public class SkinMenuSceneIscatScene extends AbstractIscatScene {
    public SkinMenuSceneIscatScene() {
        super(new StackPane(), true);
        initialize("/uni/gaben/iscat/fxml/skin_menu.fxml");
    }

    @Override
    public void onShow() {
        if (getStarryBackground() != null) {
            getStarryBackground().setFollowMouse(true);
            setOnMouseMoved(e -> getStarryBackground().updateMousePosition(e.getSceneX(), e.getSceneY()));
            fadeIn();
        }
    }

    @Override
    protected void initStyles() {

    }

    @Override
    protected void initNodes() {

    }

    @Override
    protected void initLayout() {

    }

    @Override
    protected void initBindings() {

    }

    @Override
    protected void initEventHandlers() {

    }
}
