package uni.gaben.iscat.screens.pause_menu;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.layout.StackPane;
import uni.gaben.iscat.controller.IscatFxmlController;
import uni.gaben.iscat.screens.game.controller.GameController;
import uni.gaben.iscat.screens.game.view.GameView;
import uni.gaben.iscat.utils.AudioManager;
import uni.gaben.iscat.utils.AudioSettings;

public class PauseMenuController implements IscatFxmlController {

    @FXML private Slider   musicSlider;
    @FXML private Slider   sfxSlider;
    @FXML private CheckBox fpsCheck;
    @FXML private CheckBox debugCheck;

    @FXML private Button resumeBtn;
    @FXML private Button menuBtn;
    @FXML private Button quitBtn;
    @FXML private Button optionBtn;

    private GameController gameController;
    private GameView       gameView;

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

        musicSlider.setValue(AudioSettings.VOLUME_BGM);
        sfxSlider.setValue(AudioSettings.VOLUME_SFX);

        musicSlider.valueProperty().addListener((obs, oldV, newV) ->
                AudioManager.getInstance().setBgmVolume(newV.doubleValue()));

        sfxSlider.valueProperty().addListener((obs, oldV, newV) ->
                AudioManager.getInstance().setSfxVolume(newV.doubleValue()));
    }

    /**
     * Inietta controller e view. Chiamato da GameView dopo il caricamento FXML.
     */
    public void initData(GameController controller, GameView view) {
        this.gameController = controller;
        this.gameView       = view;

        fpsCheck.setSelected(gameController.isFpsOn());
        debugCheck.setSelected(gameController.isDebugModeOn());

        fpsCheck.selectedProperty().addListener((obs, oldV, newV) ->
                gameController.setShowFps(newV));

        debugCheck.selectedProperty().addListener((obs, oldV, newV) ->
                gameController.setShowDebugMode(newV));
    }

    @FXML
    private void handleResume() {
        if (gameView != null) gameView.transitionTo(
                gameView.getGameState().onEscape());
    }

    @FXML
    private void handleQuitToMenu() {
        if (gameController != null) gameController.quitToMainMenu();
    }

    @FXML
    private void handleQuitGame() {
        if (gameController != null) gameController.quitGame();
    }

    @FXML
    private void openOptionMenu() {
        if (gameView != null) gameView.openOptions();
    }

    @Override
    public void setContentRoot(StackPane contentRoot) { /* non usato */ }
}