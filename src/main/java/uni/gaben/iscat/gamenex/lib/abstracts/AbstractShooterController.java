package uni.gaben.iscat.gamenex.lib.abstracts;

import org.dyn4j.collision.CollisionBody;
import uni.gaben.iscat.gamenex.lib.interfaces.model.HasProjectile;
import uni.gaben.iscat.gamenex.universe.UniverseSpawner;

public abstract class AbstractShooterController<T extends CollisionBody & HasProjectile> {

    protected T model;

    protected AbstractShooterController(T model) {
        this.model = model;
    }

    public final void shoot() {
        if(model.projectileCooldown().isCoolingDown()) return;
        for (var p : shootingLogic(model.getProjectile())) {
            UniverseSpawner.getInstance().spawnProjectile(p);
        }
        model.projectileCooldown().set(model.getProjectileCooldownTickCount());
    }

    protected abstract AbstractProjectileModel[] shootingLogic(AbstractProjectileModel projectile);

}
