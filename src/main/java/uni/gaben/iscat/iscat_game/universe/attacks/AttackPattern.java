package uni.gaben.iscat.iscat_game.universe.attacks;

import uni.gaben.iscat.iscat_game.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.iscat_game.lib.interfaces.model.HasProjectile;
import uni.gaben.iscat.iscat_game.universe.projectiles.Projectile;
import uni.gaben.iscat.iscat_game.universe.projectiles.Shooter;

// Aggiungendo <?> diciamo a Java: "T deve implementare HasProjectile, ma non mi importa di quale tipo specifico di proiettile"
public interface AttackPattern<T extends AbstractEntityModel & HasProjectile<?>> {
    /**
     * @return true se l'attacco ha terminato tutti i suoi colpi, false se è ancora in corso.
     */
    boolean updateAndExecute(T entity, Shooter<T> shooter, Projectile template, double targetAngle, double dt);

    /** Resetta lo stato interno per permettere il riuso dell'istanza. */
    void reset();

    default void onStart(AbstractEntityModel entity) {}
}