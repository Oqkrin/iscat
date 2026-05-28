package uni.gaben.iscat.controller;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Duration;
import uni.gaben.iscat.model.IscatModel;
import uni.gaben.iscat.view.IscatTitleBar;

public class IscatWindowController {

    private static final int    RESIZE_MARGIN = 3;
    private static final double MIN_W         = 1280;
    private static final double MIN_H         = 720;

    // -------------------------------------------------------------------------
    // Local Window Math State
    // -------------------------------------------------------------------------
    private double dragOffsetX = 0;
    private double dragOffsetY = 0;

    private enum ResizeDir { NONE, N, S, E, W, NE, NW, SE, SW }
    private ResizeDir resizeDir = ResizeDir.NONE;

    private double resizeStartX, resizeStartY;
    private double resizeStartW, resizeStartH;
    private double resizeStartStageX, resizeStartStageY;

    // -------------------------------------------------------------------------
    // Core Dependencies & Animation Trackers
    // -------------------------------------------------------------------------
    private final IscatModel model;
    private final Stage      stage;
    private final Scene iscatScene;
    private final IscatTitleBar iscatTitleBar;

    private boolean barVisible = true;
    private TranslateTransition translateTransition;
    private FadeTransition fadeTransition;

    public IscatWindowController(IscatModel model, Stage stage, Scene iscatScene, IscatTitleBar iscatTitleBar) {
        this.model = model;
        this.stage = stage;
        this.iscatScene = iscatScene;
        this.iscatTitleBar = iscatTitleBar;

        model.pinnedProperty().addListener((obs, old, isPinned) -> syncWindowState());
        wireCustomDecoration();
        initializeWindow();
    }

    public void initializeWindow() {
        // Enforce boundary parameters directly inside standard OS toolkit layer
        stage.setMinWidth(MIN_W);
        stage.setMinHeight(MIN_H);
        stage.setWidth(MIN_W);
        stage.setHeight(MIN_H);
    }

