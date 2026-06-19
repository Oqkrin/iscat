package uni.gaben.iscat.view.components;

import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import uni.gaben.iscat.controller.IscatFxmlController;
import uni.gaben.iscat.model.IscatViewLifecycle;

import java.io.IOException;

/**
 * Classe base per scene ISCAT — responsabilità SOLO del contenuto interno della vista.
 * <p>
 * Non si occupa più della generazione strutturale di bordi, clip e titlebar.
 * Esegue facoltativamente il rendering dello sfondo stellato come layer inferiore diretto.
 * <p>
 * Pattern di inizializzazione :
 * initStyles → initNodes → initLayout → initBindings → initEventHandlers → initAnimations
 */
public abstract class AbstractIscatStackPane extends StackPane implements IscatViewLifecycle {

    private boolean active = false;
    private boolean initialized = false;
    private StarryBackgroundCanvas starryBackground;

    protected AbstractIscatStackPane() {
        this(null, false);
    }

    protected AbstractIscatStackPane(Parent root) {
        this(root, false);
    }

    protected AbstractIscatStackPane(Parent root, boolean withStarryBackground) {
        // Applica gli stili di base del pannello di contenuto
        this.getStyleClass().add(withStarryBackground ? "window-content-transparent" : "window-content");
        this.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        this.setMinSize(0, 0);

        // Se lo sfondo stellato è richiesto, viene iniettato sul fondo del layout di questa vista
        if (withStarryBackground) {
            starryBackground = new StarryBackgroundCanvas();
            starryBackground.widthProperty().bind(this.widthProperty());
            starryBackground.heightProperty().bind(this.heightProperty());
            starryBackground.setMouseTransparent(true);
            this.getChildren().add(starryBackground);
        }

        if (root != null) {
            this.getChildren().add(root);
            StackPane.setAlignment(root, Pos.CENTER);
        }
    }

    // =========================================================================
    // Alterable & Initialization Pattern
    // =========================================================================

    public final void initialize() {
        if (initialized) return;
        initNodes();
        initStyles();
        initLayout();
        initBindings();
        initEventHandlers();
        initAnimations();
        initialized = true;
    }

    protected void initStyles() {}
    protected void initNodes() {}
    protected void initLayout() {}
    protected void initBindings() {}
    protected void initEventHandlers() {}
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

    @Override
    public void onShow() {
        if (starryBackground != null) {
            starryBackground.setFollowMouse(true);
            this.setOnMouseMoved(e -> starryBackground.updateMousePosition(e.getSceneX(), e.getSceneY()));
        }
        fadeIn();
    }

    @Override
    public void onHide() {
        this.setOnMouseMoved(null);
        if (starryBackground != null) {
            starryBackground.setFollowMouse(false);
        }
    }

    // =========================================================================
    // Protected API & Helpers
    // =========================================================================

    /**
     * Il componente stesso funge da ContentRoot nelle gerarchie delle sottoclassi.
     */
    public StackPane getViewRootPointer() {
        return this;
    }

    protected StarryBackgroundCanvas getStarryBackground() {
        return starryBackground;
    }

    /**
     * Carica un FXML all'interno di questo StackPane e inietta il riferimento al controller FXML.
     */
    protected void initialize(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent fxmlContent = loader.load();

            if (loader.getController() instanceof IscatFxmlController controller) {
                controller.setPointerToView(this);
            }


            if (fxmlContent instanceof Region region) {
                region.setMinSize(0, 0);
                region.prefWidthProperty().bind(this.widthProperty());
                region.prefHeightProperty().bind(this.heightProperty());
            }


            this.getChildren().add(fxmlContent);
            StackPane.setAlignment(fxmlContent, Pos.CENTER);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void fadeIn(Duration duration) {
        this.setOpacity(0.0);
        FadeTransition fade = new FadeTransition(duration, this);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();
    }

    protected void fadeIn() {
        fadeIn(Duration.millis(300));
    }

    protected <C> StackPane loadFxml(String path, java.util.function.Consumer<C> init) {
        try {
            var loader = new FXMLLoader(getClass().getResource(path));
            StackPane view = loader.load();
            C controller = loader.getController();
            if (controller instanceof IscatFxmlController c) c.setPointerToView(this);
            init.accept(controller);
            return view;
        } catch (IOException e) {
            throw new RuntimeException("Errore fatale: impossibile caricare " + path, e);
        }
    }
}