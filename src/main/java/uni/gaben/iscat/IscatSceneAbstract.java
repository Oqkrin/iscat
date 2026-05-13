package uni.gaben.iscat;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import uni.gaben.iscat.utils.components.StarryBackgroundCanvas;

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
 * initStyles → initNodes → initLayout → initBindings → initEventHandlers → initAnimations
 */
public abstract class IscatSceneAbstract extends Scene implements IscatSceneLifecycleInterface {

    private static final double CORNER_RADIUS = 16.0;

    private boolean active      = false;
    private boolean initialized = false;
    
    private StarryBackgroundCanvas starryBackground;

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Instantiates a new Iscat scene abstract.
     *
     * @param root the root
     */
    protected IscatSceneAbstract(Parent root) {
        this(root, false);
    }
    
    /**
     * Instantiates a new Iscat scene abstract with optional starry background.
     *
     * @param root the root
     * @param withStarryBackground whether to include animated starry background
     */
    protected IscatSceneAbstract(Parent root, boolean withStarryBackground) {
        super(buildChrome(root, withStarryBackground));
        setFill(Color.web("#010203"));
        applyRoundedClip();
        if (withStarryBackground) {
            starryBackground = extractStarryBackground();
        }
    }

    /**
     * Instantiates a new Iscat scene abstract.
     *
     * @param root   the root
     * @param width  the width
     * @param height the height
     */
    protected IscatSceneAbstract(Parent root, double width, double height) {
        this(root, width, height, false, SceneAntialiasing.BALANCED);
    }
    
    /**
     * Instantiates a new Iscat scene abstract with optional starry background.
     *
     * @param root   the root
     * @param width  the width
     * @param height the height
     * @param withStarryBackground whether to include animated starry background
     */
    protected IscatSceneAbstract(Parent root, double width, double height, boolean withStarryBackground, SceneAntialiasing ant) {
        super(buildChrome(root, withStarryBackground), width, height, false, ant);
        setFill(Color.web("#010203"));
        applyRoundedClip();
        if (withStarryBackground) {
            starryBackground = extractStarryBackground();
        }
    }

    public IscatSceneAbstract(StackPane root, boolean starry, SceneAntialiasing ant) {
        this(root, 0, 0, starry, ant);
        //System.out.println("[IscatSceneAbstract] getAntiAliasing(): " + getAntiAliasing());
    }

    // -------------------------------------------------------------------------
    // Chrome construction
    // -------------------------------------------------------------------------

