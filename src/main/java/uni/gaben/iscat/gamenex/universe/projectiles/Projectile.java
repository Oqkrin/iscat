package uni.gaben.iscat.gamenex.universe.projectiles;

import uni.gaben.iscat.gamenex.lib.abstracts.AbstractProjectileModel;

public class Projectile extends AbstractProjectileModel {
    @Override
    public AbstractProjectileModel blueprint() {
        return new Projectile();
    }
}
