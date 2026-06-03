package uni.gaben.iscat.controller.game;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import uni.gaben.iscat.controller.IscatFxmlController;
import uni.gaben.iscat.model.game.GameState;
import uni.gaben.iscat.view.game.GameView;

public class GameOverMenuController implements IscatFxmlController {

    @FXML private Button retryBtn;
    @FXML private Button menuBtn;
    @FXML private Button quitBtn;

    private GameController gameController;
    private GameView gameView;


    @FXML
    public void initialize() {
        applyIconButton(retryBtn, "fas-redo");
        applyIconButton(menuBtn,  "fas-home");
        applyIconButton(quitBtn,  "fas-power-off");

        setupButtonHoverTween(retryBtn);
        setupButtonHoverTween(menuBtn);
        setupButtonHoverTween(quitBtn);
    }

    public void initData(GameController controller, GameView view) {
        this.gameController = controller;
        this.gameView       = view;
    }

    @FXML
    private void handleRetry() {
        if (gameView != null) gameView.transitionTo(GameState.PLAYING);
        if (gameController != null) gameController.retryGame();
    }

    @FXML
    private void handleQuitToMenu() {
        if (gameController != null) gameController.quitToMainMenu();
    }

    @FXML
    private void handleQuitGame() {
        if (gameController != null) gameController.quitGame();
    }

    @Override
    public void setContentRoot(StackPane contentRoot) {}
}