    /**
     * Configura il comportamento di gestione della finestra sulla decorazione personalizzata globale.
     * Viene eseguito una sola volta all'avvio dell'applicazione.
     */
    public void wireCustomDecoration() {
        // Interazione iniziale sulla barra del titolo globale
        iscatTitleBar.setOnMousePressed(e -> {
            if (stage.isFullScreen()) return;

            // Premium feature: Doppio click per massimizzare/ripristinare la finestra
            if (e.getClickCount() == 2) {
                stage.setMaximized(!stage.isMaximized());
                return;
            }

            // Salva le coordinate relative iniziali del cursore rispetto alla finestra
            dragOffsetX = e.getScreenX() - stage.getX();
            dragOffsetY = e.getScreenY() - stage.getY();
        });

        // Gestione avanzata del drag della finestra
        iscatTitleBar.setOnMouseDragged(e -> {
            if (stage.isFullScreen()) return;

            // OS Behavior Match: Se trascini la finestra mentre è massimizzata, questa si
            // ripristina mantenendo la proporzione orizzontale del puntatore sulla barra.
            if (stage.isMaximized()) {
                double clickPercentage = dragOffsetX / stage.getWidth();
                stage.setMaximized(false);

                // Ricalcola istantaneamente gli offset basandosi sulle dimensioni ripristinate
                dragOffsetX = stage.getWidth() * clickPercentage;
                dragOffsetY = e.getScreenY() - stage.getY();
            }

            stage.setX(e.getScreenX() - dragOffsetX);
            stage.setY(e.getScreenY() - dragOffsetY);
        });

        // Gestione unificata del movimento del mouse (Auto-hide & Resize Cursors)
        iscatScene.addEventFilter(MouseEvent.MOUSE_MOVED, e -> {
            // 1. Priorità assoluta all'auto-hide se siamo in Fullscreen
            if (stage.isFullScreen()) {
                iscatScene.setCursor(Cursor.DEFAULT);

                double barHeight = iscatTitleBar.getHeight() > 0 ? iscatTitleBar.getHeight() : 40;
                if (e.getSceneY() < 8 && !barVisible) {
                    slideIn();
                } else if (e.getSceneY() > barHeight + 8 && barVisible) {
                    slideOut();
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
            ResizeDir dir = getResizeDir(e.getSceneX(), e.getSceneY());
            if (dir != ResizeDir.NONE) {
                resizeDir = dir;
                resizeStartX = e.getScreenX();
                resizeStartY = e.getScreenY();
                resizeStartW = stage.getWidth();
                resizeStartH = stage.getHeight();
                resizeStartStageX = stage.getX();
                resizeStartStageY = stage.getY();
                e.consume();
            }
        });

        // Drag sul bordo per applicare il ridimensionamento geometrico della finestra
        iscatScene.addEventFilter(MouseEvent.MOUSE_DRAGGED, e -> {
            if (stage.isFullScreen() || stage.isMaximized() || resizeDir == ResizeDir.NONE) return;
            applyResize(e.getScreenX(), e.getScreenY());
            e.consume();
        });

        // Reset dello stato al rilascio del mouse
        iscatScene.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> {
            resizeDir = ResizeDir.NONE;
        });

        // Mappatura delle azioni dei pulsanti nativi della barra
        stage.setFullScreenExitKeyCombination(KeyCombination.keyCombination("DELETE"));
        iscatTitleBar.closeBtn.setOnAction(e -> stage.close());
        iscatTitleBar.minimizeBtn.setOnAction(e -> stage.setIconified(true));
        iscatTitleBar.maximizeBtn.setOnAction(e -> stage.setMaximized(!stage.isMaximized()));
        iscatTitleBar.fullscreenBtn.setOnAction(e -> stage.setFullScreen(!stage.isFullScreen()));
        iscatTitleBar.pinBtn.setOnAction(e -> stage.setAlwaysOnTop(!stage.isAlwaysOnTop()));

        stage.fullScreenProperty().addListener((obs, wasFs, isFs) -> handleFullscreenBar(isFs));
    }

    private void handleFullscreenBar(boolean isFs) {
        if (isFs) {
            iscatTitleBar.getStyleClass().add("title-bar-fullscreen");
            barVisible = true;
            slideOut();
        } else {
            iscatTitleBar.getStyleClass().remove("title-bar-fullscreen");
            barVisible = true;

            // Forza il blocco delle animazioni attive per evitare sfarfallii uscendo dal fullscreen
            if (translateTransition != null) translateTransition.stop();
            if (fadeTransition != null) fadeTransition.stop();

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

    /**
     * Sistema centralizzato di gestione delle transizioni per evitare sfarfallii concorrenti
     * quando si muove rapidamente il mouse dentro/fuori dal raggio di attivazione.
     */
    private void runAnimation(double toY, double toOpacity, int durationMs) {
        if (translateTransition != null) translateTransition.stop();
        if (fadeTransition != null) fadeTransition.stop();

        translateTransition = new TranslateTransition(Duration.millis(durationMs), iscatTitleBar);
        translateTransition.setToY(toY);

        fadeTransition = new FadeTransition(Duration.millis(durationMs), iscatTitleBar);
        fadeTransition.setToValue(toOpacity);

        translateTransition.play();
        fadeTransition.play();
    }

    private void slideIn() {
        barVisible = true;
        runAnimation(0, 1.0, 150);
    }

    private void slideOut() {
        barVisible = false;
        double h = iscatTitleBar.getHeight() > 0 ? iscatTitleBar.getHeight() : 40;
        runAnimation(-h - 4, 0.0, 200);
    }

    // -------------------------------------------------------------------------
    // Resize helpers
    // -------------------------------------------------------------------------

    private void applyResize(double screenX, double screenY) {
        double dx   = screenX - resizeStartX;
        double dy   = screenY - resizeStartY;
        double minW = stage.getMinWidth();
        double minH = stage.getMinHeight();

        double newW = resizeStartW, newH = resizeStartH;
        double newX = resizeStartStageX, newY = resizeStartStageY;

        switch (resizeDir) {
            case E  -> newW = Math.max(minW, resizeStartW + dx);
            case S  -> newH = Math.max(minH, resizeStartH + dy);
            case W  -> { newW = Math.max(minW, resizeStartW - dx); newX = resizeStartStageX + (resizeStartW - newW); }
            case N  -> { newH = Math.max(minH, resizeStartH - dy); newY = resizeStartStageY + (resizeStartH - newH); }
            case SE -> { newW = Math.max(minW, resizeStartW + dx); newH = Math.max(minH, resizeStartH + dy); }
            case SW -> { newW = Math.max(minW, resizeStartW - dx); newX = resizeStartStageX + (resizeStartW - newW); newH = Math.max(minH, resizeStartH + dy); }
            case NE -> { newW = Math.max(minW, resizeStartW + dx); newH = Math.max(minH, resizeStartH - dy); newY = resizeStartStageY + (resizeStartH - newH); }
            case NW -> { newW = Math.max(minW, resizeStartW - dx); newX = resizeStartStageX + (resizeStartW - newW); newH = Math.max(minH, resizeStartH - dy); newY = resizeStartStageY + (resizeStartH - newH); }
            default -> {}
        }

        stage.setX(newX); stage.setY(newY);
        stage.setWidth(newW); stage.setHeight(newH);
    }

    private ResizeDir getResizeDir(double x, double y) {
        double w = stage.getWidth(), h = stage.getHeight();
        int m = RESIZE_MARGIN;
        boolean onN = y < m, onS = y > h - m, onW = x < m, onE = x > w - m;

        if (onN && onW) return ResizeDir.NW;
        if (onN && onE) return ResizeDir.NE;
        if (onS && onW) return ResizeDir.SW;
        if (onS && onE) return ResizeDir.SE;
        if (onN) return ResizeDir.N;
        if (onS) return ResizeDir.S;
        if (onW) return ResizeDir.W;
        if (onE) return ResizeDir.E;

        return ResizeDir.NONE;
    }

    private static Cursor resizeCursor(ResizeDir dir) {
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