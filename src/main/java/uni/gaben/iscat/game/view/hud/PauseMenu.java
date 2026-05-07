package uni.gaben.iscat.game.view.hud;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import uni.gaben.iscat.game.controller.GameController;

/**
 * Menu di pausa del gioco.
 * Usa CSS classes dal design system aureo.
 */
public class PauseMenu extends StackPane {

    private final OptionsMenu optionsMenu;

    public PauseMenu(GameController controller) {
        // Applica CSS class per sfondo
        getStyleClass().add("pause-menu");
        setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        // Contenitore pulsanti principali
        VBox mainButtons = new VBox();
        mainButtons.getStyleClass().addAll("contenitore-pulsanti", "spacing-md");
        mainButtons.setAlignment(Pos.CENTER);

        // Titolo - usa CSS class
        Label pauseLabel = new Label("GAME IS NOW PAUSED");
        pauseLabel.getStyleClass().add("pause-title");

        // RESUME BUTTON
        Button resumeBtn = new Button("RIPRENDI");
        resumeBtn.getStyleClass().add("pulsante-menu");
        resumeBtn.setOnAction(e -> controller.togglePause());

        // OPTIONS BUTTON
        Button optionsBtn = new Button("OPTIONS");
        optionsBtn.getStyleClass().add("pulsante-menu");
        optionsBtn.setOnAction(e -> toggleOptions(true));

        // MAIN MENU BUTTON
        Button mainMenuBtn = new Button("MAIN MENU");
        mainMenuBtn.getStyleClass().add("pulsante-menu");
        mainMenuBtn.setOnAction(e -> controller.exitToMainMenu());

        // QUIT BUTTON
        Button quitBtn = new Button("QUIT");
        quitBtn.getStyleClass().addAll("pulsante-menu", "pericolo");
        quitBtn.setOnAction(e -> Platform.exit());

        // Aggiungi tutti i pulsanti
        mainButtons.getChildren().addAll(pauseLabel, resumeBtn, optionsBtn, mainMenuBtn, quitBtn);

        // Menu Opzioni
        optionsMenu = new OptionsMenu(() -> toggleOptions(false));
        optionsMenu.setVisible(false);

        // Aggiungi entrambi i menu
        getChildren().addAll(mainButtons, optionsMenu);

        // Inizialmente invisibile
        setVisible(false);
        setMouseTransparent(true);
    }

    /**
     * Mostra/nascondi il menu di pausa.
     * @param show true per mostrare, false per nascondere
     */
    public void show(boolean show) {
        setVisible(show);
        setMouseTransparent(!show);
        if (show) {
            toggleOptions(false); // Reset: mostra sempre i bottoni main
            toFront();
        }
    }

    private void toggleOptions(boolean showOptions) {
        optionsMenu.setVisible(showOptions);
    }
}