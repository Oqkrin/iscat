package uni.gaben.iscat.universe.rendering;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.paint.Color;
import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.controller.game.GameController;
import uni.gaben.iscat.model.game.GameModel;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.camera.CameraModel;
import uni.gaben.iscat.universe.effects.EnduranceIndicator;
import uni.gaben.iscat.universe.effects.HitSpark;
import uni.gaben.iscat.universe.entities.AbstractPhysicalEntityModel;
import uni.gaben.iscat.universe.rendering.vfx.HitSparkVFX;
import uni.gaben.iscat.utils.design.TipografiaAurea;
import uni.gaben.iscat.utils.theme.ThemeManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class UniverseRenderer {

    public static final GaussianBlur ENDURANCE_INDICATOR_EFFECT = new GaussianBlur(2);

    private final Canvas mainCanvas;
    private final GameController gameController;
    private final GameModel gameModel;
    private final StarfieldRenderer starfieldRenderer;
    private final OptimizedLayeredRenderer layers = new OptimizedLayeredRenderer();

    private final List<AbstractPhysicalEntityModel> entitySnapshotBuffer = new ArrayList<>();
    private final List<EnduranceIndicator> enduranceIndicators = new ArrayList<>();
    private final double[] fpsHistory = new double[30];
    private int fpsIdx = 0;
    private boolean bordersVisible = false;

    public UniverseRenderer(Canvas mainCanvas, GameController gameController) {
        this.mainCanvas = mainCanvas;
        this.gameController = gameController;
        this.gameModel = gameController.getGameModel();
        this.starfieldRenderer = new StarfieldRenderer();
    }

    public void updateViewport(double width, double height) {
        if (width <= 0 || height <= 0) return;
        UniverseModel universe = gameController.getUniverseController().getUniverseModel();
        if (universe != null) {
            universe.getStarfieldModel().generate(width, height);
        }
    }

    public void renderFrame(boolean debugPanelVisible) {
        GraphicsContext gc = mainCanvas.getGraphicsContext2D();
        gc.setImageSmoothing(false);

        double w = mainCanvas.getWidth();
        double h = mainCanvas.getHeight();

        // Sfondo ed Effetti Ambientali di Base
        clearAndDrawBackground(gc, w, h);

        UniverseModel universe = gameController.getUniverseController().getUniverseModel();
        if (universe == null) return;

        CameraModel camera = gameController.getCameraModel();

        // Disegno dello sfondo stellato e dei confini dell'arena
        renderSfondoEConfini(gc, universe, camera, w, h);

        // Fase di Batching (Raccolta elementi geometrici e sprite)
        layers.begin(gc, camera, w, h);

        EntityRenderer.beginFrame(camera.getZoom());

        entitySnapshotBuffer.clear();
        entitySnapshotBuffer.addAll(universe.getEntities());

        boolean debug = debugPanelVisible && gameController.isDebugModeOn();

        for (AbstractPhysicalEntityModel entity : entitySnapshotBuffer) {
            if (isEntityVisible(entity, camera, w, h)) {
                EntityRenderer.renderLayered(entity, layers, debug);
            }
        }

        renderHitSparks(universe, layers);

        // Esecuzione del disegno stratificato e ottimizzato
        layers.render();

        // Overlay Post-Rendering (Interfaccia di gioco e filtri grafici fissi)
        renderEnduranceAlterations(universe, camera, gameModel.getDt(), gc);
        renderTimeStop(camera, gc);
        renderHurt(camera, gc);

        if (gameController.isFpsOn()) {
            drawFps(gc, w);
        }
    }


    private void clearAndDrawBackground(GraphicsContext gc, double w, double h) {
        gc.clearRect(0, 0, w, h);
        gc.setFill(ThemeManager.getInstance().getBgPrimary());
        gc.fillRect(0, 0, w, h);
    }

    private void renderSfondoEConfini(GraphicsContext gc, UniverseModel universe, CameraModel camera, double w, double h) {
        starfieldRenderer.setW(w);
        starfieldRenderer.setH(h);
        starfieldRenderer.setCameraX(camera.getX());
        starfieldRenderer.setCameraY(camera.getY());
        starfieldRenderer.render(universe.getStarfieldModel(), gc);

        if(bordersVisible)
            drawUniverseBoundaries(gc, universe, camera);
    }

    /**
     * Verifica se un'entità fisica si trova all'interno del cono visivo attuale (Frustum Culling).
     */
    private boolean isEntityVisible(AbstractPhysicalEntityModel entity, CameraModel camera, double width, double height) {
        double zoom = camera.getZoom();
        double halfViewW = (width / 2.0) / zoom;
        double halfViewH = (height / 2.0) / zoom;

        double minX = camera.getX() - halfViewW;
        double maxX = camera.getX() + halfViewW;
        double minY = camera.getY() - halfViewH;
        double maxY = camera.getY() + halfViewH;

        return entity.isInsideRect(minX, maxX, minY, maxY);
    }

    private void renderHitSparks(UniverseModel universe, OptimizedLayeredRenderer layers) {
        for (HitSpark spark : universe.getHitSparks()) {
            HitSparkVFX.renderHitSpark(spark, layers);
        }
    }

    private void renderTimeStop(CameraModel camera, GraphicsContext gc) {
        if (gameModel.getTimeScale() < 1.0) {
            gc.save();
            double intensity = 1.0 - gameModel.getTimeScale();
            gc.setGlobalAlpha(intensity * 0.15);
            gc.setFill(ThemeManager.getInstance().getAccentTernary());
            gc.fillRect(0, 0, mainCanvas.getWidth(), mainCanvas.getHeight());
            gc.restore();
        }
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

    private void renderEnduranceAlterations(UniverseModel universe, CameraModel camera, double dt, GraphicsContext gc) {
        Map<Vector2, Double> altered = universe.getAlteredEndurances();
        if (!altered.isEmpty()) {
            for (Map.Entry<Vector2, Double> entry : altered.entrySet()) {
                EnduranceIndicator ind = EnduranceIndicator.create(
                        entry.getKey(), camera, entry.getValue(),
                        mainCanvas.getWidth(), mainCanvas.getHeight()
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

    private void drawUniverseBoundaries(GraphicsContext gc, UniverseModel universe, CameraModel camera) {
        double radiusPx = UU.mToPx(universe.getUniverseRadius());
        double zoom = camera.getZoom();

        double drawCenterX = (mainCanvas.getWidth() / 2.0) + (0.0 - camera.getX()) * zoom;
        double drawCenterY = (mainCanvas.getHeight() / 2.0) + (0.0 - camera.getY()) * zoom;

        double scaledRadius = radiusPx * zoom;
        double diameter = scaledRadius * 2.0;

        gc.save();
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2.0 * zoom);
        gc.strokeOval(drawCenterX - scaledRadius, drawCenterY - scaledRadius, diameter, diameter);
        gc.restore();
    }

    private void drawFps(GraphicsContext gc, double canvasWidth) {
        double dt = gameModel.getDt();
        fpsHistory[fpsIdx] = dt > 0 ? 1.0 / dt : 0;
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