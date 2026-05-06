package uni.gaben.iscat.menu.view;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;
import uni.gaben.iscat.menu.controller.MenuController;
import uni.gaben.iscat.utils.audio_manager.AudioManager;

public class MenuScene extends Scene {

    private static final String CSS_MENU_BUTTON = "menu-button";
    private static final String CSS_TITLE = "menu-title";

    public MenuScene(MenuController menuController) {
        super(new StackPane(), Color.BLACK);
        StackPane root = (StackPane) getRoot();

        Label title = new Label("ISCAT");
        title.getStyleClass().add(CSS_TITLE);

        // PlayButton
        FontIcon playIcon = new FontIcon("fas-play");
        playIcon.setIconSize(40);
        Button playButton = new Button("PLAY", playIcon);
        playButton.getStyleClass().add(CSS_MENU_BUTTON);

        // OptionButton
        FontIcon optionIcon = new FontIcon("fas-cog");
        optionIcon.setIconSize(40);
        Button optionsButton = new Button("OPTIONS", optionIcon);
        optionsButton.getStyleClass().add(CSS_MENU_BUTTON);

        // ScoreButton
        FontIcon scoreIcon = new FontIcon("fas-eye");
        scoreIcon.setIconSize(40);
        Button scoreButton = new Button("VIEW SCORE", scoreIcon);
        scoreButton.getStyleClass().add(CSS_MENU_BUTTON);

        // QuitButton
        FontIcon quitIcon = new FontIcon("fas-door-open");
        quitIcon.setIconSize(40);
        Button  quitButton = new Button("QUIT", quitIcon);
        quitButton.getStyleClass().add(CSS_MENU_BUTTON);

        // Azione del controller
        playButton.setOnAction(e -> menuController.play());
        quitButton.setOnAction(e -> menuController.quit());

        VBox layout = new VBox(title, playButton,optionsButton,scoreButton,quitButton);
        layout.setAlignment(Pos.CENTER);

        layout.setSpacing(20);

        root.getChildren().add(layout);

        String css = getClass().getResource("/uni/gaben/iscat/styles/menu.css").toExternalForm();
        getStylesheets().add(css);
    }
}