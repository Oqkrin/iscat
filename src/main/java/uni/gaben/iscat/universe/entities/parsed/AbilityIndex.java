package uni.gaben.iscat.universe.entities.parsed;

/**
 * Indice di tutte le abilità e azioni logiche eseguibili dalle entità di gioco.
 * Mappa ogni abilità alla rispettiva chiave di lettura usata nei file JSON.
 */
public enum AbilityIndex {
    /** Attacco a distanza standard. */
    SHOOT("shoot"),
    /** Attacco a distanza con pattern casuali. */
    RANDOMIZED_SHOOT("randomizedShoot"),
    /** Azione di cura o rigenerazione salute. */
    HEAL("heal"),
    /** Evocazione di altre entità di supporto o nemici minori. */
    SUMMON("summon"),
    /** Attacco corpo a corpo ravvicinato. */
    MELEE("melee"),
    /** Attacco suicida ad area con esplosione sul posto. */
    KAMIKAZE("kamikaze"),
    /** Scatto rapido lineare (evasivo o offensivo). */
    DASH("dash"),
    /** Attacco in picchiata o schiacciamento al suolo. */
    PLUNGE("plunge");

    /** Chiave di lettura utilizzata nei file di configurazione JSON. */
    public final String jsonKey;

    AbilityIndex(String jsonKey) {
        this.jsonKey = jsonKey;
    }

    /**
     * Recupera l'indice dell'abilità partendo dalla sua stringa JSON.
     * Ritorna null e stampa un errore se la chiave non viene riconosciuta.
     */
    public static AbilityIndex fromJson(String s) {
        if (s == null || s.isEmpty()) return null;
        for (AbilityIndex v : values()) {
            if (v.jsonKey.equals(s)) return v;
        }
        System.err.println("[AbilityIndex] Unknown type '" + s + "' — ability will be skipped");
        return null;
    }
}