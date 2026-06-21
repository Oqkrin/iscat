package uni.gaben.iscat.universe.entities.parsed;

/**
 * Indice degli obiettivi e comportamenti di guida IA (Steering Goals).
 * Mappa i comportamenti dell'algoritmo di Steering (inseguimento, evasione, orbita) alle rispettive
 * chiavi di configurazione JSON, garantendo una risoluzione sicura dei tipi a runtime.
 */
public enum SteeringGoalIndex {

    /** Stato di quiete o assenza di forze di steering applicate. */
    IDLE("idle"),

    /** Inseguimento attivo del bersaglio basato sulla predizione della sua traiettoria. */
    PURSUIT("pursuit"),

    /** Evasione attiva ed allontanamento predittivo dal bersaglio. */
    EVADE("evade"),

    /** Inseguimento mirato con mantenimento di una distanza di ingaggio di sicurezza. */
    PURSUIT_WITH_RANGE("pursuitWithRange"),

    /** Evasione controllata con arresto dell'allontanamento al superamento di un raggio limite. */
    EVADE_WITH_RANGE("evadeWithRange"),

    /** Movimento orbitale circolare attorno all'entità target (attualmente alias di IDLE). */
    ORBIT("Orbit");

    /** Chiave identificativa utilizzata per la serializzazione e deserializzazione nei file JSON. */
    public final String jsonKey;

    SteeringGoalIndex(String jsonKey) {
        this.jsonKey = jsonKey;
    }

    /**
     * Esegue un lookup case-sensitive per associare la stringa JSON al rispettivo enum.
     * In caso di valore nullo, vuoto o non riconosciuto, restituisce {@link #IDLE} e registra
     * un errore su standard error per prevenire il crash applicativo (Fault Tolerance).
     *
     * @param s La stringa estratta dal file di configurazione JSON.
     * @return L'istanza dell'enum corrispondente, o {@link #IDLE} come fallback di sicurezza.
     */
    public static SteeringGoalIndex fromJson(String s) {
        if (s == null || s.isEmpty()) return IDLE;
        for (SteeringGoalIndex v : values()) {
            if (v.jsonKey.equals(s)) return v;
        }
        System.err.println("[SteeringGoalIndex] Unknown type '" + s + "' — falling back to IDLE");
        return IDLE;
    }
}