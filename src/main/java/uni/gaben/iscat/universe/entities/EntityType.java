package uni.gaben.iscat.universe.entities;

/**
 * Definisce le macro-categorie di entità presenti nel gioco.
 */
public enum EntityType {
    PLAYER("player"),
    ISCAT("iscat"),
    GOBLIN("collisionAvoidance"),
    PROJECTILE("projectile");

    /** Chiave di lettura utilizzata nei file di configurazione JSON. */
    public final String jsonKey;

    EntityType(String jsonKey) {
        this.jsonKey = jsonKey;
    }

    /**
     * Recupera il tipo di entità partendo dalla sua stringa JSON.
     * Ritorna null se il tipo non viene riconosciuto.
     */
    public static EntityType fromJson(String s) {
        if (s == null || s.isEmpty()) return null;
        for (EntityType v : values()) {
            if (v.jsonKey.equals(s)) return v;
        }
        System.err.println("[EntityType] Unknown type '" + s + "' — type will be skipped");
        return null;
    }
}