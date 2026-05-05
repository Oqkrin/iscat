package uni.gaben.iscat.game.view;

import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import uni.gaben.iscat.game.controller.GameController;
import uni.gaben.iscat.game.model.entities.GameModel;

public class GameScene extends Scene {
    public GameScene() {
        super(new StackPane());

        GameModel model = new GameModel();
        GameCanvas canvas = new GameCanvas(model);
        GameController controller = new GameController(model, canvas);

        StackPane root = (StackPane) getRoot();
        root.getChildren().add(canvas);

        controller.attachInput(this);
        controller.startLoop();
    }
}