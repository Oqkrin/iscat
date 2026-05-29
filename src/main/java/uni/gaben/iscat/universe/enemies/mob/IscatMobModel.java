package uni.gaben.iscat.universe.enemies.mob;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import uni.gaben.iscat.universe.lib.implementations.LivingEntityModel;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseCollisionLayers;
import static uni.gaben.iscat.universe.enemies.mob.IscatMobSettings.ISCATMOB;

public class IscatMobModel extends LivingEntityModel {

    public IscatMobModel(double x, double y) {
        super(x, y, ISCATMOB.initLife, ISCATMOB.initLife);
        setXpReward(ISCATMOB.xpReward);

        BodyFixture fixture = addFixture(
                Geometry.createCircle(
                        UU.pxToM(ISCATMOB.dimSprite * ISCATMOB.scale / 2.0 * 0.9)));

        fixture.setFilter(UniverseCollisionLayers.ENEMY_FILTER);
        setMass(MassType.NORMAL);
        setLinearDamping(ISCATMOB.dampingLineare);
    }

    @Override
    public double getTerminalVelocity() {
        return ISCATMOB.maxVelocity;
    }
}