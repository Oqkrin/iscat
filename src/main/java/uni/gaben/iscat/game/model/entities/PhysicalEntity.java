package uni.gaben.iscat.game.model.entities;

import uni.gaben.iscat.game.model.interfaces.Physical;
import uni.gaben.iscat.game.model.interfaces.Updatable;
import uni.gaben.iscat.game.model.physics.Vec2;

/**
 * Entità che partecipa alla simulazione fisica.
 * Integratore Euleriano: F=ma → v+=a·dt → pos+=v·dt.
 * Le sottoclassi possono configurare drag, maxSpeed, deadZone per personalizzare il comportamento.
 */
public abstract class PhysicalEntity extends Entity implements Physical, Updatable {

    /** Massa in kg (unità di gioco). Oggetti più pesanti accelerano meno. */
    protected double mass = 1.0;

    protected Vec2 velocity   = Vec2.ZERO;
    protected Vec2 forceAccum = Vec2.ZERO;
    
    /** Fattore di attrito (0-1). Valori più bassi = più attrito. Default: 0.92 */
    protected double drag = 0.92;
    
    /** Velocità massima. Default: 10.0 */
    protected double maxSpeed = 10.0;
    
    /** Soglia sotto cui la velocità viene azzerata. Default: 0.01 */
    protected double deadZone = 0.01;

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
     * Integrazione Euleriana con drag, cap velocità e dead-zone.
     * Le sottoclassi possono sovrascrivere per comportamenti personalizzati,
     * ma nella maggior parte dei casi basta configurare drag/maxSpeed/deadZone.
     */
    @Override
    public void integrate(double dt) {
        if (mass <= 0) return;
        
        // Integrazione Euleriana base
        Vec2 acceleration = forceAccum.scale(1.0 / mass);
        velocity   = velocity.add(acceleration.scale(dt));
        setPosition(getPosition().add(velocity.scale(dt)));
        forceAccum = Vec2.ZERO;
        
        // Applica attrito
        velocity = velocity.scale(drag);
        
        // Cap velocità
        double speed = velocity.magnitude();
        if (speed > maxSpeed) {
            velocity = velocity.scale(maxSpeed / speed);
        }
        
        // Dead-zone: azzera velocità molto basse
        if (Math.abs(velocity.x) < deadZone && Math.abs(velocity.y) < deadZone) {
            velocity = Vec2.ZERO;
        }
    }

    // --- Updatable ---

    /** Tick di default: solo integrazione. Sovrascrivere per aggiungere AI, input, ecc. */
    @Override
    public void update(double dt) { integrate(dt); }

    // --- Setters per configurazione fisica ---
    
    public void setMass(double mass) { this.mass = mass; }
    
    /** Imposta il fattore di attrito (0-1). Valori più bassi = più attrito. */
    public void setDrag(double drag) { this.drag = drag; }
    
    /** Imposta la velocità massima. */
    public void setMaxSpeed(double maxSpeed) { this.maxSpeed = maxSpeed; }
    
    /** Imposta la soglia dead-zone. */
    public void setDeadZone(double deadZone) { this.deadZone = deadZone; }
    
    // --- Getters ---
    
    public double getDrag() { return drag; }
    public double getMaxSpeed() { return maxSpeed; }
    public double getDeadZone() { return deadZone; }
}
