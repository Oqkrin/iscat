package uni.gaben.iscat.universe.entities;

/**
 * Indice degli obiettivi e comportamenti di rotazione IA (Rotation Goals).
 * Mappa le logiche di orientamento angolare delle entità (puntamento al target, rotazione continua, blocco)
 * alle rispettive chiavi di configurazione JSON per l'inizializzazione del motore logico.
 */
public enum RotationGoalIndex {

    /** Rotazione fissa e bloccata all'angolo corrente. */
    STILL("still"),

    /** Stato di riposo con orientamento nominale predefinito. */
    IDLE("idle"),

    /** Allineamento automatico dell'orientamento al vettore di movimento/velocità corrente. */
    MOVEMENT("movement"),

    /** Puntamento cinematico continuo verso le coordinate di un'entità target. */
    TARGET("target"),

    /** Rotazione angolare uniforme e continua a velocità costante (effetto rotazione su se stessi). */
    CONTINUES_SPIN("continuesSpin"),

    /** Rotazione ciclica ad intervalli temporali regolari alternati a pause. */
    INTERVAL_SPIN("intervalSpin"),

    /** Vincolo di orientamento ancorato rigidamente a una coordinata o asse specifico. */
    LOCKED("locked");

    /** Chiave identificativa utilizzata per la serializzazione e deserializzazione nei file JSON. */
    public final String jsonKey;

    RotationGoalIndex(String jsonKey) {
        this.jsonKey = jsonKey;
    }

    /**
     * Esegue un lookup case-sensitive per associare la stringa JSON al rispettivo enum.
     * In caso di valore nullo, vuoto o non riconosciuto, restituisce {@link #IDLE} e registra
     * una segnalazione su standard error per garantire la tolleranza ai guasti (Fault Tolerance).
     *
     * @param s La stringa estratta dal file di configurazione JSON.
     * @return L'istanza dell'enum corrispondente, o {@link #IDLE} come fallback di sicurezza.
     */
    public static RotationGoalIndex fromJson(String s) {
        if (s == null || s.isEmpty()) return IDLE;
        for (RotationGoalIndex v : values()) {
            if (v.jsonKey.equals(s)) return v;
        }
        System.err.println("[RotationGoalIndex] Unknown type '" + s + "' — falling back to IDLE");
        return IDLE;
    }
}