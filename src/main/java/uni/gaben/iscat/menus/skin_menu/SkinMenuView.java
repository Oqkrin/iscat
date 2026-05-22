package uni.gaben.iscat.menus.skin_menu;

import javafx.scene.layout.StackPane;
import uni.gaben.iscat.AbstractIscatStackPane;

public class SkinMenuView extends AbstractIscatStackPane {
    public SkinMenuView() {
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
