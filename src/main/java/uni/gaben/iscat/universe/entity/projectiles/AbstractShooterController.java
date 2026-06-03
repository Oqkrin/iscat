package uni.gaben.iscat.universe.entity.projectiles;

import org.dyn4j.collision.CollisionBody;

public abstract class AbstractShooterController<T extends CollisionBody> {

    protected T model;

    protected AbstractShooterController(T model) {
        this.model = model;
    }

    public abstract void shoot(ProjectileType type);

    protected abstract AbstractProjectileModel shootingLogic(ProjectileType type);
}