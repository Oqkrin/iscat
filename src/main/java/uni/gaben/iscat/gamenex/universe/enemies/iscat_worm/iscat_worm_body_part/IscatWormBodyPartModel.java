package uni.gaben.iscat.gamenex.universe.enemies.iscat_worm.iscat_worm_body_part;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.gamenex.lib.implementations.LivingEntityModel;
import uni.gaben.iscat.gamenex.universe.UniverseCollisionLayers;
import uni.gaben.iscat.gamenex.universe.UniverseSettings;
import uni.gaben.iscat.gamenex.universe.enemies.iscat_worm.IscatWormSegment;

public class IscatWormBodyPartModel extends LivingEntityModel implements IscatWormSegment {

    private boolean consumed = false;

    public IscatWormBodyPartModel(double x, double y) {
        super(x, y, IscatWormBodyPartSettings.HP_INIZIALI, IscatWormBodyPartSettings.HP_INIZIALI); // usa HP_INIZIALI dalle settings

        BodyFixture fixture = addFixture(
                Geometry.createCircle(IscatWormBodyPartSettings.RAGGIO_COLLISIONE_PX / UniverseSettings.SCALE)
        );

        fixture.setFilter(UniverseCollisionLayers.ENEMY_FILTER);
        setMass(MassType.NORMAL);
        setLinearDamping(IscatWormBodyPartSettings.DAMPING_LINEARE);
    }

    @Override
    public void onDeath() {
        super.onDeath();
    }

    @Override
    public double getTerminalVelocity() {
        return IscatWormBodyPartSettings.MAX_VELOCITY_MS;
    }

    public boolean isConsumed() {
        return consumed;
    }

    public void consume() {
        this.consumed = true;
    }

    @Override
    public Vector2 getPosition() {
        return getTransform().getTranslation();
    }

    @Override
    public void setRotation(double angle) {
        getTransform().setRotation(angle);
    }

    @Override
    public double getRotation() {
        return getTransform().getRotationAngle();
    }
}