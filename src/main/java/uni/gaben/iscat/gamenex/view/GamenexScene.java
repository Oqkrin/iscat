package uni.gaben.iscat.gamenex.view;

import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import uni.gaben.iscat.IscatSceneAbstract;
import uni.gaben.iscat.gamenex.universe.iscat_mob.IscatMobModel;
import uni.gaben.iscat.gamenex.universe.iscat_mob.IscatMobView;
import uni.gaben.iscat.gamenex.universe.player.PlayerView;
import uni.gaben.iscat.gamenex.view.camera.CameraModel;
import uni.gaben.iscat.gamenex.controller.GamenexController;
import uni.gaben.iscat.gamenex.model.GamenexModel;
import uni.gaben.iscat.gamenex.universe.player.PlayerModel;
import uni.gaben.iscat.gamenex.lib.interfaces.view.Drawable;
import uni.gaben.iscat.gamenex.universe.asteroid.AsteroidModel;
import uni.gaben.iscat.gamenex.universe.asteroid.AsteroidView;
import uni.gaben.iscat.gamenex.universe.UniverseController;
import uni.gaben.iscat.gamenex.universe.UniverseModel;
import uni.gaben.iscat.gamenex.universe.starfield.StarfieldView;
import uni.gaben.iscat.utils.design.TipografiaAurea;

import java.util.Objects;

/**
 * Scena principale di Gamenex (View).
 * Gestisce il rendering su Canvas, l'interfaccia utente (UI) e
 * il coordinamento tra il modello fisico e la visualizzazione.
 */
public class GamenexScene extends IscatSceneAbstract {

    private GamenexModel gamenexModel;
    private GamenexController gamenexController;
    private StackPane root;
    private Canvas canvas;
    private final StarfieldView starfieldView = new StarfieldView();
    private GamenexSpawnerToolbar spawnerToolbar;
    private GamenexPauseMenu pauseMenu;

    public GamenexScene(GamenexController gamenexController, GamenexModel gamenexModel) {
        super(new StackPane());
        this.gamenexModel = gamenexModel;
        this.gamenexController = gamenexController;
        this.root = getContentRoot();

        // Make root transparent so stars show through
        root.setStyle("-fx-background-color: transparent;");

        // Register default renderers in the View layer
        ViewRegistry registry = ViewRegistry.getInstance();
        registry.register(PlayerModel.class, new PlayerView());
        registry.register(AsteroidModel.class, new AsteroidView());
        registry.register(IscatMobModel.class, new IscatMobView());

        initialize();
    }

    @Override
    protected void initStyles() {
        getStylesheets().add(Objects.requireNonNull(GamenexScene.class.getResource("/uni/gaben/iscat/styles/game.css"))
                .toExternalForm());
    }

    @Override
    protected void initNodes() {
        canvas = new Canvas();
        spawnerToolbar = new GamenexSpawnerToolbar(gamenexController);
        pauseMenu = new GamenexPauseMenu(gamenexController);
    }

    @Override
    protected void initLayout() {
        root.getChildren().addAll(canvas, spawnerToolbar, pauseMenu);
        StackPane.setAlignment(spawnerToolbar, Pos.BOTTOM_CENTER);
        // Pause menu covers everything
    }

    @Override
    protected void initBindings() {
        canvas.widthProperty().bind(root.widthProperty());
        canvas.heightProperty().bind(root.heightProperty());

        // Bind SpaceModel to Canvas dimensions
        UniverseController universeController = gamenexController.getSpaceController();
        UniverseModel space = universeController.getSpaceModel();
        if (space != null) {
            space.widthProperty().bind(canvas.widthProperty());
            space.heightProperty().bind(canvas.heightProperty());

            // Regenerate stars when dimensions change
            space.widthProperty().addListener((obs, oldV, newV) -> universeController.getStarfieldController()
                    .regenerate(space.getStarfieldModel(), newV.doubleValue(), space.getHeight()));
            space.heightProperty().addListener((obs, oldV, newV) -> universeController.getStarfieldController()
                    .regenerate(space.getStarfieldModel(), space.getWidth(), newV.doubleValue()));

            // Bind StarfieldView dimensions
            starfieldView.wProperty().bind(canvas.widthProperty());
            starfieldView.hProperty().bind(canvas.heightProperty());
        }
    }

