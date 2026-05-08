package uni.gaben.iscat.game.utils.interfaces;

import uni.gaben.iscat.game.utils.physics.Vec2;

/**
 * Entità con collisore circolare.
 * Sufficiente per un gioco spaziale; sostituibile con AABB o poligono senza toccare la gerarchia.
 *
 * Collision layers: ogni entità ha un layer (bit) e una mask (bitmask dei layer con cui collide).
 * Il sistema di collisioni salta la coppia se (a.mask & b.layer) == 0.
 * Default: layer=1, mask=0xFFFF → collide con tutto.
 */
public interface Collidable {

    // --- Layer constants ---
    int LAYER_PLAYER     = 1;       // 0001
    int LAYER_ENEMY      = 1 << 1;  // 0010
    int LAYER_PROJECTILE = 1 << 2;  // 0100
    int LAYER_OBJECT     = 1 << 3;  // 1000
    int LAYER_ALL        = 0xFFFF;

    /** Raggio del collisore in pixel. */
    double getCollisionRadius();

    /** Centro del collisore (di solito posizione + offset metà sprite). */
    Vec2 getColliderCenter();

    /** Chiamato dal sistema collisioni quando questo oggetto si sovrappone a {@code other}. */
    void onCollision(Collidable other);

    /**
     * Layer di appartenenza di questa entità (singolo bit).
     * Default: LAYER_OBJECT.
     */
    default int getCollisionLayer() { return LAYER_OBJECT; }

    /**
     * Bitmask dei layer con cui questa entità vuole collidere.
     * Default: tutto.
     */
    default int getCollisionMask() { return LAYER_ALL; }

    /**
     * {@code true} se i due collisori si sovrappongono E i layer sono compatibili.
     */
    default boolean collidesWith(Collidable other) {
        // Check layer compatibility both ways
        if ((getCollisionMask()  & other.getCollisionLayer()) == 0) return false;
        if ((other.getCollisionMask() & getCollisionLayer())  == 0) return false;
        return getColliderCenter().distanceTo(other.getColliderCenter())
                < (getCollisionRadius() + other.getCollisionRadius());
    }
}
