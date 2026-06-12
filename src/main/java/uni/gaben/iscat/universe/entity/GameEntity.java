package uni.gaben.iscat.universe.entity;

import org.dyn4j.dynamics.Body;
import uni.gaben.iscat.universe.entity.interfaces.Dynamic;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.entity.modules.*;
import uni.gaben.iscat.utils.Updatable;

import java.util.function.Consumer;
import java.util.ArrayList;
import java.util.List;

public class GameEntity extends Body implements Updatable, Dynamic {

    private final EntityRecord record;
    
    // Explicit module fields for O(1) access and cache locality
    private SpriteModule spriteModule;
    public PhysicsModule physicsModule;
    private MovementModule movementModule;
    private EnduranceModule enduranceModule;
    private StateModule stateModule;
    private XpModule xpModule;
    private BrainModule brainModule;
    
    private final List<EntityModule> allModules = new ArrayList<>();
    
    private boolean shouldRemove = false;
    private Consumer<GameEntity> onCollision;

    public GameEntity(double x, double y, EntityRecord entity) {
        super();
        this.record = entity;
        this.setUserData(this);
        translate(UU.pxToM(x), UU.pxToM(y));
    }

    public EntityRecord getRecord() {
        return record;
    }

    public void addModule(EntityModule module) {
        allModules.add(module);
        
        if (module instanceof SpriteModule m) spriteModule = m;
        else if (module instanceof PhysicsModule m) physicsModule = m;
        else if (module instanceof MovementModule m) movementModule = m;
        else if (module instanceof EnduranceModule m) enduranceModule = m;
        else if (module instanceof StateModule m) stateModule = m;
        else if (module instanceof XpModule m) xpModule = m;
        else if (module instanceof BrainModule m) brainModule = m;
        
        module.attach(this);
    }
    
    // Fast explicit getters
    public SpriteModule getSpriteModule() { return spriteModule; }
    public PhysicsModule getPhysicsModule() { return physicsModule; }
    public MovementModule getMovementModule() { return movementModule; }
    public EnduranceModule getEnduranceModule() { return enduranceModule; }
    public StateModule getStateModule() { return stateModule; }
    public XpModule getXpModule() { return xpModule; }
    public BrainModule getBrainModule() { return brainModule; }

    // Backwards compatibility for easy code usage, but optimized
    @SuppressWarnings("unchecked")
    public <T extends EntityModule> T getModule(Class<T> type) {
        if (type == SpriteModule.class) return (T) spriteModule;
        if (type == PhysicsModule.class) return (T) physicsModule;
        if (type == MovementModule.class) return (T) movementModule;
        if (type == EnduranceModule.class) return (T) enduranceModule;
        if (type == StateModule.class) return (T) stateModule;
        if (type == XpModule.class) return (T) xpModule;
        if (type == BrainModule.class) return (T) brainModule;
        return null;
    }
    
    public boolean hasModule(Class<? extends EntityModule> type) {
        if (type == SpriteModule.class) return spriteModule != null;
        if (type == PhysicsModule.class) return physicsModule != null;
        if (type == MovementModule.class) return movementModule != null;
        if (type == EnduranceModule.class) return enduranceModule != null;
        if (type == StateModule.class) return stateModule != null;
        if (type == XpModule.class) return xpModule != null;
        if (type == BrainModule.class) return brainModule != null;
        return false;
    }

    @Override
    public void update(double dt) {
        for (EntityModule module : allModules) {
            module.update(dt);
        }
    }

    public boolean shouldRemove() {
        return shouldRemove;
    }

    public void setShouldRemove(boolean shouldRemove) {
        this.shouldRemove = shouldRemove;
        if (shouldRemove) {
            for (EntityModule module : allModules) {
                module.onRemove();
            }
        }
    }

    public void setOnCollision(Consumer<GameEntity> onCollision) {
        this.onCollision = onCollision;
    }

    public boolean hasCollision() {
        return onCollision != null;
    }

    public void triggerCollision(GameEntity other) {
        if (hasCollision()) {
            onCollision.accept(other);
        }
    }
    
    // Quick helpers
    public boolean isStunned() {
        return stateModule != null && stateModule.isStunned();
    }
    
    public void stun(double duration) {
        if (stateModule != null) stateModule.stun(duration);
    }
    
    public double getEndurance() {
        return enduranceModule != null ? enduranceModule.getEndurance() : 0;
    }
    
    public double getMaxEndurance() {
        return enduranceModule != null ? enduranceModule.getMaxEndurance() : 0;
    }
    
    public void setEndurance(double val) {
        if (enduranceModule != null) enduranceModule.setEndurance(val);
    }
    
    public void setMaxEndurance(double val) {
        if (enduranceModule != null) enduranceModule.setEndurance(val);
    }
    
    public void alter(double amount) {
        if (enduranceModule != null) enduranceModule.alter(amount);
    }
    
    public void setKilledByProjectile(boolean killed) {
        if (enduranceModule != null) enduranceModule.setKilledByProjectile(killed);
    }
    
    public boolean isKilledByProjectile() {
        return enduranceModule != null && enduranceModule.isKilledByProjectile();
    }
    
    public double getXpReward() {
        return xpModule != null ? xpModule.getXpReward() : 0;
    }
    
    public void setXpReward(double val) {
        if (xpModule != null) xpModule.setXpReward(val);
    }
    
    public int getLevel() {
        return xpModule != null ? xpModule.getLevel() : 1;
    }
    
    public void incrementExperience(double xp) {
        if (xpModule != null) xpModule.incrementExperience(xp);
    }
    
    public void extinguish() {
        if (enduranceModule != null) enduranceModule.completeDeath();
        else setShouldRemove(true);
    }
    
    public void extinguish(boolean immediate) {
        setShouldRemove(true); // Temporary simple fallback
    }

    @Override
    public double getTerminalVelocity() {
        return movementModule != null ? movementModule.getTerminalVelocity() : 0;
    }

    @Override
    public double getAcceleration() {
        return movementModule != null ? movementModule.getAcceleration() : 0;
    }

    @Override
    public double getMaxAngularVelocity() {
        return movementModule != null ? movementModule.getMaxAngularVelocity() : 0;
    }

    // ---- Viewport culling ----
    public boolean isInsideViewport(double minX, double maxX, double minY, double maxY) {
        double cx = UU.mToPx(this.getTransform().getTranslationX());
        double cy = UU.mToPx(this.getTransform().getTranslationY());
        double padding = Math.max(physicsModule.getWidthPx(), physicsModule.getHeightPx());
        return (cx + padding >= minX) && (cx - padding <= maxX) &&
                (cy + padding >= minY) && (cy - padding <= maxY);
    }
}
