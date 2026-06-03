package uni.gaben.iscat.universe.interfaces;

/**
 * Interfaccia per entità dotate di punti vita.
 * Fornisce metodi per leggere e modificare la salute dell'oggetto.
 */
public interface LifeDeath {
    /** Restituisce la salute attuale. */
    double getLife();
    /** Restituisce la salute massima. */
    double getMaxLife();
    /** Imposta la salute attuale. */
    void setLife(double life);

    /** Chiamato per applicare un delta alla salute attuale **/
    void deltaToLife(double delta);

    /** Verifica se l'entità è ancora in vita (HP > 0). */
    default boolean isAlive() { return getLife() > 0; }

    void kill();

    void onDeath();

}
