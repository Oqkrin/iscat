package uni.gaben.iscat.gamenex.lib.implementations;

import uni.gaben.iscat.gamenex.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.gamenex.lib.interfaces.model.Alive;
import uni.gaben.iscat.gamenex.lib.interfaces.model.Mortal;

/**
 * Implementazione di un'entità dotata di vita e soggetta a mortalità.
 * Gestisce i punti vita (HP), la guarigione e il danneggiamento.
 * Permette di registrare callback per eventi specifici come il ferimento o la morte.
 */
public class LivingEntityModel extends AbstractEntityModel implements Alive, Mortal {
    /** Salute attuale dell'entità. */
    protected double currentHp;
    /** Salute massima raggiungibile. */
    protected double maxHp;

    /**
     * Crea un'entità vivente in una posizione specifica.
     * @param x Coordinata X (pixel).
     * @param y Coordinata Y (pixel).
     * @param currentHp Salute iniziale.
     * @param maxHp Salute massima.
     */
    public LivingEntityModel(double x, double y,double currentHp, double maxHp) {
        super(x, y);
        this.currentHp = currentHp;
        this.maxHp = maxHp;
    }

    /** Restituisce la salute attuale. */
    public double getLife() {
        return currentHp;
    }

    /** Restituisce la salute massima. */
    public double getMaxLife() {
        return maxHp;
    }

    /** Imposta la salute attuale, assicurandosi che resti nei limiti [0, maxLife]. */
    public void setLife(double life) {
        this.currentHp = Math.clamp(life, 0, maxHp);
    }

    /** Imposta la salute massima e aggiorna la salute attuale se necessario. */
    public void setMaxLife(double maxLife) {
        this.maxHp = maxLife;
        setLife(currentHp);
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
     * Sottrae salute all'entità. Se la salute scende a zero, l'entità viene uccisa.
     * @param amount Quantità di danno subito.
     */
    public void take_damage(double amount) {
        this.currentHp -= (int) amount;
        if (onHurt != null && currentHp >= 0) onHurt.run();
        if (this.currentHp <= 0) {
            this.currentHp = 0;
            kill();
        }
    }

    public void heal(int amount) {
        if (!isAlive()) return;

        currentHp = Math.min(maxHp, currentHp + amount);;
        //TODO Eventuale effetto visivo / suono qui
    }

    /**
     * Uccide istantaneamente l'entità ed esegue il callback di morte.
     */
    @Override
    public void kill() {
        if (onDeath != null) {
            onDeath.run();
        }
    }

    @Override
    public void onDeath() {
        // Implementazione vuota, sovrascrivibile nelle classi figlie
    }

}
