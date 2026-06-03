package uni.gaben.iscat.universe.rendering;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import uni.gaben.iscat.controller.game.GameController;
import uni.gaben.iscat.model.game.GameModel;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.camera.CameraModel;
import uni.gaben.iscat.universe.enviroment.starfield.StarfieldView;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.rendering.vfx.VFXRenderer;
import uni.gaben.iscat.utils.design.TipografiaAurea;
import uni.gaben.iscat.utils.theme.ThemeManager;
import uni.gaben.iscat.view.components.StarryText;

import java.util.ArrayList;
import java.util.List;

/**
 * Master renderer for the entire universe scene.
 *
 * <p>Order of operations each frame:
 * <ol>
 *   <li>Clear canvas and fill background colour.</li>
 *   <li>Draw parallax starfield (StarfieldView is updated with live canvas dims).</li>
 *   <li>Apply camera transform and draw all game entities.</li>
 *   <li>Draw HUD (timer canvas).</li>
 *   <li>Optionally draw the FPS counter.</li>
 * </ol>
 * </p>
 */
public class UniverseRenderer {

    private final Canvas          mainCanvas;
    private final GameController  gameController;
    private final GameModel       gameModel;
    private final StarfieldView   starfieldView;

    private final double[] fpsHistory = new double[30];
    private int fpsIdx = 0;
    private int frameCount = 0;

    public UniverseRenderer(Canvas mainCanvas, GameController gameController, StarfieldView starfieldView) {
        this.mainCanvas      = mainCanvas;
        this.gameController  = gameController;
        this.gameModel       = gameController.getGameModel();
        this.starfieldView   = starfieldView;
    }

    /** Called every frame tick from the game loop via {@link GameController}. */
    public void renderFrame(Canvas timerCanvas, StarryText starryTimer, boolean debugPanelVisible) {
        GraphicsContext gc = mainCanvas.getGraphicsContext2D();
        gc.setImageSmoothing(false);

        double w = mainCanvas.getWidth();
        double h = mainCanvas.getHeight();

        if (frameCount < 5) {
            System.out.println("=== RENDER FRAME " + frameCount + " ===");
            System.out.println("Canvas: " + w + " x " + h);
        }

        // 1. Clear + background fill
        gc.clearRect(0, 0, w, h);
        gc.setFill(ThemeManager.getInstance().getBgPrimary());
        gc.fillRect(0, 0, w, h);

        UniverseModel universe = gameController.getUniverseController().getUniverseModel();
        if (universe == null) {
            if (frameCount < 5) System.out.println("Universe is NULL!");
            return;
        }

        CameraModel camera = gameController.getCameraModel();

        if (frameCount < 5) {
            System.out.println("Universe: " + universe.getWidth() + " x " + universe.getHeight());
            System.out.println("Camera: " + camera.getX() + ", " + camera.getY() + " zoom: " + camera.getZoom());
            System.out.println("Starfield: " + universe.getStarfieldModel().getStars().size() + " stars");
            System.out.println("Entities: " + universe.getEntities().size());
        }

        // 2. Starfield – push live canvas size so parallax wrap is always correct
        starfieldView.setW(w);
        starfieldView.setH(h);
        starfieldView.setCameraX(camera.getX());
        starfieldView.setCameraY(camera.getY());
        starfieldView.draw(universe.getStarfieldModel(), gc);

        // 3. Camera transform + entities
        gc.save();
        double zoom = camera.getZoom();
        gc.translate(w / 2 - camera.getX() * zoom, h / 2 - camera.getY() * zoom);
        gc.scale(zoom, zoom);

        boolean debug = debugPanelVisible && gameController.isDebugModeOn();
        List<AbstractEntityModel> snapshot = new ArrayList<>(universe.getEntities());
        for (AbstractEntityModel entity : snapshot) {
            EntityRenderer.draw(entity, gc);
            if (debug) VFXRenderer.drawDebugCollision(entity, gc);
        }

        gc.restore();

        // 4. HUD timer
        if (timerCanvas != null && starryTimer != null) {
            GraphicsContext tgc = timerCanvas.getGraphicsContext2D();
            tgc.clearRect(0, 0, timerCanvas.getWidth(), timerCanvas.getHeight());
            starryTimer.updateAndDraw(tgc);
        }

        // 5. FPS overlay
        if (gameController.isFpsOn()) drawFps(gc, w);
        
        frameCount++;
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
