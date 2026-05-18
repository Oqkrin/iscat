package uni.gaben.iscat.gamenex.universe.enemies.iscat_worm.iscat_worm_head;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.gamenex.lib.implementations.LivingEntityModel;
import uni.gaben.iscat.gamenex.universe.UniverseCollisionLayers;
import uni.gaben.iscat.gamenex.universe.UniverseSettings;
import uni.gaben.iscat.gamenex.universe.enemies.iscat_worm.IscatWormSegment;

public class IscatWormHeadModel extends LivingEntityModel implements IscatWormSegment {

    private boolean consumed = false;

    public IscatWormHeadModel(double x, double y) {
        super(x, y, IscatWormHeadSettings.HP_INIZIALI, IscatWormHeadSettings.HP_INIZIALI);

        BodyFixture fixture = addFixture(
                Geometry.createCircle(IscatWormHeadSettings.RAGGIO_COLLISIONE_PX / UniverseSettings.SCALE)
        );

        fixture.setFilter(UniverseCollisionLayers.ENEMY_FILTER);
        setMass(MassType.NORMAL);
        setLinearDamping(IscatWormHeadSettings.DAMPING_LINEARE);
    }

    @Override
    public void onDeath() {
        super.onDeath();
    }

    @Override
    public double getTerminalVelocity() {
        return IscatWormHeadSettings.MAX_VELOCITY_MS;
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