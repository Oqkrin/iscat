package uni.gaben.iscat.iscat_game.universe.enemies.iscat_dasher;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.iscat_game.lib.implementations.LivingEntityModel;
import uni.gaben.iscat.iscat_game.utils.UU;
import uni.gaben.iscat.iscat_game.universe.UniverseCollisionLayers;
import uni.gaben.iscat.iscat_game.universe.player.PlayerModel;

import static uni.gaben.iscat.iscat_game.universe.enemies.iscat_dasher.IscatDasherSettings.ISCATDASHER;

public class IscatDasherModel extends LivingEntityModel {
    public IscatDasherModel(double x, double y) {
        this(x, y, ISCATDASHER.initLife, ISCATDASHER.initLife);
    }
    public IscatDasherModel(double x, double y, double life, double maxLife) {
        super(x, y, life, maxLife);
        setXpReward(ISCATDASHER.xpReward);

        BodyFixture fixture = addFixture(
                Geometry.createCircle(
                        UU.pxToM(ISCATDASHER.dimSprite * ISCATDASHER.scale / 2.0 * 0.9)));

        fixture.setFilter(UniverseCollisionLayers.ENEMY_FILTER);
        setMass(MassType.NORMAL);
        setLinearDamping(ISCATDASHER.dampingLineare);
        
        // Damage tick on collision proportional to velocity
        this.setOnCollision(other -> {
            if (other instanceof PlayerModel player) {
                Vector2 vel = this.getLinearVelocity();
                double speed = vel.getMagnitude();
                if (speed > 2.0) {
                    player.deltaToLife(-IscatDasherSettings.BASE_TICK_DAMAGE * speed * 0.1);
                }
            }
        });
    }

    @Override
    public double getTerminalVelocity() {
        return ISCATDASHER.maxVelocity * 1.5; // Can exceed a bit during dash
    }
}
