package uni.gaben.iscat.universe.enemies.mother;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import uni.gaben.iscat.universe.lib.implementations.LivingEntityModel;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseCollisionLayers;

import static uni.gaben.iscat.universe.enemies.mother.IscatMotherSettings.ISCATMOTHER;

public class IscatMotherModel extends LivingEntityModel {

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