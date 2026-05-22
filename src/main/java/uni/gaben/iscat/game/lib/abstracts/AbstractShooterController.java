package uni.gaben.iscat.game.lib.abstracts;

import org.dyn4j.collision.CollisionBody;
import uni.gaben.iscat.game.lib.interfaces.model.HasProjectile;
import uni.gaben.iscat.game.universe.UniverseSpawner;

public abstract class AbstractShooterController<T extends CollisionBody & HasProjectile> {

    protected T model;

    protected AbstractShooterController(T model) {
        this.model = model;
    }

    public abstract void shoot(AbstractProjectileModel template);

    protected abstract AbstractProjectileModel shootingLogic(AbstractProjectileModel projectile);

}
