package uni.gaben.iscat.universe.entities.interfaces;

/**
 * Interfaccia per entità dotate di punti vita.
 * Fornisce metodi per leggere e modificare la salute dell'oggetto.
 */
public interface Alterable {
    /** Restituisce la salute attuale. */
    double getEndurance();
    /** Restituisce la salute massima. */
    double getMaxEndurance();
    /** Imposta la salute attuale. */
    void setEndurance(double amount);

    /** Chiamato per applicare un delta alla resistenza attuale **/
    void alter(double delta);
    default void damage(double positiveAmount) {
        alter(-positiveAmount);
    }
    default void restore(double positiveAmount) {
        alter(positiveAmount);
    }
    /** Verifica se l'entità è ancora in vita (HP > 0). */
    default boolean canEndure() { return getEndurance() > 0; }

    default void extinguish() {
        setEndurance(0);
    }
}
