package uni.gaben.iscat;

import javafx.scene.Scene;

/**
 * Classe base per scene ISCAT con gestione del ciclo di vita e pattern di inizializzazione.
 * 
 * Ogni scena segue questo pattern:
 * 1. initStyles() - carica CSS e configura stili
 * 2. initNodes() - crea componenti UI
 * 3. initLayout() - organizza componenti nel layout
 * 4. initBindings() - collega model a view
 * 5. initEventHandlers() - registra event handlers
 * 6. initAnimations() - configura animazioni (opzionale)
 * 
 * Il costruttore chiama questi metodi in ordine, ma NON avvia processi attivi.
 * I processi attivi (game loop, timer, thread) vengono avviati in onShow().
 */
public abstract class IscatSceneAbstract extends Scene implements IscatSceneLifecycleInterface {
    
    private boolean active = false;
    private boolean initialized = false;
    
    /**
     * Costruttore base.
     * Le sottoclassi devono passare il root node.
     */
    protected IscatSceneAbstract(javafx.scene.Parent root) {
        super(root);
    }
    
    /**
     * Costruttore con dimensioni.
     */
    protected IscatSceneAbstract(javafx.scene.Parent root, double width, double height) {
        super(root, width, height);
    }
    
    /**
     * Inizializza la scena seguendo il pattern standard.
     * Chiamato automaticamente dal costruttore delle sottoclassi.
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
    
    // --- Pattern di inizializzazione (da sovrascrivere nelle sottoclassi) ---
    
    /**
     * Fase 1: Carica CSS e configura stili base.
     * Esempio: getStylesheets().add(cssUrl)
     */
    protected abstract void initStyles();
    
    /**
     * Fase 2: Crea tutti i nodi UI (Label, Button, TextField, ecc.).
     * NON organizzarli ancora nel layout.
     */
    protected abstract void initNodes();
    
    /**
     * Fase 3: Organizza i nodi nel layout (VBox, HBox, StackPane, ecc.).
     * Configura allineamenti, spacing, padding.
     */
    protected abstract void initLayout();
    
    /**
     * Fase 4: Collega properties del model alle properties della view.
     * Esempio: label.textProperty().bind(model.nameProperty())
     */
    protected abstract void initBindings();
    
    /**
     * Fase 5: Registra event handlers (click, key press, ecc.).
     * Esempio: button.setOnAction(controller::handleClick)
     */
    protected abstract void initEventHandlers();
    
    /**
     * Fase 6 (opzionale): Configura animazioni UI (fade, slide, ecc.).
     * NON avviare processi attivi qui - usa onShow() per quello.
     */
    protected void initAnimations() {
        // Default: nessuna animazione
    }
    
    // --- Ciclo di vita IscatScene ---
    
    @Override
    public void onLoad() {
        // Default: nessuna azione
        // Sovrascrivere per caricare risorse (sprite, audio, dati)
    }
    
    @Override
    public void onShow() {
        // Default: nessuna azione
        // Sovrascrivere per avviare processi attivi (game loop, timer, thread)
    }
    
    @Override
    public void onHide() {
        // Default: nessuna azione
        // Sovrascrivere per fermare processi attivi
    }
    
    @Override
    public void onUnload() {
        // Default: nessuna azione
        // Sovrascrivere per rilasciare risorse
    }
    
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
        } else {
            onHide();
        }
    }
    
    /**
     * Chiamato quando la scena viene completamente distrutta.
     * Da chiamare manualmente quando non serve più.
     */
    public void destroy() {
        if (active) {
            setActive(false);
        }
        onUnload();
    }
}
