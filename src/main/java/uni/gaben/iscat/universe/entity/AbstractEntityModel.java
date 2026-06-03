package uni.gaben.iscat.universe.entity;

import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.AABB;
import org.dyn4j.geometry.Transform;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.utils.Updatable;

import java.util.function.Consumer;

/**
 * Rappresentazione astratta di un'entità nel mondo fisico.
 * Calcola in modo dinamico le dimensioni basandosi sulle Fixture fisiche attive.
 */
public abstract class AbstractEntityModel extends Body implements HasTerminalVelocity, Updatable {
    private String entityId;
    private int state = 0;
    private double terminalVelocity = Double.MAX_VALUE;
    private double baseAccelerationPerTick = Double.MAX_VALUE;
    private boolean shouldRemove = false;

    // Callback di notifica per le collisioni intercettate
    private Consumer<AbstractEntityModel> collisionCallback;

    protected AbstractEntityModel(double x, double y) {
        super();
        this.setUserData(this);
        translate(UU.pxToM(x), UU.pxToM(y));
    }

    private double statetime = 0.0;

    /**
     * Returns a unique identifier for this entity type.
     * For GenericEntityModel, this is the database entityKey.
     * For other entities, this is the simple class name.
     */
    public String getEntityKey() {
        // Override in subclasses if they have a specific key
        return this.getClass().getSimpleName();
    }

    /**
     * Permette a un agente esterno (es. Controller) di definire cosa succede all'impatto.
     */
    public void setOnCollision(Consumer<AbstractEntityModel> callback) {
        this.collisionCallback = callback;
    }

    /**
     * Innesca la logica di notifica associata a questa entità.
     */
    public void triggerCollision(AbstractEntityModel other) {
        if (collisionCallback != null) {
            collisionCallback.accept(other);
        }
    }

    /** Calcola la larghezza totale racchiusa dalla shape di collisione (in Metri) */
    public double getWidthMeters() {
        if (getFixtureCount() == 0) return 0.0;
        AABB aabb = createAABB(new Transform());
        return aabb.getWidth();
    }

    /** Calcola l'altezza totale racchiusa dalla shape di collisione (in Metri) */
    public double getHeightMeters() {
        if (getFixtureCount() == 0) return 0.0;
        AABB aabb = createAABB(new Transform());
        return aabb.getHeight();
    }

    /** Helper pratico per estrarre la larghezza calcolata in Pixel */
    public double getWidthPx() { return UU.mToPx(getWidthMeters()); }

    /** Helper pratico per estrarre l'altezza calcolata in Pixel */
    public double getHeightPx() { return UU.mToPx(getHeightMeters()); }

    public String getEntityId() { return entityId; }
    public void setEntityId(String id) { this.entityId = id; }
    
    @Override public double getTerminalVelocity() { return terminalVelocity; }
    @Override public double getBaseAccelerationPerTick() { return baseAccelerationPerTick; }
    public boolean shouldRemove() { return shouldRemove; }
    public void setShouldRemove(boolean shouldRemove) { this.shouldRemove = shouldRemove; }

    @Override
    public void update(double dt) {
        updateStateTime(dt);
    }

    public void updateStateTime(double dt) {
        this.statetime += dt;
    }

    public double getStateTime() {
        return statetime;
    }

    public void setStateTime(double statetime) {
        this.statetime = statetime;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}