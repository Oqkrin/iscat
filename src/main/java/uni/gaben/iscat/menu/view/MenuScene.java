package uni.gaben.iscat.menu.view;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;
import uni.gaben.iscat.menu.controller.MenuController;
import uni.gaben.iscat.utils.rapporto_aureo.ScalareAureo;

public class MenuScene extends Scene {

    private static final String CSS_MENU_BUTTON = "menu-button";

    public MenuScene(MenuController menuController) {
        super(new StackPane(), Color.BLACK);
        StackPane root = (StackPane) getRoot();

        FontIcon playIcon = new FontIcon("fas-play");
        playIcon.setIconSize(40);

        Button playButton = new Button("PLAY", playIcon);
        playButton.getStyleClass().add(CSS_MENU_BUTTON);

        // Azione del controller
        playButton.setOnAction(e -> menuController.play());

        VBox layout = new VBox(playButton);
        layout.setAlignment(Pos.CENTER);

        layout.setSpacing(20);

        root.getChildren().add(layout);

        // css in futuro
        // String css = getClass().getResource("/uni/gaben/iscat/styles/menu.css").toExternalForm();
        // getStylesheets().add(css);
    }
}