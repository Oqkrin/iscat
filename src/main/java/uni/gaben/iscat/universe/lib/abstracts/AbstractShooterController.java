package uni.gaben.iscat.universe.lib.abstracts;

import org.dyn4j.collision.CollisionBody;

import uni.gaben.iscat.universe.projectiles.ProjectileType;

public abstract class AbstractShooterController<T extends CollisionBody> {

    protected T model;

    protected AbstractShooterController(T model) {
        this.model = model;
    }

    public abstract void shoot(ProjectileType type);

    protected abstract AbstractProjectileModel shootingLogic(ProjectileType type);
}