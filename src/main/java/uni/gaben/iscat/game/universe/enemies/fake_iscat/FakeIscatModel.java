package uni.gaben.iscat.game.universe.enemies.fake_iscat;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import uni.gaben.iscat.game.lib.implementations.LivingEntityModel;
import uni.gaben.iscat.game.lib.interfaces.model.HasProjectile;
import uni.gaben.iscat.game.lib.utils.UU;
import uni.gaben.iscat.game.universe.UniverseCollisionLayers;
import uni.gaben.iscat.game.universe.projectiles.Projectile;
import uni.gaben.iscat.utils.Cooldown;

public class FakeIscatModel extends LivingEntityModel implements HasProjectile<Projectile> {

    private final Projectile projectile = new Projectile();
    private final Cooldown weaponCooldown = new Cooldown();

    public FakeIscatModel(double x, double y) {
        super(x, y, FakeIscatSettings.HP_INIZIALI, FakeIscatSettings.HP_INIZIALI);

        BodyFixture fixture = addFixture(
                Geometry.createCircle(
                        UU.pxToM(FakeIscatSettings.DIM_SPRITE * FakeIscatSettings.SCALE / 2.0 * 0.9)));

        fixture.setFilter(UniverseCollisionLayers.ENEMY_FILTER);
        setMass(MassType.NORMAL);
        setLinearDamping(FakeIscatSettings.DAMPING_LINEARE);
    }

    public void update(double dt) {
        weaponCooldown.update(dt);
    }

    public boolean isSparoDisponibile() {
        return weaponCooldown.isReady();
    }

    public void startCooldownFuoco() {
        weaponCooldown.start(FakeIscatSettings.COOLDOWN_FUOCO_SEC);
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
        return (int) UU.sToTicks(FakeIscatSettings.COOLDOWN_FUOCO_SEC);
    }

    @Override
    public void setProjectileCooldownTickCount(int tickCount) {
        weaponCooldown.start(UU.ticksToS(tickCount));
    }

    @Override
    public double getTerminalVelocity() {
        return FakeIscatSettings.MAX_VELOCITY_MS;
    }
}