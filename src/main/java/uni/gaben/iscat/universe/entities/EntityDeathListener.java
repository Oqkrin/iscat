package uni.gaben.iscat.universe.entities;

/**
 * Callback per notificare la morte di un'entità.
 * Permette alla logica di gioco di reagire senza accoppiare il motore fisico dyn4j al ciclo principale.
 */
@FunctionalInterface
public interface EntityDeathListener {

    /**
     * Viene invocato quando un'entità viene eliminata dal gioco.
     *
     * @param entity             L'entità che è morta.
     * @param killedByProjectile true se è stata colpita da un proiettile, false altrimenti.
     */
    void onEntityDied(AbstractPhysicalEntityModel entity, boolean killedByProjectile);
}