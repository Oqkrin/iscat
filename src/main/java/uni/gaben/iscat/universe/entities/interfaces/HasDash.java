package uni.gaben.iscat.universe.entities.interfaces;

/**
 * Interfaccia per entità capaci di eseguire scatti rapidi o accelerazioni istantanee (Dash Capability).
 * <p>
 * Fornisce i metodi necessari per innescare un movimento impulsivo ad alta velocità lungo un vettore angolare specifico
 * e per monitorare lo stato di disponibilità (cooldown) e di attivazione del ciclo di scatto.
 * </p>
 */
public interface HasDash {

    /**
     * Innesca uno scatto rapido forzando l'accelerazione dell'entità lungo la direzione angolare specificata.
     *
     * @param angle L'orientamento assoluto verso cui dirigere il dash, espresso in radianti.
     */
    void dashTowards(double angle);

    /**
     * Verifica se i requisiti logici e i timer di ricarica (cooldown) consentono l'attivazione di un nuovo scatto.
     *
     * @return {@code true} se l'entità è pronta per scattare, altrimenti {@code false}.
     */
    boolean canDash();

    /**
     * Specifica se l'entità si trova attualmente nel bel mezzo della transizione cinetica ad alta velocità dello scatto.
     *
     * @return {@code true} se la routine di dash è attiva e in esecuzione, altrimenti {@code false}.
     */
    boolean isDashing();
}