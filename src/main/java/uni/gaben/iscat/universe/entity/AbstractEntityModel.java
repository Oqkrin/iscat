package uni.gaben.iscat.universe.entity;

import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.AABB;
import org.dyn4j.geometry.Transform;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.entity.interfaces.Collidable;
import uni.gaben.iscat.universe.entity.interfaces.Dynamic;
import uni.gaben.iscat.universe.entity.interfaces.Removable;
import uni.gaben.iscat.universe.entity.interfaces.Stateful;
import uni.gaben.iscat.utils.Updatable;

import java.util.function.Consumer;

public abstract class AbstractEntityModel extends Body implements Dynamic, Updatable, Removable, Collidable, Stateful {
    protected final EntityRecord entity;
    protected int state = 0;
    protected boolean shouldRemove = false;
    protected double stateTime = 0.0;

    // Callback for collisions
    protected Consumer<AbstractEntityModel> onCollision;

    protected AbstractEntityModel(double x, double y, EntityRecord entity) {
        super();
        this.entity = entity;
        this.setUserData(this);
        translate(UU.pxToM(x), UU.pxToM(y));
    }

    public EntityRecord getEntityRecord() { return entity; }

    @Override
    public int getState() { return state; }
    @Override
    public void setState(int state) { this.state = state; }
    @Override
    public boolean shouldRemove() { return shouldRemove; }
    @Override
    public boolean setShouldRemove(boolean shouldRemove) { return this.shouldRemove = shouldRemove; }
    @Override
    public double getStateTime() { return stateTime; }
    @Override
    public void setStateTime(double stateTime) { this.stateTime = stateTime; }
    @Override
    public void updateStateTime(double dt) { this.stateTime += dt; }

    // ---- Collision callbacks ----
    @Override
    public void setOnCollision(Consumer<AbstractEntityModel> onCollision) { this.onCollision = onCollision; }
    @Override
    public boolean hasCollision() { return onCollision != null; }
    @Override
    public void triggerCollision(AbstractEntityModel other) {
        if (hasCollision()) onCollision.accept(other);
    }

    // ---- Physical capability getters (delegated to definition) ----
    @Override public double getTerminalVelocity() { return entity.maxVelocity(); }
    @Override public double getAcceleration() { return entity.maxForce(); }
    @Override
    public double getMaxAngularVelocity() { return entity.maxAngularVelocity(); }

    // ---- Geometry helpers (based on fixtures) ----
    public double getWidthMeters() {
        if (getFixtureCount() == 0) return 0.0;
        AABB aabb = createAABB(new Transform());
        return aabb.getWidth();
    }
    public double getHeightMeters() {
        if (getFixtureCount() == 0) return 0.0;
        AABB aabb = createAABB(new Transform());
        return aabb.getHeight();
    }
    public double getWidthPx() { return UU.mToPx(getWidthMeters()); }
    public double getHeightPx() { return UU.mToPx(getHeightMeters()); }

    // ---- Viewport culling ----
    public boolean isInsideViewport(double minX, double maxX, double minY, double maxY) {
        double cx = UU.mToPx(this.getTransform().getTranslationX());
        double cy = UU.mToPx(this.getTransform().getTranslationY());
        double padding = Math.max(getWidthPx(), getHeightPx());
        return (cx + padding >= minX) && (cx - padding <= maxX) &&
                (cy + padding >= minY) && (cy - padding <= maxY);
    }

    // ---- Updatable ----
    @Override
    public void update(double dt) {
        updateStateTime(dt);
    }
}