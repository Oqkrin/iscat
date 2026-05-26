package uni.gaben.iscat.iscat_screens.game.model;

import javafx.beans.property.*;

import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.camera.CameraModel;

/**
 * Modello dello stato globale di Gamenex.
 * Mantiene informazioni sul tempo di gioco, lo stato di pausa
 * e i riferimenti ai modelli principali dell'universo e della telecamera.
 */
public class GameModel {
    public static final double ONE_SECOND_IN_NANO_SECONDS = 1_000_000_000.0;
    public static final double ACCUMULATORUNIT = 0.25;

    private UniverseModel universeModel = new UniverseModel();
    private final CameraModel cameraModel = new CameraModel();

    private LongProperty lastUpdate = new SimpleLongProperty(0);
    private DoubleProperty dt = new SimpleDoubleProperty(0);
    private LongProperty now = new SimpleLongProperty(0);
    private LongProperty start = new SimpleLongProperty(-1);
    private DoubleProperty accumulator = new SimpleDoubleProperty(0);
    private BooleanProperty paused = new SimpleBooleanProperty(false);
    private BooleanProperty wave = new SimpleBooleanProperty(true);
    private IntegerProperty timer = new SimpleIntegerProperty(0);
    private final BooleanProperty gameOver = new SimpleBooleanProperty(false);

    private final DoubleProperty totalElapsedSeconds = new SimpleDoubleProperty(0.0);

    public DoubleProperty totalElapsedSecondsProperty() {
        return totalElapsedSeconds;
    }

    public double getTotalElapsedSeconds() {
        return totalElapsedSeconds.get();
    }

    public void setTotalElapsedSeconds(double value) {
        this.totalElapsedSeconds.set(value);
    }

    public BooleanProperty gameOverProperty() { return gameOver; }
    public boolean isGameOver() { return gameOver.get(); }
    public void setGameOver(boolean value) { gameOver.set(value); }


    public GameModel() {
        dt.bind(now.subtract(lastUpdate).divide(ONE_SECOND_IN_NANO_SECONDS));
    }

    public void setUniverseModel(UniverseModel universeModel) {
        this.universeModel = universeModel;
    }

    public UniverseModel getUniverseModel() {
        return universeModel;
    }

    public CameraModel getCameraModel() {
        return cameraModel;
    }

    public long getLastUpdate() {
        return lastUpdate.get();
    }

    public LongProperty lastUpdateProperty() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate.set(lastUpdate);
    }

    public double getDt() {
        return dt.get();
    }

    public DoubleProperty dtProperty() {
        return dt;
    }

    public long getNow() {
        return now.get();
    }

    public LongProperty nowProperty() {
        return now;
    }

    public void setNow(long now) {
        this.now.set(now);
    }

    public double getAccumulator() {
        return accumulator.get();
    }

    public DoubleProperty accumulatorProperty() {
        return accumulator;
    }

    public void setAccumulator(double accumulator) {
        this.accumulator.set(accumulator);
    }

    public boolean isPaused() {
        return paused.get();
    }

    public void setPaused(boolean p) {
        this.paused.set(p);
    }

    public BooleanProperty pausedProperty() {
        return paused;
    }

    public long getStart() {
        return start.get();
    }

    public LongProperty startProperty() {
        return start;
    }

    public void setStart(long start) {
        this.start.set(start);
    }

    public IntegerProperty timerProperty() {
        return timer;
    }

    public final int getTimer() {
        return timer.get();
    }

    public final void setTimer(int value) {
        this.timer.set(value);
    }

    public boolean isWaveing() {
        return wave.get();
    }
    public BooleanProperty waveProperty() {
        return wave;
    }
}
