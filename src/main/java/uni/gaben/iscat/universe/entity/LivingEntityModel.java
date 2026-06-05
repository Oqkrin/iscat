package uni.gaben.iscat.universe.entity;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import uni.gaben.iscat.utils.AudioManager;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseSpawner;
import uni.gaben.iscat.universe.entity.consumables.heart.HeartModel;
import uni.gaben.iscat.universe.entity.player.PlayerModel;
import uni.gaben.iscat.universe.entity.projectiles.Projectile;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.utils.SessionScoreTracker;

/**
 * Modello base per tutte le entità dotate di punti vita, dinamiche di danno e stati di morte.
 * Implementa l'interfaccia LifeDeath e funge da superclasse per il Giocatore e i Nemici,
 * centralizzando la gestione dei drop, dei trigger audio e dell'assegnazione dei punti esperienza.
 */
public class LivingEntityModel extends AbstractEntityModel implements LifeDeath {
    protected DoubleProperty life = new SimpleDoubleProperty();
    protected double maxLife;
    protected double xpReward = 0.0;

    /** Chiave univoca corrispondente al campo 'EntityKey' nella tabella 'Entita' del database. */
    protected String entityKey = null;

    private Runnable onHurt;
    private Runnable onDeath;

    /** Stato di abbattimento: true se il colpo di grazia finale è stato inferto da un proiettile del giocatore. */
    private boolean killedByProjectile = false;

    public LivingEntityModel(double x, double y, double life, double maxLife) {
        super(x, y);
        this.life.set(life);
        this.maxLife = maxLife;
    }

    /**
     * Returns the entity key from the database for GenericEntityModel,
     * or the simple class name for other entity types.
     */
    @Override
    public String getEntityKey() { 
        return entityKey != null ? entityKey : super.getEntityKey(); 
    }
    
    public void setEntityKey(String entityKey) { this.entityKey = entityKey; }

    public void setKilledByProjectile(boolean value) { this.killedByProjectile = value; }
    public boolean isKilledByProjectile() { return killedByProjectile; }

    public DoubleProperty lifeProperty() { return life; }
    public double getLife() { return life.get(); }
    public double getMaxLife() { return maxLife; }

    /**
     * Aggiorna i punti vita attuali forzandoli all'interno del range consentito [0, maxLife].
     * Se la vita scende a 0, avvia automaticamente la procedura di rimozione e distruzione dell'entità.
     */
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

    /**
     * Sets maxLife and brings life up to the new maximum WITHOUT triggering the kill-check.
     * Use this in customizers applied after a projectile is already alive.
     */
    public void setMaxLifeDirect(double maxLife) {
        this.maxLife = maxLife;
        this.life.set(maxLife);
    }

    public void setOnHurt(Runnable callback) { this.onHurt = callback; }
    public void setOnDeath(Runnable callback) { this.onDeath = callback; }

    @Override
    public void kill() {
        kill(false);
    }

    /**
     * Gestisce la distruzione logica, fisica e acustica dell'entità.
     * Incrementa i contatori delle ondate e, in caso di abbattimento da proiettile valido,
     * aggiorna le statistiche di sessione del punteggio e calcola la probabilità di drop di un consumabile.
     *
     * @param silent Se true, disabilita la riproduzione degli effetti sonori di esplosione/morte.
     */
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

            // Riproduzione SFX differenziata per tipologia di entità
            if (!silent && !isProjectile && !isHeart) {
                AudioManager.getInstance().playSFX("explosion");
            }

            if (isHeart) {
                AudioManager.getInstance().playSFX("heal");
            }

            // Elaborazione logiche di gioco specifiche per i nemici comuni e speciali
            if (!isHeart && !isProjectile && !isPlayer) {
                // Logiche di ricompensa e spawn attive solo se ucciso attivamente dal giocatore
                if (killedByProjectile) {
                    SessionScoreTracker.getInstance().addDeaths(1);

                    // Calcolo probabilistico del drop (25% di possibilità di spawnare un Cuore)
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

    public double getXpReward() { return xpReward; }
    public void setXpReward(double xpReward) { this.xpReward = xpReward; }
}