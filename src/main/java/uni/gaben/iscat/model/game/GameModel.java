package uni.gaben.iscat.model.game;

import javafx.beans.property.*;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.camera.CameraModel;

/**
 * Global game state for ISCAT.
 * Holds time state, wave flag, and references to universe & camera models.
 * <p>
 * Pause and game-over conditions are derived from {@link GameState} via
 * {@link #getGameState()} — there are no separate boolean flags for them.
 */
public class GameModel {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    public static final double ONE_SECOND_IN_NANOS = 1_000_000_000.0;
    public static final double ACCUMULATORUNIT     = 1d / 4d;

    // ------------------------------------------------------------------------
    // Time & game loop related properties
    // ------------------------------------------------------------------------
    private final DoubleProperty dt                  = new SimpleDoubleProperty(0);
    private final LongProperty   now                 = new SimpleLongProperty(0);
    private final LongProperty   start               = new SimpleLongProperty(-1);
    private final LongProperty   lastUpdate          = new SimpleLongProperty(0);
    private final DoubleProperty totalElapsedSeconds = new SimpleDoubleProperty(0.0);
    private final DoubleProperty timeScale = new SimpleDoubleProperty(1.0);

    // ------------------------------------------------------------------------
    // Game state
    // ------------------------------------------------------------------------
    private final BooleanProperty             waveActive = new SimpleBooleanProperty(true);
    private final ObjectProperty<GameState>   gameState  = new SimpleObjectProperty<>(GameState.PLAYING);

    // ------------------------------------------------------------------------
    // Misc UI counter (elapsed time rendered as HH:MM:SS)
    // ------------------------------------------------------------------------
    private final IntegerProperty timer = new SimpleIntegerProperty(0);

    // ------------------------------------------------------------------------
    // Core models
    // ------------------------------------------------------------------------
    private UniverseModel universeModel;
    private final CameraModel cameraModel = new CameraModel();

    public GameModel() {
        this.universeModel = new UniverseModel();
        // dt = (now - lastUpdate) / ONE_SECOND_IN_NANOS
        dt.bind(now.subtract(lastUpdate).divide(ONE_SECOND_IN_NANOS));
    }

    // Time & loop
    public double getDt()            { return dt.get(); }
    public DoubleProperty dtProperty() { return dt; }

    public long getNow()             { return now.get(); }
    public LongProperty nowProperty()  { return now; }
    public void setNow(long now)     { this.now.set(now); }

    public long getStart()           { return start.get(); }
    public LongProperty startProperty() { return start; }

    public long getLastUpdate()                     { return lastUpdate.get(); }
    public LongProperty lastUpdateProperty()         { return lastUpdate; }
    public void setLastUpdate(long lastUpdate)       { this.lastUpdate.set(lastUpdate); }

    public double getTotalElapsedSeconds()           { return totalElapsedSeconds.get(); }
    public DoubleProperty totalElapsedSecondsProperty() { return totalElapsedSeconds; }
    public void setTotalElapsedSeconds(double value) { totalElapsedSeconds.set(value); }

    // Game state
    public GameState getGameState()                           { return gameState.get(); }
    public ObjectProperty<GameState> gameStateProperty()      { return gameState; }
    public void setGameState(GameState state)                 { gameState.set(state); }

    /** Convenience delegate — equivalent to {@code getGameState().isPaused()}. */
    public boolean isPaused()   { return gameState.get().isPaused(); }

    /** Convenience delegate — equivalent to {@code getGameState() == GameState.GAME_OVER}. */
    public boolean isGameOver() { return gameState.get() == GameState.GAME_OVER; }

    public boolean isWaveActive()                    { return waveActive.get(); }
    public BooleanProperty waveActiveProperty()       { return waveActive; }
    public void setWaveActive(boolean active)         { waveActive.set(active); }

    // Misc
    public int getTimer()              { return timer.get(); }
    public IntegerProperty timerProperty() { return timer; }
    public void setTimer(int value)    { timer.set(value); }


    public double getTimeScale() { return timeScale.get(); }
    public DoubleProperty timeScaleProperty() { return timeScale; }
    public void setTimeScale(double scale) { timeScale.set(scale); }

    // Models
    public UniverseModel getUniverseModel() { return universeModel; }
    public CameraModel   getCameraModel()   { return cameraModel; }

    public void resetUniverse() {
        this.universeModel = new UniverseModel();
    }
}