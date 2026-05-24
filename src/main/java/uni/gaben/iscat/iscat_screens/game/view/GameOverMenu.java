package uni.gaben.iscat.iscat_screens.game.view;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import uni.gaben.iscat.iscat_screens.game.controller.GameController;

public class GameOverMenu extends VBox {

    public GameOverMenu(GameController controller) {
        getStyleClass().addAll("spacing-lg", "game-pause-overlay");
        setAlignment(Pos.CENTER);

        Label title = new Label("GAME OVER");
        title.getStyleClass().add("pause-title");

        Button retryBtn = createBigButton("RETRY");
        retryBtn.setOnAction(e -> controller.retryGame());

        Button menuBtn = createBigButton("RETURN TO MAIN MENU");
        menuBtn.setOnAction(e -> controller.quitToMainMenu());

        Button quitBtn = createBigButton("QUIT GAME");
        quitBtn.getStyleClass().add("btn-danger-outline");
        quitBtn.setOnAction(e -> controller.quitGame());

        getChildren().addAll(title, retryBtn, menuBtn, quitBtn);
    }

    private Button createBigButton(String text) {
        Button btn = new Button(text);
        btn.getStyleClass().add("pulsante-menu");
        btn.setPrefWidth(300);
        btn.setPrefHeight(50);
        return btn;
    }
}