package uni.gaben.iscat.universe.entities;

/**
 * Stati logici e di animazione in cui può trovarsi un'entità di gioco.
 */
public enum EntityState {
    /** Animazione iniziale di ingresso del boss. */
    ENTRANCE,
    /** Entità ferma o in movimento standard. */
    IDLE,
    /** Scatto veloce (schivata o attacco). */
    DASH,
    /** Entità colpita che subisce danno (stordimento). */
    HIT,
    /** Esecuzione di un attacco standard. */
    ATTACK,
    /** Esecuzione di un attacco di evocazione nemici. */
    SPAWN_ATTACK,
    /** Animazione e logica di morte. */
    DEATH
}