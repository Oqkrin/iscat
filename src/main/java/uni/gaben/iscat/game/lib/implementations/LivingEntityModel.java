package uni.gaben.iscat.game.lib.implementations;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import uni.gaben.iscat.IscatAudioManager;
import uni.gaben.iscat.game.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.game.lib.interfaces.model.Lifecycle;
import uni.gaben.iscat.game.universe.player.PlayerModel;

/**
 * Implementazione di un'entità dotata di vita e soggetta a mortalità.
 * Gestisce i punti vita (HP), la guarigione e il danneggiamento.
 */
public class LivingEntityModel extends AbstractEntityModel implements Lifecycle {
    protected DoubleProperty life = new SimpleDoubleProperty();
    protected double maxLife;

    private Runnable onHurt;
    private Runnable onDeath;

    public LivingEntityModel(double x, double y, double life, double maxLife) {
        super(x, y);
        this.life.set(life);
        this.maxLife = maxLife;
    }

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
                uni.gaben.iscat.IscatAudioManager.getInstance().playSFX("hurt");
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
            if (!silent) {
                // Riproduci SFX di esplosione per tutte le morti
                IscatAudioManager.getInstance().playSFX("explosion");
            }
            onDeath();
        }
    }

    @Override public void onDeath() {}
    @Override public double getBaseAccelerationPerTick() { return 0; }
}