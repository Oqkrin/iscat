package uni.gaben.iscat.game.interfaces;

/** Qualsiasi oggetto che il motore deve aggiornare ogni tick. */
public interface Updatable {
    /**
     * Avanza lo stato di un tick.
     * @param dt delta-time (secondi, o 1.0 per tick fisso)
     */
    void update(double dt);
}
