package uni.gaben.iscat.universe.rendering;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import uni.gaben.iscat.controller.game.GameController;
import uni.gaben.iscat.model.game.GameModel;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.camera.CameraModel;
import uni.gaben.iscat.universe.entity.AbstractEntityModel;
import uni.gaben.iscat.utils.design.TipografiaAurea;
import uni.gaben.iscat.utils.theme.ThemeManager;
import uni.gaben.iscat.view.components.StarryText;

import java.util.ArrayList;
import java.util.List;

public class UniverseRenderer {

    private final Canvas          mainCanvas;
    private final GameController  gameController;
    private final GameModel       gameModel;
    private final StarfieldRenderer starfieldRenderer;

    private final double[] fpsHistory = new double[30];
    private int fpsIdx = 0;

    // Persistent buffered array list layout to stop generating snapshot garbage 60+ times per second
    private final List<AbstractEntityModel> entitySnapshotBuffer = new ArrayList<>();

    public UniverseRenderer(Canvas mainCanvas, GameController gameController, StarfieldRenderer starfieldRenderer) {
        this.mainCanvas       = mainCanvas;
        this.gameController   = gameController;
        this.gameModel        = gameController.getGameModel();
        this.starfieldRenderer = starfieldRenderer;
    }

    public void renderFrame(Canvas timerCanvas, StarryText starryTimer, boolean debugPanelVisible) {
        GraphicsContext gc = mainCanvas.getGraphicsContext2D();
        gc.setImageSmoothing(false);

        double w = mainCanvas.getWidth();
        double h = mainCanvas.getHeight();

        // 1. Clear + background fill
        gc.clearRect(0, 0, w, h);
        gc.setFill(ThemeManager.getInstance().getBgPrimary());
        gc.fillRect(0, 0, w, h);

        UniverseModel universe = gameController.getUniverseController().getUniverseModel();
        if (universe == null) return;

        CameraModel camera = gameController.getCameraModel();

        // 2. Starfield
        starfieldRenderer.setW(w);
        starfieldRenderer.setH(h);
        starfieldRenderer.setCameraX(camera.getX());
        starfieldRenderer.setCameraY(camera.getY());
        starfieldRenderer.render(universe.getStarfieldModel(), gc);

        // 3. Camera transform + entities
        gc.save();
        double zoom = camera.getZoom();
        gc.translate(w / 2 - camera.getX() * zoom, h / 2 - camera.getY() * zoom);
        gc.scale(zoom, zoom);

        double halfViewW = (w / 2.0) / zoom;
        double halfViewH = (h / 2.0) / zoom;
        double minX = camera.getX() - halfViewW;
        double maxX = camera.getX() + halfViewW;
        double minY = camera.getY() - halfViewH;
        double maxY = camera.getY() + halfViewH;

        boolean debug = debugPanelVisible && gameController.isDebugModeOn();

        // REPLACED: new ArrayList<>(...) with clear() & addAll() on a private final buffer list.
        // This preserves safety against concurrent alterations while completely reusing the underlying array space.
        entitySnapshotBuffer.clear();
        entitySnapshotBuffer.addAll(universe.getEntities());

        for (AbstractEntityModel entity : entitySnapshotBuffer) {
            if (!entity.isInsideViewport(minX, maxX, minY, maxY)) continue;
            EntityRenderer.draw(entity, gc);
            if (debug) VFXRenderer.drawDebugCollision(entity, gc);
        }

        gc.restore();

        drawHurt(camera, gc);

        drawTimer(timerCanvas, starryTimer);

        // 5. FPS overlay
        if (gameController.isFpsOn()) drawFps(gc, w);
    }

    private static void drawTimer(Canvas timerCanvas, StarryText starryTimer) {
        if (timerCanvas != null && starryTimer != null) {
            GraphicsContext tgc = timerCanvas.getGraphicsContext2D();
            tgc.clearRect(0, 0, timerCanvas.getWidth(), timerCanvas.getHeight());
            starryTimer.updateAndDraw(tgc);
        }
    }

    private void drawHurt(CameraModel camera, GraphicsContext gc) {
        if (camera.getHurtFlashIntensity() > 0.01) {
            gc.save();
            // Peak opacity is scaled to a maximum of 40% so the player isn't blinded during heavy combat
            gc.setGlobalAlpha(camera.getHurtFlashIntensity() * 0.40);
            gc.setFill(javafx.scene.paint.Color.RED);

            // Draw full screen rectangle across live canvas geometry bounds
            gc.fillRect(0, 0, mainCanvas.getWidth(), mainCanvas.getHeight());
            gc.restore();
        }
    }

    private void drawFps(GraphicsContext gc, double canvasWidth) {
        double dt  = gameModel.getDt();
        double fps = dt > 0 ? 1.0 / dt : 0;
        fpsHistory[fpsIdx] = fps;
        fpsIdx = (fpsIdx + 1) % fpsHistory.length;

        double avg = 0;
        for (double f : fpsHistory) avg += f;
        avg /= fpsHistory.length;

        if (avg >= 60)      gc.setFill(ThemeManager.getInstance().getColorSuccess());
        else if (avg >= 30) gc.setFill(ThemeManager.getInstance().getColorWarning());
        else                gc.setFill(ThemeManager.getInstance().getColorError());

        gc.setLineWidth(TipografiaAurea.LABEL[TipografiaAurea.SMALL]);
        gc.fillText(String.format("FPS: %.0f", avg), canvasWidth - 80, 50);
    }

}