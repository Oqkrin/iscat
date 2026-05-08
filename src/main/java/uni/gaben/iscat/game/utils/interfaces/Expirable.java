package uni.gaben.iscat.game.utils.interfaces;

/**
 * Entità con una durata limitata che scade automaticamente.
 *
 * GameModel rimuove le entità scadute ogni tick tramite cleanupDeadEntities().
 * Implementare questa interfaccia elimina l'instanceof ProjectileModel nel cleanup.
 *
 * Esempi: proiettili, effetti particellari, power-up temporanei.
 */
public interface Expirable {

    /** {@code true} se l'entità ha esaurito la sua durata e deve essere rimossa. */
    boolean isExpired();
}
