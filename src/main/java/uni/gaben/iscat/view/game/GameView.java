package uni.gaben.iscat.view.game;

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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import uni.gaben.iscat.controller.game.GameController;
import uni.gaben.iscat.controller.game.GameOverMenuController;
import uni.gaben.iscat.model.game.GameState;
import uni.gaben.iscat.model.game.GameModel;
import uni.gaben.iscat.controller.OptionsMenuController;
import uni.gaben.iscat.controller.game.GamePauseMenuController;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.camera.CameraModel;
import uni.gaben.iscat.universe.rendering.StarfieldRenderer;
import uni.gaben.iscat.universe.rendering.UniverseRenderer;
import uni.gaben.iscat.utils.SessionManager;
import uni.gaben.iscat.utils.design.CssHelper;
import uni.gaben.iscat.utils.design.ScalareAureo;
import uni.gaben.iscat.utils.theme.ThemeManager;
import uni.gaben.iscat.view.components.AbstractIscatStackPane;
import uni.gaben.iscat.view.components.StarryText;

import java.util.Objects;

import static javafx.application.Platform.runLater;

public class GameView extends AbstractIscatStackPane {

    private final GameModel      gameModel;
    private final GameController gameController;
    private final StackPane      root;

    private Canvas           canvas;
    private Canvas           timerCanvas;
    private StarryText       starryTimer;
    private UniverseRenderer universeRenderer;
    private final StarfieldRenderer starfieldRenderer = new StarfieldRenderer();

    private GameSpawnerToolbar spawnerToolbar;
    private StackPane          pauseMenu;
    private GamePauseMenuController gamePauseMenuController;
    private StackPane          gameOverMenu;

    private HBox   debugButtonsContainer;
    private Button debugButton;
    private Button toggleWave;
    private Label  levelLabel;

    private boolean debugPanelVisible = false;

    public GameView(GameController gameController) {
        super(new StackPane());
        this.gameController = gameController;
        this.gameModel      = gameController.getGameModel();
        this.root           = getContentRoot();
        this.gameController.setContentRoot(this.root);
        initialize();
    }

    // -------------------------------------------------------------------------
    // AbstractIscatStackPane lifecycle
    // -------------------------------------------------------------------------

    @Override
    protected void initNodes() {
        canvas      = new Canvas();
        timerCanvas = new Canvas(300, 100);
        timerCanvas.setMouseTransparent(true);
        timerCanvas.setFocusTraversable(false);

        starryTimer    = new StarryText(300, 100);
        spawnerToolbar = new GameSpawnerToolbar(gameController);
        pauseMenu      = loadPauseMenu();
        gameOverMenu   = loadGameOverMenu();

        levelLabel = new Label("LEVEL 1");
        levelLabel.setFocusTraversable(false);
        levelLabel.setMouseTransparent(true);

        debugButton = new Button("DEBUG");
        debugButton.setFocusTraversable(false);

        toggleWave = new Button("pause wave");
        toggleWave.setFocusTraversable(false);

        debugButtonsContainer = new HBox(10, debugButton, toggleWave);
        debugButtonsContainer.setFocusTraversable(false);
        debugButtonsContainer.setPickOnBounds(false);
        spawnerToolbar.setPickOnBounds(false);

        universeRenderer = new UniverseRenderer(canvas, gameController, starfieldRenderer);
    }

    @Override
    protected void initStyles() {
        root.getStyleClass().add("game-view-container");
        getStylesheets().add(Objects.requireNonNull(
                GameView.class.getResource("/uni/gaben/iscat/styles/screens/game.css"))
                .toExternalForm());

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
                canvas, timerCanvas, levelLabel,
                spawnerToolbar, debugButtonsContainer,
                pauseMenu, gameOverMenu
        );

        StackPane.setAlignment(spawnerToolbar,        Pos.BOTTOM_CENTER);
        StackPane.setAlignment(debugButtonsContainer, Pos.TOP_LEFT);
        StackPane.setAlignment(timerCanvas,           Pos.TOP_CENTER);
        StackPane.setAlignment(levelLabel,            Pos.BOTTOM_RIGHT);

