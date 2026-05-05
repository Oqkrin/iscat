package uni.gaben.iscat.game.view;

import javafx.scene.Scene;
import javafx.scene.layout.StackPane;

public class GameScene extends Scene {
    private GameCanvas gameCanvas;
    public GameScene(GameController gameController, GameModel gameModel) {
        super(new StackPane());
        gameCanvas = new GameCanvas(gameModel, gameController);
    }
}
