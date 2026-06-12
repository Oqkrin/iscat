package uni.gaben.iscat.universe.entity.modules;

import uni.gaben.iscat.universe.entity.GameEntity;
import uni.gaben.iscat.universe.entity.interfaces.Stateful;
import uni.gaben.iscat.universe.entity.interfaces.Stunnable;
import uni.gaben.iscat.universe.entity.record.StateData;
import uni.gaben.iscat.utils.Cooldown;

public class StateModule implements EntityModule, Stateful, Stunnable {

    public static final int STATE_ENTRANCE = 0;
    public static final int STATE_IDLE     = 1;
    public static final int STATE_DEATH    = 6;

    private GameEntity entity;
    private StateData data;

    private int currentState = STATE_IDLE;
    private double stateTime = 0.0;
    private boolean completeKillCalled = false;
    
    private final Cooldown stunCooldown = new Cooldown();

    @Override
    public void attach(GameEntity entity) {
        this.entity = entity;
        this.data = entity.getRecord().state();

        if (data != null && data.hasEntranceAnimation()) {
            this.currentState = STATE_ENTRANCE;
        }
    }

    @Override
    public void update(double dt) {
        stateTime += dt;
        stunCooldown.update(dt);

        if (data == null) return;

        double frameDuration = entity.hasModule(SpriteModule.class) ? 
                entity.getModule(SpriteModule.class).getFrameDuration() : 0.05;
        double duration = getFramesForState(currentState) * frameDuration;

        if (currentState == STATE_ENTRANCE) {
            if (stateTime >= duration) {
                setCurrentState(STATE_IDLE);
                // Also trigger shockwave for boss here if needed
            }
        }
        else if (currentState != STATE_IDLE && currentState != STATE_DEATH) {
            if (stateTime >= duration) {
                setCurrentState(STATE_IDLE);
            }
        }
        else if (currentState == STATE_DEATH) {
            if (stateTime >= duration && !completeKillCalled) {
                completeKillCalled = true;
                if (entity.hasModule(EnduranceModule.class)) {
                    entity.getModule(EnduranceModule.class).completeDeath();
                } else {
                    entity.setShouldRemove(true);
                }
            }
        }
    }

    public void setCurrentState(int state) {
        if (this.currentState != state) {
            this.currentState = state;
            this.stateTime = 0.0;
        }
    }

    public int getFramesForState(int state) {
        if (data.animationFrames() != null && state >= 0 && state < data.animationFrames().length) {
            return data.animationFrames()[state];
        }
        return 1;
    }

    @Override
    public int getState() {
        return currentState;
    }

    @Override
    public void setState(int state) {
        setCurrentState(state);
    }

    @Override
    public double getStateTime() {
        return stateTime;
    }

    @Override
    public void setStateTime(double stateTime) {
        this.stateTime = stateTime;
    }

    @Override
    public void updateStateTime(double dt) {
        this.stateTime += dt;
    }

    @Override
    public void stun(double duration) {
        stunCooldown.start(duration);
    }

    @Override
    public boolean isStunned() {
        return stunCooldown.isCoolingDown();
    }
}
