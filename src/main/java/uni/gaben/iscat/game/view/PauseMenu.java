package uni.gaben.iscat.game.view;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import uni.gaben.iscat.game.controller.GameController;

public class PauseMenu extends VBox {

    private static final String CSS_MENU_BUTTON = "menu-button";

    public PauseMenu(GameController controller) {
        // Configurazione layout
        setSpacing(20);
        setAlignment(Pos.CENTER);
        setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");

        // Per coprire l'intero schermo
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        // Componenti
        Label pauseLabel = new Label("GAME IS NOW PAUSED");
        pauseLabel.setStyle("-fx-text-fill: white; -fx-font-size: 40px;");

        Button resumeBtn = new Button("RIPRENDI");
        resumeBtn.getStyleClass().add(CSS_MENU_BUTTON);
        resumeBtn.setOnAction(e -> controller.togglePause());

        Button optionsBtn = new Button("OPTIONS");
        optionsBtn.getStyleClass().add(CSS_MENU_BUTTON);
        /* TODO: aprire menu opzioni */

        Button mainMenuBtn = new Button("MAIN MENU");
        mainMenuBtn.getStyleClass().add(CSS_MENU_BUTTON);
        /* TODO: far tornare il gioco al menu principale */

        Button quitBtn = new Button("QUIT");
        quitBtn.getStyleClass().add(CSS_MENU_BUTTON);
        quitBtn.setOnAction(e -> Platform.exit());

        getChildren().addAll(pauseLabel, resumeBtn, optionsBtn, mainMenuBtn, quitBtn);

        // Inizialmente invisibile e trasparente ai click
        setVisible(false);
        setMouseTransparent(true);
    }

    public void show(boolean show) {
        this.setVisible(show);
        this.setMouseTransparent(!show);
        if (show) this.toFront();
    }
}