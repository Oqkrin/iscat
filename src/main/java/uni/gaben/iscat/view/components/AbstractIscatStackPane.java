package uni.gaben.iscat.view.components;

import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import uni.gaben.iscat.controller.interfaces.IscatFxmlController;
import uni.gaben.iscat.model.IscatViewLifecycle;

import java.io.IOException;

/**
 * Classe base astratta per le schermate e le viste grafiche di ISCAT.
 * <p>
 * Ha la responsabilità esclusiva della gestione del contenuto interno della vista.
 * Non si occupa della decorazione strutturale esterna (bordi, clip o barre del titolo),
 * ma gestisce opzionalmente il rendering di uno sfondo stellato dinamico come layer inferiore.
 * </p>
 * <p>
 * <b>Pattern di inizializzazione rigoroso:</b><br>
 * {@code initNodes() -> initStyles() -> initLayout() -> initBindings() -> initEventHandlers() -> initAnimations()}
 * </p>
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

    /**
     * Costruttore completo. Configura le proprietà di ridimensionamento del pannello
     * e inizializza l'eventuale sfondo stellato interattivo.
     *
     * @param root                 Nodo radice opzionale da inserire immediatamente nel layout.
     * @param withStarryBackground Se {@code true}, istanzia e aggancia lo sfondo stellato dinamico.
     */
    protected AbstractIscatStackPane(Parent root, boolean withStarryBackground) {
        // Applica le classi CSS di stile in base alla trasparenza richiesta dallo sfondo
        this.getStyleClass().add(withStarryBackground ? "window-content-transparent" : "window-content");
        this.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        this.setMinSize(0, 0);

        // Configurazione e binding bidimensionale dello sfondo stellato
        if (withStarryBackground) {
            starryBackground = new StarryBackgroundCanvas();
            starryBackground.widthProperty().bind(this.widthProperty());
            starryBackground.heightProperty().bind(this.heightProperty());
            starryBackground.setMouseTransparent(true); // Evita di intercettare i click destinati alla UI
            this.getChildren().add(starryBackground);
        }

        if (root != null) {
            this.getChildren().add(root);
            StackPane.setAlignment(root, Pos.CENTER);
        }
    }

    /**
     * Inizializza la vista eseguendo la pipeline sequenziale dei metodi protetti.
     * Garantisce l'esecuzione atomica e singola tramite flag di controllo.
     */
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
            if (starryBackground != null) {
                starryBackground.start();
            }
        } else {
            onHide();
            if (starryBackground != null) {
                starryBackground.stop();
            }
        }
    }

    /**
     * Distrugge la vista corrente, arrestando i thread o i timer attivi.
     */
    public void destroy() {
        if (active) {
            setActive(false);
        }
        onUnload();
    }

    @Override
    public void onShow() {
        if (starryBackground != null) {
            starryBackground.setFollowMouse(true);
            // Sincronizza lo spostamento del mouse per l'effetto di parallasse delle stelle
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

    /**
     * Restituisce il puntatore al contenitore radice di questa vista.
     */
    public StackPane getViewRootPointer() {
        return this;
    }

    protected StarryBackgroundCanvas getStarryBackground() {
        return starryBackground;
    }

    /**
     * Carica un file FXML all'interno di questo StackPane e assegna il riferimento
     * di questa vista al rispettivo controller JavaFX.
     *
     * @param fxmlPath Il percorso relativo della risorsa FXML (es. "/fxml/menu.fxml").
     */
    protected void initialize(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent fxmlContent = loader.load();

            // Sincronizza il controller con la vista accoppiata
            if (loader.getController() instanceof IscatFxmlController controller) {
                controller.setPointerToView(this);
            }

            // Forza il responsive binding del layout caricato dall'FXML
            if (fxmlContent instanceof Region region) {
                region.setMinSize(0, 0);
                region.prefWidthProperty().bind(this.widthProperty());
                region.prefHeightProperty().bind(this.heightProperty());
            }

            this.getChildren().add(fxmlContent);
            StackPane.setAlignment(fxmlContent, Pos.CENTER);

        } catch (IOException e) {
            System.err.println("[AbstractIscatStackPane] Errore critico nel caricamento dell'FXML: " + fxmlPath);
            e.printStackTrace();
        }
    }

    /**
     * Esegue una transizione di dissolvenza in entrata (Fade In) con durata personalizzabile.
     */
    protected void fadeIn(Duration duration) {
        this.setOpacity(0.0);
        FadeTransition fade = new FadeTransition(duration, this);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);
        fade.play();
    }

    /**
     * Esegue una transizione di dissolvenza in entrata standard (300 millisecondi).
     */
    protected void fadeIn() {
        fadeIn(Duration.millis(300));
    }

    /**
     * Helper avanzato per il caricamento in linea di componenti FXML con configurazione immediata del controller.
     */
    protected <C> StackPane loadFxml(String path, java.util.function.Consumer<C> init) {
        try {
            var loader = new FXMLLoader(getClass().getResource(path));
            StackPane view = loader.load();
            C controller = loader.getController();

            if (controller instanceof IscatFxmlController c) {
                c.setPointerToView(this);
            }

            init.accept(controller);
            return view;
        } catch (IOException e) {
            throw new RuntimeException("Errore fatale: impossibile caricare e iniettare l'asset FXML: " + path, e);
        }
    }
}