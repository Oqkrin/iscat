package uni.gaben.iscat;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import uni.gaben.iscat.utils.ThemeColors;
import uni.gaben.iscat.utils.components.StarryBackgroundCanvas;

import java.io.IOException;

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
public abstract class AbstractIscatScene extends StackPane implements IscatSceneLifecycleInterface {

    // =========================================================================
    // Constants & Fields
    // =========================================================================

    private static final double CORNER_RADIUS = 16.0;

    private boolean active = false;
    private boolean initialized = false;
    private boolean barVisible = true;

    private StarryBackgroundCanvas starryBackground;

    // Riferimento al wrapper interno creato da buildChrome
    private final StackPane chromeRoot;

    // =========================================================================
    // Constructors
    // =========================================================================

    protected AbstractIscatScene(Parent root) {
        this(root, false);
    }

    protected AbstractIscatScene(Parent root, boolean withStarryBackground) {
        ThemeColors.ensureLoaded();
        this.chromeRoot = buildChrome(root, withStarryBackground);
        this.getChildren().add(chromeRoot);
        applyRoundedClip();
        if (withStarryBackground) {
            starryBackground = extractStarryBackground();
        }
    }

    // =========================================================================
    // Lifecycle & Initialization Pattern
    // =========================================================================

    /**
     * Inizializza la scena chiamando i metodi di costruzione nell'ordine corretto.
     */
    protected final void initialize() {
        if (initialized) return;
        initNodes();
        initStyles();
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

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void setActive(boolean active) {
        if (this.active == active) return;
        this.active = active;
        if (active) {
            onLoad();
            onShow();
            if (starryBackground != null) starryBackground.start();
        } else {
            onHide();
            if (starryBackground != null) starryBackground.stop();
        }
    }

    public void destroy() {
        if (active) setActive(false);
        onUnload();
    }

    // =========================================================================
    // Public / Package-Private API (Used by IscatController)
    // =========================================================================

    /**
     * Package-visible so IscatController can wire button actions and drag.
     */
    public IscatTitleBar getTitleBar() {
        if (chromeRoot.getChildren().get(0) instanceof StackPane chrome) {
            for (var child : chrome.getChildren()) {
                if (child instanceof IscatTitleBar tb) return tb;
            }
        }
        return null;
    }

    /**
     * Called by IscatController when the stage enters fullscreen.
     */
    public void onEnterFullscreen() {
        IscatTitleBar bar = getTitleBar();
        if (bar == null) return;

        bar.getStyleClass().add("title-bar-fullscreen");
        barVisible = true;
        slideOut(bar);

        this.setOnMouseMoved(e -> {
            if (e.getSceneY() < 8 && !barVisible) slideIn(bar);
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
        barVisible = true;
        bar.setTranslateY(0);
        bar.setOpacity(1.0);
    }

    // =========================================================================
    // Protected API & Helpers
    // =========================================================================

    /**
     * Returns the content root passed by the subclass to super().
     */
    @SuppressWarnings("unchecked")
    public StackPane getContentRoot() {
        if (chromeRoot.getChildren().get(0) instanceof StackPane chrome
                && chrome.getChildren().get(0) instanceof StackPane wrapper) {
            return wrapper;
        }
        return null;
    }

    protected StarryBackgroundCanvas getStarryBackground() {
        return starryBackground;
    }

    /**
     * Carica un FXML nella contentRoot e inietta il contentRoot nel controller
     * se implementa IscatFxmlController.
     */
    protected void initialize(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent fxmlContent = loader.load();

            if (loader.getController() instanceof IscatFxmlController controller) {
                controller.setContentRoot(getContentRoot());
            }

            if (fxmlContent instanceof Region region) {
                region.setMinSize(0, 0);
                StackPane contentRoot = getContentRoot();
                region.prefWidthProperty().bind(contentRoot.widthProperty());
                region.prefHeightProperty().bind(contentRoot.heightProperty());
            }

            StackPane contentRoot = getContentRoot();
            contentRoot.getChildren().add(fxmlContent);
            StackPane.setAlignment(fxmlContent, Pos.CENTER);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void fadeIn(Duration duration) {
        StackPane contentRoot = getContentRoot();
        if (contentRoot == null) return;

        contentRoot.setOpacity(0.0);
        FadeTransition fade = new FadeTransition(duration, contentRoot);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();
    }

    protected void fadeIn() {
        fadeIn(Duration.millis(300));
    }

    // =========================================================================
    // Private Helpers (Chrome & Animations)
    // =========================================================================

    private StackPane buildChrome(Parent content, boolean withStarryBackground) {
        StackPane contentWrapper = new StackPane();
        contentWrapper.getStyleClass().add(withStarryBackground ? "window-content-transparent" : "window-content");
        contentWrapper.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        contentWrapper.setMinSize(0, 0);

        if (withStarryBackground) {
            StarryBackgroundCanvas starryBg = new StarryBackgroundCanvas();
            starryBg.widthProperty().bind(contentWrapper.widthProperty());
            starryBg.heightProperty().bind(contentWrapper.heightProperty());
            starryBg.setMouseTransparent(true);
            contentWrapper.getChildren().add(starryBg);
        }

        contentWrapper.getChildren().add(content);

        Rectangle contentClip = new Rectangle();
        contentClip.widthProperty().bind(contentWrapper.widthProperty());
        contentClip.heightProperty().bind(contentWrapper.heightProperty());
        contentWrapper.setClip(contentClip);

        IscatTitleBar titleBar = new IscatTitleBar();
        titleBar.setMaxHeight(Region.USE_PREF_SIZE);
        titleBar.setMouseTransparent(false);

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
        if (chromeRoot.getChildren().get(0) instanceof StackPane chrome
                && chrome.getChildren().get(0) instanceof StackPane wrapper
                && !wrapper.getChildren().isEmpty()
                && wrapper.getChildren().get(0) instanceof StarryBackgroundCanvas bg) {
            return bg;
        }
        return null;
    }

    private void applyRoundedClip() {
        StackPane chrome = (StackPane) chromeRoot.getChildren().get(0);
        Rectangle clip = new Rectangle();
        clip.setArcWidth(CORNER_RADIUS * 2);
        clip.setArcHeight(CORNER_RADIUS * 2);
        clip.widthProperty().bind(chrome.widthProperty());
        clip.heightProperty().bind(chrome.heightProperty());
        chrome.setClip(clip);
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
        t.setToY(-bar.getHeight() - 4);
        FadeTransition f = new FadeTransition(Duration.millis(200), bar);
        f.setToValue(0.0);
        t.play();
        f.play();
    }
}