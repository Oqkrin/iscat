package uni.gaben.iscat.universe.entities.interfaces;

/**
 * Interfaccia per entità soggette a meccaniche di stordimento o interruzione logica (Stun Capability).
 * <p>
 * Definisce i metodi necessari per disabilitare temporaneamente le routine decisionali (es. il comportamento del {@code Brain})
 * o i sistemi di movimento di un'entità per una finestra temporale definita, esponendo al contempo un flag di controllo per verificarne lo stato.
 * </p>
 */
public interface Stunnable {

    /**
     * Innesca lo stato di stordimento sull'entità, congelandone le funzioni logico-motorie per la durata specificata.
     *
     * @param ms La durata complessiva dello stordimento espressa in millisecondi (ms).
     */
    void stun(double ms);

    /**
     * Interrogato dai controller o dai sistemi di update per verificare se l'entità è attualmente sotto l'effetto di uno stordimento.
     *
     * @return {@code true} se l'entità è stordita e le sue azioni sono bloccate, altrimenti {@code false}.
     */
    boolean isStunned();
}