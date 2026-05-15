package uni.gaben.iscat.gamenex.lib.implementations;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import uni.gaben.iscat.gamenex.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.gamenex.lib.interfaces.model.Lifecycle;

/**
 * Implementazione di un'entità dotata di vita e soggetta a mortalità.
 * Gestisce i punti vita (HP), la guarigione e il danneggiamento.
 * Permette di registrare callback per eventi specifici come il ferimento o la morte.
 */
public class LivingEntityModel extends AbstractEntityModel implements Lifecycle {
    /** Salute attuale dell'entità. */
    protected DoubleProperty life = new SimpleDoubleProperty();
    /** Salute massima raggiungibile. */
    protected double maxLife;

    /**
     * Crea un'entità vivente in una posizione specifica.
     * @param x Coordinata X (pixel).
     * @param y Coordinata Y (pixel).
     * @param life Salute iniziale.
     * @param maxLife Salute massima.
     */
    public LivingEntityModel(double x, double y, double life, double maxLife) {
        super(x, y);
        this.life.set(life);
        this.maxLife = maxLife;
    }

    public DoubleProperty lifeProperty() {
        return life;
    }
    /** Restituisce la salute attuale. */
    public double getLife() {
        return life.get();
    }

    /** Restituisce la salute massima. */
    public double getMaxLife() {
        return maxLife;
    }

    /** Imposta la salute attuale, assicurandosi che resti nei limiti [0, maxLife]. */
    public void setLife(double life) {
        this.life.set(Math.clamp(life, 0, maxLife));
    }

    @Override
    public void deltaToLife(double delta) {
        setLife(getLife() + delta);
    }

    /** Imposta la salute massima e aggiorna la salute attuale se necessario. */
    public void setMaxLife(double maxLife) {
        this.maxLife = maxLife;
        setLife(getLife());
    }

    // --- Gestione Eventi ---

    private Runnable onHurt;
    private Runnable onDeath;

    /** Imposta un'azione da eseguire quando l'entità subisce danni. */
    public void setOnHurt(Runnable callback) {
        this.onHurt = callback;
    }

    /** Imposta un'azione da eseguire quando l'entità muore. */
    public void setOnDeath(Runnable callback) {
        this.onDeath = callback;
    }

    /**
     * Uccide istantaneamente l'entità ed esegue il callback di morte.
     */
    @Override
    public void kill() {
        if (onDeath != null) {
            onDeath.run();
        }
        setShouldRemove(true);
    }

    @Override
    public void onDeath() {
        // Implementazione vuota, sovrascrivibile nelle classi figlie
    }

    @Override
    public double getBaseAccelerationPerTick() {
        return 0;
    }
}
