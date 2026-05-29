package uni.gaben.iscat.universe.enemies.fallen_star_golem;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseCollisionLayers;
import uni.gaben.iscat.universe.lib.implementations.LivingEntityModel;


import static uni.gaben.iscat.universe.enemies.fallen_star_golem.FallenStarGolemSettings.FALLENSTARGOLEM;

public class FallenStarGolemModel extends LivingEntityModel {

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



    @Override
    public double getTerminalVelocity() {
        return FALLENSTARGOLEM.maxVelocity;
    }
}