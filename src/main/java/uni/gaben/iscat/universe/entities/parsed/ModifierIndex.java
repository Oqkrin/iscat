package uni.gaben.iscat.universe.entities.parsed;

/**
 * Indice dei modificatori di Steering applicabili alle entità.
 * Mappa le stringhe di configurazione JSON ai corrispettivi tipi logici del motore di IA.
 */
public enum ModifierIndex {
    /** Forza che spinge l'entità ad allontanarsi dai vicini per evitare sovrapposizioni. */
    SEPARATION("separation"),

    /** Forza che allinea la direzione dell'entità a quella del gruppo circostante. */
    ALIGNMENT("alignment"),

    /** Forza che attira l'entità verso il centro di massa dei vicini. */
    COHESION("cohesion"),

    /** Algoritmo predittivo per evitare la collisione con ostacoli imminenti o proiettili. */
    COLLISION_AVOIDANCE("collisionavoidance");

    /** Chiave normalizzata utilizzata nei file di configurazione JSON. */
    public final String jsonKey;

    ModifierIndex(String jsonKey) {
        this.jsonKey = jsonKey.toLowerCase();
    }

    /**
     * Recupera il modificatore corrispondente alla stringa fornita.
     * * @param s La stringa proveniente dal JSON.
     * @return Il {@link ModifierIndex} corrispondente, o {@code null} se non riconosciuto.
     */
    public static ModifierIndex fromJson(String s) {
        if (s == null || s.isEmpty()) return null;

        String normalizedInput = s.toLowerCase().trim();

        for (ModifierIndex v : values()) {
            if (v.jsonKey.equalsIgnoreCase(normalizedInput)) {
                return v;
            }
        }
        System.err.println("[ModifierIndex] Unknown type '" + s + "' — modifier will be skipped.");
        return null;
    }
}