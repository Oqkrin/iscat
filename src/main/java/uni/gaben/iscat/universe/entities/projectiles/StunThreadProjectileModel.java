package uni.gaben.iscat.universe.entities.projectiles;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.entities.interfaces.Stunnable;

/**
 * Modello di proiettile speciale ("Stun Thread") che si muove lungo una traiettoria
 * ondulatoria (sinusoidale) e applica uno stato di stordimento (stun) ai bersagli colpiti.
 */
public class StunThreadProjectileModel extends ProjectileModel {

    public static final double DEFAULT_STUN_DURATION = 1.5;
    public static final double DEFAULT_FORWARD_SPEED = 10.0;
    public static final double DEFAULT_AMPLITUDE    = 10.0;
    public static final double DEFAULT_FREQUENCY    = 1.0;

    private double stunDuration;
    private double forwardSpeed;
    private double amplitude;
    private double frequency;

    private TrajectoryModifier trajectoryModifier;
    private boolean justSpawned = true;

    /**
     * Costruttore di default. Inizializza il proiettile al centro del mondo,
     * fermo e con i parametri di oscillazione e stordimento predefiniti.
     */
    public StunThreadProjectileModel() {
        this(UU.vector2zero(), UU.vector2zero(), DEFAULT_FORWARD_SPEED, DEFAULT_AMPLITUDE, DEFAULT_FREQUENCY);
    }

    /**
     * Costruttore intermedio con durata dello stordimento predefinita.
     *
     * @param position     La posizione iniziale del proiettile.
     * @param direction    Il vettore di direzione del movimento principale.
     * @param forwardSpeed La velocità di avanzamento lineare dell'asse.
     * @param amplitude    L'ampiezza dell'oscillazione laterale.
     * @param frequency    La frequenza dell'oscillazione.
     */
    public StunThreadProjectileModel(Vector2 position, Vector2 direction,
                                     double forwardSpeed, double amplitude, double frequency) {
        this(position, direction, forwardSpeed, amplitude, frequency, DEFAULT_STUN_DURATION);
    }

    /**
     * Costruttore completo per definire esplicitamente tutti i parametri fisici e di stato.
     *
     * @param position     La posizione iniziale del proiettile.
     * @param direction    Il vettore di direzione del movimento principale.
     * @param forwardSpeed La velocità di avanzamento lineare dell'asse.
     * @param amplitude    L'ampiezza dell'oscillazione laterale.
     * @param frequency    La frequenza dell'oscillazione.
     * @param stunDuration La durata dell'effetto di stordimento sul bersaglio colpito (in secondi).
     */
    public StunThreadProjectileModel(Vector2 position, Vector2 direction,
                                     double forwardSpeed, double amplitude,
                                     double frequency, double stunDuration) {
        super(ProjectileType.STUN_BULLET);
        this.forwardSpeed = forwardSpeed;
        this.amplitude = amplitude;
        this.frequency = frequency;
        this.stunDuration = stunDuration;
        getTransform().setTranslation(position);
        setLinearVelocity(direction.getNormalized().multiply(forwardSpeed));
        setMaxEnduranceDirect(1.0);
        setEndurance(1.0);
        setStunCollisionHandler(stunDuration);
    }

    @Override
    public void reset(ProjectileType type) {
        super.reset(type);
        this.justSpawned = true;
        setStunCollisionHandler(DEFAULT_STUN_DURATION);
    }

    @Override
    public void update(double dt) {
        super.update(dt);

        // Al primo update dopo lo spawn, inizializza il modificatore di traiettoria
        // basandosi sui vettori ortogonali della velocità lineare corrente.
        if (justSpawned) {
            Vector2 fwd = getLinearVelocity().getNormalized();
            trajectoryModifier = new TrajectoryModifier(
                    fwd, fwd.getLeftHandOrthogonalVector(),
                    amplitude, frequency, 0.0
            );
            justSpawned = false;
        }

        trajectoryModifier.update(dt);
        setLinearVelocity(trajectoryModifier.getTrajectory(forwardSpeed));
    }

    /**
     * Configura il gestore delle collisioni del proiettile per applicare l'effetto di stun.
     * Pulisce i vecchi handler e aggiunge la logica di interazione fisica (impulso) e logica (stordimento).
     *
     * @param duration Durata dell'effetto di stordimento (in secondi).
     */
    public void setStunCollisionHandler(double duration) {
        this.stunDuration = duration;
        clearOnCollisions();
        addOnCollision("stun", other -> {
            other.applyImpulse(getLinearVelocity().copy().multiply(12000));
            if (other instanceof Stunnable toStun) {
                toStun.stun(duration);
                extinguish(true);
            } else if (!(other instanceof StunThreadProjectileModel)) {
                extinguish(true);
            }
        });
    }
}