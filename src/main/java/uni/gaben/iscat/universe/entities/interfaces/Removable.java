package uni.gaben.iscat.universe.entities.interfaces;

/**
 * Interfaccia per la gestione del ciclo di vita e della rimozione differita delle entità (Lifecycle Flagging).
 * <p>
 * Implementa un pattern di rimozione in due fasi (Deferred Pruning): l'entità viene marcata per l'eliminazione
 * tramite un flag booleano, consentendo al gestore del mondo di gioco (Universe Loop) di ripulire in modo sicuro
 * e centralizzato le risorse e i corpi rigidi alla fine del frame corrente, evitando eccezioni di modifica concorrente.
 * </p>
 */
public interface Removable {

    /**
     * Modifica lo stato del flag di rimozione dell'entità, richiedendone o annullandone l'epurazione dal mondo.
     *
     * @param shouldRemove {@code true} se l'entità deve essere marcata per la rimozione immediata nel prossimo ciclo di pulizia.
     * @return Lo stato precedente del flag di rimozione prima di questa invocazione.
     */
    boolean setShouldRemove(boolean shouldRemove);

    /**
     * Interrogato dal ciclo principale dell'universo per verificare se l'oggetto è pronto per essere rimosso dal motore.
     *
     * @return {@code true} se l'entità è stata marcata per l'eliminazione e deve essere distrutta, altrimenti {@code false}.
     */
    boolean shouldRemove();
}