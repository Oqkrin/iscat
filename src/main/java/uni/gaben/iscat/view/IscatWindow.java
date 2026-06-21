package uni.gaben.iscat.view;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import uni.gaben.iscat.model.Dir;
import uni.gaben.iscat.utils.theme.ThemeManager;
import uni.gaben.iscat.view.components.IscatTitleBar;

/**
 * Finestra principale dell'applicazione (custom window decorata).
 * Gestisce il drag della finestra, il ridimensionamento manuale tramite i bordi,
 * e l'animazione a scomparsa della barra del titolo in modalità a schermo intero.
 */
public class IscatWindow extends StackPane {

    private static final int    RESIZE_MARGIN = 3;
    private static final double MIN_W = 1280, MIN_H = 720;

    private final Stage stage;
    private final Scene scene;
    private final IscatTitleBar titleBar;
    private final StackPane viewPane;     // Area in cui IscatViewController scambia le viste
    private final Region borderOverlay;

    // Stato di trascinamento e ridimensionamento
    private double dragOffsetX, dragOffsetY;
    private Dir dir = Dir.NONE;
    private double resizeStartX, resizeStartY;
    private double resizeStartW, resizeStartH;
    private double resizeStartStageX, resizeStartStageY;

    // Animazione della barra in modalità a schermo intero
    private boolean barVisible = true;
    private TranslateTransition translateTransition;
    private FadeTransition fadeTransition;

    /**
     * Costruisce la finestra principale inizializzando la barra del titolo personalizzata,
     * il contenitore delle viste e i filtri di input per il ridimensionamento.
     *
     * @param stage Lo stage nativo di JavaFX da controllare
     * @param scene La scena in cui questa finestra funge da nodo radice
     */
    public IscatWindow(Stage stage, Scene scene) {
        this.stage = stage;
        this.scene = scene;
        this.titleBar = new IscatTitleBar();
        this.viewPane = new StackPane();
        this.borderOverlay = new Region();
        buildLayout();
        setOnInputs();
        bindStageLimits();
        setupFullscreenBar();

        // La finestra stessa diventa il nodo radice della Scena
        scene.setRoot(this);
    }

    /** Espone l'area di contenuto in cui vengono scambiate le schermate. */
    public StackPane getView() { return viewPane; }

    /** Restituisce l'istanza della barra del titolo personalizzata. */
    public IscatTitleBar getTitleBar() { return titleBar; }

    /** Dispone graficamente la barra del titolo e la sovrapposizione del bordo sopra il pannello delle viste. */
    private void buildLayout() {
        titleBar.setMaxHeight(IscatTitleBar.TITLE_BAR_HEIGHT);
        borderOverlay.getStyleClass().add("window-border");
        borderOverlay.setMouseTransparent(true);
        borderOverlay.visibleProperty().bind(stage.fullScreenProperty().not());

        getChildren().addAll(viewPane, titleBar, borderOverlay);
        StackPane.setAlignment(titleBar, Pos.TOP_CENTER);
    }

    /** Imposta i limiti minimi geometrici e le dimensioni di partenza dello stage. */
    private void bindStageLimits() {
        stage.setMinWidth(MIN_W);
        stage.setMinHeight(MIN_H);
        stage.setWidth(MIN_W);
        stage.setHeight(MIN_H);
    }

    // ---------------------------------------------------------------------
    // Eventi del mouse – trascinamento, ridimensionamento, barra schermo intero
    // ---------------------------------------------------------------------

