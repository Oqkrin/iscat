package uni.gaben.iscat.game.lib.interfaces.model;


import uni.gaben.iscat.game.lib.abstracts.AbstractProjectileModel;
import uni.gaben.iscat.utils.Cooldown;

public interface HasProjectile<T extends AbstractProjectileModel> {
    T getProjectile();
    boolean hasAmmo();
    Cooldown projectileCooldown();

    int getProjectileCooldownTickCount();
    void setProjectileCooldownTickCount(int tickCount);
}
