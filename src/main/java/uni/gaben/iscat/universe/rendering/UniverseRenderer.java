package uni.gaben.iscat.universe.rendering;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import uni.gaben.iscat.universe.camera.CameraModel;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.enviroment.starfield.StarfieldView;
import uni.gaben.iscat.screens.game.controller.GameController;
import uni.gaben.iscat.screens.game.model.GameModel;
import uni.gaben.iscat.universe.rendering.vfx.VFXRenderer;
import uni.gaben.iscat.view.StarryText;
import uni.gaben.iscat.utils.theme.ThemeManager;
import uni.gaben.iscat.utils.design.TipografiaAurea;

import java.util.ArrayList;
import java.util.List;

/**
 * Master renderer for the entire universe scene.
 * <p>
 * Handles background clearing, starfield, entity rendering (delegated to
 * {@link EntityRenderer}), HUD overlay, and FPS display.
 * No per‑entity View objects are created – everything is stateless and data‑driven.
 */
public class UniverseRenderer {

    private final Canvas mainCanvas;
    private final GameController gameController;
    private final GameModel gameModel;
    private final StarfieldView starfieldView;

    private final double[] fpsHistory = new double[30];
    private int fpsIdx = 0;

    public UniverseRenderer(Canvas mainCanvas, GameController gameController, StarfieldView starfieldView) {
        this.mainCanvas = mainCanvas;
        this.gameController = gameController;
        this.gameModel = gameController.getGameModel();
        this.starfieldView = starfieldView;
    }

    /**
     * Entry point called on every frame tick from the GameLoop via GameController.
     */
    public void renderFrame(Canvas timerCanvas, StarryText starryTimer, boolean debugPanelVisible) {
        GraphicsContext gc = mainCanvas.getGraphicsContext2D();
        gc.setImageSmoothing(false);

        double w = mainCanvas.getWidth();
        double h = mainCanvas.getHeight();

        // 1. Clear background and draw base colour
        gc.clearRect(0, 0, w, h);
        gc.setFill(ThemeManager.getInstance().getBgPrimary());
        gc.fillRect(0, 0, w, h);

        UniverseModel universe = gameController.getUniverseController().getUniverseModel();
        if (universe == null) return;

        CameraModel cameraModel = gameController.getCameraModel();

        // 2. Render background starfield (parallax layer)
        starfieldView.setCameraX(cameraModel.getX());
        starfieldView.setCameraY(cameraModel.getY());
        starfieldView.draw(universe.getStarfieldModel(), gc);

        // 3. Apply camera transform and draw all game entities
        gc.save();

        double cx = cameraModel.getX();
        double cy = cameraModel.getY();
        double zoom = cameraModel.getZoom();
        double screenW = mainCanvas.getWidth();
        double screenH = mainCanvas.getHeight();

        gc.translate(screenW / 2 - cx * zoom, screenH / 2 - cy * zoom);
        gc.scale(zoom, zoom);

        boolean renderCollisionBoxes = debugPanelVisible && gameController.isDebugModeOn();

        // Snapshot entity list to avoid concurrent modification
        List<AbstractEntityModel> entitiesCopy = new ArrayList<>(universe.getEntities());
        for (var entity : entitiesCopy) {
            drawEntity(entity, gc, renderCollisionBoxes);
        }

        gc.restore();

        // 4. Render HUD overlay (timer canvas)
        if (timerCanvas != null && starryTimer != null) {
            GraphicsContext timerGc = timerCanvas.getGraphicsContext2D();
            timerGc.clearRect(0, 0, timerCanvas.getWidth(), timerCanvas.getHeight());
            starryTimer.updateAndDraw(timerGc);
        }

        // 5. Render FPS counter
        drawFps(gc, w);
    }

    /**
     * Draws a single entity and optionally its debug collision shape.
     * Delegates all rendering to the stateless {@link EntityRenderer}.
     */
    private void drawEntity(AbstractEntityModel entity, GraphicsContext gc, boolean renderCollisionBoxes) {
        EntityRenderer.draw(entity, gc);
        if (renderCollisionBoxes) {
            VFXRenderer.drawDebugCollision(entity, gc);
        }
    }

    /**
     * Draws a smoothed FPS readout in the top‑right corner.
     */
    private void drawFps(GraphicsContext gc, double canvasWidth) {
        if (!gameController.isFpsOn()) return;

        double dt = gameModel.getDt();
        double fps = 1.0 / dt;
        fpsHistory[fpsIdx] = fps;
        fpsIdx = (fpsIdx + 1) % fpsHistory.length;

        double avg = 0;
        for (double f : fpsHistory) avg += f;
        avg /= fpsHistory.length;

        if (avg >= 60) {
            gc.setFill(ThemeManager.getInstance().getColorSuccess());
        } else {
            gc.setFill(avg >= 30 ? ThemeManager.getInstance().getColorWarning()
                    : ThemeManager.getInstance().getColorError());
        }
        gc.setLineWidth(TipografiaAurea.LABEL[TipografiaAurea.SMALL]);
        gc.fillText(String.format("FPS: %.0f", avg), canvasWidth - 80, 50);
    }
}