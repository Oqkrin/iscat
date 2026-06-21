package uni.gaben.iscat.universe.entities.parsed;

/**
 * Indice di tutti i pattern di sparo e di evocazione riconosciuti dal parser JSON.
 * Associa la configurazione testuale alla rispettiva logica di distribuzione geometrica dei proiettili.
 */
public enum PatternIndex {
    /** Sparo singolo standard nella direzione di puntamento dell'entità. */
    SINGLE_SHOT("singleShot"),

    /** Ventaglio di proiettili con un angolo di offset tra l'uno e l'altro. */
    SPREAD("spread"),

    /** Direzioni multiple simultanee calcolate in base a passi angolari in radianti. */
    MULTI_DIRECTION("multiDirection"),

    /** Anello completo di proiettili distribuito uniformemente a 360 gradi. */
    RING("ring"),

    /** Ripetitore sequenziale che riesegue un sotto-pattern a intervalli di tempo regolari. */
    REPEATER("repeater"),

    /** Proiettili paralleli distanziati linearmente (lungo una linea ortogonale al tiro). */
    PARALLEL_LINE("parallelLine"),

    /** Pattern speciale per l'evocazione di minion o entità nel raggio d'azione. */
    SUMMON("summon"),

    /** Disposizione geometrica complessa basata su una forma o figura predefinita. */
    FIGURE("figure"),

    /** Formazione a freccia ("V") con proiettili ritardati sequenzialmente. */
    VARROW("varrow"),

    /** Flusso continuo di proiettili con angolo di emissione in costante rotazione. */
    SPIRAL("spiral");

    /** Chiave canonica utilizzata all'interno dei file di configurazione JSON. */
    public final String jsonKey;

    PatternIndex(String jsonKey) {
        this.jsonKey = jsonKey;
    }

    /**
     * Recupera il pattern corrispondente alla stringa fornita (case-sensitive).
     * In caso di mancata corrispondenza, effettua il fallback su {@link #SINGLE_SHOT}.
     * * @param s La stringa proveniente dal JSON.
     * @return Il {@link PatternIndex} corrispondente o {@link #SINGLE_SHOT} come default.
     */
    public static PatternIndex fromJson(String s) {
        if (s == null || s.isEmpty()) return SINGLE_SHOT;
        for (PatternIndex v : values()) {
            if (v.jsonKey.equals(s)) return v;
        }
        System.err.println("[PatternIndex] Unknown type '" + s + "' — falling back to SINGLE_SHOT");
        return SINGLE_SHOT;
    }
}