package uni.gaben.iscat.menu.view;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

public class MenuScene extends Scene {
    public MenuScene() {
        super(new StackPane());
        Button button = new Button("play");
        button.fire();
    }
}
