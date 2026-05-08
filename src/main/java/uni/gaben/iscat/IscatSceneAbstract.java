package uni.gaben.iscat;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * Classe base per scene ISCAT — responsabilità SOLO view.
 * <p>
 * Costruisce il chrome della finestra (title bar + content wrapper + border overlay),
 * applica il clip arrotondato e gestisce l'animazione della title bar in fullscreen.
 * <p>
 * Tutto il comportamento della finestra (drag, resize, pulsanti, fullscreen key)
 * è gestito da IscatController tramite wireScene().
 * <p>
 * Pattern di inizializzazione:
 *   initStyles → initNodes → initLayout → initBindings → initEventHandlers → initAnimations
 */
public abstract class IscatSceneAbstract extends Scene implements IscatSceneLifecycleInterface {

    private static final double CORNER_RADIUS = 16.0;

    private boolean active      = false;
    private boolean initialized = false;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    protected IscatSceneAbstract(Parent root) {
        super(buildChrome(root));
        setFill(Color.web("#010203"));
        applyRoundedClip();
    }

    protected IscatSceneAbstract(Parent root, double width, double height) {
        super(buildChrome(root), width, height);
        setFill(Color.web("#010203"));
        applyRoundedClip();
    }

    // -------------------------------------------------------------------------
    // Chrome construction
    // -------------------------------------------------------------------------

    private static StackPane buildChrome(Parent content) {
        // Content wrapper — fills all available space
        StackPane contentWrapper = new StackPane(content);
        contentWrapper.getStyleClass().add("window-content");
        contentWrapper.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        contentWrapper.setMinSize(0, 0);

        Rectangle contentClip = new Rectangle();
        contentClip.widthProperty().bind(contentWrapper.widthProperty());
        contentClip.heightProperty().bind(contentWrapper.heightProperty());
        contentWrapper.setClip(contentClip);

        // Title bar — sits on top of content wrapper as an overlay
        TitleBar titleBar = new TitleBar();
        // Prevent StackPane from stretching the bar to full height
        titleBar.setMaxHeight(Region.USE_PREF_SIZE);
        titleBar.setMouseTransparent(false); // bar must receive mouse events

        // Chrome: StackPane with content filling everything, bar anchored to top
        StackPane chrome = new StackPane();
        chrome.getStyleClass().add("window-chrome");
        StackPane.setAlignment(contentWrapper, Pos.CENTER);
        StackPane.setAlignment(titleBar, Pos.TOP_LEFT);
        chrome.getChildren().addAll(contentWrapper, titleBar);
        Region borderOverlay = new Region();
        borderOverlay.getStyleClass().add("window-border-overlay");
        borderOverlay.setMouseTransparent(true);

        return new StackPane(chrome, borderOverlay);
    }

    private void applyRoundedClip() {
        StackPane rootWrapper = (StackPane) getRoot();
        StackPane chrome = (StackPane) rootWrapper.getChildren().get(0);
        Rectangle clip = new Rectangle();
        clip.setArcWidth(CORNER_RADIUS * 2);
        clip.setArcHeight(CORNER_RADIUS * 2);
        clip.widthProperty().bind(chrome.widthProperty());
        clip.heightProperty().bind(chrome.heightProperty());
        chrome.setClip(clip);
    }

    // -------------------------------------------------------------------------
    // Fullscreen title bar animation  (called by IscatController)
    // -------------------------------------------------------------------------

    private boolean barVisible = true;

    /** Called by IscatController when the stage enters fullscreen. */
    public void onEnterFullscreen() {
        TitleBar bar = getTitleBar();
        if (bar == null) return;
        bar.getStyleClass().add("title-bar-fullscreen");
        barVisible = true;
        slideOut(bar);

        // Show bar when mouse is near the top of the screen
        getRoot().setOnMouseMoved(e -> {
            if (e.getSceneY() < 8 && !barVisible)                       slideIn(bar);
            else if (e.getSceneY() > bar.getPrefHeight() + 8 && barVisible) slideOut(bar);
        });
    }

    /** Called by IscatController when the stage exits fullscreen. */
    public void onExitFullscreen() {
        TitleBar bar = getTitleBar();
        if (bar == null) return;
        bar.getStyleClass().remove("title-bar-fullscreen");
        getRoot().setOnMouseMoved(null);
        barVisible = true;
        bar.setTranslateY(0);
        bar.setOpacity(1.0);
    }

