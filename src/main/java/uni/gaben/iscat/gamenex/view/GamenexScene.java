package uni.gaben.iscat.gamenex.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import uni.gaben.iscat.AbstractIscatScene;
import uni.gaben.iscat.gamenex.lib.abstracts.AbstractEntityView;
import uni.gaben.iscat.gamenex.view.camera.CameraModel;
import uni.gaben.iscat.gamenex.controller.GamenexController;
import uni.gaben.iscat.gamenex.model.GamenexModel;
import uni.gaben.iscat.gamenex.lib.interfaces.view.Drawable;
import uni.gaben.iscat.gamenex.universe.UniverseController;
import uni.gaben.iscat.gamenex.universe.UniverseModel;
import uni.gaben.iscat.gamenex.universe.starfield.StarfieldView;
import uni.gaben.iscat.utils.ThemeColors;
import uni.gaben.iscat.utils.design.CssHelper;
import uni.gaben.iscat.utils.design.TipografiaAurea;

import java.util.Objects;

import static javafx.application.Platform.runLater;
import static javafx.beans.binding.Bindings.when;

public class GamenexScene extends AbstractIscatScene {

    private GamenexModel gamenexModel;
    private GamenexController gamenexController;
    private StackPane root;
    private Canvas canvas;
    private final StarfieldView starfieldView = new StarfieldView();
    private GamenexSpawnerToolbar spawnerToolbar;
    private GamenexPauseMenu pauseMenu;
    private Button debugButton;
    private boolean debugPanelVisible = false;
    public GamenexScene(GamenexController gamenexController, GamenexModel gamenexModel) {
        super(new StackPane());
        this.gamenexModel = gamenexModel;
        this.gamenexController = gamenexController;
        this.root = getContentRoot();

        root.setStyle("-fx-background-color: transparent;");
        initialize();
    }

    @Override
    protected void initNodes() {
        canvas = new Canvas();
        spawnerToolbar = new GamenexSpawnerToolbar(gamenexController);
        pauseMenu = new GamenexPauseMenu(gamenexController);
        debugButton = new Button("DEBUG");
        debugButton.setFocusTraversable(false);
    }

    @Override
    protected void initStyles() {
        getStylesheets().add(Objects.requireNonNull(GamenexScene.class.getResource("/uni/gaben/iscat/styles/game.css"))
                .toExternalForm());
        CssHelper.stilePulsanteMenu(debugButton);
        CssHelper.testoPrimario(debugButton);
    }

    @Override
    protected void initLayout() {
        root.getChildren().addAll(canvas, spawnerToolbar, pauseMenu, debugButton);

        StackPane.setAlignment(spawnerToolbar, Pos.BOTTOM_CENTER);
        StackPane.setAlignment(debugButton, Pos.TOP_LEFT);
        StackPane.setMargin(debugButton, new Insets(50, 0, 0, 50));
    }

    @Override
    protected void initBindings() {
        canvas.widthProperty().bind(root.widthProperty());
        canvas.heightProperty().bind(root.heightProperty());

        CameraModel camera = gamenexController.getCameraModel();
        camera.screenWidthProperty().bind(canvas.widthProperty());
        camera.screenHeightProperty().bind(canvas.heightProperty());

        UniverseController universeController = gamenexController.getUniverseController();
        UniverseModel universe = universeController.getUniverseModel();
        if (universe != null) {
            universe.widthProperty().bind(canvas.widthProperty());
            universe.heightProperty().bind(canvas.heightProperty());

            universe.widthProperty().addListener((obs, oldV, newV) -> universeController.getStarfieldController()
                    .regenerate(universe.getStarfieldModel(), newV.doubleValue(), universe.getHeight()));
            universe.heightProperty().addListener((obs, oldV, newV) -> universeController.getStarfieldController()
                    .regenerate(universe.getStarfieldModel(), universe.getWidth(), newV.doubleValue()));

            starfieldView.wProperty().bind(canvas.widthProperty());
            starfieldView.hProperty().bind(canvas.heightProperty());

            pauseMenu.visibleProperty().bind(gamenexModel.pausedProperty());
            pauseMenu.managedProperty().bind(pauseMenu.visibleProperty());
        }
    }

    @Override
    protected void initEventHandlers() {
        gamenexController.getInputManager().attachToScene(this);
        gamenexController.getInputManager().attachToCanvas(canvas);

        this.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                gamenexController.togglePause();
                e.consume();
            }
        });

        // === DEBUG BUTTON FIX ===
        debugButton.setOnAction(_ -> {
            debugPanelVisible = !debugPanelVisible;

            spawnerToolbar.setVisible(debugPanelVisible);

            debugButton.setText(debugPanelVisible ? "HIDE DEBUG" : "DEBUG");
        });

        // Force initial state
        spawnerToolbar.setVisible(false);
        debugButton.setText("DEBUG");
    }

    @Override
    public void onShow() {
        super.onShow();
        gamenexController.setDrawCall(this::renderFrame);
        gamenexController.startGameLoop();
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

        UniverseController universeController = gamenexController.getUniverseController();
        UniverseModel universe = universeController.getUniverseModel();

        if (universe == null) return;

        CameraModel cameraModel = gamenexController.getCameraModel();

        // 2. Rendering di Sfondo (Parallasse dello Starfield)
        starfieldView.setCameraX(cameraModel.getX());
        starfieldView.setCameraY(cameraModel.getY());
        starfieldView.draw(universe.getStarfieldModel(), gc);

        gc.save();

        // Spostiamo la matrice del contesto grafico in base all'angolo in alto a sinistra della telecamera
        gc.translate(-cameraModel.getViewportLeftX(), -cameraModel.getViewportTopY());

        for (var entity : universe.getEntities()) {
            Drawable renderer = ViewRegistry.getInstance().getRenderer(entity.getClass());
            if (renderer != null) {
                // IL FIX DELLA DOPPIA ROTAZIONE: PlayerView.draw() deve invocare renderEntity(..., 0.0)
                renderer.draw(entity, gc);

                if (debugPanelVisible && renderer instanceof AbstractEntityView entityView) {
                    gc.save();
                    entityView.setPos(entity);
                    entityView.drawDebugCollision(entity, gc);
                    gc.restore();
                }
            }
        }
        gc.restore();

        // 3. UI Overlay
        drawFps(gc, w);
    }

    private double[] fpsHistory = new double[30];
    private int fpsIdx = 0;

    private void drawFps(GraphicsContext gc, double w) {
        if (gamenexController.isFpsOn()) {
            double fps = 1.0 / gamenexModel.getDt();
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
        gamenexController.setPaused(true);
    }

    @Override
    public void onUnload() {
        super.onUnload();
        gamenexController.stopGameLoop();
    }
}