        StackPane.setMargin(debugButtonsContainer, new Insets(50, 0, 0, 50));
        StackPane.setMargin(timerCanvas,           new Insets(50, 0, 0, 0));
        StackPane.setMargin(levelLabel,            new Insets(0, 50, 50, 0));
    }

    @Override
    protected void initBindings() {
        canvas.widthProperty().bind(root.widthProperty());
        canvas.heightProperty().bind(root.heightProperty());

        spawnerToolbar.maxHeightProperty().bind(root.heightProperty().divide(5));
        spawnerToolbar.maxWidthProperty().bind(root.widthProperty().multiply(ScalareAureo.IPHI_D));

        // Camera screen dimensions always track the canvas — this binding is permanent
        CameraModel camera = gameController.getCameraModel();
        camera.screenWidthProperty().bind(canvas.widthProperty());
        camera.screenHeightProperty().bind(canvas.heightProperty());

        // Notify the universe when the canvas is resized so starfields and spawn
        // centres remain accurate
        canvas.widthProperty().addListener((obs, oldW, newW) -> onCanvasSizeChanged());
        canvas.heightProperty().addListener((obs, oldH, newH) -> onCanvasSizeChanged());

        gameOverMenu.visibleProperty().bind(
                gameModel.gameStateProperty().isEqualTo(GameState.GAME_OVER));
        gameOverMenu.managedProperty().bind(gameOverMenu.visibleProperty());

        pauseMenu.visibleProperty().bind(
                gameModel.gameStateProperty().isEqualTo(GameState.IN_PAUSE));
        pauseMenu.managedProperty().bind(pauseMenu.visibleProperty());

        gameModel.gameStateProperty().addListener((obs, oldState, newState) -> {
            syncGameModelFromState(newState);

            if (newState == GameState.IN_PAUSE && gamePauseMenuController != null) {
                gamePauseMenuController.syncVisualState();
            }

            if (newState == GameState.PLAYING) runLater(() -> canvas.requestFocus());
        });

        gameModel.gameOverProperty().addListener((obs, was, isOver) -> {
            if (isOver) transitionTo(GameState.GAME_OVER);
        });

        gameController.debugModeProperty().addListener((obs, oldV, debugOn) -> {
            debugButtonsContainer.setVisible(debugOn);
            debugButtonsContainer.setManaged(debugOn);
            if (!debugOn) {
                spawnerToolbar.setVisible(false);
                spawnerToolbar.setManaged(false);
                debugPanelVisible = false;
                debugButton.setText("DEBUG");
            }
        });

        boolean initDebug = gameController.isDebugModeOn();
        debugButtonsContainer.setVisible(initDebug);
        debugButtonsContainer.setManaged(initDebug);
        spawnerToolbar.setVisible(false);
        spawnerToolbar.setManaged(false);

        gameModel.timerProperty().addListener((obs, oldV, newV) -> updateTimerText(newV.intValue()));
        runLater(() -> updateTimerText(gameModel.getTimer()));
    }

    @Override
    protected void initEventHandlers() {
        gameController.getInputManager().attachToCanvas(canvas);

        this.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ESCAPE
                    && gameModel.getGameState() != GameState.IN_OPTIONS) {
                transitionTo(gameModel.getGameState().onEscape());
                e.consume();
            }
        });

        debugButton.setOnAction(ev -> {
            if (gameController.isDebugModeOn()) {
                debugPanelVisible = !debugPanelVisible;
                spawnerToolbar.setVisible(debugPanelVisible);
                spawnerToolbar.setManaged(debugPanelVisible);
                debugButton.setText(debugPanelVisible ? "HIDE DEBUG" : "DEBUG");
                canvas.requestFocus();
            }
        });

        toggleWave.setOnAction(ev -> {
            if (gameController.isDebugModeOn()) {
                gameModel.waveActiveProperty().set(!gameModel.isWaveActive());
                toggleWave.setText(gameModel.isWaveActive() ? "pause wave" : "restart wave");
                canvas.requestFocus();
            }
        });

        this.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (gameModel.isPaused()) return;
            if (e.getCode() == KeyCode.PLUS || e.getCode() == KeyCode.ADD) {
                gameController.getCameraModel().addZoom(0.1);
                e.consume();
            } else if (e.getCode() == KeyCode.MINUS || e.getCode() == KeyCode.SUBTRACT) {
                gameController.getCameraModel().addZoom(-0.1);
                e.consume();
            }
        });
    }

    // -------------------------------------------------------------------------
    // Show / hide / unload
    // -------------------------------------------------------------------------

    @Override
    public void onShow() {
        super.onShow();
        transitionTo(GameState.PLAYING);

        var currentSettings = SessionManager.getInstance().getCurrentSettings();
        if (currentSettings != null) {
            gameController.setShowFps(currentSettings.getShowFps() == 1);
            gameController.setShowDebugMode(currentSettings.getDebugMode() == 1);
        }

        gameController.setDrawCall(
                () -> universeRenderer.renderFrame(timerCanvas, starryTimer, debugPanelVisible));
        gameController.getInputManager().attachToScene(this.getScene());

        // After reset the view needs to rebind the level label to the new player
        gameController.setOnUniverseResetCallback(this::bindToCurrentUniverse);

        // Defer the first dimension seeding + loop start to the next layout pulse.
        // At this point the canvas is just being added to the scene — its width/height
        // are still 0. We need one layout pass to complete so the canvas gets its real
        // size bound from the root StackPane before we start the game loop or generate
        // the starfield.
        runLater(() -> {
            System.out.println("=== GAME INIT DEBUG ===");
            System.out.println("Canvas: " + canvas.getWidth() + " x " + canvas.getHeight());
            System.out.println("Universe scale: " + uni.gaben.iscat.universe.UU.getUniverseScale());
            
            onCanvasSizeChanged();      // seeds universe dimensions + generates starfield
            
            UniverseModel u = gameController.getUniverseModel();
            System.out.println("Universe: " + u.getWidth() + " x " + u.getHeight());
            System.out.println("Starfield stars: " + u.getStarfieldModel().getStars().size());
            System.out.println("Player pos (m): " + u.getPlayer().getTransform().getTranslationX() 
                + ", " + u.getPlayer().getTransform().getTranslationY());
            System.out.println("Player pos (px): " + uni.gaben.iscat.universe.UU.mToPx(u.getPlayer().getTransform().getTranslationX()) 
                + ", " + uni.gaben.iscat.universe.UU.mToPx(u.getPlayer().getTransform().getTranslationY()));
            System.out.println("Camera: " + gameController.getCameraModel().getX() + ", " + gameController.getCameraModel().getY());
            System.out.println("======================");
            
            bindToCurrentUniverse();    // binds level label to the (now-ready) player
            gameController.startGameLoop();
            canvas.requestFocus();
        });
    }

    @Override
    public void onHide() {
        super.onHide();
        if (gameModel.getGameState() == GameState.PLAYING)
            transitionTo(GameState.IN_PAUSE);
    }

    @Override
    public void onUnload() {
        super.onUnload();
        gameController.stopGameLoop();
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /** Single entry-point for state transitions. */
    public void transitionTo(GameState next) {
        if (gameModel.gameStateProperty().get() != next)
            gameModel.setGameState(next);
    }

    private void syncGameModelFromState(GameState state) {
        gameModel.setPaused(state.isPaused());
    }

    /**
     * Called when the canvas changes size. Pushes the current dimensions into
     * the active universe so spawn-centres and starfield wrapping stay correct.
     */
    private void onCanvasSizeChanged() {
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        if (w <= 0 || h <= 0) return;
        gameController.getUniverseModel().setDimensions(w, h);
        gameController.getUniverseModel().getStarfieldModel().generate(w, h);
    }

    /**
     * Re-binds UI elements that reference the current universe/player.
     * Called once on first show (deferred via runLater) and again after every
     * {@code resetUniverse()} callback (already on the FX thread).
     */
    private void bindToCurrentUniverse() {
        levelLabel.textProperty().unbind();
        UniverseModel universe = gameController.getUniverseModel();
        if (universe == null) return;
        var player = universe.getPlayer();
        if (player != null) {
            levelLabel.textProperty().bind(
                    Bindings.concat("LEVEL ", player.levelProperty().asString()));
        }
    }

    public void openOptions() {
        transitionTo(GameState.IN_OPTIONS);
        final StackPane[] wrapper = new StackPane[1];
        wrapper[0] = loadFxml("/uni/gaben/iscat/fxml/options/options_menu.fxml",
                (OptionsMenuController c) -> {
                    c.initGameContext(gameController);
                    c.setCustomBackAction(() -> closeOptions(wrapper[0]));
                    c.syncAllProperties();
                });
        wrapper[0].getStyleClass().add("game-pause-overlay");
        root.getChildren().add(wrapper[0]);
    }

    private void closeOptions(StackPane optionsView) {
        root.getChildren().remove(optionsView);
        transitionTo(GameState.IN_PAUSE);
    }

    private StackPane loadPauseMenu() {
        return loadFxml("/uni/gaben/iscat/fxml/pause-menu.fxml",
                (GamePauseMenuController c) -> {
                    this.gamePauseMenuController = c;
                    c.initData(gameController, this);
                });
    }

    private StackPane loadGameOverMenu() {
        return loadFxml("/uni/gaben/iscat/fxml/game-over-menu.fxml",
                (GameOverMenuController c) -> c.initData(gameController, this));
    }

    private void updateTimerText(int val) {
        int hours   = val / 10000;
        int minutes = (val % 10000) / 100;
        int seconds = val % 100;
        String time = hours > 0
                ? String.format("%02d:%02d:%02d", hours, minutes, seconds)
                : String.format("%02d:%02d", minutes, seconds);
        starryTimer.formText(time, Font.font("Miracode", FontWeight.BOLD, 32));
    }
}
