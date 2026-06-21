package uni.gaben.iscat.universe.entities.interfaces;

/**
 * Interfaccia per la gestione dei sistemi di crescita, livelli ed esperienza delle entità (Progression System).
 * <p>
 * Fornisce il contratto per tracciare i punti esperienza (XP), determinare le soglie di incremento tramite curve di calcolo
 * dedicate e gestire il passaggio al livello successivo. Include una logica di default per automatizzare lo scatto di livello.
 * </p>
 */
public interface Progression {

    /**
     * @return Il livello attuale in cui si trova l'entità.
     */
    int getLevel();

    /**
     * Incrementa i punti esperienza attuali dell'entità del valore specificato.
     * Solitamente viene accoppiato a una verifica interna per innescare {@link #levelUp()} se si supera la soglia necessaria.
     *
     * @param amount La quantità di punti esperienza (XP) da aggiungere.
     */
    void incrementExperience(double amount);

    /**
     * Incrementa unitariamente il livello corrente dell'entità ($Level = Level + 1$).
     * Può essere sovrascritta per iniettare logiche aggiuntive, come il ripristino delle statistiche o l'assegnazione di punti abilità.
     */
    default void levelUp() {
        setLevel(getLevel() + 1);
    }

    /**
     * Forza il livello dell'entità al valore specificato.
     *
     * @param level Il nuovo livello assoluto da assegnare.
     */
    void setLevel(int level);


    /**
     * Imposta direttamente il valore assoluto dell'esperienza dell'entità.
     *
     * @param experience Il nuovo valore di punti esperienza da assegnare.
     */
    void setExperience(double experience);
}