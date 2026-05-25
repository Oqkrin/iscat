package uni.gaben.iscat.iscat_game.universe.enemies.fallen_star_golem;

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

import static uni.gaben.iscat.iscat_game.universe.enemies.fallen_star_golem.FallenStarGolemSettings.FALLENSTARGOLEM;

public class FallenStarGolemModel extends LivingEntityModel implements HasProjectile<Projectile> {

    private final Projectile projectile = new Projectile(ProjectileType.ENEMY_BULLET);
    private final Cooldown weaponCooldown = new Cooldown();

    public FallenStarGolemModel(double x, double y) {
        super(x, y, FALLENSTARGOLEM.initLife, FALLENSTARGOLEM.initLife);
        setXpReward(FALLENSTARGOLEM.xpReward);

        BodyFixture fixture = addFixture(
                Geometry.createCircle(
                        UU.pxToM(FALLENSTARGOLEM.dimSprite * FALLENSTARGOLEM.scale / 2.0 * 0.9)));

        fixture.setFilter(UniverseCollisionLayers.ENEMY_FILTER);
        setMass(MassType.NORMAL);
        setLinearDamping(FALLENSTARGOLEM.dampingLineare);
    }

    public void update(double dt) {
        weaponCooldown.update(dt);
    }

    public boolean isSparoDisponibile() {
        return weaponCooldown.isReady();
    }

    public void startCooldownFuoco() {
        weaponCooldown.start(FALLENSTARGOLEM.fireCooldownS);
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
        return (int) UU.sToTicks(FALLENSTARGOLEM.fireCooldownS);
    }

    @Override
    public void setProjectileCooldownTickCount(int tickCount) {
        weaponCooldown.start(UU.ticksToS(tickCount));
    }

    @Override
    public double getTerminalVelocity() {
        return FALLENSTARGOLEM.maxVelocity;
    }
}