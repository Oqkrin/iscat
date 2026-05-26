package uni.gaben.iscat.iscat_game.lib.abstracts;

import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.AABB;
import org.dyn4j.geometry.Transform;
import uni.gaben.iscat.iscat_game.lib.interfaces.model.HasTerminalVelocity;
import uni.gaben.iscat.iscat_game.utils.UU;
import java.util.function.Consumer;

/**
 * Rappresentazione astratta di un'entità nel mondo fisico.
 * Calcola in modo dinamico le dimensioni basandosi sulle Fixture fisiche attive.
 */
public abstract class AbstractEntityModel extends Body implements HasTerminalVelocity {
    private String entityId;
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

    private double lifetime = 0.0;

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


    public void updateLifetime(double dt) {
        this.lifetime += dt;
    }

    public double getLifetime() {
        return lifetime;
    }

    public void setLifetime(double lifetime) {
        this.lifetime = lifetime;
    }
}