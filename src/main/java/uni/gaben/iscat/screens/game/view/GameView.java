package uni.gaben.iscat.screens.game.view;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import uni.gaben.iscat.screens.pause_menu.PauseMenuController;
import uni.gaben.iscat.view.AbstractIscatStackPane;
import uni.gaben.iscat.universe.camera.CameraModel;
import uni.gaben.iscat.universe.rendering.UniverseRenderer;
import uni.gaben.iscat.screens.game.controller.GameController;
import uni.gaben.iscat.screens.game.model.GameModel;
import uni.gaben.iscat.universe.UniverseController;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.enviroment.starfield.StarfieldView;
import uni.gaben.iscat.view.StarryText;
import uni.gaben.iscat.utils.theme.ThemeManager;
import uni.gaben.iscat.utils.design.CssHelper;
import uni.gaben.iscat.utils.design.ScalareAureo;

import java.util.Objects;
import static javafx.application.Platform.runLater;

public class GameView extends AbstractIscatStackPane {

    private final GameModel gameModel;
    private final GameController gameController;
    private final StackPane root;

    private Canvas canvas;
    private Canvas timerCanvas;
    private StarryText starryTimer;
    private final StarfieldView starfieldView = new StarfieldView();
    private UniverseRenderer universeRenderer;

    private GameSpawnerToolbar spawnerToolbar;
    private VBox pauseMenu;
    private GameOverMenu gameOverMenu;

    private HBox debugButtonsContainer;
    private Button debugButton;
    private Button toggleWave;
    private Label levelLabel;

    private boolean debugPanelVisible = false;

    public GameView(GameController gameController) {
        super(new StackPane());
        this.gameController = gameController;
        this.gameModel = gameController.getGameModel();
        this.root = getContentRoot();
        this.gameController.setContentRoot(this.root);

        initialize();
    }

    @Override
    protected void initNodes() {
        canvas = new Canvas();

        spawnerToolbar = new GameSpawnerToolbar(gameController);
        gameOverMenu = new GameOverMenu(gameController);
        this.pauseMenu = loadPauseMenu();

        levelLabel = new Label("LEVEL 1");
        levelLabel.setFocusTraversable(false);
        levelLabel.setMouseTransparent(true);

        timerCanvas = new Canvas(300, 100);
        timerCanvas.setMouseTransparent(true);
        timerCanvas.setFocusTraversable(false);
        starryTimer = new StarryText(300, 100);

        debugButton = new Button("DEBUG");
        debugButton.setFocusTraversable(false);

        toggleWave = new Button("|>");
        toggleWave.setFocusTraversable(false);

        debugButtonsContainer = new HBox(10, debugButton, toggleWave);
        debugButtonsContainer.setFocusTraversable(false);

        debugButtonsContainer.setPickOnBounds(false);
        spawnerToolbar.setPickOnBounds(false);

        universeRenderer = new UniverseRenderer(canvas, gameController, starfieldView);
    }

    private VBox loadPauseMenu() {
        try {
            var loader = new javafx.fxml.FXMLLoader(getClass().getResource("/uni/gaben/iscat/fxml/pause-menu.fxml"));
            VBox view = loader.load();

            PauseMenuController pauseController = loader.getController();
            pauseController.initData(gameController);

            return view;
        } catch (java.io.IOException e) {
            throw new RuntimeException("Errore fatale: impossibile caricare l'FXML della pausa!", e);
        }
    }

