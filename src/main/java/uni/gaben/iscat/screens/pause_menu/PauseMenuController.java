package uni.gaben.iscat.screens.pause_menu;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import uni.gaben.iscat.controller.IscatFxmlController;
import uni.gaben.iscat.screens.game.controller.GameController;
import uni.gaben.iscat.screens.options.OptionsMenuController;
import uni.gaben.iscat.utils.AudioManager;
import uni.gaben.iscat.utils.AudioSettings;

public class PauseMenuController implements IscatFxmlController {

    @FXML private Slider musicSlider;
    @FXML private Slider sfxSlider;
    @FXML private CheckBox fpsCheck;
    @FXML private CheckBox debugCheck;

    @FXML private Button resumeBtn;
    @FXML private Button menuBtn;
    @FXML private Button quitBtn;
    @FXML private Button optionBtn;

    private GameController gameController;
    private StackPane contentRoot;

    @FXML
    public void initialize() {
        // Iniezione delle icone stabili
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
                AudioManager.getInstance().setBgmVolume(newV.doubleValue())
        );

        sfxSlider.valueProperty().addListener((obs, oldV, newV) ->
                AudioManager.getInstance().setSfxVolume(newV.doubleValue())
        );
    }

    /**
     * Inietta il GameController e sincronizza lo stato delle checkbox di gioco.
     */
    public void initData(GameController controller) {
        this.gameController = controller;

        fpsCheck.setSelected(gameController.isFpsOn());
        debugCheck.setSelected(gameController.isDebugModeOn());

        fpsCheck.selectedProperty().addListener((obs, oldV, newV) ->
                gameController.setShowFps(newV)
        );

        debugCheck.selectedProperty().addListener((obs, oldV, newV) ->
                gameController.setShowDebugMode(newV)
        );
    }

    @FXML
    private void handleResume() {
        if (gameController != null) {
            gameController.togglePause();
        }
    }

    @FXML
    private void handleQuitToMenu() {
        if (gameController != null) {
            gameController.quitToMainMenu();
        }
    }

    @FXML
    private void handleQuitGame() {
        if (gameController != null) {
            gameController.quitGame();
        }
    }

    @FXML
    public void openOptionMenu() {
        try {
            VBox pauseView = (VBox) resumeBtn.getParent();
            StackPane gameRoot = (StackPane) pauseView.getParent();

            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/uni/gaben/iscat/fxml/options_menu.fxml")
            );
            StackPane optionsView = loader.load();

            OptionsMenuController optionsController = loader.getController();

            gameController.setOptionsMenuOpen(true);

            pauseView.setOpacity(0.0);
            pauseView.setMouseTransparent(true);
            pauseView.setDisable(true);

            optionsController.setCustomBackAction(() -> {
                gameRoot.getChildren().remove(optionsView);

                pauseView.setOpacity(1.0);
                pauseView.setMouseTransparent(false);
                pauseView.setDisable(false);
                gameController.setOptionsMenuOpen(false);
            });

            gameRoot.getChildren().add(optionsView);

        } catch (Exception e) {
            System.err.println("ERRORE caricamento Options da Pausa:");
            e.printStackTrace();
        }
    }

    @Override
    public void setContentRoot(StackPane contentRoot) {
        this.contentRoot = contentRoot;
    }
}