    @Override
    protected void initEventHandlers() {
        gamenexController.getInputManager().attachToScene(this);
        gamenexController.getInputManager().attachToCanvas(canvas);

        // Bind visibility to pause state
        pauseMenu.visibleProperty().bind(gamenexModel.pausedProperty());
        pauseMenu.managedProperty().bind(pauseMenu.visibleProperty());

        // Toggle pause on ESCAPE
        this.addEventHandler(javafx.scene.input.KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                gamenexController.togglePause();
                e.consume();
            }
        });
    }

    @Override
    public void onLoad() {
        super.onLoad();
    }

    @Override
    public void onShow() {
        super.onShow();
        gamenexController.setRenderCallback(this::renderFrame);
        gamenexController.startGameLoop();
        javafx.application.Platform.runLater(() -> canvas.requestFocus());
    }

    private void renderFrame() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setImageSmoothing(false); // Pixel-perfect rendering

        double w = canvas.getWidth();
        double h = canvas.getHeight();

        gc.clearRect(0, 0, w, h);
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, w, h);

        UniverseController universeController = gamenexController.getSpaceController();
        UniverseModel space = universeController.getSpaceModel();
        if (space == null)
            return;

        // space dimensions and star regeneration are now handled via bindings/listeners
        // in initBindings

        universeController.setViewSize(w, h); // let controller know view size for camera

        CameraModel cameraModel = gamenexController.getCameraModel();

        // Pass camera back to input manager for correct mouse-to-world mapping
        gamenexController.getInputManager().cameraX = cameraModel.getX();
        gamenexController.getInputManager().cameraY = cameraModel.getY();

        // 1. Draw Starry Night Parallax
        starfieldView.setCameraX(cameraModel.getX());
        starfieldView.setCameraY(cameraModel.getY());
        starfieldView.draw(space.getStarfieldModel(), gc);

        gc.save();
        gc.translate(-cameraModel.getX(), -cameraModel.getY());

        for (var body : space.getEntities()) {
            Drawable renderer = ViewRegistry.getInstance().getRenderer(body.getClass());
            if (renderer != null) {
                renderer.draw(body, gc);
            }
        }
        gc.restore();

        // 3. Draw UI Overlay (FPS Counter)
        drawFps(gc, w);
    }

    private double[] fpsHistory = new double[30];
    private int fpsIdx = 0;

    private void drawFps(GraphicsContext gc, double w) {
        if (gamenexController.isShowFps()) {
            double fps = 1.0 / gamenexModel.getDt();
            fpsHistory[fpsIdx] = fps;
            fpsIdx = (fpsIdx + 1) % fpsHistory.length;
            
            double avg = 0;
            for (double f : fpsHistory) avg += f;
            avg /= fpsHistory.length;

            gc.setFill(Color.web("#00ff88", 0.7));
            gc.setLineWidth(TipografiaAurea.LABEL[TipografiaAurea.SMALL]);
            gc.fillText(String.format("FPS: %.0f", avg), w - 80, 50);
        }
    }

    @Override
    public void onHide() {
        super.onHide();
        gamenexController.stopGameLoop();
    }

    @Override
    public void onUnload() {
        super.onUnload();
        gamenexController.stopGameLoop();
    }

    // Ricarica la skin del player
    public void reloadPlayerSkin() {
        ViewRegistry registry = ViewRegistry.getInstance();
        Drawable renderer = registry.getRenderer(PlayerModel.class);

        if (renderer instanceof PlayerView playerView) {
            playerView.reloadSprite();
        }
    }
}
