package uni.gaben.iscat.game.view;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import uni.gaben.iscat.game.controller.GameController;

/** Scena di gioco: posiziona il canvas e collega il controller. */
public class GameScene extends Scene {
    private StackPane root;
    private final PauseMenu pauseMenu;

    public GameScene(GameController controller, GameCanvas canvas) {
        super(new StackPane());
        this.root = (StackPane) getRoot();

        this.pauseMenu = new PauseMenu(controller);
        root.getChildren().addAll(canvas, pauseMenu);

        String css = getClass().getResource("/uni/gaben/iscat/styles/menu.css").toExternalForm();
        getStylesheets().add(css);

        controller.setOnPauseToggle(pauseMenu::show);
        controller.attachInput(this);
        controller.startLoop();
    }

}
