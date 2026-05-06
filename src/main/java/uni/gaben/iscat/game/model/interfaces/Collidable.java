package uni.gaben.iscat.game.model.interfaces;

import uni.gaben.iscat.game.model.physics.Vec2;

/**
 * Entità con collisore circolare.
 * Sufficiente per un gioco spaziale; sostituibile con AABB o poligono senza toccare la gerarchia.
 */
public interface Collidable {

    /** Raggio del collisore in pixel. */
    double getCollisionRadius();

    /** Centro del collisore (di solito posizione + offset metà sprite). */
    Vec2 getColliderCenter();

    /** Chiamato dal sistema collisioni quando questo oggetto si sovrappone a {@code other}. */
    void onCollision(Collidable other);

    /** {@code true} se i due collisori si sovrappongono. */
    default boolean collidesWith(Collidable other) {
        return getColliderCenter().distanceTo(other.getColliderCenter())
                < (getCollisionRadius() + other.getCollisionRadius());
    }
}
