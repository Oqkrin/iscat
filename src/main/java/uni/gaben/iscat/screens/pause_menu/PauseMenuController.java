package uni.gaben.iscat.screens.pause_menu;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import uni.gaben.iscat.controller.IscatFxmlController;
import uni.gaben.iscat.screens.confirmation_overlay.ConfirmationOverlayController;
import uni.gaben.iscat.screens.game.controller.GameController;
import uni.gaben.iscat.screens.game.view.GameView;
import uni.gaben.iscat.screens.options.OptionAudioController;
import uni.gaben.iscat.screens.options.OptionDisplayController;

public class PauseMenuController implements IscatFxmlController {

    @FXML private Button resumeBtn;
    @FXML private Button menuBtn;
    @FXML private Button quitBtn;
    @FXML private Button optionBtn;

    @FXML private OptionDisplayController subDisplayController;
    @FXML private OptionAudioController subAudioController;

    private GameController gameController;
    private GameView       gameView;

    @FXML private StackPane confirmOverlay;
    @FXML private ConfirmationOverlayController confirmOverlayController;

    @FXML
    public void initialize() {
        applyIconButton(resumeBtn, "fas-play");
        applyIconButton(menuBtn,   "fas-home");
        applyIconButton(quitBtn,   "fas-power-off");
        applyIconButton(optionBtn, "fas-sliders-h");

        setupButtonHoverTween(resumeBtn);
        setupButtonHoverTween(menuBtn);
        setupButtonHoverTween(quitBtn);
        setupButtonHoverTween(optionBtn);
    }

    /**
     * Inietta controller e view della partita.
     */
    public void initData(GameController controller, GameView view) {
        this.gameController = controller;
        this.gameView       = view;

        if (subDisplayController != null) {
            subDisplayController.getCheckFps().setSelected(gameController.isFpsOn());
            subDisplayController.getDebugModeCheck().setSelected(gameController.isDebugModeOn());

            subDisplayController.getCheckFps().selectedProperty().addListener((obs, oldV, newV) ->
                    gameController.setShowFps(newV));

            subDisplayController.getDebugModeCheck().selectedProperty().addListener((obs, oldV, newV) ->
                    gameController.setShowDebugMode(newV));
        }
    }

    @FXML
    private void handleResume() {
        if (gameView != null) gameView.transitionTo(gameView.getGameState().onEscape());
    }

    @FXML
    private void handleQuitToMenu() {
        if (confirmOverlayController != null) {
            confirmOverlayController.ask(
                    "Uscire dal Gioco?",
                    "Lo score della partita corrente verrà calcolato.",
                    () -> { if (gameController != null) gameController.quitToMainMenu(); }
            );
        } else {
            if (gameController != null) gameController.quitToMainMenu();
        }
    }

    @FXML
    private void handleQuitGame() {
        if (confirmOverlayController != null) {
            confirmOverlayController.ask(
                    "Uscire dal Gioco?",
                    "I progressi della partita corrente andranno persi.",
                    () -> { if (gameController != null) gameController.quitGame(); }
            );
        } else {
            if (gameController != null) gameController.quitGame();
        }
    }

    @FXML
    private void openOptionMenu() {
        if (gameView != null) gameView.openOptions();
    }

    @Override
    public void setContentRoot(StackPane contentRoot) {}
}