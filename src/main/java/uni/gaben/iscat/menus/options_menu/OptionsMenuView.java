package uni.gaben.iscat.menus.options_menu;

import javafx.scene.layout.StackPane;
import uni.gaben.iscat.AbstractIscatStackPane;

public class OptionsMenuView extends AbstractIscatStackPane {
    public OptionsMenuView() {
        super(new StackPane(), true);
        initialize("/uni/gaben/iscat/fxml/options_menu.fxml");
    }
    @Override protected void initStyles()        {}
    @Override protected void initNodes()         {}
    @Override protected void initLayout()        {}
    @Override protected void initBindings()      {}
    @Override protected void initEventHandlers() {}
}
