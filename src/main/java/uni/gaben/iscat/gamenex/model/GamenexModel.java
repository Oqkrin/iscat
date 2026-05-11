package uni.gaben.iscat.gamenex.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;

/**
 * Modello dello stato globale di Gamenex.
 * Mantiene informazioni sul tempo di gioco, lo stato di pausa
 * e i riferimenti ai controller principali.
 */
public class GamenexModel {

    public static final double NANOSECUNIT = 1_000_000_000.0;
    public static final double TICKUNIT = 1.0 / 60.0;
    public static final double ACCUMULATORUNIT = 0.25;

    private LongProperty lastUpdate = new SimpleLongProperty(0);
    private DoubleProperty dt = new SimpleDoubleProperty(0);
    private LongProperty now = new SimpleLongProperty(0);
    private DoubleProperty accumulator = new SimpleDoubleProperty(0);
    private BooleanProperty paused = new SimpleBooleanProperty(false);

    public GamenexModel() {
        dt.bind(now.subtract(lastUpdate).divide(NANOSECUNIT));
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

    public javafx.beans.property.BooleanProperty pausedProperty() {
        return paused;
    }
}
