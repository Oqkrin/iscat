package uni.gaben.iscat.gamenex.lib.interfaces.model;


import uni.gaben.iscat.gamenex.lib.abstracts.AbstractProjectileModel;

public interface Shooter<T extends AbstractProjectileModel> {
    T getProjectile();
    void shoot();
    void onShoot();
}
