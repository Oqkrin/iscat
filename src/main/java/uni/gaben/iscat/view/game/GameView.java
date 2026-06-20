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
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import uni.gaben.iscat.controller.game.GameController;
import uni.gaben.iscat.controller.game.GameOverMenuController;
import uni.gaben.iscat.model.game.GameState;
import uni.gaben.iscat.model.game.GameModel;
import uni.gaben.iscat.controller.SettingsMenuController;
import uni.gaben.iscat.controller.game.GamePauseMenuController;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.camera.CameraModel;
import uni.gaben.iscat.universe.rendering.UniverseRenderer;
import uni.gaben.iscat.utils.SessionManager;
import uni.gaben.iscat.utils.design.CssHelper;
import uni.gaben.iscat.utils.design.ScalareAureo;
import uni.gaben.iscat.utils.theme.ThemeManager;
import uni.gaben.iscat.view.components.AbstractIscatStackPane;
import uni.gaben.iscat.view.game.debug.DebugToolBar;

import java.util.Objects;

import static javafx.application.Platform.runLater;

public class GameView extends AbstractIscatStackPane {

    private final GameModel      gameModel;
    private final GameController gameController;

    private Canvas           canvas;
    private UniverseRenderer universeRenderer;

    private GameHudBar hudBar;
    private DebugToolBar debugToolBar;
    private StackPane               pauseMenu;
    private GamePauseMenuController gamePauseMenuController;
    private StackPane               gameOverMenu;

    private Label levelLabel;

    private Label godModeLabel;
    private Label ghostModeLabel;
    private Label debugWarningLabel; // <--- Nuova label per l'avviso di salvataggio disabilitato
    private VBox cheatLabelsContainer;

    private HBox   debugButtonsContainer;
    private Button debugButton;
    private Button toggleWave;

    private boolean debugPanelVisible = false;

    public GameView(GameController gameController) {
        super(new StackPane());
        this.gameController = gameController;
        this.gameModel      = gameController.getGameModel();
        initialize();
    }

    @Override
    protected void initNodes() {
        canvas = new Canvas();

        universeRenderer = new UniverseRenderer(canvas, gameController);

        hudBar         = new GameHudBar(gameController);
        debugToolBar   = new DebugToolBar(gameController);
        pauseMenu      = loadPauseMenu();
        gameOverMenu   = loadGameOverMenu();

        levelLabel = new Label("LEVEL 1");
        levelLabel.setFocusTraversable(false);
        levelLabel.setMouseTransparent(true);

        godModeLabel = new Label("GOD MODE ACTIVE");
        godModeLabel.setFocusTraversable(false);
        godModeLabel.setMouseTransparent(true);

        ghostModeLabel = new Label("GHOST MODE ACTIVE");
        ghostModeLabel.setFocusTraversable(false);
        ghostModeLabel.setMouseTransparent(true);

        // Inizializzazione della label di avviso per il debug
        debugWarningLabel = new Label("DEBUG MODE ACTIVATED - SCORE WILL NOT BE SAVED");
        debugWarningLabel.setFocusTraversable(false);
        debugWarningLabel.setMouseTransparent(true);

        // Aggiunta al contenitore dei cheat visivi a schermo
        cheatLabelsContainer = new VBox(12, debugWarningLabel, godModeLabel, ghostModeLabel);
        cheatLabelsContainer.setFocusTraversable(false);
        cheatLabelsContainer.setMouseTransparent(true);

        debugButton = new Button("DEBUG");
        debugButton.setFocusTraversable(false);

        toggleWave = new Button("pause wave");
        toggleWave.setFocusTraversable(false);

        debugButtonsContainer = new HBox(10, debugButton, toggleWave);
        debugButtonsContainer.setFocusTraversable(false);
        debugButtonsContainer.setPickOnBounds(false);
        debugToolBar.setPickOnBounds(false);
    }

    @Override
    protected void initStyles() {
        getViewRootPointer().getStyleClass().add("game-view-container");
        getStylesheets().add(Objects.requireNonNull(
                        GameView.class.getResource("/uni/gaben/iscat/styles/screens/game/game.css"))
                .toExternalForm());

        CssHelper.stilePulsanteMenu(debugButton);
        CssHelper.testoPrimario(debugButton);
        CssHelper.stilePulsanteMenu(toggleWave);
        CssHelper.testoPrimario(toggleWave);

        levelLabel.setFont(Font.font("Miracode", FontWeight.BOLD, 24));
        levelLabel.setTextFill(ThemeManager.getInstance().getColorSuccess());
        levelLabel.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 5, 0, 0, 0);");

        godModeLabel.setFont(Font.font("Miracode", FontWeight.BOLD, 20));
        godModeLabel.setStyle("-fx-text-fill: #f1c40f; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.9), 6, 0, 0, 0);");