    private void slideIn(TitleBar bar) {
        barVisible = true;
        TranslateTransition t = new TranslateTransition(Duration.millis(150), bar);
        t.setToY(0);
        FadeTransition f = new FadeTransition(Duration.millis(150), bar);
        f.setToValue(1.0);
        t.play(); f.play();
    }

    private void slideOut(TitleBar bar) {
        barVisible = false;
        TranslateTransition t = new TranslateTransition(Duration.millis(200), bar);
        t.setToY(-bar.getHeight() - 4);
        FadeTransition f = new FadeTransition(Duration.millis(200), bar);
        f.setToValue(0.0);
        t.play(); f.play();
    }

    // -------------------------------------------------------------------------
    // Accessors for IscatController
    // -------------------------------------------------------------------------

    /** Package-visible so IscatController can wire button actions and drag. */
    TitleBar getTitleBar() {
        // root → StackPane(root) → [0] StackPane(chrome) → children include TitleBar
        if (getRoot() instanceof StackPane root
                && root.getChildren().get(0) instanceof StackPane chrome) {
            for (var child : chrome.getChildren()) {
                if (child instanceof TitleBar tb) return tb;
            }
        }
        return null;
    }

    /** Returns the content root passed by the subclass to super(). */
    @SuppressWarnings("unchecked")
    protected <T extends Parent> T getContentRoot() {
        // root → StackPane(root) → [0] StackPane(chrome) → [0] StackPane(contentWrapper) → [0] content
        if (getRoot() instanceof StackPane root
                && root.getChildren().get(0) instanceof StackPane chrome
                && chrome.getChildren().get(0) instanceof StackPane wrapper) {
            return (T) wrapper.getChildren().get(0);
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // TitleBar — pure UI node
    // -------------------------------------------------------------------------

    static class TitleBar extends HBox {

        final Button closeBtn      = makeBtn("✕",  "title-bar-btn-close",      "Close");
        final Button maximizeBtn   = makeBtn("⬜",  "title-bar-btn-maximize",   "Maximize");
        final Button fullscreenBtn = makeBtn("⛶",  "title-bar-btn-fullscreen", "Fullscreen");
        final Button minimizeBtn   = makeBtn("—",  "title-bar-btn-minimize",   "Minimize");
        final Button pinBtn        = makeBtn("📌", "title-bar-btn-pin",        "Always on top");

        TitleBar() {
            getStyleClass().add("title-bar");
            setAlignment(Pos.CENTER);
            setPadding(new Insets(5, 10, 5, 10));

            HBox left  = new HBox(pinBtn);
            left.setAlignment(Pos.CENTER_LEFT);

            HBox center = new HBox(4, maximizeBtn, closeBtn, fullscreenBtn);
            center.setAlignment(Pos.CENTER);

            HBox right = new HBox(minimizeBtn);
            right.setAlignment(Pos.CENTER_RIGHT);

            Region leftSpacer  = new Region();
            Region rightSpacer = new Region();
            HBox.setHgrow(leftSpacer,  Priority.ALWAYS);
            HBox.setHgrow(rightSpacer, Priority.ALWAYS);

            getChildren().addAll(left, leftSpacer, center, rightSpacer, right);
        }

        private static Button makeBtn(String text, String styleClass, String tooltip) {
            Button btn = new Button(text);
            btn.getStyleClass().addAll("title-bar-btn", styleClass);
            btn.setTooltip(new Tooltip(tooltip));
            btn.setFocusTraversable(false);
            return btn;
        }
    }

    // -------------------------------------------------------------------------
    // Initialization pattern
    // -------------------------------------------------------------------------

    protected final void initialize() {
        if (initialized) return;
        initStyles();
        initNodes();
        initLayout();
        initBindings();
        initEventHandlers();
        initAnimations();
        initialized = true;
    }

    protected abstract void initStyles();
    protected abstract void initNodes();
    protected abstract void initLayout();
    protected abstract void initBindings();
    protected abstract void initEventHandlers();
    protected void initAnimations() {}

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    @Override public boolean isActive() { return active; }

    @Override
    public void setActive(boolean active) {
        if (this.active == active) return;
        this.active = active;
        if (active) { onLoad(); onShow(); }
        else        { onHide(); }
    }

    public void destroy() {
        if (active) setActive(false);
        onUnload();
    }
}
