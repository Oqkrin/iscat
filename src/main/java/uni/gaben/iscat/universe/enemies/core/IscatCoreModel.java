package uni.gaben.iscat.universe.enemies.core;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import uni.gaben.iscat.universe.lib.implementations.LivingEntityModel;
import uni.gaben.iscat.universe.lib.interfaces.model.HasProjectile;
import uni.gaben.iscat.universe.lib.interfaces.model.Updatable;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseCollisionLayers;
import uni.gaben.iscat.universe.projectiles.Projectile;
import uni.gaben.iscat.universe.projectiles.ProjectileType;
import uni.gaben.iscat.utils.Cooldown;

import static uni.gaben.iscat.universe.enemies.core.IscatCoreSettings.ISCATCORE;

public class IscatCoreModel extends LivingEntityModel implements HasProjectile<Projectile>, Updatable {

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

        this.setOnCollision(other -> {
            if (other instanceof uni.gaben.iscat.universe.player.PlayerModel player) {
                org.dyn4j.geometry.Vector2 vel = this.getLinearVelocity();
                double speed = vel.getMagnitude();
                if (speed > ISCATCORE.maxVelocity * 1.5) {
                    player.deltaToLife(-30.0); // Heavy Slam Damage
                } else {
                    player.deltaToLife(-2.0); // Light Contact Damage
                }
            }
        });
    }

    public void update(double dt) {
        weaponCooldown.update(dt);
        updateStateTime(dt);
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