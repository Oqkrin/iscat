package uni.gaben.iscat.universe.entities;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import uni.gaben.iscat.universe.entities.interfaces.Alterable;
import uni.gaben.iscat.universe.entities.interfaces.hasXpReward;
import uni.gaben.iscat.universe.entities.hardcoded.player.PlayerModel;
import uni.gaben.iscat.universe.entities.hardcoded.projectiles.ProjectileModel;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.spawn.UniverseSpawner;
import uni.gaben.iscat.universe.entities.hardcoded.heart.HeartModel;
import uni.gaben.iscat.utils.EntityAudioManager;
import uni.gaben.iscat.utils.SessionScoreTracker;

public abstract class AbstractLivingEntityModel extends AbstractPhysicalEntityModel implements Alterable, hasXpReward {
    protected final DoubleProperty endurance = new SimpleDoubleProperty();
    protected double maxEndurance;
    protected double xpReward;
    protected String entityKey;
    protected boolean killedByProjectile = false;
    private Runnable onDeath;

    protected AbstractLivingEntityModel(double x, double y, EntityRecord data) {
        super(x, y, data);
        this.maxEndurance = data.initLife();
        this.endurance.set(data.initLife());
        this.xpReward = data.xpReward();
        this.entityKey = data.entityKey();
    }

    public DoubleProperty enduranceProperty() { return endurance; }

    @Override
    public double getEndurance() {
        return endurance.get();
    }

    @Override
    public double getMaxEndurance() { return maxEndurance; }
    public void setMaxEndurance(double maxEndurance) { this.maxEndurance = maxEndurance; setEndurance(getEndurance()); }

    public void setEndurance(double endurance) {
        double clamped = Math.clamp(endurance, 0, maxEndurance);
        this.endurance.set(clamped);
        if (clamped <= 0 && !shouldRemove()) {
            extinguish();
        }
    }

    @Override
    public void alter(double amount) { setEndurance(getEndurance() + amount); }

    public void setMaxEnduranceDirect(double maxLife) {
        this.maxEndurance = maxLife;
        this.endurance.set(maxLife);
    }

    @Override
    public void setXpReward(double xp) { this.xpReward = xp; }
    @Override
    public double getXpReward() { return xpReward; }

    public void setKilledByProjectile(boolean value) { this.killedByProjectile = value; }
    public boolean isKilledByProjectile() { return killedByProjectile; }

    public void setOnDeath(Runnable callback) { this.onDeath = callback; }


    @Override
    public void extinguish() { extinguish(false); }
    public void extinguish(boolean silent) {
        if (!shouldRemove()) {
            if(getEndurance() > 0) setEndurance(0);
            setShouldRemove(true);
            if (onDeath != null) onDeath.run();

            boolean isProjectile = this instanceof ProjectileModel;
            boolean isPlayer = this instanceof PlayerModel;
            boolean isHeart = this instanceof HeartModel;

            if (!silent && !isProjectile && !isHeart) {
                AbstractLivingEntityModel entityModel = this;
                EntityAudioManager.playEventAudio(entityModel, "death");
            }

            if (!isHeart && !isProjectile && !isPlayer && killedByProjectile) {
                SessionScoreTracker.getInstance().addDeaths(1);
                if (Math.random() < 0.25) {
                    double px = UU.mToPx(getTransform().getTranslationX());
                    double py = UU.mToPx(getTransform().getTranslationY());
                    UniverseSpawner.getInstance().spawn("HEART", px, py);
                }
            }
            onDeath();
        }
    }

    public void onDeath() {}
}