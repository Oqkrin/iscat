package uni.gaben.iscat.game.utils.interfaces;

/**
 * Entità che può morire e deve essere rimossa dal mondo quando è morta.
 *
 * Complementare a {@link Expirable} (scadenza per tempo),
 * {@code Mortal} rappresenta la morte per danno o evento.
 *
 * GameModel usa questa interfaccia in cleanupDeadEntities()
 * eliminando l'instanceof LivingEntityModel.
 */
public interface Mortal {

    /** {@code true} se l'entità è morta e deve essere rimossa dal mondo. */
    boolean isDead();
}
