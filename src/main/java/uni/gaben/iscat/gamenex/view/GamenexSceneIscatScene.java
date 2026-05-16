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

/**
 * Scena principale di Gamenex (View).
 * Gestisce il rendering su Canvas, l'interfaccia utente (UI) e
 * il coordinamento tra il modello fisico e la visualizzazione.
 */
public class GamenexSceneIscatScene extends AbstractIscatScene {

    private GamenexModel gamenexModel;
    private GamenexController gamenexController;
    private StackPane root;
    private Canvas canvas;
    private final StarfieldView starfieldView = new StarfieldView();
    private GamenexSpawnerToolbar spawnerToolbar;
    private GamenexPauseMenu pauseMenu;
    private Button debugButton;

    public GamenexSceneIscatScene(GamenexController gamenexController, GamenexModel gamenexModel) {
        super(new StackPane());
        this.gamenexModel = gamenexModel;
        this.gamenexController = gamenexController;
        this.root = getContentRoot();

        // Make root transparent so stars show through
        root.setStyle("-fx-background-color: transparent;");

        initialize();
    }

    @Override
    protected void initNodes() {
        canvas = new Canvas();
        spawnerToolbar = new GamenexSpawnerToolbar(gamenexController);
        pauseMenu = new GamenexPauseMenu(gamenexController);
        // ==================== DEBUG BUTTON ====================
        debugButton = new Button("DEBUG");
        debugButton.setFocusTraversable(false);

    }

    @Override
    protected void initStyles() {
        getStylesheets().add(Objects.requireNonNull(GamenexSceneIscatScene.class.getResource("/uni/gaben/iscat/styles/game.css"))
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

        // Bind SpaceModel to Canvas dimensions
        UniverseController universeController = gamenexController.getSpaceController();
        UniverseModel space = universeController.getUniverseModel();
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

            // Bind visibility to pause state
            pauseMenu.visibleProperty().bind(gamenexModel.pausedProperty());
            pauseMenu.managedProperty().bind(pauseMenu.visibleProperty());
        }
    }

    @Override
    protected void initEventHandlers() {
        gamenexController.getInputManager().attachToScene(this);
        gamenexController.getInputManager().attachToCanvas(canvas);


        // Toggle pause on ESCAPE
        this.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                gamenexController.togglePause();
                e.consume();
            }
        });

        // ==================== DEBUG BUTTON ACTION ====================
        debugButton.setOnAction(_ -> {
            boolean visible = !spawnerToolbar.isSpawnButtonsVisible();
            spawnerToolbar.setSpawnButtonsVisible(visible);
            debugButton.setText(visible ? "HIDE DEBUG" : "DEBUG");
        });
    }



    @Override
    public void onLoad() {
        super.onLoad();
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
        gc.setImageSmoothing(false); // Pixel-perfect rendering

        double w = canvas.getWidth();
        double h = canvas.getHeight();

        gc.clearRect(0, 0, w, h);
        ThemeColors.ensureLoaded();
        gc.setFill(ThemeColors.parsedColors.get("bg-primary"));
        gc.fillRect(0, 0, w, h);

        UniverseController universeController = gamenexController.getSpaceController();
        UniverseModel space = universeController.getUniverseModel();

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

        for (var entity : space.getEntities()) {
            Drawable renderer = ViewRegistry.getInstance().getRenderer(entity.getClass());
            if (renderer != null) {
                renderer.draw(entity, gc);
            }
        }
        gc.restore();

        // 3. Draw UI Overlay (FPS Counter)
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
