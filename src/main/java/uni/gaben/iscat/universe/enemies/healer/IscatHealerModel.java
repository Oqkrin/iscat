package uni.gaben.iscat.universe.enemies.healer;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import uni.gaben.iscat.universe.lib.implementations.LivingEntityModel;
import uni.gaben.iscat.universe.lib.interfaces.model.Updatable;
import uni.gaben.iscat.universe.lib.interfaces.model.HasShockwave;
import uni.gaben.iscat.universe.rendering.vfx.ShockwaveModel;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseCollisionLayers;

import static uni.gaben.iscat.universe.enemies.healer.IscatHealerSettings.ISCATHEALER;

public class IscatHealerModel extends LivingEntityModel implements HasShockwave, Updatable {

    private ShockwaveModel healingWave = new ShockwaveModel();
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

    @Override
    public void update(double dt) {
        healingWave.update(dt);
    }
}
