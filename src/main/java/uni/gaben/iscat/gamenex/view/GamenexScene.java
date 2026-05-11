package uni.gaben.iscat.gamenex.view;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.dyn4j.dynamics.Body;
import uni.gaben.iscat.IscatSceneAbstract;
import uni.gaben.iscat.gamenex.camera.CameraModel;
import uni.gaben.iscat.gamenex.controller.GamenexController;
import uni.gaben.iscat.gamenex.interfaces.model.AbstractEntityModel;
import uni.gaben.iscat.gamenex.model.GamenexModel;
import uni.gaben.iscat.gamenex.player.PlayerModel;
import uni.gaben.iscat.gamenex.interfaces.view.Drawable;
import uni.gaben.iscat.gamenex.world.enviroment.asteroid.AsteroidModel;
import uni.gaben.iscat.gamenex.world.enviroment.asteroid.AsteroidView;
import uni.gaben.iscat.gamenex.world.enviroment.space.SpaceController;
import uni.gaben.iscat.gamenex.world.enviroment.space.SpaceModel;
import uni.gaben.iscat.gamenex.world.enviroment.space.starfield.StarfieldView;


import java.util.Objects;

public class GamenexScene extends IscatSceneAbstract {

    private GamenexModel gamenexModel;
    private GamenexController gamenexController;
    private StackPane root;
    private Canvas canvas;
    private final StarfieldView starfieldView = new StarfieldView();
    private java.util.Map<Class<?>, Drawable> renderers = new java.util.HashMap<>();

    public GamenexScene(GamenexController gamenexController, GamenexModel gamenexModel) {
        super(new StackPane());
        this.gamenexModel = gamenexModel;
        this.gamenexController = gamenexController;
        this.root = getContentRoot();

        // Make root transparent so stars show through
        root.setStyle("-fx-background-color: transparent;");

        renderers.put(PlayerModel.class, new uni.gaben.iscat.gamenex.player.PlayerView());
        renderers.put(AsteroidModel.class, new AsteroidView());

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
    }

    @Override
    protected void initLayout() {
        root.getChildren().add(canvas);
    }

    @Override
    protected void initBindings() {
        canvas.widthProperty().bind(root.widthProperty());
        canvas.heightProperty().bind(root.heightProperty());

        // Bind SpaceModel to Canvas dimensions
        SpaceController spaceController = gamenexController.getSpaceController();
        SpaceModel space = spaceController.getSpaceModel();
        if (space != null) {
            space.widthProperty().bind(canvas.widthProperty());
            space.heightProperty().bind(canvas.heightProperty());

            // Regenerate stars when dimensions change
            space.widthProperty().addListener((obs, oldV, newV) -> 
                spaceController.getStarfieldController().regenerate(space.getStarfieldModel(), newV.doubleValue(), space.getHeight()));
            space.heightProperty().addListener((obs, oldV, newV) -> 
                spaceController.getStarfieldController().regenerate(space.getStarfieldModel(), space.getWidth(), newV.doubleValue()));

            // Bind StarfieldView dimensions
            starfieldView.wProperty().bind(canvas.widthProperty());
            starfieldView.hProperty().bind(canvas.heightProperty());
        }
    }

    @Override
    protected void initEventHandlers() {
        gamenexController.getInputManager().attachToScene(this);
        gamenexController.getInputManager().attachToCanvas(canvas);

        // Ensure canvas can receive mouse/key focus
        canvas.setFocusTraversable(true);
        canvas.setOnMouseClicked(e -> canvas.requestFocus());
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

        SpaceController spaceController = gamenexController.getSpaceController();
        SpaceModel space = spaceController.getSpaceModel();
        if (space == null)
            return;

        // space dimensions and star regeneration are now handled via bindings/listeners in initBindings

        spaceController.setViewSize(w, h); // let controller know view size for camera

        CameraModel cameraModel = gamenexController.getCameraModel();

        // Pass camera back to input manager for correct mouse-to-world mapping
        gamenexController.getInputManager().cameraX = cameraModel.getX();
        gamenexController.getInputManager().cameraY = cameraModel.getY();


        // 1. Draw Starry Night Parallax
        starfieldView.setCameraX(cameraModel.getX());
        starfieldView.setCameraY(cameraModel.getY());
        starfieldView.render(space.getStarfieldModel(), gc);

        gc.save();
        gc.translate(-cameraModel.getX(), -cameraModel.getY());

        for (int i = 0; i < space.getBodyCount(); i++) {
            Body body = space.getBody(i);
            if (body instanceof AbstractEntityModel entity) {
                Drawable renderer = renderers.get(entity.getClass());
                if (renderer != null) {
                    renderer.render(entity, gc);
                }
            }
        }
        gc.restore();
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
}
