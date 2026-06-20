package uni.gaben.iscat.controller.game;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import uni.gaben.iscat.controller.interfaces.IscatFxmlController;
import uni.gaben.iscat.controller.components.ConfirmationOverlayController;
import uni.gaben.iscat.utils.ComponentsUtils;
import uni.gaben.iscat.view.game.GameView;
import uni.gaben.iscat.controller.components.settings.AudioSettingsController;
import uni.gaben.iscat.controller.components.settings.DisplaySettingsController;

public class GamePauseMenuController implements IscatFxmlController {

    @FXML private Button resumeBtn;
    @FXML private Button menuBtn;
    @FXML private Button quitBtn;
    @FXML private Button settingsBtn;

    @FXML private DisplaySettingsController subDisplayController;
    @FXML private AudioSettingsController subAudioController;

    private GameController gameController;
    private GameView       gameView;

    @FXML private StackPane confirmOverlay;
    @FXML private ConfirmationOverlayController confirmOverlayController;

    @FXML
    public void initialize() {
        ComponentsUtils.applyIconButton(resumeBtn, "fas-play");
        ComponentsUtils.applyIconButton(menuBtn,   "fas-home");
        ComponentsUtils.applyIconButton(quitBtn,   "fas-power-off");
        ComponentsUtils.applyIconButton(settingsBtn, "fas-sliders-h");

        ComponentsUtils.setupButtonHoverTween(resumeBtn);
        ComponentsUtils.setupButtonHoverTween(menuBtn);
        ComponentsUtils.setupButtonHoverTween(quitBtn);
        ComponentsUtils.setupButtonHoverTween(settingsBtn);
    }

    /**
     * Inietta controller e view della partita.
     */
    public void initData(GameController controller, GameView view) {
        this.gameController = controller;
        this.gameView       = view;

        if (subDisplayController != null) {
            subDisplayController.setConfirmOverlayController(this.confirmOverlayController);

            subDisplayController.getCheckFps().setSelected(gameController.isFpsOn());
            subDisplayController.getDebugModeCheck().setSelected(gameController.isDebugModeOn());

            subDisplayController.getCheckFps().selectedProperty().addListener((obs, oldV, newV) ->
                    gameController.setShowFps(newV));

            gameController.debugModeProperty().addListener((obs, oldV, newV) -> {
                Platform.runLater(() -> {
                    if (subDisplayController.getDebugModeCheck().isSelected() != newV) {
                        subDisplayController.getDebugModeCheck().setSelected(newV);
                    }
                });
            });

            subDisplayController.setGameController(gameController);
        }
    }

    public void syncVisualState() {
        if (subDisplayController != null && gameController != null) {
            subDisplayController.getCheckFps().setSelected(gameController.isFpsOn());
            subDisplayController.getDebugModeCheck().setSelected(gameController.isDebugModeOn());

            if (resumeBtn != null && resumeBtn.getScene() != null) {
                javafx.stage.Stage stage = (javafx.stage.Stage) resumeBtn.getScene().getWindow();
                if (stage != null && subDisplayController.getFullscreenCheck() != null) {
                    subDisplayController.getFullscreenCheck().setSelected(stage.isFullScreen());
                }
            }
        }
    }

    @FXML
    private void handleResume() {
        if (gameView != null) gameView.transitionTo(gameController.getGameModel().getGameState().onEscape());
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
    private void openSettingsMenu() {
        if (gameView != null) gameView.openSettings();
    }

    @Override
    public void setPointerToView(StackPane pointer) {}
}