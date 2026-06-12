package uni.gaben.iscat.universe.entity.modules;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import uni.gaben.iscat.universe.entity.GameEntity;
import uni.gaben.iscat.universe.entity.interfaces.Progression;
import uni.gaben.iscat.universe.entity.interfaces.hasXpReward;
import uni.gaben.iscat.universe.entity.record.XpData;
import uni.gaben.iscat.utils.AudioManager;

public class XpModule implements EntityModule, Progression, hasXpReward {

    private GameEntity entity;
    private XpData data;

    private final IntegerProperty level = new SimpleIntegerProperty(1);
    private final DoubleProperty xp = new SimpleDoubleProperty(0);
    private double xpNeeded = 100.0; // Base xp needed

    private double xpReward = 10.0;

    @Override
    public void attach(GameEntity entity) {
        this.entity = entity;
        this.data = entity.getRecord().xp();
        if (data != null) {
            this.xpReward = data.xpReward();
        }
    }

    @Override
    public double getXpReward() {
        return xpReward;
    }

    @Override
    public void setXpReward(double xp) {
        this.xpReward = xp;
    }

    public IntegerProperty levelProperty() { return level; }
    
    @Override
    public int getLevel() { return level.get(); }

    public DoubleProperty xpProperty() { return xp; }

    @Override
    public double getExperience() { return xp.get(); }

    @Override
    public void setExperience(double experience) {
        xp.set(experience);
    }

    @Override
    public double getNeededXpFor(int level) {
        return xpNeeded;
    }

    @Override
    public void incrementExperience(double amount) {
        if (amount <= 0) return;
        setExperience(this.xp.get() + amount);
        while (this.xp.get() >= xpNeeded) {
            levelUp();
        }
    }

    @Override
    public void levelUp() {
        this.xp.set(this.xp.get() - xpNeeded);
        this.level.set(this.level.get() + 1);
        this.xpNeeded = this.xpNeeded * (data != null && data.xpMultiplier() > 0 ? data.xpMultiplier() : 1.2);

        AudioManager.getInstance().playSFX("levelup");
        
        if (entity.hasModule(EnduranceModule.class)) {
            EnduranceModule em = entity.getModule(EnduranceModule.class);
            // Example level up logic: max health increases, heal to full
            // In a real system, these stats might be re-calculated from a formula.
            // em.setMaxEndurance(em.getMaxEndurance() + 100);
            em.setEndurance(em.getMaxEndurance());
        }
        
        System.out.println("[LEVEL UP] New Level: " + getLevel() + " | Next: " + this.xpNeeded + " XP");
    }

    @Override
    public void setLevel(int targetLevel) {
        for (int i = getLevel(); i < targetLevel; i++) {
            levelUp();
        }
    }
}