    @Override
    protected void initStyles() {
        root.getStyleClass().add("game-view-container");
        getStylesheets().add(Objects.requireNonNull(GameView.class.getResource("/uni/gaben/iscat/styles/screens/game.css")).toExternalForm());

        CssHelper.stilePulsanteMenu(debugButton);
        CssHelper.testoPrimario(debugButton);

        CssHelper.stilePulsanteMenu(toggleWave);
        CssHelper.testoPrimario(toggleWave);

        levelLabel.setFont(Font.font("Miracode", FontWeight.BOLD, 24));
        levelLabel.setTextFill(ThemeManager.getInstance().getColorSuccess());
        levelLabel.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 5, 0, 0, 0);");
    }

    @Override
    protected void initLayout() {
        root.getChildren().addAll(
                canvas,
                timerCanvas,
                levelLabel,
                spawnerToolbar,
                debugButtonsContainer,
                pauseMenu,
                gameOverMenu
        );

        StackPane.setAlignment(spawnerToolbar, Pos.BOTTOM_CENTER);
        StackPane.setAlignment(debugButtonsContainer, Pos.TOP_LEFT);
        StackPane.setAlignment(timerCanvas, Pos.TOP_CENTER);
        StackPane.setAlignment(levelLabel, Pos.BOTTOM_RIGHT);

        StackPane.setMargin(debugButtonsContainer, new Insets(50, 0, 0, 50));
        StackPane.setMargin(timerCanvas, new Insets(50, 0, 0, 0));
        StackPane.setMargin(levelLabel, new Insets(0, 50, 50, 0));
    }

    @Override
    protected void initBindings() {
        canvas.widthProperty().bind(root.widthProperty());
        canvas.heightProperty().bind(root.heightProperty());

        spawnerToolbar.maxHeightProperty().bind(root.heightProperty().divide(5));
        spawnerToolbar.maxWidthProperty().bind(root.widthProperty().multiply(ScalareAureo.IPHI_D));

        CameraModel camera = gameController.getCameraModel();
        camera.screenWidthProperty().bind(canvas.widthProperty());
        camera.screenHeightProperty().bind(canvas.heightProperty());

        gameOverMenu.visibleProperty().bind(gameModel.gameOverProperty());
        gameOverMenu.managedProperty().bind(gameOverMenu.visibleProperty());

        // === MODIFICA IMPORTANTE: PauseMenu visibile solo quando in pausa e opzioni chiuse ===
        pauseMenu.visibleProperty().bind(
                gameModel.pausedProperty()
                        .and(gameModel.gameOverProperty().not())
                        .and(gameController.optionsMenuOpenProperty().not())
        );
        pauseMenu.managedProperty().bind(pauseMenu.visibleProperty());

        gameModel.pausedProperty().addListener((obs, wasPaused, isPausedNow) -> {
            if (!isPausedNow) {
                runLater(() -> canvas.requestFocus());
            }
        });

        gameController.debugModeProperty().addListener((obs, oldV, isDebugActive) -> {
            debugButtonsContainer.setVisible(isDebugActive);
            debugButtonsContainer.setManaged(isDebugActive);

            if (!isDebugActive) {
                spawnerToolbar.setVisible(false);
                spawnerToolbar.setManaged(false);
                debugPanelVisible = false;
                debugButton.setText("DEBUG");
            }
        });

        boolean initialDebug = gameController.isDebugModeOn();
        debugButtonsContainer.setVisible(initialDebug);
        debugButtonsContainer.setManaged(initialDebug);
        spawnerToolbar.setVisible(false);
        spawnerToolbar.setManaged(false);

        gameModel.timerProperty().addListener((obs, oldVal, newVal) -> updateTimerText(newVal.intValue()));
        runLater(() -> updateTimerText(gameModel.getTimer()));
    }

    /**
     * Configura i binding dinamici legati all'universo di gioco.
     */
    private void setupUniverseBoundings(UniverseModel universe, UniverseController universeController) {
        levelLabel.textProperty().unbind();

        universe.widthProperty().bind(canvas.widthProperty());
        universe.heightProperty().bind(canvas.heightProperty());

        universe.widthProperty().addListener((obs, oldV, newV) -> universeController.getStarfieldController()
                .regenerate(universe.getStarfieldModel(), newV.doubleValue(), universe.getHeight()));
        universe.heightProperty().addListener((obs, oldV, newV) -> universeController.getStarfieldController()
                .regenerate(universe.getStarfieldModel(), universe.getWidth(), newV.doubleValue()));

        starfieldView.wProperty().bind(canvas.widthProperty());
        starfieldView.hProperty().bind(canvas.heightProperty());

        var player = universe.getPlayer();
        if (player != null) {
            levelLabel.textProperty().bind(Bindings.concat("LEVEL ", player.levelProperty().asString()));
        } else {
            levelLabel.setText("LEVEL 1");
        }
    }

    @Override
    protected void initEventHandlers() {
        gameController.getInputManager().attachToCanvas(canvas);

        this.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                gameController.togglePause();
                e.consume();
            }
        });

        debugButton.setOnAction(event -> {
            if (gameController.isDebugModeOn()) {
                debugPanelVisible = !debugPanelVisible;
                spawnerToolbar.setVisible(debugPanelVisible);
                spawnerToolbar.setManaged(debugPanelVisible);
                debugButton.setText(debugPanelVisible ? "HIDE DEBUG" : "DEBUG");
                canvas.requestFocus();
            }
        });

        toggleWave.setOnAction(event -> {
            if (gameController.isDebugModeOn()) {
                gameModel.waveProperty().set(!gameModel.isWaveing());
                toggleWave.setText(gameModel.isWaveing() ? "||" : "|>");
                canvas.requestFocus();
            }
        });
    }

    @Override
    public void onShow() {
        super.onShow();
        gameModel.setPaused(false);
        gameController.setDrawCall(() -> universeRenderer.renderFrame(timerCanvas, starryTimer, debugPanelVisible));
        gameController.getInputManager().attachToScene(this.getScene());
        gameController.startGameLoop();
        UniverseController universeController = gameController.getUniverseController();
        UniverseModel universe = universeController.getUniverseModel();
        if (universe != null) {
            setupUniverseBoundings(universe, universeController);
        }

        runLater(() -> canvas.requestFocus());
    }

    private void updateTimerText(int val) {
        int hours = val / 10000;
        int minutes = (val % 10000) / 100;
        int seconds = val % 100;
        String timeStr = hours > 0
                ? String.format("%02d:%02d:%02d", hours, minutes, seconds)
                : String.format("%02d:%02d", minutes, seconds);
        starryTimer.formText(timeStr, Font.font("Miracode", FontWeight.BOLD, 32));
    }

    @Override
    public void onHide() {
        super.onHide();
        if (!gameModel.isPaused()) {
            gameController.togglePause();
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        gameController.stopGameLoop();
    }
}