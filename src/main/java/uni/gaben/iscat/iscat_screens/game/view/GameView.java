package uni.gaben.iscat.iscat_screens.game.view;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import uni.gaben.iscat.iscat_m_view_c.AbstractIscatStackPane;
import uni.gaben.iscat.iscat_game.camera.CameraModel;
import uni.gaben.iscat.iscat_game.rendering.UniverseRenderer;
import uni.gaben.iscat.iscat_screens.game.controller.GameController;
import uni.gaben.iscat.iscat_screens.game.model.GameModel;
import uni.gaben.iscat.iscat_game.universe.UniverseController;
import uni.gaben.iscat.iscat_game.universe.UniverseModel;
import uni.gaben.iscat.iscat_game.universe.starfield.StarfieldView;
import uni.gaben.iscat.iscat_m_view_c.StarryText;
import uni.gaben.iscat.utils.ThemeColors;
import uni.gaben.iscat.utils.design.CssHelper;
import uni.gaben.iscat.utils.design.ScalareAureo;

import java.util.Objects;
import static javafx.application.Platform.runLater;

public class GameView extends AbstractIscatStackPane {

    private final GameModel gameModel;
    private final GameController gameController;
    private final StackPane root;

    private Canvas canvas;
    private final StarfieldView starfieldView = new StarfieldView();
    private UniverseRenderer universeRenderer;

    private GameSpawnerToolbar spawnerToolbar;
    private GamePauseMenu pauseMenu;
    private GameOverMenu gameOverMenu;
    private Button debugButton;
    private boolean debugPanelVisible = false;

    private Canvas timerCanvas;
    private StarryText starryTimer;
    private Label levelLabel;

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
        pauseMenu = new GamePauseMenu(gameController);
        gameOverMenu = new GameOverMenu(gameController);

        debugButton = new Button("DEBUG");
        debugButton.setFocusTraversable(false);

        timerCanvas = new Canvas(300, 100);
        timerCanvas.setMouseTransparent(true);
        timerCanvas.setFocusTraversable(false);
        starryTimer = new StarryText(300, 100);

        levelLabel = new Label("LEVEL 1");
        levelLabel.setFocusTraversable(false);
        levelLabel.setMouseTransparent(true);

        // Instantiate renderer by injecting its dependencies cleanly
        universeRenderer = new UniverseRenderer(canvas, gameController, starfieldView);
    }

    @Override
    protected void initStyles() {
        root.getStyleClass().add("game-view-container");
        getStylesheets().add(Objects.requireNonNull(GameView.class.getResource("/uni/gaben/iscat/styles/game.css")).toExternalForm());

        CssHelper.stilePulsanteMenu(debugButton);
        CssHelper.testoPrimario(debugButton);

        levelLabel.setFont(Font.font("Miracode", FontWeight.BOLD, 24));
        levelLabel.setTextFill(ThemeColors.getColorSuccess());
        levelLabel.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 5, 0, 0, 0);");
    }

    @Override
    protected void initLayout() {
        root.getChildren().addAll(canvas, timerCanvas, spawnerToolbar, pauseMenu, gameOverMenu, debugButton, levelLabel);

        StackPane.setAlignment(spawnerToolbar, Pos.BOTTOM_CENTER);
        StackPane.setAlignment(debugButton, Pos.TOP_LEFT);
        StackPane.setMargin(debugButton, new Insets(50, 0, 0, 50));

        StackPane.setAlignment(timerCanvas, Pos.TOP_CENTER);
        StackPane.setMargin(timerCanvas, new Insets(50, 0, 0, 0));

        StackPane.setAlignment(levelLabel, Pos.BOTTOM_RIGHT);
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

        UniverseController universeController = gameController.getUniverseController();
        UniverseModel universe = universeController.getUniverseModel();

        gameOverMenu.visibleProperty().bind(gameModel.gameOverProperty());
        gameOverMenu.managedProperty().bind(gameOverMenu.visibleProperty());
        pauseMenu.visibleProperty().bind(gameModel.pausedProperty().and(gameModel.gameOverProperty().not()));

        if (universe != null) {
            universe.widthProperty().bind(canvas.widthProperty());
            universe.heightProperty().bind(canvas.heightProperty());

            universe.widthProperty().addListener((obs, oldV, newV) -> universeController.getStarfieldController()
                    .regenerate(universe.getStarfieldModel(), newV.doubleValue(), universe.getHeight()));
            universe.heightProperty().addListener((obs, oldV, newV) -> universeController.getStarfieldController()
                    .regenerate(universe.getStarfieldModel(), universe.getWidth(), newV.doubleValue()));

            starfieldView.wProperty().bind(canvas.widthProperty());
            starfieldView.hProperty().bind(canvas.heightProperty());

            pauseMenu.managedProperty().bind(pauseMenu.visibleProperty());

            gameModel.pausedProperty().addListener((obs, wasPaused, isPausedNow) -> {
                if (!isPausedNow) {
                    runLater(() -> canvas.requestFocus());
                }
            });

            gameController.debugModeProperty().addListener((obs, oldV, isDebugActive) -> {
                debugButton.setVisible(isDebugActive);
                debugButton.setManaged(isDebugActive);

                if (!isDebugActive) {
                    spawnerToolbar.setVisible(false);
                    spawnerToolbar.setManaged(false);
                    debugPanelVisible = false;
                    debugButton.setText("DEBUG");
                }
            });

            boolean initialDebug = gameController.isDebugModeOn();
            debugButton.setVisible(initialDebug);
            debugButton.setManaged(initialDebug);
            spawnerToolbar.setVisible(false);
            spawnerToolbar.setManaged(false);

            gameModel.timerProperty().addListener((obs, oldVal, newVal) -> updateTimerText(newVal.intValue()));
            runLater(() -> updateTimerText(gameModel.getTimer()));

            var player = universe.getPlayer();
            if (player != null) {
                levelLabel.textProperty().bind(Bindings.concat("LEVEL ", player.levelProperty().asString()));
            }
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
            }
        });
    }

    @Override
    public void onShow() {
        super.onShow();
        gameModel.setPaused(false);

        // Lambda matches the functional callback interface requirement perfectly
        gameController.setDrawCall(() -> universeRenderer.renderFrame(timerCanvas, starryTimer, debugPanelVisible));

        gameController.getInputManager().attachToScene(this.getScene());
        gameController.startGameLoop();

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