    /** Associa i listener di trascinamento alla barra del titolo e i filtri globali per il ridimensionamento sulla scena. */
    private void setOnInputs() {
        // Trascinamento della barra del titolo
        titleBar.setOnMousePressed(e -> {
            if (stage.isFullScreen()) return;
            if (e.getClickCount() == 2) {
                stage.setMaximized(!stage.isMaximized());
                return;
            }
            dragOffsetX = e.getScreenX() - stage.getX();
            dragOffsetY = e.getScreenY() - stage.getY();
        });
        titleBar.setOnMouseDragged(e -> {
            if (stage.isFullScreen()) return;
            if (stage.isMaximized()) {
                double ratio = dragOffsetX / stage.getWidth();
                stage.setMaximized(false);
                dragOffsetX = stage.getWidth() * ratio;
                dragOffsetY = e.getScreenY() - stage.getY();
            }
            stage.setX(e.getScreenX() - dragOffsetX);
            stage.setY(e.getScreenY() - dragOffsetY);
        });

        // Ridimensionamento e barra schermo intero – aggiunge i filtri sulla scena
        scene.addEventFilter(MouseEvent.MOUSE_MOVED, this::onMouseMoved);
        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, this::onMousePressed);
        scene.addEventFilter(MouseEvent.MOUSE_DRAGGED, this::onMouseDragged);
        scene.addEventFilter(MouseEvent.MOUSE_RELEASED, e -> dir = Dir.NONE);
    }

    /** Gestisce il movimento del mouse per aggiornare il cursore sui bordi o mostrare/nascondere la barra in fullscreen. */
    private void onMouseMoved(MouseEvent e) {
        if (stage.isFullScreen()) {
            scene.setCursor(Cursor.DEFAULT);
            double barH = titleBar.getHeight() > 0 ? titleBar.getHeight() : 40;
            if (e.getSceneY() < 8 && !barVisible) slideIn();
            else if (e.getSceneY() > barH + 8 && barVisible) slideOut();
        } else if (stage.isMaximized()) {
            scene.setCursor(Cursor.DEFAULT);
        } else {
            scene.setCursor(resizeCursor(getResizeDir(e.getSceneX(), e.getSceneY())));
        }
    }

    /** Intercetta la pressione del mouse sui margini per avviare la modalità di ridimensionamento della finestra. */
    private void onMousePressed(MouseEvent e) {
        if (stage.isFullScreen() || stage.isMaximized()) return;
        Dir cirrentDir = getResizeDir(e.getSceneX(), e.getSceneY());
        if (cirrentDir != Dir.NONE) {
            this.dir = cirrentDir;
            resizeStartX = e.getScreenX();
            resizeStartY = e.getScreenY();
            resizeStartW = stage.getWidth();
            resizeStartH = stage.getHeight();
            resizeStartStageX = stage.getX();
            resizeStartStageY = stage.getY();
            e.consume();
        }
    }

    /** Ricalcola le coordinate dello stage e le sue dimensioni in base alla direzione di ridimensionamento attiva. */
    private void onMouseDragged(MouseEvent e) {
        if (stage.isFullScreen() || stage.isMaximized() || dir == Dir.NONE) return;
        double dx = e.getScreenX() - resizeStartX;
        double dy = e.getScreenY() - resizeStartY;
        double minW = MIN_W, minH = MIN_H;
        double newW = resizeStartW, newH = resizeStartH;
        double newX = resizeStartStageX, newY = resizeStartStageY;

        switch (dir) {
            case E  -> newW = Math.max(minW, resizeStartW + dx);
            case S  -> newH = Math.max(minH, resizeStartH + dy);
            case W  -> { newW = Math.max(minW, resizeStartW - dx); newX = resizeStartStageX + (resizeStartW - newW); }
            case N  -> { newH = Math.max(minH, resizeStartH - dy); newY = resizeStartStageY + (resizeStartH - newH); }
            case SE -> { newW = Math.max(minW, resizeStartW + dx); newH = Math.max(minH, resizeStartH + dy); }
            case SW -> { newW = Math.max(minW, resizeStartW - dx); newX = resizeStartStageX + (resizeStartW - newW); newH = Math.max(minH, resizeStartH + dy); }
            case NE -> { newW = Math.max(minW, resizeStartW + dx); newH = Math.max(minH, resizeStartH - dy); newY = resizeStartStageY + (resizeStartH - newH); }
            case NW -> { newW = Math.max(minW, resizeStartW - dx); newX = resizeStartStageX + (resizeStartW - newW); newH = Math.max(minH, resizeStartH - dy); newY = resizeStartStageY + (resizeStartH - newH); }
        }

        stage.setX(newX); stage.setY(newY);
        stage.setWidth(newW); stage.setHeight(newH);
        e.consume();
    }

    /** Calcola la direzione del ridimensionamento (Nord, Sud, Ovest, Est o diagonali) in base ai margini della coordinata di input. */
    private Dir getResizeDir(double x, double y) {
        double w = stage.getWidth();
        double h = stage.getHeight();
        int m = RESIZE_MARGIN;
        boolean onN = y < m;
        boolean onS = y > h - m;
        boolean onW = x < m;
        boolean onE = x > w - m;
        if (onN && onW) return Dir.NW;
        if (onN && onE) return Dir.NE;
        if (onS && onW) return Dir.SW;
        if (onS && onE) return Dir.SE;
        if (onN) return Dir.N;
        if (onS) return Dir.S;
        if (onW) return Dir.W;
        if (onE) return Dir.E;
        return Dir.NONE;
    }

    /** Associa la direzione calcolata al rispettivo cursore nativo di ridimensionamento di JavaFX. */
    private static Cursor resizeCursor(Dir dir) {
        return switch (dir) {
            case N -> Cursor.N_RESIZE; case S -> Cursor.S_RESIZE;
            case E -> Cursor.E_RESIZE; case W -> Cursor.W_RESIZE;
            case NE -> Cursor.NE_RESIZE; case NW -> Cursor.NW_RESIZE;
            case SE -> Cursor.SE_RESIZE; case SW -> Cursor.SW_RESIZE;
            default -> Cursor.DEFAULT;
        };
    }

    // ---------------------------------------------------------------------
    // Animazioni della barra a schermo intero
    // ---------------------------------------------------------------------

    /** Configura i listener di transizione per alterare lo stile e resettare la barra quando si entra/esce dal fullscreen. */
    private void setupFullscreenBar() {
        stage.fullScreenProperty().addListener((obs, wasFs, isFs) -> {
            if (isFs) {
                titleBar.getStyleClass().add("title-bar-fullscreen");
                barVisible = true;
                slideOut();
            } else {
                titleBar.getStyleClass().remove("title-bar-fullscreen");
                barVisible = true;
                stopAnimations();
                titleBar.setTranslateY(0);
                titleBar.setOpacity(1.0);
            }
        });
    }

    /** Attiva l'animazione di scivolamento verso il basso per mostrare la barra del titolo. */
    private void slideIn() {
        barVisible = true;
        runAnimation(0, 1.0, 150);
    }

    /** Attiva l'animazione di scivolamento verso l'alto per nascondere la barra del titolo oltre il bordo dello schermo. */
    private void slideOut() {
        barVisible = false;
        double h = titleBar.getHeight() > 0 ? titleBar.getHeight() : IscatTitleBar.TITLE_BAR_HEIGHT;
        runAnimation(-h - 4, 0.0, 200);
    }

    /** Configura ed esegue in parallelo le transizioni di spostamento verticale (Translate) e opacità (Fade). */
    private void runAnimation(double toY, double toOpacity, int durationMs) {
        stopAnimations();
        translateTransition = new TranslateTransition(Duration.millis(durationMs), titleBar);
        translateTransition.setToY(toY);
        fadeTransition = new FadeTransition(Duration.millis(durationMs), titleBar);
        fadeTransition.setToValue(toOpacity);
        translateTransition.play();
        fadeTransition.play();
    }

    /** Interrompe forzatamente tutte le animazioni attive sulla barra del titolo. */
    private void stopAnimations() {
        if (translateTransition != null) translateTransition.stop();
        if (fadeTransition != null) fadeTransition.stop();
    }
}