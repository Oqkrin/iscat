package uni.gaben.iscat.game.interfaces;

import uni.gaben.iscat.game.physics.Vec2;

/**
 * Corpo che esercita attrazione gravitazionale sui {@link Physical} vicini.
 * Il motore chiama {@link #applyGravityTo} su ogni {@link Physical} nel raggio ogni tick.
 * Forza: F = G · (m1 · m2) / d²
 */
public interface Gravitational {

    /** G locale dell'oggetto. Buco nero = alto, pianeta = basso. */
    double getGravitationalConstant();

    /** Distanza massima in cui si sente la gravità (px). {@link Double#MAX_VALUE} = infinita. */
    double getGravityRadius();

    /** Massa del corpo gravitazionale. */
    double getMass();

    /** Posizione del corpo gravitazionale. */
    Vec2 getPosition();

    /**
     * Calcola e applica la forza gravitazionale su {@code target}.
     * Implementazione di default: legge di Newton. Sovrascrivere per gravità esotica.
     */
    default void applyGravityTo(Physical target) {
        Vec2   myPos  = getPosition();
        Vec2   tPos   = target.getPosition();
        double dx     = myPos.x - tPos.x;
        double dy     = myPos.y - tPos.y;
        double distSq = dx * dx + dy * dy;

        if (distSq < 1.0) return;

        double dist = Math.sqrt(distSq);
        if (dist > getGravityRadius()) return;

        double f = getGravitationalConstant() * getMass() * target.getMass() / distSq;
        target.applyForce(new Vec2((dx / dist) * f, (dy / dist) * f));
    }
}
