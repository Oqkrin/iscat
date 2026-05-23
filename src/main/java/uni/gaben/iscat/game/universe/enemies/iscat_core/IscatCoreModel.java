package uni.gaben.iscat.game.universe.enemies.iscat_core;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import uni.gaben.iscat.game.lib.implementations.LivingEntityModel;
import uni.gaben.iscat.game.lib.interfaces.model.HasProjectile;
import uni.gaben.iscat.game.lib.utils.UU;
import uni.gaben.iscat.game.universe.UniverseCollisionLayers;
import uni.gaben.iscat.game.universe.projectiles.Projectile;
import uni.gaben.iscat.game.universe.projectiles.ProjectileType;
import uni.gaben.iscat.utils.Cooldown;

public class IscatCoreModel extends LivingEntityModel implements HasProjectile<Projectile> {

    private final Projectile projectile = new Projectile(ProjectileType.ENEMY_BULLET);
    private final Cooldown weaponCooldown = new Cooldown();

    public IscatCoreModel(double x, double y) {
        super(x, y, IscatCoreSettings.HP_INIZIALI, IscatCoreSettings.HP_INIZIALI);
        setXpReward(IscatCoreSettings.XP_REWARD);

        BodyFixture fixture = addFixture(
                Geometry.createSquare(
                        UU.pxToM(IscatCoreSettings.DIM_SPRITE * IscatCoreSettings.SCALE * 0.9)));

        fixture.setFilter(UniverseCollisionLayers.ENEMY_FILTER);
        setMass(MassType.NORMAL);
        setLinearDamping(IscatCoreSettings.DAMPING_LINEARE);
    }

    public void update(double dt) {
        weaponCooldown.update(dt);
    }

    // ==================== SHOOTING ====================

    @Override
    public Projectile getProjectile() {
        return projectile;
    }

    @Override
    public boolean hasAmmo() {
        return true; // cambia in false se vuoi munizioni limitate
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