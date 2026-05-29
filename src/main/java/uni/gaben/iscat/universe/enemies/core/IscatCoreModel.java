package uni.gaben.iscat.universe.enemies.core;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import uni.gaben.iscat.universe.lib.implementations.LivingEntityModel;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseCollisionLayers;

import static uni.gaben.iscat.universe.enemies.core.IscatCoreSettings.ISCATCORE;

public class IscatCoreModel extends LivingEntityModel {

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
        updateStateTime(dt);
    }

    public double getTerminalVelocity() {
        return ISCATCORE.maxVelocity;
    }
}