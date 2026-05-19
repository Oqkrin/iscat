package uni.gaben.iscat.game.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;

import uni.gaben.iscat.game.universe.UniverseModel;
import uni.gaben.iscat.game.view.camera.CameraModel;

/**
 * Modello dello stato globale di Gamenex.
 * Mantiene informazioni sul tempo di gioco, lo stato di pausa
 * e i riferimenti ai modelli principali dell'universo e della telecamera.
 */
public class GameModel {
    public static final double ONE_SECOND_IN_NANO_SECONDS = 1_000_000_000.0;
    public static final double ACCUMULATORUNIT = 0.25;

    private UniverseModel universeModel;
    private final CameraModel cameraModel;

    private LongProperty lastUpdate = new SimpleLongProperty(0);
    private DoubleProperty dt = new SimpleDoubleProperty(0);
    private LongProperty now = new SimpleLongProperty(0);
    private DoubleProperty accumulator = new SimpleDoubleProperty(0);
    private BooleanProperty paused = new SimpleBooleanProperty(false);

    public GameModel(UniverseModel universeModel, CameraModel cameraModel) {
        this.universeModel = universeModel;
        this.cameraModel = cameraModel;
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
        return !paused.get();
    }

    public void setPaused(boolean p) {
        this.paused.set(p);
    }

    public BooleanProperty pausedProperty() {
        return paused;
    }
}
