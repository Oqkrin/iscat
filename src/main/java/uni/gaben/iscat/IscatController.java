package uni.gaben.iscat;

import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.util.EnumMap;

/**
 * Controller dell'applicazione.
 *
 * Responsabilità:
 * - Transizioni di scena (osserva IscatModel.currentScene)
 * - Gestione finestra: drag, resize, pulsanti title bar, fullscreen
 *
 * Tutto lo stato è in IscatModel. Questo controller è puro comportamento.
 */
public class IscatController {

    private static final int    RESIZE_MARGIN = 8;
    private static final double MIN_W         = 300;
    private static final double MIN_H         = 150;

    private final IscatModel model;
    private final Stage      stage;
    private final EnumMap<IscatScenes, Scene> sceneMap;

    public IscatController(IscatModel model, Stage stage,
                           EnumMap<IscatScenes, Scene> sceneMap) {
        this.model    = model;
        this.stage    = stage;
        this.sceneMap = sceneMap;

        model.currentSceneProperty().addListener((obs, old, next) ->
                performSceneTransition(next));
    }

    /** Called by IscatApplication after the stage is shown. */
    public void initializeScene() {
        performSceneTransition(model.getCurrentScene());
    }

    /**
     * Called by IscatApplication once per scene after it is created.
     * Wires all window-management behaviour to the scene's title bar.
     */
    public void wireScene(IscatSceneAbstract scene) {
        IscatSceneAbstract.TitleBar bar = scene.getTitleBar();
        if (bar == null) return;

        // --- title bar drag ---
        bar.setOnMousePressed(e -> {
            if (stage.isFullScreen() || stage.isMaximized()) return;
            model.dragOffsetX = e.getScreenX() - stage.getX();
            model.dragOffsetY = e.getScreenY() - stage.getY();
        });
        bar.setOnMouseDragged(e -> {
            if (stage.isFullScreen() || stage.isMaximized()) return;
            stage.setX(e.getScreenX() - model.dragOffsetX);
            stage.setY(e.getScreenY() - model.dragOffsetY);
        });

        // --- resize event filters on the scene ---
        scene.addEventFilter(MouseEvent.MOUSE_MOVED, e -> {
            if (stage.isFullScreen()) { scene.setCursor(Cursor.DEFAULT); return; }
            scene.setCursor(resizeCursor(getResizeDir(e.getSceneX(), e.getSceneY())));
        });
        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (stage.isFullScreen() || stage.isMaximized()) return;
            if (bar.isHover()) return; // title bar handles its own drag
            IscatModel.ResizeDir dir = getResizeDir(e.getSceneX(), e.getSceneY());
            if (dir == IscatModel.ResizeDir.NONE) return;
            model.resizeDir         = dir;
            model.resizeStartX      = e.getScreenX();
            model.resizeStartY      = e.getScreenY();
            model.resizeStartW      = stage.getWidth();
            model.resizeStartH      = stage.getHeight();
            model.resizeStartStageX = stage.getX();
            model.resizeStartStageY = stage.getY();
            e.consume();
        });
        scene.addEventFilter(MouseEvent.MOUSE_DRAGGED, e -> {
            if (stage.isFullScreen() || model.resizeDir == IscatModel.ResizeDir.NONE) return;
            applyResize(e.getScreenX(), e.getScreenY());
            e.consume();
        });
        scene.addEventFilter(MouseEvent.MOUSE_RELEASED,
                e -> model.resizeDir = IscatModel.ResizeDir.NONE);

        // --- title bar buttons ---
        stage.setFullScreenExitKeyCombination(KeyCombination.keyCombination("DELETE"));

        bar.closeBtn.setOnAction(e -> stage.close());
        bar.minimizeBtn.setOnAction(e -> stage.setIconified(true));
        bar.maximizeBtn.setOnAction(e -> stage.setMaximized(!stage.isMaximized()));
        bar.fullscreenBtn.setOnAction(e -> stage.setFullScreen(!stage.isFullScreen()));
        bar.pinBtn.setOnAction(e -> {
            boolean pinned = !model.isPinned();
            model.setPinned(pinned);
            stage.setAlwaysOnTop(pinned);
            bar.pinBtn.getStyleClass().removeAll("title-bar-btn-pin-active");
            if (pinned) bar.pinBtn.getStyleClass().add("title-bar-btn-pin-active");
        });

        // --- fullscreen → hide/show title bar ---
        stage.fullScreenProperty().addListener((obs, wasFs, isFs) -> {
            if (isFs) scene.onEnterFullscreen();
            else      scene.onExitFullscreen();
        });
    }

    // -------------------------------------------------------------------------
    // Scene transition
    // -------------------------------------------------------------------------

    private void performSceneTransition(IscatScenes next) {
        Scene current = stage.getScene();
        if (current instanceof IscatSceneAbstract old) old.onHide();

        IscatAudioManager.getInstance().playBGM(model.getBgmPath(next), true);

        // JavaFX exits fullscreen when setScene() is called — save and restore it
        boolean wasFullScreen = stage.isFullScreen();

        Scene nextScene = sceneMap.get(next);
        stage.setScene(nextScene);

        if (wasFullScreen) {
            stage.setFullScreen(true);
        }

        if (nextScene instanceof IscatSceneAbstract newScene) {
            syncWindowState(newScene);
            newScene.onShow();
        }
    }

    /**
     * Applies the current window state from the Stage and IscatModel
     * to the incoming scene's title bar so it looks consistent after a transition.
     */
    private void syncWindowState(IscatSceneAbstract scene) {
        IscatSceneAbstract.TitleBar bar = scene.getTitleBar();
        if (bar == null) return;

        // Pin button visual
        bar.pinBtn.getStyleClass().removeAll("title-bar-btn-pin-active");
        if (model.isPinned()) {
            bar.pinBtn.getStyleClass().add("title-bar-btn-pin-active");
        }
        // Fullscreen is handled by the stage.fullScreenProperty listener wired in wireScene()
    }

    // -------------------------------------------------------------------------
    // Resize helpers
    // -------------------------------------------------------------------------

    private void applyResize(double screenX, double screenY) {
        double dx   = screenX - model.resizeStartX;
        double dy   = screenY - model.resizeStartY;
        double minW = stage.getMinWidth()  > 0 ? stage.getMinWidth()  : MIN_W;
        double minH = stage.getMinHeight() > 0 ? stage.getMinHeight() : MIN_H;

        double newW = model.resizeStartW, newH = model.resizeStartH;
        double newX = model.resizeStartStageX, newY = model.resizeStartStageY;

        switch (model.resizeDir) {
            case E  -> newW = Math.max(minW, model.resizeStartW + dx);
            case S  -> newH = Math.max(minH, model.resizeStartH + dy);
            case W  -> { newW = Math.max(minW, model.resizeStartW - dx); newX = model.resizeStartStageX + (model.resizeStartW - newW); }
            case N  -> { newH = Math.max(minH, model.resizeStartH - dy); newY = model.resizeStartStageY + (model.resizeStartH - newH); }
            case SE -> { newW = Math.max(minW, model.resizeStartW + dx); newH = Math.max(minH, model.resizeStartH + dy); }
            case SW -> { newW = Math.max(minW, model.resizeStartW - dx); newX = model.resizeStartStageX + (model.resizeStartW - newW); newH = Math.max(minH, model.resizeStartH + dy); }
            case NE -> { newW = Math.max(minW, model.resizeStartW + dx); newH = Math.max(minH, model.resizeStartH - dy); newY = model.resizeStartStageY + (model.resizeStartH - newH); }
            case NW -> { newW = Math.max(minW, model.resizeStartW - dx); newX = model.resizeStartStageX + (model.resizeStartW - newW); newH = Math.max(minH, model.resizeStartH - dy); newY = model.resizeStartStageY + (model.resizeStartH - newH); }
            default -> {}
        }

        stage.setX(newX); stage.setY(newY);
        stage.setWidth(newW); stage.setHeight(newH);
    }

    private IscatModel.ResizeDir getResizeDir(double x, double y) {
        double w = stage.getWidth(), h = stage.getHeight();
        int m = RESIZE_MARGIN;
        boolean onN = y < m, onS = y > h - m, onW = x < m, onE = x > w - m;
        if (onN && onW) return IscatModel.ResizeDir.NW;
        if (onN && onE) return IscatModel.ResizeDir.NE;
        if (onS && onW) return IscatModel.ResizeDir.SW;
        if (onS && onE) return IscatModel.ResizeDir.SE;
        if (onN) return IscatModel.ResizeDir.N;
        if (onS) return IscatModel.ResizeDir.S;
        if (onW) return IscatModel.ResizeDir.W;
        if (onE) return IscatModel.ResizeDir.E;
        return IscatModel.ResizeDir.NONE;
    }

    private static Cursor resizeCursor(IscatModel.ResizeDir dir) {
        return switch (dir) {
            case N  -> Cursor.N_RESIZE;
            case S  -> Cursor.S_RESIZE;
            case E  -> Cursor.E_RESIZE;
            case W  -> Cursor.W_RESIZE;
            case NE -> Cursor.NE_RESIZE;
            case NW -> Cursor.NW_RESIZE;
            case SE -> Cursor.SE_RESIZE;
            case SW -> Cursor.SW_RESIZE;
            default -> Cursor.DEFAULT;
        };
    }
}
