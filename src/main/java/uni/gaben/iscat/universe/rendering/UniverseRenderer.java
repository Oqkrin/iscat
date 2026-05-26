package uni.gaben.iscat.universe.rendering;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import uni.gaben.iscat.universe.camera.CameraModel;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityView;
import uni.gaben.iscat.universe.lib.interfaces.view.Drawable;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.enviroment.starfield.StarfieldView;
import uni.gaben.iscat.iscat_screens.game.controller.GameController;
import uni.gaben.iscat.iscat_screens.game.model.GameModel;
import uni.gaben.iscat.iscat_m_view_c.StarryText;
import uni.gaben.iscat.utils.ThemeManager;
import uni.gaben.iscat.utils.design.TipografiaAurea;

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

        // 1. Core Background clearing
        gc.clearRect(0, 0, w, h);
        gc.setFill(ThemeManager.getInstance().getBgPrimary());
        gc.fillRect(0, 0, w, h);

        UniverseModel universe = gameController.getUniverseController().getUniverseModel();
        if (universe == null) return;

        CameraModel cameraModel = gameController.getCameraModel();

        // 2. Render Background Starfield
        starfieldView.setCameraX(cameraModel.getX());
        starfieldView.setCameraY(cameraModel.getY());
        starfieldView.draw(universe.getStarfieldModel(), gc);

        // 3. Render Game Entities inside Camera Space Matrix
        gc.save();
        gc.translate(-cameraModel.getViewportLeftX(), -cameraModel.getViewportTopY());

        boolean renderCollisionBoxes = debugPanelVisible && gameController.isDebugModeOn();
        for (var entity : universe.getEntities()) {
            drawEntity(entity, gc, renderCollisionBoxes);
        }
        gc.restore();

        // 4. Render Independent Overlay HUD Canvas Components
        if (timerCanvas != null && starryTimer != null) {
            GraphicsContext timerGc = timerCanvas.getGraphicsContext2D();
            timerGc.clearRect(0, 0, timerCanvas.getWidth(), timerCanvas.getHeight());
            starryTimer.updateAndDraw(timerGc);
        }

        // 5. Render Engine Performance Metrics
        drawFps(gc, w);
    }

    @SuppressWarnings("unchecked")
    private <T extends AbstractEntityModel> void drawEntity(T entity, GraphicsContext gc, boolean renderCollisionBoxes) {
        Drawable<T> renderer = (Drawable<T>) RenderRegistry.getInstance().getRenderer(entity.getClass());
        if (renderer == null) return;

        renderer.draw(entity, gc);

        if (renderCollisionBoxes && renderer instanceof AbstractEntityView) {
            AbstractEntityView<T> entityView = (AbstractEntityView<T>) renderer;
            gc.save();
            entityView.setPos(entity);
            entityView.drawDebugCollision(entity, gc);
            gc.restore();
        }
    }

    private void drawFps(GraphicsContext gc, double w) {
        if (gameController.isFpsOn()) {
            double fps = 1.0 / gameModel.getDt();
            fpsHistory[fpsIdx] = fps;
            fpsIdx = (fpsIdx + 1) % fpsHistory.length;

            double avg = 0;
            for (double f : fpsHistory) avg += f;
            avg /= fpsHistory.length;

            if (avg >= 60) gc.setFill(ThemeManager.getInstance().getColorSuccess());
            else
                gc.setFill(avg >= 30 ? ThemeManager.getInstance().getColorWarning() : ThemeManager.getInstance().getColorError());
            gc.setLineWidth(TipografiaAurea.LABEL[TipografiaAurea.SMALL]);
            gc.fillText(String.format("FPS: %.0f", avg), w - 80, 50);
        }
    }
}