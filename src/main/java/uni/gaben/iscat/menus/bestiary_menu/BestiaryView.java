package uni.gaben.iscat.menus.bestiary_menu;

import javafx.scene.layout.StackPane;
import uni.gaben.iscat.AbstractIscatStackPane;

public class BestiaryView extends AbstractIscatStackPane {
    public BestiaryView() {
        super(new StackPane(),true);
        initialize("/uni/gaben/iscat/fxml/bestiary_menu.fxml");
    }
    @Override protected void initStyles()        {}
    @Override protected void initNodes()         {}
    @Override protected void initLayout()        {}
    @Override protected void initBindings()      {}
    @Override protected void initEventHandlers() {}
}
