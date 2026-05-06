package uni.gaben.iscat.utils;

/**
 * Molla smorzata 1D simulata fisicamente.
 *
 * Ogni tick applica:
 *   F = -stiffness*(posizione - target) - damping*velocità
 *   a = F / massa
 *   v += a * dt
 *   x += v * dt
 *
 * Con damping critico (damping = 2*sqrt(stiffness*massa)) non oscilla.
 * Sotto-smorzata → oscillazione; sovra-smorzata → ritorno lento senza rimbalzo.
 *
 * Usare {@link #impulse} per applicare una spinta istantanea (es. dodge).
 */
public final class Spring {

    private double position;
    private double velocity;
    private double target;

    private final double stiffness; // N/m — quanto forte tira verso il target
    private final double damping;   // N·s/m — quanto frena l'oscillazione
    private final double mass;      // kg — inerzia

    /**
     * @param initial   valore iniziale
     * @param stiffness rigidità (es. 80–200 per risposta rapida)
     * @param damping   smorzamento (es. 2*sqrt(stiffness*mass) per critico)
     * @param mass      massa (es. 1.0)
     */
    public Spring(double initial, double stiffness, double damping, double mass) {
        this.position  = initial;
        this.velocity  = 0;
        this.target    = initial;
        this.stiffness = stiffness;
        this.damping   = damping;
        this.mass      = mass;
    }

    /** Factory con smorzamento critico automatico (nessuna oscillazione). */
    public static Spring critico(double initial, double stiffness, double mass) {
        double damping = 2.0 * Math.sqrt(stiffness * mass);
        return new Spring(initial, stiffness, damping, mass);
    }

    /** Factory leggermente sotto-smorzata (piccolo rimbalzo, più vivace). */
    public static Spring sottoSmorzata(double initial, double stiffness, double mass) {
        double damping = 1.6 * Math.sqrt(stiffness * mass); // ~80% del critico
        return new Spring(initial, stiffness, damping, mass);
    }

    /**
     * Avanza la simulazione di un tick.
     * @param dt delta-time (usare 1.0 per tick fisso)
     */
    public void update(double dt) {
        double force = -stiffness * (position - target) - damping * velocity;
        double accel = force / mass;
        velocity += accel * dt;
        position += velocity * dt;
    }

    /** Applica un impulso istantaneo alla velocità (es. dodge, esplosione). */
    public void impulse(double dv) { velocity += dv; }

    /** Imposta il target verso cui la molla converge. */
    public void setTarget(double target) { this.target = target; }

    /** Forza la posizione corrente senza toccare la velocità. */
    public void setPosition(double position) { this.position = position; }

    /** Azzera velocità e posizione al target (reset istantaneo). */
    public void snap() { position = target; velocity = 0; }

    public double getPosition() { return position; }
    public double getVelocity() { return velocity; }
    public double getTarget()   { return target; }

    /** {@code true} se la molla è praticamente ferma (utile per ottimizzazioni). */
    public boolean isSettled(double threshold) {
        return Math.abs(velocity) < threshold && Math.abs(position - target) < threshold;
    }
}
