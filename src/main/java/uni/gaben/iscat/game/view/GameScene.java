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
    private VBox pauseMenu;

    private static final String CSS_MENU_BUTTON = "menu-button";

    public GameScene(GameController controller, GameCanvas canvas) {
        super(new StackPane());
        this.root = (StackPane) getRoot();
        root.getChildren().add(canvas);

        createPauseMenu(controller);
        controller.setOnPauseToggle(isPaused -> showPauseMenu(isPaused));

        controller.attachInput(this);
        controller.startLoop();
    }

    public void showPauseMenu(boolean show) {
        pauseMenu.setVisible(show);
        pauseMenu.setMouseTransparent(!show);
    }

    private void createPauseMenu(GameController controller) {
        pauseMenu = new VBox(20);
        pauseMenu.setAlignment(Pos.CENTER);
        pauseMenu.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);"); // Sfondo semi-trasparente

        Label pauseLabel = new Label("GAME IS NOW PAUSED");
        pauseLabel.setStyle("-fx-text-fill: white; -fx-font-size: 40px;");

        Button resumeBtn = new Button("RIPRENDI");
        resumeBtn.getStyleClass().add(CSS_MENU_BUTTON);
        resumeBtn.setOnAction(e -> controller.togglePause()); // Chiamerà un metodo nel controller

        Button mainMenuBtn = new Button("MAIN MENU");
        mainMenuBtn.getStyleClass().add(CSS_MENU_BUTTON);
        mainMenuBtn.setOnAction(e -> controller.togglePause());

        Button quitBtn = new Button("QUIT");
        quitBtn.getStyleClass().add(CSS_MENU_BUTTON);
        quitBtn.setOnAction(e -> Platform.exit());

        pauseMenu.getChildren().addAll(pauseLabel, resumeBtn, mainMenuBtn, quitBtn);
        pauseMenu.setVisible(false); // Nascosto all'inizio

        String css = getClass().getResource("/uni/gaben/iscat/styles/menu.css").toExternalForm();
        getStylesheets().add(css);

        root.getChildren().add(pauseMenu);
    }
}
