package uni.gaben.iscat.game;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;

public class GameScene extends Scene {
    private GameScene(Parent root) {
        super(root);
    }

    public GameScene() {
        super(new StackPane());
        StackPane root = (StackPane) getRoot();
        //root.getChildren().add(new GamePanel());
    }
}
