package uni.gaben.iscat.universe.enemies.fake;

import uni.gaben.iscat.universe.lib.abstracts.AbstractEnemyModel;
import uni.gaben.iscat.universe.lib.interfaces.model.HasProjectile;
import uni.gaben.iscat.universe.lib.interfaces.model.Updatable;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.projectiles.Projectile;
import uni.gaben.iscat.universe.projectiles.ProjectileType;
import uni.gaben.iscat.utils.Cooldown;

public class FakeIscatModel extends AbstractEnemyModel implements HasProjectile<Projectile>, Updatable {

    private final Projectile projectile = new Projectile(ProjectileType.ENEMY_BULLET);
    private final Cooldown weaponCooldown = new Cooldown();

    public FakeIscatModel(double x, double y) {
        super(x, y, FakeIscatSettings.FAKEISCAT);
    }

    public void update(double dt) {
        weaponCooldown.update(dt);
    }

    @Override
    public Projectile getProjectile() {
        return projectile;
    }

    @Override
    public boolean hasAmmo() {
        return true;
    }

    @Override
    public Cooldown projectileCooldown() {
        return weaponCooldown;
    }

    @Override
    public int getProjectileCooldownTickCount() {
        return 0;
    }

    @Override
    public void setProjectileCooldownTickCount(int tickCount) {
        weaponCooldown.start(UU.ticksToS(tickCount));
    }
}