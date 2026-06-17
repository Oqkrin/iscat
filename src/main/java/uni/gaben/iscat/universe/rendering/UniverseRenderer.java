package uni.gaben.iscat.universe.rendering;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.GaussianBlur;
import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.controller.game.GameController;
import uni.gaben.iscat.model.game.GameModel;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.camera.CameraModel;
import uni.gaben.iscat.universe.effects.EnduranceIndicator;
import uni.gaben.iscat.universe.entities.AbstractPhysicalEntityModel;
import uni.gaben.iscat.utils.design.TipografiaAurea;
import uni.gaben.iscat.utils.theme.ThemeManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class UniverseRenderer {

    public static final GaussianBlur ENDURANCE_INDICATOR_EFFECT = new GaussianBlur(2);
    private final Canvas          mainCanvas;
    private final GameController  gameController;
    private final GameModel       gameModel;
    private final StarfieldRenderer starfieldRenderer;

    private final double[] fpsHistory = new double[30];
    private int fpsIdx = 0;
    private final List<AbstractPhysicalEntityModel> entitySnapshotBuffer = new ArrayList<>();

    // Batched drawing helper
    private final OptimizedLayeredRenderer layers = new OptimizedLayeredRenderer();

    public UniverseRenderer(Canvas mainCanvas, GameController gameController, StarfieldRenderer starfieldRenderer) {
        this.mainCanvas       = mainCanvas;
        this.gameController   = gameController;
        this.gameModel        = gameController.getGameModel();
        this.starfieldRenderer = starfieldRenderer;
    }

    public void renderFrame(boolean debugPanelVisible) {
        GraphicsContext gc = mainCanvas.getGraphicsContext2D();
        gc.setImageSmoothing(false);

        double w = mainCanvas.getWidth();
        double h = mainCanvas.getHeight();

        // 1. Clear + background
        gc.clearRect(0, 0, w, h);
        gc.setFill(ThemeManager.getInstance().getBgPrimary());
        gc.fillRect(0, 0, w, h);

        UniverseModel universe = gameController.getUniverseController().getUniverseModel();
        if (universe == null) return;

        CameraModel camera = gameController.getCameraModel();

        // 2. Starfield (drawn directly – it’s already a single loop)
        starfieldRenderer.setW(w);
        starfieldRenderer.setH(h);
        starfieldRenderer.setCameraX(camera.getX());
        starfieldRenderer.setCameraY(camera.getY());
        starfieldRenderer.render(universe.getStarfieldModel(), gc);

        // 3. Prepare the batch collector for this frame
        layers.begin(gc, camera, w, h);

        // 4. Collect all visible entities into the batch (no drawing yet)
        entitySnapshotBuffer.clear();
        entitySnapshotBuffer.addAll(universe.getEntities());

        double zoom = camera.getZoom();
        double halfViewW = (w / 2.0) / zoom;
        double halfViewH = (h / 2.0) / zoom;
        double minX = camera.getX() - halfViewW;
        double maxX = camera.getX() + halfViewW;
        double minY = camera.getY() - halfViewH;
        double maxY = camera.getY() + halfViewH;

        boolean debug = debugPanelVisible && gameController.isDebugModeOn();

        for (AbstractPhysicalEntityModel entity : entitySnapshotBuffer) {
            if (!entity.isInsideViewport(minX, maxX, minY, maxY)) continue;
            EntityRenderer.renderLayered(entity, layers, debug);
        }

        layers.render();

        renderEnduranceAlterations(universe, camera, gameModel.getDt(), gc);

        renderHurt(camera, gc);

        if (gameController.isFpsOn()) drawFps(gc, w);
    }

    private void renderHurt(CameraModel camera, GraphicsContext gc) {
        if (camera.getHurtFlashIntensity() > 0.01) {
            gc.save();
            gc.setGlobalAlpha(camera.getHurtFlashIntensity() * 0.40);
            gc.setFill(ThemeManager.getInstance().getColorError());
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

    private final List<EnduranceIndicator> enduranceIndicators = new ArrayList<>();

    private void renderEnduranceAlterations(UniverseModel universe, CameraModel camera, double dt, GraphicsContext gc) {
        Map<Vector2, Double> altered = universe.getAlteredEndurances();
        if (!altered.isEmpty()) {
            for (Map.Entry<Vector2, Double> entry : altered.entrySet()) {
                EnduranceIndicator ind = EnduranceIndicator.create(
                        entry.getKey(),
                        camera,
                        entry.getValue(),
                        mainCanvas.getWidth(),
                        mainCanvas.getHeight()
                );
                enduranceIndicators.add(ind);
            }
            altered.clear();
        }

        Iterator<EnduranceIndicator> it = enduranceIndicators.iterator();
        while (it.hasNext()) {
            EnduranceIndicator ind = it.next();
            ind.update(dt);
            if (ind.shouldRemove()) {
                it.remove();
            }
        }

        gc.save();
        gc.setFont(EnduranceIndicator.FONT);
        gc.setEffect(ENDURANCE_INDICATOR_EFFECT);
        for (EnduranceIndicator ind : enduranceIndicators) {
            DrawVFX.drawEnduranceIndicator(ind, gc);
        }
        gc.restore();
    }

}