package uni.gaben.iscat.game.view;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import uni.gaben.iscat.game.GameModel;
import uni.gaben.iscat.game.components.entities.EntityModel;
import uni.gaben.iscat.game.components.space.SpaceModel;
import uni.gaben.iscat.game.components.space.SpaceView;
import uni.gaben.iscat.game.utils.interfaces.EntityRenderer;
import uni.gaben.iscat.game.utils.settings.VisualSettings;

/**
 * Coordinatore del rendering di gioco.
 *
 * Ordine di rendering (back to front):
 *   1. Sfondo (stelle)
 *   2. Entità (ordine di inserimento nel GameModel — back to front)
 *   3. HUD (FPS)
 *
 * Nessun instanceof — ogni entità ha il proprio renderer registrato in GameModel.
 */
public class GameCanvas extends Canvas {

    public static final double TILE_SIZE = VisualSettings.DIMENSIONE_TILE;

    private final GameModel model;
    private final SpaceModel space;
    private final SpaceView  spaceView = new SpaceView();

    public GameCanvas(GameModel model) {
        this.model = model;
        this.space = new SpaceModel(0, 0);
        space.widthProperty().bind(widthProperty().asObject().map(Number::intValue));
        space.heightProperty().bind(heightProperty().asObject().map(Number::intValue));
    }

    /** Called by the game loop every frame. */
    @SuppressWarnings("unchecked")
    public void render(int currentFps) {
        double w = getWidth();
        double h = getHeight();
        if (w <= 0 || h <= 0) return;

        GraphicsContext gc = getGraphicsContext2D();

        // Clear
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, w, h);
        gc.setImageSmoothing(false);

        // 1. Background
        spaceView.draw(gc, space);

        // 2. All entities — renderer looked up from GameModel, no instanceof
        for (var entry : model.getRenderables().entrySet()) {
            EntityModel entity   = entry.getKey();
            EntityRenderer renderer = entry.getValue();
            renderer.draw(gc, entity);
        }

        // 3. HUD
        if (VisualSettings.MOSTRA_FPS) {
            drawFPS(gc, currentFps);
        }
    }

    private void drawFPS(GraphicsContext gc, int fps) {
        gc.save();
        gc.setFill(Color.LIME);
        gc.setFont(Font.font("Miracode", 14));
        gc.fillText("FPS: " + fps, 10, 25);
        gc.restore();
    }

    public SpaceModel getSpace() { return space; }
}
