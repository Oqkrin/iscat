package uni.gaben.iscat.game.utils.interfaces;

/**
 * Entità con lifecycle di spawn/despawn.
 *
 * {@link #onSpawn()} viene chiamato da GameModel quando l'entità viene aggiunta al mondo.
 * {@link #onDespawn()} viene chiamato da GameModel quando l'entità viene rimossa
 * (per morte, scadenza, o rimozione esplicita).
 *
 * Utile per: inizializzare effetti visivi, registrare audio, liberare risorse.
 */
public interface Spawnable {

    /** Chiamato una volta quando l'entità entra nel mondo. */
    default void onSpawn() {}

    /** Chiamato una volta quando l'entità lascia il mondo. */
    default void onDespawn() {}
}
