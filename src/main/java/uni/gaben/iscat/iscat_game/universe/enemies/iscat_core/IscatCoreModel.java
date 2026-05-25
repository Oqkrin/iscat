package uni.gaben.iscat.iscat_game.universe.enemies.iscat_core;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import uni.gaben.iscat.iscat_game.lib.implementations.LivingEntityModel;
import uni.gaben.iscat.iscat_game.lib.interfaces.model.HasProjectile;
import uni.gaben.iscat.iscat_game.utils.UU;
import uni.gaben.iscat.iscat_game.universe.UniverseCollisionLayers;
import uni.gaben.iscat.iscat_game.universe.projectiles.Projectile;
import uni.gaben.iscat.iscat_game.universe.projectiles.ProjectileType;
import uni.gaben.iscat.utils.Cooldown;

import static uni.gaben.iscat.iscat_game.universe.enemies.iscat_core.IscatCoreSettings.ISCATCORE;

public class IscatCoreModel extends LivingEntityModel implements HasProjectile<Projectile> {

    private final Projectile projectile = new Projectile(ProjectileType.ENEMY_BULLET);
    private final Cooldown weaponCooldown = new Cooldown();

    public IscatCoreModel(double x, double y) {
        super(x, y, ISCATCORE.initLife, ISCATCORE.initLife);
        setXpReward(ISCATCORE.xpReward);

        BodyFixture fixture = addFixture(
                Geometry.createSquare(
                        UU.pxToM(ISCATCORE.dimSprite * ISCATCORE.scale * 0.9)));

        fixture.setFilter(UniverseCollisionLayers.ENEMY_FILTER);
        setMass(MassType.NORMAL);
        setLinearDamping(ISCATCORE.dampingLineare);
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

    public double getTerminalVelocity() {
        return ISCATCORE.maxVelocity;
    }
}