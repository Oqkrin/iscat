package uni.gaben.iscat.gamenex.universe.enemies.iscat_mob;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import uni.gaben.iscat.gamenex.lib.implementations.LivingEntityModel;
import uni.gaben.iscat.gamenex.lib.interfaces.model.HasProjectile;
import uni.gaben.iscat.gamenex.universe.UniverseCollisionLayers;
import uni.gaben.iscat.gamenex.universe.UniverseSettings;
import uni.gaben.iscat.gamenex.universe.projectiles.Projectile;
import uni.gaben.iscat.utils.Cooldown;

public class IscatMobModel extends LivingEntityModel implements HasProjectile<Projectile> {

    private final Projectile projectile = new Projectile();
    private final Cooldown weaponCooldown = new Cooldown();

    public IscatMobModel(double x, double y) {
        super(x, y, IscatMobSettings.HP_INIZIALI, IscatMobSettings.HP_INIZIALI);

        BodyFixture fixture = addFixture(
                Geometry.createCircle(IscatMobSettings.RAGGIO_COLLISIONE_PX / UniverseSettings.SCALE)
        );

        fixture.setFilter(UniverseCollisionLayers.ENEMY_FILTER);
        setMass(MassType.NORMAL);
        setLinearDamping(IscatMobSettings.DAMPING_LINEARE);
    }

    public void update(double dt) {
        weaponCooldown.update(dt);
    }

    // ==================== SHOOTING ====================

    public boolean isSparoDisponibile() {
        return weaponCooldown.isReady();
    }

    public void startCooldownFuoco() {
        weaponCooldown.start(IscatMobSettings.COOLDOWN_FUOCO_SEC);
    }

    @Override
    public Projectile getProjectile() {
        return projectile;
    }

    @Override
    public boolean hasAmmo() {
        return true;        // cambia in false se vuoi munizioni limitate
    }

    @Override
    public Cooldown projectileCooldown() {
        return weaponCooldown;
    }

    @Override
    public int getProjectileCooldownTickCount() {
        return (int) (IscatMobSettings.COOLDOWN_FUOCO_SEC * 60); // approssimazione
    }

    @Override
    public void setProjectileCooldownTickCount(int tickCount) {
        weaponCooldown.start(tickCount / 60.0);
    }

    @Override
    public double getTerminalVelocity() {
        return IscatMobSettings.MAX_VELOCITY_MS;
    }
}