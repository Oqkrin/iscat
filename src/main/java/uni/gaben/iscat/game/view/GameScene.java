package uni.gaben.iscat.game.view;

import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import uni.gaben.iscat.game.controller.GameController;

/** Scena di gioco: posiziona il canvas e collega il controller. */
public class GameScene extends Scene {

    public GameScene(GameController controller, GameCanvas canvas) {
        super(new StackPane());
        ((StackPane) getRoot()).getChildren().add(canvas);
        controller.attachInput(this);
        controller.startLoop();
    }
}
