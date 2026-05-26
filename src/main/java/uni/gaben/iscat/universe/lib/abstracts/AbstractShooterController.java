package uni.gaben.iscat.universe.lib.abstracts;

import org.dyn4j.collision.CollisionBody;

public abstract class AbstractShooterController<T extends CollisionBody> {

    protected T model;

    protected AbstractShooterController(T model) {
        this.model = model;
    }

    public abstract void shoot(AbstractProjectileModel template);

    protected abstract AbstractProjectileModel shootingLogic(AbstractProjectileModel projectile);
}