package uni.gaben.iscat.gamenex.universe.enemies.fallen_star_golem;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import uni.gaben.iscat.gamenex.lib.implementations.LivingEntityModel;
import uni.gaben.iscat.gamenex.lib.interfaces.model.HasProjectile;
import uni.gaben.iscat.gamenex.universe.UniverseCollisionLayers;
import uni.gaben.iscat.gamenex.universe.UniverseSettings;
import uni.gaben.iscat.gamenex.universe.projectiles.Projectile;
import uni.gaben.iscat.utils.Cooldown;

public class FallenStarGolemModel extends LivingEntityModel implements HasProjectile<Projectile> {

    private final Projectile projectile = new Projectile();
    private final Cooldown weaponCooldown = new Cooldown();

    public FallenStarGolemModel(double x, double y) {
        super(x, y, FallenStarGolemSettings.HP_INIZIALI, FallenStarGolemSettings.HP_INIZIALI);

        BodyFixture fixture = addFixture(
                Geometry.createCircle(
                        FallenStarGolemSettings.RAGGIO_COLLISIONE_PX / UniverseSettings.SCALE
                )
        );

        fixture.setFilter(UniverseCollisionLayers.ENEMY_FILTER);
        setMass(MassType.NORMAL);
        setLinearDamping(FallenStarGolemSettings.DAMPING_LINEARE);
    }

    public void update(double dt) {
        weaponCooldown.update(dt);
    }

    public boolean isSparoDisponibile() {
        return weaponCooldown.isReady();
    }

    public void startCooldownFuoco() {
        weaponCooldown.start(FallenStarGolemSettings.COOLDOWN_FUOCO_SEC);
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
        return (int) (FallenStarGolemSettings.COOLDOWN_FUOCO_SEC * 60);
    }

    @Override
    public void setProjectileCooldownTickCount(int tickCount) {
        weaponCooldown.start(tickCount / 60.0);
    }

    @Override
    public double getTerminalVelocity() {
        return FallenStarGolemSettings.MAX_VELOCITY_MS;
    }
}