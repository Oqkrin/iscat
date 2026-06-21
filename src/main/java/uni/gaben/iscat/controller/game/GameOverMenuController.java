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

/**
 * Controller per la gestione dei menu di fine partita (Vittoria o Sconfitta).
 */
public class GameOverMenuController implements IscatFxmlController {

    @FXML private Label titleLabel;
    @FXML private Label sessionScoreLabel;
    @FXML private Button continueBtn;
    @FXML private Button retryBtn, menuBtn, quitBtn;

    private GameController gameController;
    private GameView gameView;

    /**
     * Inizializza i componenti grafici applicando icone ed effetti visivi ai pulsanti.
     */
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

    /**
     * Collega il controller e la vista di gioco, registrando il listener sullo stato della partita.
     *
     * @param controller Il controller logico del gioco.
     * @param view       La vista principale del gioco.
     */
    public void initData(GameController controller, GameView view) {
        this.gameController = controller;
        this.gameView       = view;

        controller.getGameModel().gameStateProperty().addListener((obs, oldState, newState) -> {
            if (newState == GameState.GAME_OVER) {
                int frozenScore = SessionScoreTracker.getInstance().getScore();
                SessionScoreTracker.getInstance().resetScore();
                titleLabel.setText("YOU DIED");
                sessionScoreLabel.setText(String.format("SCORE: %,d", frozenScore));
                setWinMode(false);
            }
            if (newState == GameState.WIN) {
                int frozenScore = SessionScoreTracker.getInstance().getScore();
                SessionScoreTracker.getInstance().resetScore();
                titleLabel.setText("HAI VINTO");
                sessionScoreLabel.setText(String.format("SCORE: %,d", frozenScore));
                setWinMode(true);
            }
        });
    }

    /**
     * Modifica la visibilità e la disposizione dei pulsanti in base all'esito della partita.
     *
     * @param win Vero se la partita è stata vinta, falso se è terminata in game over.
     */
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

    /**
     * Genera la stringa testuale formattata associata al punteggio corrente.
     *
     * @return La stringa del punteggio formattata.
     */
    private String buildScoreText() {
        int score = SessionScoreTracker.getInstance().getScore();
        return String.format("SCORE: %,d", score);
    }

    /**
     * Gestisce l'azione del pulsante di continuazione verso i titoli di coda.
     */
    @FXML
    private void handleContinue() {
        if (gameController != null) gameController.stopGameLoop();
        IscatNavigator.getInstance().navigateWithFade(IscatViews.CREDITS);
    }

    /**
     * Gestisce l'azione del pulsante per ricominciare una nuova partita.
     */
    @FXML
    private void handleRetry() {
        if (gameView != null) gameView.transitionTo(GameState.PLAYING);
        if (gameController != null) gameController.retryGame();
    }

    /**
     * Gestisce l'azione del pulsante per tornare al menu principale.
     */
    @FXML
    private void handleQuitToMenu() {
        if (gameController != null) gameController.quitToMainMenu();
    }

    /**
     * Gestisce l'azione del pulsante per chiudere l'applicazione.
     */
    @FXML
    private void handleQuitGame() {
        if (gameController != null) gameController.quitGame();
    }

    @Override
    public void setPointerToView(StackPane pointer) {}
}