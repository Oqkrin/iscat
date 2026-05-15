package uni.gaben.iscat.gamenex.lib.interfaces.model;


import uni.gaben.iscat.gamenex.lib.abstracts.AbstractProjectileModel;
import uni.gaben.iscat.utils.Cooldown;

public interface HasProjectile<T extends AbstractProjectileModel> {
    T getProjectile();
    boolean hasAmmo();
    Cooldown projectileCooldown();

    int getProjectileCooldownTickCount();
    void setProjectileCooldownTickCount(int tickCount);
}
