package uni.gaben.iscat.universe.entities;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import uni.gaben.iscat.universe.entities.interfaces.Alterable;
import uni.gaben.iscat.universe.entities.interfaces.hasXpReward;
import uni.gaben.iscat.universe.entities.player.PlayerModel;

/**
 * Modello astratto per tutte le entità viventi o dotate di barra della vita (Endurance).
 * Ottimizzato tramite caching del valore primitivo per evitare l'overhead di JavaFX Property nel loop di gioco.
 */
public abstract class AbstractLivingEntityModel extends AbstractPhysicalEntityModel implements Alterable, hasXpReward {

    protected final DoubleProperty endurance = new SimpleDoubleProperty();
    protected double enduranceValue; // Cache primitiva per letture ultra-rapide O(1) nel game loop
    protected double maxEndurance;
    protected double xpReward;
    protected String entityKey;
    protected boolean killedByProjectile = false;
    protected boolean killedByMeele = false;
    private Runnable onDeath;
    protected PlayerModel meleeAttacker = null;

    protected AbstractLivingEntityModel(double x, double y, EntityRecord data) {
        super(x, y, data);
        this.maxEndurance = data.initLife();
        this.enduranceValue = data.initLife();
        this.endurance.set(this.enduranceValue);
        this.xpReward = data.xpReward();
        this.entityKey = data.entityKey();
    }

    public DoubleProperty enduranceProperty() {
        return endurance;
    }

    /**
     * Ritorna l'endurance corrente sfruttando la cache locale per saltare l'overhead di JavaFX.
     */
    @Override
    public double getEndurance() {
        return enduranceValue;
    }

    @Override
    public double getMaxEndurance() {
        return maxEndurance;
    }

    public void setMaxEndurance(double maxEndurance) {
        this.maxEndurance = maxEndurance;
        setEndurance(this.enduranceValue);
    }

    public void setMeleeAttacker(PlayerModel attacker) {
        this.meleeAttacker = attacker;
    }

    /**
     * Aggiorna il valore di endurance. Sfrutta Math.clamp ed evita scritture sulla Property se non necessarie.
     */
    public void setEndurance(double endurance) {
        double clamped = Math.clamp(endurance, 0, maxEndurance);

        if (this.enduranceValue != clamped) {
            this.enduranceValue = clamped;
            this.endurance.set(clamped); // Aggiorna JavaFX (UI) solo se c'è una reale variazione
        }

        if (clamped <= 0.0 && !shouldRemove()) {
            extinguish(false);
        }
    }

    @Override
    public void alter(double amount) {
        setEndurance(this.enduranceValue + amount);
    }

    public void setMaxEnduranceDirect(double maxLife) {
        this.maxEndurance = maxLife;
        this.enduranceValue = maxLife;
        this.endurance.set(maxLife);
    }

    @Override
    public void setXpReward(double xp) { this.xpReward = xp; }

    @Override
    public double getXpReward() { return xpReward; }

    public void setKilledByProjectile(boolean value) { this.killedByProjectile = value; }
    public boolean isKilledByProjectile() { return killedByProjectile; }

    public void setKilledByMeele(boolean value) { this.killedByMeele = value; }
    public boolean isKilledByMeele() { return killedByMeele; }

    public void setOnDeath(Runnable callback) { this.onDeath = callback; }

    @Override
    public void extinguish() {
        extinguish(false);
    }

    /**
     * Disattiva ed estingue l'entità rimuovendola dal loop.
     * Ottimizzato per evitare chiamate ricorsive a setEndurance.
     */
    public void extinguish(boolean silent) {
        if (shouldRemove()) return;

        if (this.enduranceValue > 0.0) {
            this.enduranceValue = 0.0;
            this.endurance.set(0.0);
        }

        setShouldRemove(true);
        if (onDeath != null) {
            onDeath.run();
        }
        onDeath();
    }

    public void onDeath() {}
}