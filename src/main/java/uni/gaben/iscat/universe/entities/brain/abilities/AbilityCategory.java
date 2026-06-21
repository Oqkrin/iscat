package uni.gaben.iscat.universe.entities.brain.abilities;

/**
 * Categorie funzionali delle abilità delle IA (Ability Categories).
 * Definisce i vincoli di mutua esclusione e parallelismo per la coda delle azioni:
 * ad esempio, permette ad un'entità di fare fuoco durante il movimento, ma impedisce l'attivazione simultanea di più attacchi o traiettorie.
 */
public enum AbilityCategory {

    /** Abilità legate allo spostamento cinematico e alla navigazione. Consente una sola istanza attiva alla volta. */
    MOVEMENT,

    /** Abilità balistiche e pattern di offesa. Consente una sola istanza attiva alla volta, parallela al movimento. */
    ATTACK,

    /** Azioni ausiliarie (es. scudi, evocazioni, teletrasporto) eseguibili in parallelo senza bloccare movimento o attacchi. */
    SPECIAL
}