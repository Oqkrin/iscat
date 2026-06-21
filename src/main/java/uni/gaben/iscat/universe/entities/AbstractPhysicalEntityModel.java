package uni.gaben.iscat.universe.entities;

import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.AABB;
import org.dyn4j.geometry.Transform;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.entities.interfaces.Collidable;
import uni.gaben.iscat.universe.entities.interfaces.Dynamic;
import uni.gaben.iscat.universe.entities.interfaces.Removable;
import uni.gaben.iscat.universe.entities.interfaces.Stateful;
import uni.gaben.iscat.universe.entities.parsed.EntityRecord;
import uni.gaben.iscat.utils.Updatable;

import java.util.HashMap;
import java.util.function.Consumer;

/**
 * Modello fisico di base dell'engine basato sul corpo rigido di dyn4j.
 * Evita l'allocazione di nuovi oggetti nel game loop per ottimizzare le performance.
 */
public abstract class AbstractPhysicalEntityModel extends Body implements Dynamic, Updatable, Removable, Collidable, Stateful {

    private static final Transform IDENTITY_TRANSFORM = new Transform();

    protected final EntityRecord entity;
    protected int state = 0;
    protected boolean shouldRemove = false;
    protected double stateTime = 0.0;
    protected final HashMap<String, Consumer<AbstractPhysicalEntityModel>> collisionEffects = new HashMap<>();

    private double tempTerminalVelocity = -1;
    private double originalLinearDamping;

    protected AbstractPhysicalEntityModel(double x, double y, EntityRecord entity) {
        super();
        this.entity = entity;
        this.setUserData(this);
        translate(UU.pxToM(x), UU.pxToM(y));
    }

    public EntityRecord getEntityRecord() { return entity; }

    @Override public int getState() { return state; }
    @Override public void setState(int state) { this.state = state; }

    @Override public boolean shouldRemove() { return shouldRemove; }
    @Override public boolean setShouldRemove(boolean shouldRemove) { return this.shouldRemove = shouldRemove; }

    @Override public double getStateTime() { return stateTime; }
    @Override public void setStateTime(double stateTime) { this.stateTime = stateTime; }
    @Override public void updateStateTime(double dt) { this.stateTime += dt; }

    /**
     * Registra un callback da eseguire in caso di collisione.
     */
    @Override
    public void addOnCollision(String id, Consumer<AbstractPhysicalEntityModel> onCollision) {
        this.collisionEffects.put(id, onCollision);
    }

    /**
     * Esegue tutti gli effetti di collisione registrati con l'altra entità.
     */
    @Override
    public void triggerAllCollisions(AbstractPhysicalEntityModel other) {
        if (!collisionEffects.isEmpty()) {
            for (Consumer<AbstractPhysicalEntityModel> effect : collisionEffects.values()) {
                effect.accept(other);
            }
        }
    }

    @Override public void clearOnCollisions() { collisionEffects.clear(); }

    @Override public double getAcceleration() { return entity.maxForce(); }
    @Override public double getMaxAngularVelocity() { return entity.maxAngularVelocity(); }

    /**
     * Calcola la larghezza dell'entità in metri senza allocare memoria.
     */
    public double getWidthMeters() {
        if (getFixtureCount() == 0) return 0.0;
        AABB aabb = createAABB(IDENTITY_TRANSFORM);
        return aabb.getWidth();
    }

    /**
     * Calcola l'altezza dell'entità in metri senza allocare memoria.
     */
    public double getHeightMeters() {
        if (getFixtureCount() == 0) return 0.0;
        AABB aabb = createAABB(IDENTITY_TRANSFORM);
        return aabb.getHeight();
    }

    public double getWidthPx() { return UU.mToPx(getWidthMeters()); }
    public double getHeightPx() { return UU.mToPx(getHeightMeters()); }

    /**
     * Controlla se l'entità si trova all'interno di un rettangolo di vincolo (es. la telecamera).
     */
    public boolean isInsideRect(double minX, double maxX, double minY, double maxY) {
        double cx = UU.mToPx(this.transform.getTranslationX());
        double cy = UU.mToPx(this.transform.getTranslationY());
        double padding = Math.max(getWidthPx(), getHeightPx());
        return (cx + padding >= minX) && (cx - padding <= maxX) &&
                (cy + padding >= minY) && (cy - padding <= maxY);
    }

    @Override
    public void update(double dt) {
        this.stateTime += dt;
    }

    @Override
    public double getTerminalVelocity() {
        return tempTerminalVelocity >= 0 ? tempTerminalVelocity : entity.maxVelocity();
    }

    /**
     * Cambia temporaneamente il limite massimo di velocità dell'entità.
     */
    public void setTemporaryTerminalVelocity(double tempTerminalVelocity) {
        this.tempTerminalVelocity = tempTerminalVelocity;
    }

    /**
     * Ripristina la velocità massima originale dell'entità.
     */
    public void restoreTerminalVelocity() {
        this.tempTerminalVelocity = -1;
    }

    /**
     * Modifica il linear damping per gestire le decelerazioni speciali (es. durante un Dash).
     */
    public void setDashLinearDamping(double damping) {
        this.originalLinearDamping = this.getLinearDamping();
        this.setLinearDamping(damping);
    }

    /**
     * Ripristina il valore di linear damping originale.
     */
    public void restoreLinearDamping() {
        this.setLinearDamping(originalLinearDamping);
    }

    public int getTemporaryVelocity() {
        return (int) tempTerminalVelocity;
    }
}