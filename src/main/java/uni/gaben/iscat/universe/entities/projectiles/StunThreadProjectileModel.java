package uni.gaben.iscat.universe.entities.projectiles;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.entities.interfaces.Stunnable;

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

    // ---- constructors ----
    public StunThreadProjectileModel() {
        this(UU.vector2zero(), UU.vector2zero(), DEFAULT_FORWARD_SPEED, DEFAULT_AMPLITUDE, DEFAULT_FREQUENCY);
    }

    public StunThreadProjectileModel(Vector2 position, Vector2 direction, double forwardSpeed) {
        this(position, direction, forwardSpeed, DEFAULT_AMPLITUDE, DEFAULT_FREQUENCY);
    }

    public StunThreadProjectileModel(Vector2 position, Vector2 direction,
                                     double forwardSpeed, double amplitude, double frequency) {
        this(position, direction, forwardSpeed, amplitude, frequency, DEFAULT_STUN_DURATION);
    }

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

        if (justSpawned) {
            Vector2 fwd = getLinearVelocity().getNormalized();
            trajectoryModifier = new TrajectoryModifier(
                    fwd, fwd.getLeftHandOrthogonalVector(),
                    amplitude, frequency, 0.0   // or a non‑zero phaseOffset if you like
            );
            justSpawned = false;
        }

        trajectoryModifier.update(dt);
        setLinearVelocity(trajectoryModifier.getTrajectory(forwardSpeed));
    }

    // Make setters actually useful
    public void setAmplitude(double amplitude) {
        this.amplitude = amplitude;
        if (trajectoryModifier != null) trajectoryModifier.setAmplitude(amplitude);
    }

    public void setFrequency(double frequency) {
        this.frequency = frequency;
        if (trajectoryModifier != null) trajectoryModifier.setFrequency(frequency);
    }

    // ---- stun collision ----
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

    public double getStunDuration() { return stunDuration; }
    public double getForwardSpeed() { return forwardSpeed; }
    public void setForwardSpeed(double forwardSpeed) { this.forwardSpeed = forwardSpeed; }
    public double getAmplitude() { return amplitude; }
    public double getFrequency() { return frequency; }
}