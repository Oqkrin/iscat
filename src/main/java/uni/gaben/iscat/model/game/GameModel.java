package uni.gaben.iscat.model.game;

import javafx.beans.property.*;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.camera.CameraModel;

/**
 * Global game state for Gamenex.
 * Holds time state, pause/over flags, and references to universe & camera models.
 */
public class GameModel {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
        public static final double ONE_SECOND_IN_NANOS = 1_000_000_000.0;
        public static final double ACCUMULATORUNIT = 1d/4d;

    // ------------------------------------------------------------------------
    // Time & game loop related properties
    // ------------------------------------------------------------------------
    private final DoubleProperty dt = new SimpleDoubleProperty(0);
    private final LongProperty now = new SimpleLongProperty(0);
    private final LongProperty start = new SimpleLongProperty(-1);
    private final DoubleProperty accumulator = new SimpleDoubleProperty(0);
    private final LongProperty lastUpdate = new SimpleLongProperty(0);
    private final DoubleProperty totalElapsedSeconds = new SimpleDoubleProperty(0.0);

    // ------------------------------------------------------------------------
    // Game state flags
    // ------------------------------------------------------------------------
    private final BooleanProperty paused = new SimpleBooleanProperty(false);
    private final BooleanProperty waveActive = new SimpleBooleanProperty(true);
    private final BooleanProperty gameOver = new SimpleBooleanProperty(false);
    private final ObjectProperty<GameState> gameState = new SimpleObjectProperty<>(GameState.PLAYING);

    // ------------------------------------------------------------------------
    // Misc UI counter (e.g. countdown before wave, etc.)
    // ------------------------------------------------------------------------
    private final IntegerProperty timer = new SimpleIntegerProperty(0);

    // ------------------------------------------------------------------------
    // Core models
    // ------------------------------------------------------------------------
    private UniverseModel universeModel;
    private final CameraModel cameraModel = new CameraModel();

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------
    public GameModel() {
        this.universeModel = new UniverseModel();
        // dt = (now - lastUpdate) / ONE_SECOND_IN_NANOS
        dt.bind(now.subtract(lastUpdate).divide(ONE_SECOND_IN_NANOS));
    }

    // Time & loop
    public double getDt() { return dt.get(); }
    public DoubleProperty dtProperty() { return dt; }

    public long getNow() { return now.get(); }
    public LongProperty nowProperty() { return now; }
    public void setNow(long now) { this.now.set(now); }

    public long getStart() { return start.get(); }
    public LongProperty startProperty() { return start; }
    public void setStart(long start) { this.start.set(start); }

    public double getAccumulator() { return accumulator.get(); }
    public DoubleProperty accumulatorProperty() { return accumulator; }
    public void setAccumulator(double accumulator) { this.accumulator.set(accumulator); }

    public long getLastUpdate() { return lastUpdate.get(); }
    public LongProperty lastUpdateProperty() { return lastUpdate; }
    public void setLastUpdate(long lastUpdate) { this.lastUpdate.set(lastUpdate); }

    public double getTotalElapsedSeconds() { return totalElapsedSeconds.get(); }
    public DoubleProperty totalElapsedSecondsProperty() { return totalElapsedSeconds; }
    public void setTotalElapsedSeconds(double value) { totalElapsedSeconds.set(value); }

    // Game state flags
    public boolean isPaused() { return paused.get(); }
    public BooleanProperty pausedProperty() { return paused; }
    public void setPaused(boolean paused) { this.paused.set(paused); }

    public boolean isWaveActive() { return waveActive.get(); }
    public BooleanProperty waveActiveProperty() { return waveActive; }
    public void setWaveActive(boolean active) { this.waveActive.set(active); }

    public boolean isGameOver() { return gameOver.get(); }
    public BooleanProperty gameOverProperty() { return gameOver; }
    public void setGameOver(boolean gameOver) { this.gameOver.set(gameOver); }

    public GameState getGameState() { return gameState.get(); }
    public ObjectProperty<GameState> gameStateProperty() { return gameState; }
    public void setGameState(GameState state) { gameState.set(state); }

    // Misc
    public int getTimer() { return timer.get(); }
    public IntegerProperty timerProperty() { return timer; }
    public void setTimer(int value) { timer.set(value); }

    // Models
    public UniverseModel getUniverseModel() { return universeModel; }
    public CameraModel getCameraModel() { return cameraModel; }

    public void resetUniverse() {
        universeModel = new UniverseModel();
    }
}