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
 * Modello fisico di base dell'engine che estende direttamente il corpo rigido di dyn4j.
 * Ottimizzato per prevenire l'allocazione di oggetti di calcolo (come AABB e Transform) nel game loop.
 */
public abstract class AbstractPhysicalEntityModel extends Body implements Dynamic, Updatable, Removable, Collidable, Stateful {

    // Identità statica per le trasformazioni a costo zero
    private static final Transform IDENTITY_TRANSFORM = new Transform();

    protected final EntityRecord entity;
    protected int state = 0;
    protected boolean shouldRemove = false;
    protected double stateTime = 0.0;

    // Mappa dei callback di collisione
    protected final HashMap<String, Consumer<AbstractPhysicalEntityModel>> collisionEffects = new HashMap<>();

    private double tempTerminalVelocity = -1;   // -1 = usa il default
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

    // ---- Callback di Collisione Ottimizzati ----
    @Override
    public void addOnCollision(String id, Consumer<AbstractPhysicalEntityModel> onCollision) {
        this.collisionEffects.put(id, onCollision);
    }

    @Override
    public boolean hasAnyCollision() {
        return !collisionEffects.isEmpty();
    }

    /**
     * Esegue tutti gli effetti di collisione registrati.
     * Ottimizzato sostituendo il forEach generico con un ciclo diretto sui valori della mappa.
     */
    @Override
    public void triggerAllCollisions(AbstractPhysicalEntityModel other) {
        if (!collisionEffects.isEmpty()) {
            for (Consumer<AbstractPhysicalEntityModel> effect : collisionEffects.values()) {
                effect.accept(other);
            }
        }
    }

    @Override
    public void triggerCollision(String id, AbstractPhysicalEntityModel other) {
        Consumer<AbstractPhysicalEntityModel> effect = collisionEffects.get(id);
        if (effect != null) {
            effect.accept(other);
        }
    }

    @Override public void clearOnCollisions() { collisionEffects.clear(); }
    @Override public void removeOnCollision(String id) { collisionEffects.remove(id); }

    // ---- Proprietà Fisiche Delegate ----
    @Override public double getAcceleration() { return entity.maxForce(); }
    @Override public double getMaxAngularVelocity() { return entity.maxAngularVelocity(); }

    // ---- Geometria e Calcolo Dimensionale (Zero-Allocation) ----

    /**
     * Calcola la larghezza in metri riutilizzando un'istanza di trasformazione statica per non allocare memoria.
     */
    public double getWidthMeters() {
        if (getFixtureCount() == 0) return 0.0;
        AABB aabb = createAABB(IDENTITY_TRANSFORM);
        return aabb.getWidth();
    }

    /**
     * Calcola l'altezza in metri riutilizzando un'istanza di trasformazione statica per non allocare memoria.
     */
    public double getHeightMeters() {
        if (getFixtureCount() == 0) return 0.0;
        AABB aabb = createAABB(IDENTITY_TRANSFORM);
        return aabb.getHeight();
    }

    public double getWidthPx() { return UU.mToPx(getWidthMeters()); }
    public double getHeightPx() { return UU.mToPx(getHeightMeters()); }

    /**
     * Determina se l'entità interseca un rettangolo di vincolo (es. Frustum culling o viewport).
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

    // ---- Gestione dinamica dei limiti di velocità e Dash ----
    @Override
    public double getTerminalVelocity() {
        return tempTerminalVelocity >= 0 ? tempTerminalVelocity : entity.maxVelocity();
    }

    public void setTemporaryTerminalVelocity(double tempTerminalVelocity) {
        this.tempTerminalVelocity = tempTerminalVelocity;
    }

    public void restoreTerminalVelocity() {
        this.tempTerminalVelocity = -1;
    }

    public void setDashLinearDamping(double damping) {
        this.originalLinearDamping = this.getLinearDamping();
        this.setLinearDamping(damping);
    }

    public void restoreLinearDamping() {
        this.setLinearDamping(originalLinearDamping);
    }
}