package uni.gaben.iscat.gamenex.lib.interfaces.model;

/**
 * Interfaccia per entità dotate di punti vita.
 * Fornisce metodi per leggere e modificare la salute dell'oggetto.
 */
public interface Alive {
    /** Restituisce la salute attuale. */
    double getLife();
    /** Restituisce la salute massima. */
    double getMaxLife();
    /** Imposta la salute attuale. */
    void setLife(double life);

    /** Verifica se l'entità è ancora in vita (HP > 0). */
    default boolean isAlive() { return getLife() > 0; }

    /** 
     * Applica un danno all'entità sottraendo punti vita.
     * @param amount Quantità di punti vita da rimuovere.
     */
    default void bleed(double amount) {
        setLife(Math.clamp(getLife() - amount, 0, getMaxLife()));
    }

}
