package uni.gaben.iscat;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.EnumMap;

public class IscatController {

    private static final int    RESIZE_MARGIN = 3;
    private static final double MIN_W         = 1280;
    private static final double MIN_H         = 720;

    private final IscatModel model;
    private final Stage      stage;
    private final Scene iscatScene;
    private final EnumMap<IscatScenes, AbstractIscatStackPane> viewMap;

    private final StackPane iscatContentRoot;
    private final IscatTitleBar iscatTitleBar;

    private boolean barVisible = true;

    public IscatController(IscatModel model, Stage stage, Scene iscatScene,
                           StackPane iscatContentRoot, IscatTitleBar iscatTitleBar,
                           EnumMap<IscatScenes, AbstractIscatStackPane> viewMap) {
        this.model = model;
        this.stage = stage;
        this.iscatScene = iscatScene;
        this.iscatContentRoot = iscatContentRoot;
        this.iscatTitleBar = iscatTitleBar;
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
     * Configura il comportamento di gestione della finestra sulla decorazione personalizzata globale.
     * Viene eseguito una sola volta all'avvio dell'applicazione.
     */
    public void wireCustomDecoration() {
        // Trascinamento della finestra tramite la barra del titolo globale
        iscatTitleBar.setOnMousePressed(e -> {
            if (stage.isFullScreen() || stage.isMaximized()) return;
            model.dragOffsetX = e.getScreenX() - stage.getX();
            model.dragOffsetY = e.getScreenY() - stage.getY();
        });

        iscatTitleBar.setOnMouseDragged(e -> {
            if (stage.isFullScreen() || stage.isMaximized()) return;
            stage.setX(e.getScreenX() - model.dragOffsetX);
            stage.setY(e.getScreenY() - model.dragOffsetY);
        });

        // Gestione unificata del movimento del mouse (Auto-hide & Resize Cursors)
        iscatScene.addEventFilter(MouseEvent.MOUSE_MOVED, e -> {
            // 1. Priorità assoluta all'auto-hide se siamo in Fullscreen
            if (stage.isFullScreen()) {
                iscatScene.setCursor(Cursor.DEFAULT);

                double barHeight = iscatTitleBar.getHeight() > 0 ? iscatTitleBar.getHeight() : 40;
                if (e.getSceneY() < 8 && !barVisible) {
                    slideIn(iscatTitleBar);
                } else if (e.getSceneY() > barHeight + 8 && barVisible) {
                    slideOut(iscatTitleBar);
                }
                return; // Salta il calcolo del resize dei bordi
            }

            // 2. Se è massimizzato, resetta il cursore e ignora i bordi
            if (stage.isMaximized()) {
                iscatScene.setCursor(Cursor.DEFAULT);
                return;
            }

            // 3. Altrimenti, aggiorna il cursore per il ridimensionamento standard
            iscatScene.setCursor(resizeCursor(getResizeDir(e.getSceneX(), e.getSceneY())));
        });

        // Intercettazione del click sul bordo per avviare il ridimensionamento
        iscatScene.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
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
                e.consume();
            }
        });

        // Drag sul bordo per applicare il ridimensionamento geometrico della finestra
        iscatScene.addEventFilter(MouseEvent.MOUSE_DRAGGED, e -> {
            if (stage.isFullScreen() || stage.isMaximized() || model.resizeDir == IscatModel.ResizeDir.NONE) return;
            applyResize(e.getScreenX(), e.getScreenY());
            e.consume();
        });

        // Reset dello stato al rilascio del mouse
        iscatScene.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> {
            model.resizeDir = IscatModel.ResizeDir.NONE;
        });

        // Mappatura delle azioni dei pulsanti nativi della barra
        stage.setFullScreenExitKeyCombination(KeyCombination.keyCombination("DELETE"));
        iscatTitleBar.closeBtn.setOnAction(e -> stage.close());
        iscatTitleBar.minimizeBtn.setOnAction(e -> stage.setIconified(true));
        iscatTitleBar.maximizeBtn.setOnAction(e -> stage.setMaximized(!stage.isMaximized()));
        iscatTitleBar.fullscreenBtn.setOnAction(e -> stage.setFullScreen(!stage.isFullScreen()));

        // Listener unico per lo stato di Fullscreen della finestra
        stage.fullScreenProperty().addListener((obs, wasFs, isFs) -> handleFullscreenBar(isFs));
    }

    // -------------------------------------------------------------------------
    // Scene transition
    // -------------------------------------------------------------------------

    private void performSceneTransition(IscatScenes next) {
        // Disattivazione del ciclo di vita della vecchia vista interna
        if (!iscatContentRoot.getChildren().isEmpty() && iscatContentRoot.getChildren().getFirst() instanceof IscatViewLifecycleInterface old) {
            old.setActive(false);
        }

        IscatAudioManager.getInstance().playBGM(model.getBgmPath(next), true);

        AbstractIscatStackPane nextView = viewMap.get(next);
        nextView.initialize(); // Lazy-init specifico della vista

        // SWAP DEI NODI: Cambia unicamente il core interno alla vista
        iscatContentRoot.getChildren().setAll(nextView);

        syncWindowState();
        nextView.setActive(true);

        // Allinea lo stato della barra se la transizione avviene a fullscreen attivo
        handleFullscreenBar(stage.isFullScreen());
    }

    private void handleFullscreenBar(boolean isFs) {
        if (isFs) {
            iscatTitleBar.getStyleClass().add("title-bar-fullscreen");
            barVisible = true;
            slideOut(iscatTitleBar);
        } else {
            iscatTitleBar.getStyleClass().remove("title-bar-fullscreen");
            barVisible = true;
            iscatTitleBar.setTranslateY(0);
            iscatTitleBar.setOpacity(1.0);
        }
    }

    private void syncWindowState() {
        iscatTitleBar.pinBtn.getStyleClass().removeAll("title-bar-btn-pin-active");
        if (model.isPinned()) {
            iscatTitleBar.pinBtn.getStyleClass().add("title-bar-btn-pin-active");
        }
    }

    private void slideIn(IscatTitleBar bar) {
        barVisible = true;
        TranslateTransition t = new TranslateTransition(Duration.millis(150), bar);
        t.setToY(0);
        FadeTransition f = new FadeTransition(Duration.millis(150), bar);
        f.setToValue(1.0);
        t.play();
        f.play();
    }

    private void slideOut(IscatTitleBar bar) {
        barVisible = false;
        TranslateTransition t = new TranslateTransition(Duration.millis(200), bar);
        double h = bar.getHeight() > 0 ? bar.getHeight() : 40;
        t.setToY(-h - 4);
        FadeTransition f = new FadeTransition(Duration.millis(200), bar);
        f.setToValue(0.0);
        t.play();
        f.play();
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