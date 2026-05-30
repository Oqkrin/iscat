package uni.gaben.iscat.view;

import javafx.scene.layout.StackPane;

/**
 * View generica utilizzata per caricare al volo qualsiasi FXML
 * senza bisogno di creare una classe Java dedicata.
 */
public class GenericIscatView extends AbstractIscatStackPane {
    public GenericIscatView(String fxmlPath) {
        super(new StackPane(), true);
        initialize(fxmlPath);
    }
}