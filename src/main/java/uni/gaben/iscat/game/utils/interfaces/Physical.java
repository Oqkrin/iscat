package uni.gaben.iscat.game.utils.interfaces;

import uni.gaben.iscat.game.utils.physics.Vec2;

/**
 * Entità che partecipa alla simulazione fisica.
 * Pipeline per tick:
 * <ol>
 *   <li>Sistemi esterni chiamano {@link #applyForce} (gravità, spinta, collisioni…)</li>
 *   <li>Il motore chiama {@link #integrate} → aggiorna velocità e posizione</li>
 *   <li>L'accumulatore forze viene azzerato</li>
 * </ol>
 */
public interface Physical {

    /** Massa in kg (unità di gioco). Usata per F=ma e attrazione gravitazionale. */
    double getMass();

    Vec2 getPosition();
    void setPosition(Vec2 pos);

    Vec2 getVelocity();
    void setVelocity(Vec2 vel);

    /** Accumula una forza da applicare al prossimo {@link #integrate}. */
    void applyForce(Vec2 force);

    /**
     * Integra le forze accumulate in velocità e posizione, poi azzera l'accumulatore.
     * @param dt delta-time (secondi, o 1.0 per giochi a tick fisso)
     */
    void integrate(double dt);
}
