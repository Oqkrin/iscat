package uni.gaben.iscat.game.view;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import uni.gaben.iscat.AbstractIscatStackPane;
import uni.gaben.iscat.game.lib.abstracts.AbstractEntityView;
import uni.gaben.iscat.game.view.camera.CameraModel;
import uni.gaben.iscat.game.controller.GameController;
import uni.gaben.iscat.game.model.GameModel;
import uni.gaben.iscat.game.lib.interfaces.view.Drawable;
import uni.gaben.iscat.game.universe.UniverseController;
import uni.gaben.iscat.game.universe.UniverseModel;
import uni.gaben.iscat.game.universe.starfield.StarfieldView;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import uni.gaben.iscat.utils.components.StarryText;
import uni.gaben.iscat.utils.ThemeColors;
import uni.gaben.iscat.utils.design.CssHelper;
import uni.gaben.iscat.utils.design.ScalareAureo;
import uni.gaben.iscat.utils.design.TipografiaAurea;

import java.util.Objects;

import static javafx.application.Platform.runLater;

public class GameView extends AbstractIscatStackPane {

    private final GameModel gameModel;
    private final GameController gameController;
    private final StackPane root;
    private GameOverMenu gameOverMenu;

    private Canvas canvas;
    private StarfieldView starfieldView = new StarfieldView();
    private GameSpawnerToolbar spawnerToolbar;
    private GamePauseMenu pauseMenu;
    private Button debugButton;
    private boolean debugPanelVisible = false;
    private Canvas timerCanvas;
    private StarryText starryTimer;
    private Label levelLabel;

    public GameView(GameController gameController, GameModel gameModel) {
        super(new StackPane());
        this.gameModel = gameModel;
        this.gameController = gameController;
        this.root = getContentRoot();
        this.gameController.setContentRoot(this.root);

        initialize();
    }

    @Override
    protected void initNodes() {
        canvas = new Canvas();
        spawnerToolbar = new GameSpawnerToolbar(gameController);
        pauseMenu = new GamePauseMenu(gameController);
        debugButton = new Button("DEBUG");
        debugButton.setFocusTraversable(false);

        timerCanvas = new Canvas(300, 100);
        timerCanvas.setMouseTransparent(true);
        timerCanvas.setFocusTraversable(false);
        starryTimer = new StarryText(300, 100);

        levelLabel = new javafx.scene.control.Label("LEVEL 1");
        levelLabel.setFocusTraversable(false);
        levelLabel.setMouseTransparent(true);

        gameOverMenu = new GameOverMenu(gameController);
    }

    @Override
    protected void initStyles() {
        root.getStyleClass().add("game-view-container");

        getStylesheets().add(Objects.requireNonNull(GameView.class.getResource("/uni/gaben/iscat/styles/game.css"))
                .toExternalForm());
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

            pauseMenu.visibleProperty().bind(gameModel.pausedProperty().and(gameModel.gameOverProperty().not()));
            pauseMenu.managedProperty().bind(pauseMenu.visibleProperty());

            gameModel.pausedProperty().addListener((obs, wasPaused, isPausedNow) -> {
                if (!isPausedNow) {
                    runLater(() -> canvas.requestFocus());
                }
            });

            // GESTIONE DEL DEBUG PULITA (Senza bind problematici)
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

            // Configurazione iniziale basata sul controller
            boolean initialDebug = gameController.isDebugModeOn();
            debugButton.setVisible(initialDebug);
            debugButton.setManaged(initialDebug);
            spawnerToolbar.setVisible(false);
            spawnerToolbar.setManaged(false);

            // Connect UI timer
            gameModel.timerProperty().addListener((obs, oldVal, newVal) -> {
                updateTimerText(newVal.intValue());
            });
            runLater(() -> updateTimerText(gameModel.getTimer()));

            // Level Label
            var player = universe.getPlayer();
            if (player != null) {
                levelLabel.textProperty().bind(
                        Bindings.concat("LEVEL ", player.levelProperty().asString())
                );
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
        gameController.setDrawCall(this::renderFrame);
        gameController.getInputManager().attachToScene(this.getScene());
        gameController.startGameLoop();

        runLater(() -> canvas.requestFocus());
    }

    private void renderFrame() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setImageSmoothing(false);

        double w = canvas.getWidth();
        double h = canvas.getHeight();

        gc.clearRect(0, 0, w, h);
        ThemeColors.ensureLoaded();
        gc.setFill(ThemeColors.parsedColors.get("bg-primary"));
        gc.fillRect(0, 0, w, h);

        UniverseController universeController = gameController.getUniverseController();
        UniverseModel universe = universeController.getUniverseModel();

        if (universe == null) return;

        CameraModel cameraModel = gameController.getCameraModel();

        starfieldView.setCameraX(cameraModel.getX());
        starfieldView.setCameraY(cameraModel.getY());
        starfieldView.draw(universe.getStarfieldModel(), gc);

        gc.save();
        gc.translate(-cameraModel.getViewportLeftX(), -cameraModel.getViewportTopY());

        boolean renderCollisionBoxes = debugPanelVisible && gameController.isDebugModeOn();

        for (var entity : universe.getEntities()) {
            Drawable renderer = ViewRegistry.getInstance().getRenderer(entity.getClass());
            if (renderer != null) {
                renderer.draw(entity, gc);

                if (renderCollisionBoxes && renderer instanceof AbstractEntityView entityView) {
                    gc.save();
                    entityView.setPos(entity);
                    entityView.drawDebugCollision(entity, gc);
                    gc.restore();
                }
            }
        }
        gc.restore();

        // Draw HUD timer
        GraphicsContext timerGc = timerCanvas.getGraphicsContext2D();
        timerGc.clearRect(0, 0, timerCanvas.getWidth(), timerCanvas.getHeight());
        starryTimer.updateAndDraw(timerGc);

        drawFps(gc, w);
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

    private final double[] fpsHistory = new double[30];
    private int fpsIdx = 0;

    private void drawFps(GraphicsContext gc, double w) {
        if (gameController.isFpsOn()) {
            double fps = 1.0 / gameModel.getDt();
            fpsHistory[fpsIdx] = fps;
            fpsIdx = (fpsIdx + 1) % fpsHistory.length;

            double avg = 0;
            for (double f : fpsHistory) avg += f;
            avg /= fpsHistory.length;

            gc.setFill(avg >= 60 ? ThemeColors.getColorSuccess() : avg >= 30 ? ThemeColors.getColorWarning() : ThemeColors.getColorError());
            gc.setLineWidth(TipografiaAurea.LABEL[TipografiaAurea.SMALL]);
            gc.fillText(String.format("FPS: %.0f", avg), w - 80, 50);
        }
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