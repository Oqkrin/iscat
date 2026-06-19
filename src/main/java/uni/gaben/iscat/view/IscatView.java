package uni.gaben.iscat.view;

import javafx.scene.layout.StackPane;
import uni.gaben.iscat.view.components.AbstractIscatStackPane;

/**
 * View generica utilizzata per caricare al volo qualsiasi FXML
 * senza bisogno di creare una classe Java dedicata.
 */
public class IscatView extends AbstractIscatStackPane {
    public IscatView(String fxmlPath) {
        super(new StackPane(), true);
        initialize(fxmlPath);
    }
}