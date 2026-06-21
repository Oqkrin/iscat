package uni.gaben.iscat.model.game;

import javafx.beans.property.*;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.camera.CameraModel;

/**
 * Modello contenitore dello stato globale e dei dati di sessione del gioco (ISCAT).
 * Custodisce le proprietà temporali reattive utilizzate dal loop principale, i flag di progressione
 * delle ondate e i riferimenti diretti ai sotto-modelli fisici dell'universo e della telecamera.
 * <p>
 * Le condizioni di pausa e di fine partita sono derivate dinamicamente dallo stato atomico
 * {@link GameState} tramite la proprietà {@link #gameStateProperty()}, evitando la ridondanza
 * di flag booleani separati.
 */
public class GameModel {

    // ------------------------------------------------------------------------
    // Costanti
    // ------------------------------------------------------------------------

    /** Fattore di conversione per trasformare un valore espresso in nanosecondi nel corrispettivo in secondi. */
    public static final double ONE_SECOND_IN_NANOS = 1_000_000_000.0;

    /** Unità di campionamento massima ammessa per l'accumulatore del delta time per preservare la stabilità fisica. */
    public static final double ACCUMULATORUNIT     = 1d / 4d;

    // ------------------------------------------------------------------------
    // Proprietà temporali e ciclo di gioco (JavaFX Properties)
    // ------------------------------------------------------------------------

    /** Proprietà calcolata che esprime il tempo delta (dt) trascorso tra gli ultimi due frame, in secondi. */
    private final DoubleProperty dt                  = new SimpleDoubleProperty(0);

    /** Timestamp in nanosecondi del frame corrente. */
    private final LongProperty   now                 = new SimpleLongProperty(0);

    /** Timestamp in nanosecondi del primo frame registrato all'avvio del loop di gioco. */
    private final LongProperty   start               = new SimpleLongProperty(-1);

    /** Timestamp in nanosecondi del frame immediatamente precedente a quello corrente. */
    private final LongProperty   lastUpdate          = new SimpleLongProperty(0);

    /** Accumulatore decimale del tempo totale effettivo di gameplay attivo trascorso, in secondi. */
    private final DoubleProperty totalElapsedSeconds = new SimpleDoubleProperty(0.0);

    /** Moltiplicatore della velocità del tempo di gioco (es. per effetti di slow-motion o accelerazione). */
    private final DoubleProperty timeScale = new SimpleDoubleProperty(1.0);

    // ------------------------------------------------------------------------
    // Stato del Gioco
    // ------------------------------------------------------------------------

    /** Flag reattivo che indica se un'ondata di nemici è attualmente attiva all'interno dell'arena. */
    private final BooleanProperty             waveActive = new SimpleBooleanProperty(true);

    /** Proprietà osservabile che definisce lo stato vitale e macroscopico corrente della sessione (es. PLAYING, PAUSE, WIN). */
    private final ObjectProperty<GameState>   gameState  = new SimpleObjectProperty<>(GameState.PLAYING);

    // ------------------------------------------------------------------------
    // Contatori Interfaccia Utente
    // ------------------------------------------------------------------------

    /** Contatore intero codificato nel formato HHMMSS per facilitare il binding testuale del cronometro a schermo. */
    private final IntegerProperty timer = new SimpleIntegerProperty(0);

    // ------------------------------------------------------------------------
    // Modelli Core dell'Universo
    // ------------------------------------------------------------------------

    /** Riferimento al modello logico e fisico dell'universo di gioco (mappa, entità, starfield). */
    private UniverseModel universeModel;

    /** Riferimento al modello geometrico di gestione dell'inquadratura e degli spostamenti della telecamera. */
    private final CameraModel cameraModel = new CameraModel();

    /**
     * Costruisce il modello di gioco globale inizializzando l'universo di default e applicando
     * un legame matematico (binding) per calcolare costantemente il delta time in base alla formula:
     * $$dt = \frac{now - lastUpdate}{ONE\_SECOND\_IN\_NANOS}$$
     */
    public GameModel() {
        this.universeModel = new UniverseModel();
        dt.bind(now.subtract(lastUpdate).divide(ONE_SECOND_IN_NANOS));
    }

    /** @return Il valore corrente del delta time in secondi. */
    public double getDt()            { return dt.get(); }
    /** @return La proprietà {@link DoubleProperty} associata al delta time. */
    public DoubleProperty dtProperty() { return dt; }

