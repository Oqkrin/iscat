package uni.gaben.iscat.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * Modello principale dell'applicazione che rappresenta lo stato globale condiviso.
 * <p>
 * Questa classe gestisce i parametri di stato della finestra principale, come la modalità
 * a schermo intero e il blocco della finestra (pin), oltre a orchestrare il meccanismo di
 * navigazione reattiva e le transizioni visive tra le diverse viste dell'applicazione.
 */
public class IscatModel {

    /**
     * Tipologie di transizione visiva utilizzabili durante il cambio di scena.
     */
    public enum TransitionType {
        /** Cambio di vista immediato senza effetti visivi. */
        INSTANT,
        /** Dissolvenza incrociata tra la vecchia e la nuova vista. */
        FADE
    }

    /** Proprietà reattiva contenente la vista (scena) attualmente attiva nell'applicazione. */
    private final ObjectProperty<IscatViews> currentScene = new SimpleObjectProperty<>(IscatViews.LOGIN_MENU);

    /** Proprietà reattiva che memorizza il tipo di transizione da applicare al prossimo cambio scena. */
    private final ObjectProperty<TransitionType> pendingTransition = new SimpleObjectProperty<>(TransitionType.INSTANT);

    /** Proprietà booleana che traccia se la finestra deve rimanere in primo piano (pinned). */
    private final BooleanProperty pinned = new SimpleBooleanProperty(false);

    /** Proprietà booleana che traccia l'attivazione della modalità a schermo intero (fullscreen). */
    private final BooleanProperty fullscreen = new SimpleBooleanProperty(false);

    /**
     * Ritorna il valore corrente della vista attualmente visualizzata.
     *
     * @return L'istanza di {@link IscatViews} attiva.
     */
    public IscatViews getCurrentScene() { return currentScene.get(); }

    /**
     * Ritorna la tipologia di transizione programmata per il cambio di scena in corso.
     *
     * @return Il {@link TransitionType} in attesa di esecuzione.
     */
    public TransitionType getPendingTransition() { return pendingTransition.get(); }

    /**
     * Ritorna la proprietà JavaFX associata allo stato di schermo intero.
     *
     * @return La {@link BooleanProperty} del fullscreen per il binding.
     */
    public BooleanProperty fullscreenProperty() { return fullscreen; }

    /**
     * Verifica se l'applicazione è visualizzata a schermo intero.
     *
     * @return {@code true} se la modalità fullscreen è attiva, {@code false} altrimenti.
     */
    public boolean isFullscreen() { return fullscreen.get(); }

    /**
     * Imposta lo stato della modalità a schermo intero dell'applicazione.
     *
     * @param value {@code true} per attivare lo schermo intero, {@code false} per disattivarlo.
     */
    public void setFullscreen(boolean value) { this.fullscreen.set(value); }

    /**
     * Cambia la vista corrente dell'applicazione programmando la relativa transizione visiva.
     * I componenti in ascolto sulla proprietà della scena verranno notificati reattivamente.
     *
     * @param scene La destinazione di tipo {@link IscatViews} verso cui navigare.
     * @param type  Il tipo di effetto visivo {@link TransitionType} da applicare durante il cambio.
     */
    public void navigate(IscatViews scene, TransitionType type) {
        this.pendingTransition.set(type);
        this.currentScene.set(scene);
    }

    /**
     * Ritorna la proprietà JavaFX associata alla vista corrente.
     *
     * @return La {@link ObjectProperty} avvolgente l'istanza di {@link IscatViews} per il binding.
     */
    public ObjectProperty<IscatViews> currentSceneProperty() { return currentScene; }

    /**
     * Verifica se la finestra dell'applicazione è bloccata in primo piano.
     *
     * @return {@code true} se la finestra è bloccata (pinned), {@code false} altrimenti.
     */
    public boolean isPinned() { return pinned.get(); }

    /**
     * Imposta lo stato di blocco in primo piano della finestra dell'applicazione.
     *
     * @param value {@code true} per forzare la finestra in primo piano, {@code false} altrimenti.
     */
    public void setPinned(boolean value) { pinned.set(value); }

    /**
     * Ritorna la proprietà JavaFX associata allo stato di primo piano della finestra.
     *
     * @return La {@link BooleanProperty} del pin per il binding.
     */
    public BooleanProperty pinnedProperty() { return pinned; }
}