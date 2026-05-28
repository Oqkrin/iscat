package uni.gaben.iscat.universe.lib.implementations;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import uni.gaben.iscat.utils.AudioManager;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.lib.interfaces.model.LifeDeath;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseSpawner;
import uni.gaben.iscat.universe.consumables.heart.HeartModel;
import uni.gaben.iscat.universe.player.PlayerModel;
import uni.gaben.iscat.universe.projectiles.Projectile;
import uni.gaben.iscat.universe.UniverseWaveController;

public class LivingEntityModel extends AbstractEntityModel implements LifeDeath {
    protected DoubleProperty life = new SimpleDoubleProperty();
    protected double maxLife;
    protected double xpReward = 0.0;

    private Runnable onHurt;
    private Runnable onDeath;

    private boolean killedByProjectile = false;

    public LivingEntityModel(double x, double y, double life, double maxLife) {
        super(x, y);
        this.life.set(life);
        this.maxLife = maxLife;
    }

    public void setKilledByProjectile(boolean value) { this.killedByProjectile = value; }
    public boolean isKilledByProjectile() { return killedByProjectile; }

    public DoubleProperty lifeProperty() { return life; }
    public double getLife() { return life.get(); }
    public double getMaxLife() { return maxLife; }

    public void setLife(double life) {
        double clampedLife = Math.clamp(life, 0, maxLife);
        this.life.set(clampedLife);

        if (clampedLife <= 0 && !shouldRemove()) {
            kill();
        }
    }

    @Override
    public void deltaToLife(double delta) {
        setLife(getLife() + delta);
    }

    public void setMaxLife(double maxLife) {
        this.maxLife = maxLife;
        setLife(getLife());
    }

    public void setOnHurt(Runnable callback) { this.onHurt = callback; }
    public void setOnDeath(Runnable callback) { this.onDeath = callback; }

    @Override
    public void kill() {
        kill(false);
    }

    public void kill(boolean silent) {
        if (!shouldRemove()) {
            this.life.set(0);
            setShouldRemove(true);
            if (onDeath != null) {
                onDeath.run();
            }
            boolean isProjectile = this instanceof Projectile;
            boolean isPlayer = this instanceof PlayerModel;
            boolean isHeart = this instanceof HeartModel;

            if (!silent && !isProjectile && !isHeart) {
                AudioManager.getInstance().playSFX("explosion");
            }

            if (isHeart) {
                AudioManager.getInstance().playSFX("heal");
            }

            if (!isHeart && !isProjectile && !isPlayer) {
                UniverseWaveController.incrementKills();

                if (killedByProjectile) {
                    uni.gaben.iscat.utils.SessionScoreTracker.getInstance().addDeaths(1);

                    uni.gaben.iscat.screens.login.model.SessionUser user = uni.gaben.iscat.utils.SessionManager.getInstance().getCurrentUser();
                    if (user != null) {
                        uni.gaben.iscat.database.sqlite.ScoreDAO.increment(user.id(), "Deaths", 1);
                    }

                    // Logica di spawn del cuore (25% di probabilità)
                    if (Math.random() < 0.25) {
                        double pixelX = UU.mToPx(this.getTransform().getTranslationX());
                        double pixelY = UU.mToPx(this.getTransform().getTranslationY());
                        UniverseSpawner.getInstance().spawn("HEART", pixelX, pixelY);
                    }
                }
            }

            onDeath();
        }
    }

    @Override public void onDeath() {}
    @Override public double getBaseAccelerationPerTick() { return 0; }

    public double getXpReward() {
        return xpReward;
    }
    public void setXpReward(double xpReward) {
        this.xpReward = xpReward;
    }
}