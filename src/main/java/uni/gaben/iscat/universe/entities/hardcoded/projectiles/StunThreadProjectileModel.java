package uni.gaben.iscat.universe.entities.hardcoded.projectiles;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.entities.interfaces.Stunnable;

public class StunThreadProjectileModel extends ProjectileModel {

    public static final double DEFAULT_STUN_DURATION = 0.5;
    private double stunDuration;

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
        setStunCollisionHandler(DEFAULT_STUN_DURATION);
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