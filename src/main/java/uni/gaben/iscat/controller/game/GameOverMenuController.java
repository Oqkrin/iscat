package uni.gaben.iscat.controller.game;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import uni.gaben.iscat.IscatNavigator;
import uni.gaben.iscat.controller.interfaces.IscatFxmlController;
import uni.gaben.iscat.model.IscatViews;
import uni.gaben.iscat.model.game.GameState;
import uni.gaben.iscat.utils.ComponentsUtils;
import uni.gaben.iscat.utils.SessionScoreTracker;
import uni.gaben.iscat.view.game.GameView;

public class GameOverMenuController implements IscatFxmlController {

    @FXML private Label  titleLabel;
    @FXML private Label  sessionScoreLabel;
    @FXML private Button continueBtn;
    @FXML private Button retryBtn;
    @FXML private Button menuBtn;
    @FXML private Button quitBtn;

    private GameController gameController;
    private GameView       gameView;

    @FXML
    public void initialize() {
        ComponentsUtils.applyIconButton(continueBtn, "fas-arrow-right");
        ComponentsUtils.applyIconButton(retryBtn,    "fas-redo");
        ComponentsUtils.applyIconButton(menuBtn,     "fas-home");
        ComponentsUtils.applyIconButton(quitBtn,     "fas-power-off");

        ComponentsUtils.setupButtonHoverTween(continueBtn);
        ComponentsUtils.setupButtonHoverTween(retryBtn);
        ComponentsUtils.setupButtonHoverTween(menuBtn);
        ComponentsUtils.setupButtonHoverTween(quitBtn);
    }

    public void initData(GameController controller, GameView view) {
        this.gameController = controller;
        this.gameView       = view;

        controller.getGameModel().gameStateProperty().addListener((obs, oldState, newState) -> {
            if (newState == GameState.GAME_OVER) {
                titleLabel.setText("YOU DIED");
                sessionScoreLabel.setText(buildScoreText());
                setWinMode(false);
            }
            if (newState == GameState.WIN) {
                titleLabel.setText("HAI VINTO");
                sessionScoreLabel.setText(buildScoreText());
                setWinMode(true);
            }
        });
    }

    private void setWinMode(boolean win) {
        continueBtn.setVisible(win);
        continueBtn.setManaged(win);

        retryBtn.setVisible(!win);
        retryBtn.setManaged(!win);
        menuBtn.setVisible(!win);
        menuBtn.setManaged(!win);
        quitBtn.setVisible(!win);
        quitBtn.setManaged(!win);
    }

    private String buildScoreText() {
        int score = SessionScoreTracker.getInstance().getScore();
        return String.format("SCORE: %,d", score);
    }

    @FXML
    private void handleContinue() {
        if (gameController != null) gameController.quitToMainMenu();
        IscatNavigator.getInstance().navigateWithFade(IscatViews.CREDITS);
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
    public void setPointerToView(StackPane pointer) {}
}