package uni.gaben.iscat.universe.entity.modules;

import uni.gaben.iscat.universe.entity.GameEntity;
import uni.gaben.iscat.universe.entity.interfaces.Alterable;
import uni.gaben.iscat.universe.entity.record.EnduranceData;
import uni.gaben.iscat.universe.UniverseWaveController;

public class EnduranceModule implements EntityModule, Alterable {

    private GameEntity entity;
    private EnduranceData data;
    private double currentEndurance;
    private double maxEndurance;
    private UniverseWaveController waveController;
    private Runnable onDeathCallback;

    @Override
    public void attach(GameEntity entity) {
        this.entity = entity;
        this.data = entity.getRecord().endurance();
        this.maxEndurance = data.maxLife();
        this.currentEndurance = data.initLife();
    }

    public void setWaveController(UniverseWaveController waveController) {
        this.waveController = waveController;
    }

    public void setOnDeathCallback(Runnable callback) {
        this.onDeathCallback = callback;
    }

    @Override
    public double getEndurance() {
        return currentEndurance;
    }

    @Override
    public double getMaxEndurance() {
        return maxEndurance;
    }

    @Override
    public void setEndurance(double amount) {
        this.currentEndurance = Math.max(0, Math.min(amount, maxEndurance));
        if (this.currentEndurance <= 0 && !entity.shouldRemove()) {
            triggerDeath();
        }
    }

    @Override
    public void alter(double delta) {
        setEndurance(this.currentEndurance + delta);
    }

    private void triggerDeath() {
        if (entity.hasModule(StateModule.class)) {
            StateModule sm = entity.getModule(StateModule.class);
            if (sm.getState() != StateModule.STATE_DEATH) {
                sm.setCurrentState(StateModule.STATE_DEATH);
            }
        } else {
            completeDeath();
        }
    }

    public void completeDeath() {
        if (onDeathCallback != null) onDeathCallback.run();
        
        if (entity.getRecord().identity().isBoss() && waveController != null) {
            waveController.notifyBossDead();
        }
        entity.setShouldRemove(true);
    }
}
