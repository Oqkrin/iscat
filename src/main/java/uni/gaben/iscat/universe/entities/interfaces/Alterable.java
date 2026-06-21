package uni.gaben.iscat.universe.entities.interfaces;

/**
 * Interfaccia per entità dotate di punti vita o barre di resistenza (Alterable Status).
 * <p>
 * Fornisce il contratto per leggere, impostare e modificare la salute corrente dell'oggetto.
 * Include logiche di default per l'applicazione di danni e cure basate sul segno del delta applicato,
 * integrando controlli preventivi sullo stato di inalterabilità (es. invulnerabilità).
 * </p>
 */
public interface Alterable {

    /** * @return La quantità di salute/resistenza (endurance) attualmente posseduta dall'entità.
     */
    double getEndurance();

    /** * @return Il limite massimo di salute/resistenza (endurance) raggiungibile dall'entità.
     */
    double getMaxEndurance();

    /** * Imposta direttamente il valore della salute attuale dell'entità.
     * * @param amount Il nuovo valore assoluto di endurance da assegnare.
     */
    void setEndurance(double amount);

    /** * Applica una variazione relativa (delta) alla resistenza attuale dell'entità.
     * * @param delta Il valore numerico da sommare alla salute attuale (positivo per cure, negativo per danni).
     */
    void alter(double delta);

    /**
     * Riduce la salute attuale dell'entità applicando un valore di danno.
     * Interrompe l'esecuzione se l'entità si trova in uno stato di inalterabilità.
     *
     * @param positiveValue L'ammontare di danno (espresso come valore positivo) da infliggere.
     */
    default void damage(double positiveValue) {
        if (isInalterable()) return;
        alter(-positiveValue);
    }

    /**
     * Ripristina la salute attuale dell'entità applicando un valore di cura.
     * Interrompe l'esecuzione se l'entità si trova in uno stato di inalterabilità.
     *
     * @param positiveValue L'ammontare di punti salute (espresso come valore positivo) da rigenerare.
     */
    default void restore(double positiveValue) {
        if (isInalterable()) return;
        alter(positiveValue);
    }

    /**
     * Forza l'azzeramento immediato della salute corrente dell'entità, decretandone l'abbattimento istantaneo.
     */
    default void extinguish() {
        setEndurance(0);
    }

    /**
     * Verifica se l'entità è temporaneamente o permanentemente immune a modifiche esterne della salute.
     *
     * @return {@code true} se l'entità è invulnerabile o inalterabile, altrimenti {@code false}.
     */
    boolean isInalterable();
}