    private static StackPane buildChrome(Parent content, boolean withStarryBackground) {
        // Content wrapper — fills all available space
        StackPane contentWrapper = new StackPane();
        if (withStarryBackground) {
            contentWrapper.getStyleClass().add("window-content-transparent");
        } else {
            contentWrapper.getStyleClass().add("window-content");
        }
        contentWrapper.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        contentWrapper.setMinSize(0, 0);
        
        // Add starry background if requested
        if (withStarryBackground) {
            StarryBackgroundCanvas starryBg = new StarryBackgroundCanvas();
            starryBg.widthProperty().bind(contentWrapper.widthProperty());
            starryBg.heightProperty().bind(contentWrapper.heightProperty());
            starryBg.setMouseTransparent(true);
            contentWrapper.getChildren().add(starryBg);
        }
        
        // Add content on top of background
        contentWrapper.getChildren().add(content);

        Rectangle contentClip = new Rectangle();
        contentClip.widthProperty().bind(contentWrapper.widthProperty());
        contentClip.heightProperty().bind(contentWrapper.heightProperty());
        contentWrapper.setClip(contentClip);

        // Title bar — sits on top of content wrapper as an overlay
        IscatTitleBar titleBar = new IscatTitleBar();
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
    
    private StarryBackgroundCanvas extractStarryBackground() {
        // Navigate: root → StackPane → chrome → contentWrapper → [0] StarryBackgroundCanvas
        if (getRoot() instanceof StackPane root
                && root.getChildren().get(0) instanceof StackPane chrome
                && chrome.getChildren().get(0) instanceof StackPane wrapper
                && !wrapper.getChildren().isEmpty()
                && wrapper.getChildren().get(0) instanceof StarryBackgroundCanvas bg) {
            return bg;
        }
        return null;
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
    // Starry background access
    // -------------------------------------------------------------------------
    
    /**
     * Returns the starry background canvas if enabled, null otherwise.
     */
    protected StarryBackgroundCanvas getStarryBackground() {
        return starryBackground;
    }

    // -------------------------------------------------------------------------
    // Fullscreen title bar animation  (called by IscatController)
    // -------------------------------------------------------------------------

    private boolean barVisible = true;

    /**
     * Called by IscatController when the stage enters fullscreen.
     */
    public void onEnterFullscreen() {
        IscatTitleBar bar = getTitleBar();
        if (bar == null) return;
        bar.getStyleClass().add("title-bar-fullscreen");
        barVisible = true;
        slideOut(bar);

        // Show bar when mouse is near the top of the screen
        // Store existing mouse handler to preserve starry background tracking
        var existingHandler = getRoot().getOnMouseMoved();
        getRoot().setOnMouseMoved(e -> {
            // Call existing handler first (starry background)
            if (existingHandler != null) {
                existingHandler.handle(e);
            }
            // Then handle title bar visibility
            if (e.getSceneY() < 8 && !barVisible)                       slideIn(bar);
            else if (e.getSceneY() > bar.getPrefHeight() + 8 && barVisible) slideOut(bar);
        });
    }

    /**
     * Called by IscatController when the stage exits fullscreen.
     */
    public void onExitFullscreen() {
        IscatTitleBar bar = getTitleBar();
        if (bar == null) return;
        bar.getStyleClass().remove("title-bar-fullscreen");
        // Don't clear mouse handler - let the scene manage it
        // getRoot().setOnMouseMoved(null);
        barVisible = true;
        bar.setTranslateY(0);
        bar.setOpacity(1.0);
    }

    private void slideIn(IscatTitleBar bar) {
        barVisible = true;
        TranslateTransition t = new TranslateTransition(Duration.millis(150), bar);
        t.setToY(0);
        FadeTransition f = new FadeTransition(Duration.millis(150), bar);
        f.setToValue(1.0);
        t.play(); f.play();
    }

    private void slideOut(IscatTitleBar bar) {
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

    /**
     * Package-visible so IscatController can wire button actions and drag.  @return  the title bar
     */
    IscatTitleBar getTitleBar() {
        // root → StackPane(root) → [0] StackPane(chrome) → children include TitleBar
        if (getRoot() instanceof StackPane root
                && root.getChildren().get(0) instanceof StackPane chrome) {
            for (var child : chrome.getChildren()) {
                if (child instanceof IscatTitleBar tb) return tb;
            }
        }
        return null;
    }

    /**
     * Returns the content root passed by the subclass to super().  @param <T>  the type parameter
     *
     * @return the content root
     */
    @SuppressWarnings("unchecked")
    protected <T extends Parent> T getContentRoot() {
        // root → StackPane(root) → [0] StackPane(chrome) → [0] StackPane(contentWrapper) → [last] content
        if (getRoot() instanceof StackPane root
                && root.getChildren().get(0) instanceof StackPane chrome
                && chrome.getChildren().get(0) instanceof StackPane wrapper) {
            // Content is the last child (after optional starry background)
            return (T) wrapper.getChildren().get(wrapper.getChildren().size() - 1);
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Initialization pattern
    // -------------------------------------------------------------------------

    /**
     * Initialize.
     * Chiami i metodi costruzione init
     */
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

    /**
     * Init styles.
     * Carica CSS
     * */
    protected abstract void initStyles();

    /**
     * Init nodes.
     * Istanzia nodi scena
     * */
    protected abstract void initNodes();

    /**
     * Init layout.
     * Compone layout
     * */
    protected abstract void initLayout();

    /**
     * Init bindings.
     * Impone i Bindings
     * */
    protected abstract void initBindings();

    /**
     * Init event handlers.
     * Delega Input a Controller
     * */
    protected abstract void initEventHandlers();

    /**
     * Init animations.
     */
    protected void initAnimations() {}

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    @Override public boolean isActive() { return active; }

    @Override
    public void setActive(boolean active) {
        if (this.active == active) return;
        this.active = active;
        if (active) { 
            onLoad(); 
            onShow();
            if (starryBackground != null) starryBackground.start();
        }
        else {
            onHide();
            if (starryBackground != null) starryBackground.stop();
        }
    }

    /**
     * Destroy.
     */
    public void destroy() {
        if (active) setActive(false);
        onUnload();
    }
}
