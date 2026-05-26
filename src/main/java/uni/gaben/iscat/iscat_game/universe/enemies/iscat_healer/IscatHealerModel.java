package uni.gaben.iscat.iscat_game.universe.enemies.iscat_healer;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import uni.gaben.iscat.iscat_game.lib.implementations.LivingEntityModel;
import uni.gaben.iscat.iscat_game.rendering.effects.HasShockwave;
import uni.gaben.iscat.iscat_game.rendering.effects.ShockwaveModel;
import uni.gaben.iscat.iscat_game.utils.UU;
import uni.gaben.iscat.iscat_game.universe.UniverseCollisionLayers;

import static uni.gaben.iscat.iscat_game.universe.enemies.iscat_healer.IscatHealerSettings.ISCATHEALER;

public class IscatHealerModel extends LivingEntityModel implements HasShockwave {

    private ShockwaveModel healingWave;
    public IscatHealerModel(double x, double y) {
        super(x, y, ISCATHEALER.initLife, ISCATHEALER.initLife);
        setXpReward(ISCATHEALER.xpReward);

        BodyFixture fixture = addFixture(
                Geometry.createCircle(
                        UU.pxToM(ISCATHEALER.dimSprite * ISCATHEALER.scale / 2.0 * 0.9)));

        fixture.setFilter(UniverseCollisionLayers.ENEMY_FILTER);
        setMass(MassType.NORMAL);
        setLinearDamping(ISCATHEALER.dampingLineare);
    }



    @Override
    public ShockwaveModel shockwave() {
        return healingWave;
    }
}
