package uni.gaben.iscat.game.view;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import uni.gaben.iscat.game.controller.GameController;

public class PauseMenu extends StackPane {

    private static final String CSS_MENU_BUTTON = "menu-button";
    private final VBox mainButtons;
    private final OptionsMenu optionsMenu;

    public PauseMenu(GameController controller) {

        // Sfondo menu
        setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        // Pulsanti del menu
        mainButtons = new VBox(20);
        mainButtons.setAlignment(Pos.CENTER);

        // Titolo menu
        Label pauseLabel = new Label("GAME IS NOW PAUSED");
        pauseLabel.setStyle("-fx-text-fill: white; -fx-font-size: 40px;");

        // RESUME BUTTON
        Button resumeBtn = new Button("RIPRENDI");
        resumeBtn.getStyleClass().add(CSS_MENU_BUTTON);
        resumeBtn.setOnAction(e -> controller.togglePause());

        // OPTIONS BUTTON
        Button optionsBtn = new Button("OPTIONS");
        optionsBtn.getStyleClass().add(CSS_MENU_BUTTON);
        optionsBtn.setOnAction(e -> toggleOptions(true));

        // MAIN MENU BUTTON
        Button mainMenuBtn = new Button("MAIN MENU");
        mainMenuBtn.getStyleClass().add(CSS_MENU_BUTTON);
        /* TODO: far tornare il gioco al menu principale */

        // QUIT BUTTON
        Button quitBtn = new Button("QUIT");
        quitBtn.getStyleClass().add(CSS_MENU_BUTTON);
        quitBtn.setOnAction(e -> Platform.exit());

        // Aggiungiamo tutti i pulsanti creati nel VBox
        mainButtons.getChildren().addAll(pauseLabel, resumeBtn, optionsBtn, mainMenuBtn, quitBtn);

        // Menu Opzioni
        optionsMenu = new OptionsMenu(() -> toggleOptions(false));
        optionsMenu.setVisible(false); // Nascosto all'inizi

        // Aggiungiamo allo StackPane entrambi i menu
        getChildren().addAll(mainButtons, optionsMenu);

        // Il menu è invisible inizialmente
        setVisible(false);
        setMouseTransparent(true);
    }

    public void show(boolean show) {
        setVisible(show);
        setMouseTransparent(!show);
        if (show) {
            toggleOptions(false); // Reset: mostra sempre i bottoni main quando apri la pausa
            toFront();
        }
    }

    private void toggleOptions(boolean showOptions) {
        optionsMenu.setVisible(showOptions);
    }
}