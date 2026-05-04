package uni.gaben.iscat.game;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage window) {
        GamePanel gamePanel = new GamePanel();

        Scene scene = new Scene(gamePanel);

        window.setTitle("ISCAT");
        window.setResizable(false);
        window.setScene(scene);
        window.centerOnScreen();
        window.show();

        gamePanel.startGameThread();
    }

    public static void main(String[] args) {
        launch(args);
    }
}