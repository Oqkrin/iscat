package uni.gaben.iscat.game.universe.attacks;

import uni.gaben.iscat.game.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.game.lib.interfaces.model.HasProjectile;
import uni.gaben.iscat.game.universe.projectiles.Projectile;
import uni.gaben.iscat.game.universe.projectiles.Shooter;

// Aggiungendo <?> diciamo a Java: "T deve implementare HasProjectile, ma non mi importa di quale tipo specifico di proiettile"
public interface AttackPattern<T extends AbstractEntityModel & HasProjectile<?>> {
    /**
     * @return true se l'attacco ha terminato tutti i suoi colpi, false se è ancora in corso.
     */
    boolean updateAndExecute(T entity, Shooter<T> shooter, Projectile template, double targetAngle, double dt);

    /** Resetta lo stato interno per permettere il riuso dell'istanza. */
    void reset();
}