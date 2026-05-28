package uni.gaben.iscat.screens.options;

import javafx.scene.layout.StackPane;
import uni.gaben.iscat.view.AbstractIscatStackPane;

public class OptionsMenuView extends AbstractIscatStackPane {
    public OptionsMenuView() {
        super(new StackPane(), true);

    }

    @Override
    public void onShow() {
        super.onShow();
        initialize("/uni/gaben/iscat/fxml/options_menu.fxml");    }
}
