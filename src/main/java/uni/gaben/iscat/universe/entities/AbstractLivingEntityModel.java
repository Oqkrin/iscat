package uni.gaben.iscat.universe.entities;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import uni.gaben.iscat.universe.entities.interfaces.Alterable;
import uni.gaben.iscat.universe.entities.interfaces.hasXpReward;
import uni.gaben.iscat.universe.entities.parsed.EntityRecord;
import uni.gaben.iscat.universe.entities.player.PlayerModel;

/**
 * Modello astratto per entità dotate di punti vita (Endurance).
 * Usa una cache primitiva per evitare l'overhead di JavaFX nel loop di gioco.
 */
public abstract class AbstractLivingEntityModel extends AbstractPhysicalEntityModel implements Alterable, hasXpReward {

    protected final DoubleProperty endurance = new SimpleDoubleProperty();
    protected double enduranceValue;
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

    /**
     * Ritorna la proprietà JavaFX dell'endurance (utile per la UI).
     */
    public DoubleProperty enduranceProperty() {
        return endurance;
    }

    /**
     * Ritorna i punti vita correnti usando il valore in cache.
     */
    @Override
    public double getEndurance() {
        return enduranceValue;
    }

    /**
     * Ritorna i punti vita massimi.
     */
    @Override
    public double getMaxEndurance() {
        return maxEndurance;
    }

    /**
     * Imposta i punti vita massimi.
     */
    public void setMaxEndurance(double maxEndurance) {
        this.maxEndurance = maxEndurance;
        setEndurance(this.enduranceValue);
    }

    /**
     * Memorizza il giocatore che ha eseguito l'attacco corpo a corpo.
     */
    public void setMeleeAttacker(PlayerModel attacker) {
        this.meleeAttacker = attacker;
    }

    /**
     * Aggiorna i punti vita correnti e gestisce l'eventuale morte.
     */
    public void setEndurance(double endurance) {
        double clamped = Math.clamp(endurance, 0, maxEndurance);

        if (this.enduranceValue != clamped) {
            this.enduranceValue = clamped;
            this.endurance.set(clamped);
        }

        if (clamped <= 0.0 && !shouldRemove()) {
            extinguish(false);
        }
    }

    /**
     * Modifica i punti vita correnti sommandovi il valore specificato.
     */
    @Override
    public void alter(double amount) {
        setEndurance(this.enduranceValue + amount);
    }

    /**
     * Imposta direttamente i punti vita massimi e correnti allo stesso valore.
     */
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

    /**
     * Registra un'azione da eseguire al momento della morte dell'entità.
     */
    public void setOnDeath(Runnable callback) { this.onDeath = callback; }

    /**
     * Rimuove l'entità dal gioco (richiama extinguish in modalità non silenziosa).
     */
    @Override
    public void extinguish() {
        extinguish(false);
    }

    /**
     * Rimuove l'entità dal gioco, azzera la vita ed esegue i callback di morte.
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

    /**
     * Metodo hook da sovrascrivere nelle sottoclassi per logiche di morte specifiche.
     */
    public void onDeath() {}
}