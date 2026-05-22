package uni.gaben.iscat;

import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.EnumMap;

/**
 * Controller dell'applicazione.
 * Responsabilità:
 * - Transizioni di scena (osserva IscatModel.currentScene)
 * - Gestione finestra: drag, resize, pulsanti title bar, fullscreen
 * Tutto lo stato è in IscatModel. Questo controller è puro comportamento.
 */
public class IscatController {

    private static final int    RESIZE_MARGIN = 3;
    private static final double MIN_W         = 1280;
    private static final double MIN_H         = 720;

    private final IscatModel model;
    private final Stage      stage;
    private final Scene globalScene;
    private final StackPane mainStageRoot;
    private final EnumMap<IscatScenes, AbstractIscatScene> viewMap;

    public IscatController(IscatModel model, Stage stage, Scene globalScene,
                           StackPane mainStageRoot, EnumMap<IscatScenes, AbstractIscatScene> viewMap) {
        this.model = model;
        this.stage = stage;
        this.globalScene = globalScene;
        this.mainStageRoot = mainStageRoot;
        this.viewMap = viewMap;

        model.currentSceneProperty().addListener((obs, old, next) -> performSceneTransition(next));
    }

    /** Called by IscatApplication after the stage is shown. */
    public void initializeScene() {
        stage.setWidth(MIN_W);
        stage.setHeight(MIN_H);
        performSceneTransition(model.getCurrentScene());
    }

    /**
     * Called by IscatApplication once per scene after it is created.
     * Wires all window-management behaviour to the scene's title bar.
     */
    public void wireCustomDecoration(AbstractIscatScene view) {
        IscatTitleBar bar = view.getTitleBar();
        if (bar == null) return;

        // Trascinamento della finestra tramite la TitleBar
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

        globalScene.addEventFilter(MouseEvent.MOUSE_MOVED, e -> {
            if (stage.isFullScreen() || stage.isMaximized()) { globalScene.setCursor(Cursor.DEFAULT); return; }
            globalScene.setCursor(resizeCursor(getResizeDir(e.getSceneX(), e.getSceneY())));
        });

        // Quando clicchi sul bordo, salva la posizione iniziale della finestra
        globalScene.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            if (stage.isFullScreen() || stage.isMaximized()) return;
            IscatModel.ResizeDir dir = getResizeDir(e.getSceneX(), e.getSceneY());
            if (dir != IscatModel.ResizeDir.NONE) {
                model.resizeDir = dir;
                model.resizeStartX = e.getScreenX();
                model.resizeStartY = e.getScreenY();
                model.resizeStartW = stage.getWidth();
                model.resizeStartH = stage.getHeight();
                model.resizeStartStageX = stage.getX();
                model.resizeStartStageY = stage.getY();
                e.consume(); // Impedisce ad altri componenti di intercettare il click sul bordo
            }
        });

        // Quando trascini il mouse sul bordo, ridimensiona la finestra usando applyResize
        globalScene.addEventFilter(MouseEvent.MOUSE_DRAGGED, e -> {
            if (stage.isFullScreen() || stage.isMaximized() || model.resizeDir == IscatModel.ResizeDir.NONE) return;
            applyResize(e.getScreenX(), e.getScreenY());
            e.consume();
        });

        // Quando rilasci il mouse, resetta lo stato del resize
        globalScene.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> {
            model.resizeDir = IscatModel.ResizeDir.NONE;
        });

        stage.setFullScreenExitKeyCombination(KeyCombination.keyCombination("DELETE"));
        bar.closeBtn.setOnAction(e -> stage.close());
        bar.minimizeBtn.setOnAction(e -> stage.setIconified(true));
        bar.maximizeBtn.setOnAction(e -> stage.setMaximized(!stage.isMaximized()));
        bar.fullscreenBtn.setOnAction(e -> stage.setFullScreen(!stage.isFullScreen()));

        // Listener unico per la gestione fullscreen sulla vista corrente
        stage.fullScreenProperty().addListener((obs, wasFs, isFs) -> {
            if (mainStageRoot.getChildren().getFirst() instanceof AbstractIscatScene currentView) {
                if (isFs) currentView.onEnterFullscreen();
                else currentView.onExitFullscreen();
            }
        });
    }

    // -------------------------------------------------------------------------
    // Scene transition
    // -------------------------------------------------------------------------

    private void performSceneTransition(IscatScenes next) {
        // Gestione ciclo di vita della vecchia vista
        if (!mainStageRoot.getChildren().isEmpty() && mainStageRoot.getChildren().getFirst() instanceof IscatSceneLifecycleInterface old) {
            old.setActive(false);
        }

        IscatAudioManager.getInstance().playBGM(model.getBgmPath(next), true);

        AbstractIscatScene nextView = viewMap.get(next);
        nextView.initialize(); // Lazy init delle view!

        // SCAMBIO DEI NODI NELLO STESSO STAGE/SCENE (Zero glitch grafici!)
        mainStageRoot.getChildren().setAll(nextView);

        syncWindowState(nextView);
        nextView.setActive(true);

        // Forza l'aggiornamento dello stato della barra se siamo già in fullscreen
        if (stage.isFullScreen()) {
            nextView.onEnterFullscreen();
        } else {
            nextView.onExitFullscreen();
        }
    }

    /**
     * Applies the current window state from the Stage and IscatModel
     * to the incoming scene's title bar so it looks consistent after a transition.
     */
    private void syncWindowState(AbstractIscatScene view) {
        IscatTitleBar bar = view.getTitleBar();
        if (bar == null) return;
        bar.pinBtn.getStyleClass().removeAll("title-bar-btn-pin-active");
        if (model.isPinned()) {
            bar.pinBtn.getStyleClass().add("title-bar-btn-pin-active");
        }
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
