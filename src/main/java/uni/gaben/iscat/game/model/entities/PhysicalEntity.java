package uni.gaben.iscat.game.model.entities;

import uni.gaben.iscat.game.model.interfaces.Physical;
import uni.gaben.iscat.game.model.interfaces.Updatable;
import uni.gaben.iscat.game.model.physics.Vec2;

/**
 * Entità che partecipa alla simulazione fisica.
 * Integratore Euleriano: F=ma → v+=a·dt → pos+=v·dt.
 * Le sottoclassi sovrascrivono {@link #integrate} per drag, cap velocità, ecc.
 */
public abstract class PhysicalEntity extends Entity implements Physical, Updatable {

    /** Massa in kg (unità di gioco). Oggetti più pesanti accelerano meno. */
    protected double mass = 1.0;

    protected Vec2 velocity   = Vec2.ZERO;
    protected Vec2 forceAccum = Vec2.ZERO;

    // --- Physical ---

    @Override public double getMass()              { return mass; }
    @Override public Vec2   getPosition()          { return new Vec2(x, y); }
    @Override public void   setPosition(Vec2 pos)  { x = pos.x; y = pos.y; }
    @Override public Vec2   getVelocity()          { return velocity; }
    @Override public void   setVelocity(Vec2 vel)  { this.velocity = vel; }

    @Override
    public void applyForce(Vec2 force) {
        forceAccum = forceAccum.add(force);
    }

    /**
     * Integrazione Euleriana. Le sottoclassi chiamano {@code super.integrate(dt)}
     * e poi applicano drag, cap, ecc.
     */
    @Override
    public void integrate(double dt) {
        if (mass <= 0) return;
        Vec2 acceleration = forceAccum.scale(1.0 / mass);
        velocity   = velocity.add(acceleration.scale(dt));
        setPosition(getPosition().add(velocity.scale(dt)));
        forceAccum = Vec2.ZERO;
    }

    // --- Updatable ---

    /** Tick di default: solo integrazione. Sovrascrivere per aggiungere AI, input, ecc. */
    @Override
    public void update(double dt) { integrate(dt); }

    public void setMass(double mass) { this.mass = mass; }
}
