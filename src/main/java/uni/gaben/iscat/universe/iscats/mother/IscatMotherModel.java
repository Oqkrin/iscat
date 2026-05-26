package uni.gaben.iscat.universe.iscats.mother;

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

import static uni.gaben.iscat.universe.iscats.mother.IscatMotherSettings.ISCATMOTHER;

/**
 * Modello fisico puro dell'IscatMother (boss).
 * Contiene solo stato e predicati: la logica comportamentale
 * (sparo, spawn minioni, orda) è nel Controller.
 */
public class IscatMotherModel extends LivingEntityModel implements HasProjectile<Projectile>, Updatable {

    private final Cooldown fireCooldown = new Cooldown();
    private boolean hasSpawnedMinions = false;

    public IscatMotherModel(double x, double y) {
        super(x, y, ISCATMOTHER.initLife, ISCATMOTHER.initLife);
        setXpReward(ISCATMOTHER.xpReward);

        // Hitbox: circle whose radius is 70% of the visible sprite radius
        double visualRadiusPx = (ISCATMOTHER.dimSprite * ISCATMOTHER.scale) / 2.0;
        BodyFixture fixture = addFixture(
                Geometry.createCircle(UU.pxToM(visualRadiusPx * 0.70)));
        fixture.setFilter(UniverseCollisionLayers.ENEMY_FILTER);
        setMass(MassType.NORMAL);
        setLinearDamping(ISCATMOTHER.dampingLineare);
    }

    // ─── LifeDeath ──────────────────────────────────────────────────────────

    public void update(double dt) {
        fireCooldown.update(dt);
    }

    // ─── Fire cooldown ──────────────────────────────────────────────────────

    public boolean isFireReady() {
        return fireCooldown.isReady();
    }

    public void startFireCooldown() {
        fireCooldown.start(ISCATMOTHER.fireCooldownS);
    }

    // ─── Minion spawn state ─────────────────────────────────────────────────

    public boolean hasSpawnedMinions() {
        return hasSpawnedMinions;
    }

    public void markMinionsSpawned() {
        this.hasSpawnedMinions = true;
    }

    public boolean shouldSpawnMinions() {
        return !hasSpawnedMinions
                && getLife() <= getMaxLife() * IscatMotherSettings.MINION_SPAWN_HP_THRESHOLD;
    }

    // ─── HasProjectile ──────────────────────────────────────────────────────

    @Override
    public Projectile getProjectile() {
        return new Projectile(ProjectileType.ENEMY_BULLET);
    }

    @Override
    public boolean hasAmmo() {
        return true;
    }

    @Override
    public Cooldown projectileCooldown() {
        return fireCooldown;
    }

    @Override
    public int getProjectileCooldownTickCount() {
        return (int) UU.sToTicks(ISCATMOTHER.fireCooldownS);
    }

    @Override
    public void setProjectileCooldownTickCount(int tickCount) {
        fireCooldown.start(UU.ticksToS(tickCount));
    }

    // ─── Terminal velocity ──────────────────────────────────────────────────

    @Override
    public double getTerminalVelocity() {
        return ISCATMOTHER.maxVelocity;
    }

    @Override
    public double getHeightMeters() {
        double visualRadiusPx = (ISCATMOTHER.dimSprite * ISCATMOTHER.scale) / 2.0;
        return UU.pxToM(visualRadiusPx * 0.70) * 2;
    }
}