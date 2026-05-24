package uni.gaben.iscat.game.lib.implementations;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import uni.gaben.iscat.utils.AudioManager;
import uni.gaben.iscat.game.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.game.lib.interfaces.model.LifeDeath;
import uni.gaben.iscat.game.lib.utils.UU;
import uni.gaben.iscat.game.universe.UniverseSpawner;
import uni.gaben.iscat.game.universe.heart.HeartModel;
import uni.gaben.iscat.game.universe.player.PlayerModel;
import uni.gaben.iscat.game.universe.projectiles.Projectile;

/**
 * Implementazione di un'entità dotata di vita e soggetta a mortalità.
 * Gestisce i punti vita (HP), la guarigione e il danneggiamento.
 */
public class LivingEntityModel extends AbstractEntityModel implements LifeDeath {
    protected DoubleProperty life = new SimpleDoubleProperty();
    protected double maxLife;
    protected double xpReward = 0.0;

    private Runnable onHurt;
    private Runnable onDeath;

    public LivingEntityModel(double x, double y, double life, double maxLife) {
        super(x, y);
        this.life.set(life);
        this.maxLife = maxLife;
    }

    private boolean killedByProjectile = false;

    public void setKilledByProjectile(boolean value) { this.killedByProjectile = value; }
    public boolean isKilledByProjectile() { return killedByProjectile; }

    public DoubleProperty lifeProperty() { return life; }
    public double getLife() { return life.get(); }
    public double getMaxLife() { return maxLife; }

    public void setLife(double life) {
        double clampedLife = Math.clamp(life, 0, maxLife);
        this.life.set(clampedLife);

        // Se la salute scende a zero, forziamo l'istantanea esecuzione delle routine di morte
        if (clampedLife <= 0 && !shouldRemove()) {
            kill();
        }
    }

    @Override
    public void deltaToLife(double delta) {
        if (delta < 0) {
            if (this instanceof PlayerModel) {
                AudioManager.getInstance().playSFX("hurt");
            }
        }
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

            // Se sei un proiettile, allora non fare nessun suono quando muori
            if (!silent && !isProjectile && !isHeart) {
                AudioManager.getInstance().playSFX("explosion");
            }

            if(isHeart){
                AudioManager.getInstance().playSFX("heal");
            }

            // Se muore un nemico 25% di chance di spawnare un heart
            if (!isHeart && !isProjectile && !isPlayer && Math.random() < 0.25 && killedByProjectile) {
                double pixelX = UU.mToPx(this.getTransform().getTranslationX());
                double pixelY = UU.mToPx(this.getTransform().getTranslationY());
                UniverseSpawner.getInstance().spawn("HEART", pixelX, pixelY);
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