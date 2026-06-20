package uni.gaben.iscat.universe.entities.hardcoded.projectiles;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.entities.interfaces.Stunnable;
import uni.gaben.iscat.utils.Updatable;

public class StunThreadProjectileModel extends ProjectileModel {

    public static final double DEFAULT_STUN_DURATION = 0.5;
    private double stunDuration;
    private final Vector2 wave = UU.vector2zero();

    private Vector2 forwardDir;              // cached travel direction (set once in constructor)
    private Vector2 perpendicular;           // cached perpendicular vector (90° left)
    private double amplitude = 9.0;          // how hard the lateral push is (force units)
    private double frequency = 1.2;          // oscillations per second
    private boolean justSpawned = true;


    public StunThreadProjectileModel() {
        this(UU.vector2zero(), UU.vector2zero(), 0, DEFAULT_STUN_DURATION);
    }

    public StunThreadProjectileModel(Vector2 position, Vector2 direction, double speed) {
        this(position, direction, speed, DEFAULT_STUN_DURATION);
    }

    public StunThreadProjectileModel(Vector2 position, Vector2 direction, double speed, double stunDuration) {
        super(ProjectileType.STUN_BULLET);
        this.stunDuration = stunDuration;
        getTransform().setTranslation(position);
        setLinearVelocity(direction.multiply(speed));
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
        super.update(dt);  // normal integration (collision detection, etc.)

        if (justSpawned) {
            forwardDir = getLinearVelocity().getNormalized();
            perpendicular = forwardDir.getLeftHandOrthogonalVector();    // unit vector, 90° counter‑clockwise
            justSpawned = false;
        }

        // Lateral speed oscillates (world units per second)
        double lateralSpeed = amplitude * Math.sin(2 * Math.PI * frequency * getStateTime());

        // Compose final velocity = forward + lateral
        wave.set(forwardDir).multiply(getTerminalVelocity()).add(perpendicular.copy().multiply(lateralSpeed));
        setLinearVelocity(wave);
    }

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
}