    /** @return Il timestamp in nanosecondi del frame corrente. */
    public long getNow()             { return now.get(); }
    /** @return La proprietà {@link LongProperty} del timestamp corrente. */
    public LongProperty nowProperty()  { return now; }
    /** @param now Il timestamp in nanosecondi da impostare per il frame corrente. */
    public void setNow(long now)     { this.now.set(now); }

    /** @return Il timestamp iniziale in nanosecondi della sessione di gioco. */
    public long getStart()           { return start.get(); }
    /** @return La proprietà {@link LongProperty} del timestamp di avvio. */
    public LongProperty startProperty() { return start; }

    /** @return Il timestamp in nanosecondi del frame precedente. */
    public long getLastUpdate()                     { return lastUpdate.get(); }
    /** @return La proprietà {@link LongProperty} del timestamp del frame precedente. */
    public LongProperty lastUpdateProperty()         { return lastUpdate; }
    /** @param lastUpdate Il timestamp in nanosecondi da impostare come riferimento precedente. */
    public void setLastUpdate(long lastUpdate)       { this.lastUpdate.set(lastUpdate); }

    /** @return Il quantitativo complessivo di secondi di gioco attivo trascorsi. */
    public double getTotalElapsedSeconds()           { return totalElapsedSeconds.get(); }
    /** @return La proprietà {@link DoubleProperty} del tempo totale accumulato. */
    public DoubleProperty totalElapsedSecondsProperty() { return totalElapsedSeconds; }
    /** @param value Il valore in secondi da assegnare al tempo trascorso totale. */
    public void setTotalElapsedSeconds(double value) { totalElapsedSeconds.set(value); }

    /** @return Lo stato logico macroscopico corrente della partita. */
    public GameState getGameState()                           { return gameState.get(); }
    /** @return La proprietà osservabile {@link ObjectProperty} associata allo stato del gioco. */
    public ObjectProperty<GameState> gameStateProperty()      { return gameState; }
    /** @param state Il nuovo {@link GameState} da applicare alla sessione. */
    public void setGameState(GameState state)                 { gameState.set(state); }

    /**
     * Metodo delegato di convenienza. Verifica se il gioco si trova in uno stato di sospensione.
     * * @return {@code true} se lo stato corrente è in pausa, {@code false} altrimenti.
     */
    public boolean isPaused()   { return gameState.get().isPaused(); }

    /**
     * Metodo delegato di convenienza. Verifica se la sessione si è conclusa con la sconfitta del giocatore.
     * * @return {@code true} se lo stato è identificato come Game Over, {@code false} altrimenti.
     */
    public boolean isGameOver() { return gameState.get() == GameState.GAME_OVER; }

    /** @return {@code true} se vi è un'ondata di nemici attualmente in corso, {@code false} altrimenti. */
    public boolean isWaveActive()                    { return waveActive.get(); }
    /** @return La proprietà {@link BooleanProperty} legata alla presenza di ondate attive. */
    public BooleanProperty waveActiveProperty()       { return waveActive; }
    /** @param active {@code true} per forzare lo stato di ondata attiva, {@code false} altrimenti. */
    public void setWaveActive(boolean active)         { waveActive.set(active); }

    /** @return Il valore codificato del timer per la UI. */
    public int getTimer()              { return timer.get(); }
    /** @return La proprietà {@link IntegerProperty} del cronometro HHMMSS. */
    public IntegerProperty timerProperty() { return timer; }
    /** @param value L'intero codificato da assegnare al cronometro visivo. */
    public void setTimer(int value)    { timer.set(value); }

    /** @return Il valore del moltiplicatore della velocità del tempo. */
    public double getTimeScale() { return timeScale.get(); }
    /** @return La proprietà {@link DoubleProperty} legata alla scala temporale. */
    public DoubleProperty timeScaleProperty() { return timeScale; }
    /** @param scale Il nuovo fattore di scala temporale da applicare (es. {@code 0.5} per dimezzare la velocità). */
    public void setTimeScale(double scale) { timeScale.set(scale); }

    /** @return L'istanza corrente del modello dell'universo astronomico e fisico. */
    public UniverseModel getUniverseModel() { return universeModel; }

    /** @return L'istanza del modello geometrico di controllo della telecamera di gioco. */
    public CameraModel   getCameraModel()   { return cameraModel; }

    /**
     * Ripristina da zero il modello dell'universo sovrascrivendo l'istanza precedente con un
     * nuovo oggetto {@link UniverseModel} vuoto, pronto per una nuova inizializzazione.
     */
    public void resetUniverse() {
        this.universeModel = new UniverseModel();
    }
}