        ghostModeLabel.setFont(Font.font("Miracode", FontWeight.BOLD, 20));
        ghostModeLabel.setStyle("-fx-text-fill: #9b59b6; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.9), 6, 0, 0, 0);");

        // Stile aggressivo di errore/alert per la scritta sul salvataggio bloccato
        debugWarningLabel.setFont(Font.font("Miracode", FontWeight.BOLD, 18));
        debugWarningLabel.setStyle("-fx-text-fill: #e74c3c; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.9), 6, 0, 0, 0);");
    }

    @Override
    protected void initLayout() {
        getViewRootPointer().getChildren().addAll(
                canvas,
                hudBar,
                levelLabel,
                cheatLabelsContainer,
                debugToolBar,
                debugButtonsContainer,
                pauseMenu,
                gameOverMenu
        );

        StackPane.setAlignment(hudBar,                Pos.TOP_CENTER);
        StackPane.setAlignment(debugToolBar,          Pos.BOTTOM_CENTER);
        StackPane.setAlignment(debugButtonsContainer, Pos.TOP_LEFT);
        StackPane.setAlignment(levelLabel,            Pos.BOTTOM_RIGHT);

        cheatLabelsContainer.setAlignment(Pos.BOTTOM_LEFT);
        StackPane.setAlignment(cheatLabelsContainer,  Pos.BOTTOM_LEFT);

        cheatLabelsContainer.setMaxSize(VBox.USE_PREF_SIZE, VBox.USE_PREF_SIZE);

        StackPane.setMargin(hudBar,                   new Insets(20, 0, 0, 0));
        StackPane.setMargin(debugButtonsContainer,    new Insets(50, 0, 0, 50));
        StackPane.setMargin(levelLabel,               new Insets(0, 50, 50, 0));

        StackPane.setMargin(cheatLabelsContainer,     new Insets(0, 0, 50, 50));
    }

    @Override
    protected void initBindings() {
        canvas.widthProperty().bind(getViewRootPointer().widthProperty());
        canvas.heightProperty().bind(getViewRootPointer().heightProperty());

        hudBar.maxWidthProperty().bind(
                getViewRootPointer().widthProperty().multiply(ScalareAureo.IPHI_D));

        debugToolBar.maxHeightProperty().bind(getViewRootPointer().heightProperty().multiply(0.35));
        debugToolBar.maxWidthProperty().bind(getViewRootPointer().widthProperty().multiply(ScalareAureo.IPHI_D));

        CameraModel camera = gameController.getCameraModel();
        camera.screenWidthProperty().bind(canvas.widthProperty());
        camera.screenHeightProperty().bind(canvas.heightProperty());

        canvas.widthProperty().addListener((obs, oldW, newW) -> onCanvasSizeChanged());
        canvas.heightProperty().addListener((obs, oldH, newH) -> onCanvasSizeChanged());

        godModeLabel.visibleProperty().bind(gameController.godModeProperty());
        godModeLabel.managedProperty().bind(godModeLabel.visibleProperty());

        ghostModeLabel.visibleProperty().bind(gameController.ghostModeProperty());
        ghostModeLabel.managedProperty().bind(ghostModeLabel.visibleProperty());

        // Binding dinamico per mostrare l'avviso se il debug è stato toccato/usato in sessione
        debugWarningLabel.setVisible(gameController.isDebugUsedInThisSession());
        debugWarningLabel.setManaged(debugWarningLabel.isVisible());

        gameOverMenu.visibleProperty().bind(
                gameModel.gameStateProperty().isEqualTo(GameState.GAME_OVER)
                        .or(gameModel.gameStateProperty().isEqualTo(GameState.WIN))
        );
        gameOverMenu.managedProperty().bind(gameOverMenu.visibleProperty());

        pauseMenu.visibleProperty().bind(
                gameModel.gameStateProperty().isEqualTo(GameState.IN_PAUSE));
        pauseMenu.managedProperty().bind(pauseMenu.visibleProperty());

        gameModel.gameStateProperty().addListener((obs, oldState, newState) -> {
            if (newState == GameState.IN_PAUSE && gamePauseMenuController != null) {
                gamePauseMenuController.syncVisualState();
            }
            if (newState == GameState.PLAYING) {
                runLater(() -> canvas.requestFocus());
            }
        });

        gameController.debugModeProperty().addListener((obs, oldV, debugOn) -> {
            debugButtonsContainer.setVisible(debugOn);
            debugButtonsContainer.setManaged(debugOn);

            // Quando si attiva il debug (debugOn == true), aggiorna la visibilità dell'avviso di salvataggio
            if (debugOn) {
                debugWarningLabel.setVisible(true);
                debugWarningLabel.setManaged(true);
            }

            if (!debugOn) {
                debugToolBar.setVisible(false);
                debugToolBar.setManaged(false);
                debugPanelVisible = false;
                debugButton.setText("DEBUG");
            }
        });

        boolean initDebug = gameController.isDebugModeOn();
        debugButtonsContainer.setVisible(initDebug);
        debugButtonsContainer.setManaged(initDebug);
        debugToolBar.setVisible(false);
        debugToolBar.setManaged(false);

        gameModel.timerProperty().addListener((obs, oldV, newV) -> hudBar.updateTimer(newV.intValue()));
        runLater(() -> hudBar.updateTimer(gameModel.getTimer()));
    }

    @Override
    protected void initEventHandlers() {
        gameController.getInputManager().attachToCanvas(canvas);

        this.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            KeyCode code = e.getCode();

            if (code == KeyCode.ESCAPE && gameModel.getGameState() != GameState.IN_SETTINGS) {
                transitionTo(gameModel.getGameState().onEscape());
                e.consume();
                return;
            }

            if (gameModel.getGameState().isPaused()) return;

            if (code == KeyCode.PLUS || code == KeyCode.ADD) {
                gameController.getCameraModel().addZoom(0.1);
                e.consume();
            } else if (code == KeyCode.MINUS || code == KeyCode.SUBTRACT) {
                gameController.getCameraModel().addZoom(-0.1);
                e.consume();
            }
        });

        debugButton.setOnAction(ev -> {
            if (gameController.isDebugModeOn()) {
                debugPanelVisible = !debugPanelVisible;
                debugToolBar.setVisible(debugPanelVisible);
                debugToolBar.setManaged(debugPanelVisible);
                debugButton.setText(debugPanelVisible ? "HIDE DEBUG" : "DEBUG");

                // Forza la ricomparsa visiva immediata anche al click del pannello
                debugWarningLabel.setVisible(true);
                debugWarningLabel.setManaged(true);

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
    }

    @Override
    public void onShow() {
        super.onShow();
        transitionTo(GameState.PLAYING);

        var currentSettings = SessionManager.getInstance().getCurrentSettings();
        if (currentSettings != null) {
            gameController.setShowFps(currentSettings.getShowFps() == 1);
            gameController.setShowDebugMode(currentSettings.getDebugMode() == 1);
        }

        // Forza la sincronizzazione corretta all'apertura del livello
        debugWarningLabel.setVisible(gameController.isDebugUsedInThisSession());
        debugWarningLabel.setManaged(debugWarningLabel.isVisible());

        gameController.setDrawCall(
                () -> universeRenderer.renderFrame(debugPanelVisible));

        gameController.getInputManager().attachToScene(this.getScene());

        gameController.setOnUniverseResetCallback(this::bindToCurrentUniverse);

        runLater(() -> {
            onCanvasSizeChanged();
            bindToCurrentUniverse();
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

    public void transitionTo(GameState next) {
        if (gameModel.gameStateProperty().get() != next)
            gameModel.setGameState(next);
    }

    private void onCanvasSizeChanged() {
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        if (w <= 0 || h <= 0) return;
        universeRenderer.updateViewport(w, h);
    }

    private void bindToCurrentUniverse() {
        levelLabel.textProperty().unbind();
        UniverseModel universe = gameController.getUniverseModel();
        if (universe == null) return;

        var player = universe.getPlayer();
        if (player != null) {
            levelLabel.textProperty().bind(
                    Bindings.concat("LEVEL ", player.levelProperty().asString()));
        }

        if (hudBar != null && gameController.getUniverseWaveController() != null) {
            hudBar.rebindToWaveController(gameController.getUniverseWaveController());
        }
    }

    public void openSettings() {
        transitionTo(GameState.IN_SETTINGS);
        final StackPane[] wrapper = new StackPane[1];
        wrapper[0] = loadFxml("/uni/gaben/iscat/fxml/SettingsMenu.fxml",
                (SettingsMenuController c) -> {
                    c.initGameContext(gameController);
                    c.setCustomBackAction(() -> closeSettings(wrapper[0]));
                    c.syncAllProperties();
                });
        wrapper[0].getStyleClass().add("game-pause-overlay");
        getViewRootPointer().getChildren().add(wrapper[0]);
    }

    private void closeSettings(StackPane settingsView) {
        getViewRootPointer().getChildren().remove(settingsView);
        transitionTo(GameState.IN_PAUSE);
    }

    private StackPane loadPauseMenu() {
        return loadFxml("/uni/gaben/iscat/fxml/GamePauseMenu.fxml",
                (GamePauseMenuController c) -> {
                    this.gamePauseMenuController = c;
                    c.initData(gameController, this);
                });
    }

    private StackPane loadGameOverMenu() {
        return loadFxml("/uni/gaben/iscat/fxml/GameOverMenu.fxml",
                (GameOverMenuController c) -> c.initData(gameController, this));
    }
}