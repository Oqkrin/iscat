package uni.gaben.iscat.menu.view;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;
import uni.gaben.iscat.IscatSceneAbstract;
import uni.gaben.iscat.menu.controller.MenuController;

/**
 * Schermata menu principale con pulsanti per navigare nel gioco.
 */
public class MenuScene extends IscatSceneAbstract {

    private static final String CSS_MENU_BUTTON = "menu-button";
    private static final String CSS_TITLE = "menu-title";

    private final MenuController controller;
    
    private StackPane root;
    private Label title;
    private Button playButton;
    private Button optionsButton;
    private Button scoreButton;
    private Button quitButton;

    public MenuScene(MenuController menuController) {
        super(new StackPane());
        this.controller = menuController;
        this.root = (StackPane) getRoot();
        
        initialize();
    }

    @Override
    protected void initStyles() {
        String css = getClass().getResource("/uni/gaben/iscat/styles/menu.css").toExternalForm();
        getStylesheets().add(css);
        setFill(Color.BLACK);
    }

    @Override
    protected void initNodes() {
        title = new Label("ISCAT");
        title.getStyleClass().add(CSS_TITLE);

        // Play Button
        FontIcon playIcon = new FontIcon("fas-play");
        playIcon.setIconSize(40);
        playButton = new Button("PLAY", playIcon);
        playButton.getStyleClass().add(CSS_MENU_BUTTON);

        // Options Button
        FontIcon optionIcon = new FontIcon("fas-cog");
        optionIcon.setIconSize(40);
        optionsButton = new Button("OPTIONS", optionIcon);
        optionsButton.getStyleClass().add(CSS_MENU_BUTTON);

        // Score Button
        FontIcon scoreIcon = new FontIcon("fas-eye");
        scoreIcon.setIconSize(40);
        scoreButton = new Button("VIEW SCORE", scoreIcon);
        scoreButton.getStyleClass().add(CSS_MENU_BUTTON);

        // Quit Button
        FontIcon quitIcon = new FontIcon("fas-door-open");
        quitIcon.setIconSize(40);
        quitButton = new Button("QUIT", quitIcon);
        quitButton.getStyleClass().add(CSS_MENU_BUTTON);
    }

    @Override
    protected void initLayout() {
        VBox layout = new VBox(title, playButton, optionsButton, scoreButton, quitButton);
        layout.setAlignment(Pos.CENTER);
        layout.setSpacing(20);

        root.getChildren().add(layout);
    }

    @Override
    protected void initBindings() {
        // Nessun binding necessario per il menu
    }

    @Override
    protected void initEventHandlers() {
        playButton.setOnAction(e -> controller.play());
        quitButton.setOnAction(e -> controller.quit());
        // TODO: optionsButton e scoreButton quando implementati
    }
}
