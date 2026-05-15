package uni.gaben.iscat.gamenex.lib.abstracts;

import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import uni.gaben.iscat.gamenex.lib.implementations.LivingEntityModel;
import uni.gaben.iscat.gamenex.universe.UniverseModel;

public abstract class AbstractProjectileModel extends LivingEntityModel {
    protected double baseAccelerationPerTick = 20.0;
    protected double terminalVelocity = 30.0;
    protected double size = 32;
    protected AbstractProjectileModel() {
        this(4000);

    }

    protected  AbstractProjectileModel(int maxPierceCount) {
        this(0, 0, maxPierceCount);
    }

    protected  AbstractProjectileModel(double x,  double y, int maxPierceCount) {
        super(x, y, maxPierceCount, maxPierceCount);
        setBullet(true);
        addFixture(Geometry.createCircle(UniverseModel.getUniverseScaled(size)));
        setMass(MassType.NORMAL);
    }

    @Override
    public double getTerminalVelocity() {
        return terminalVelocity;
    }

    public abstract AbstractProjectileModel blueprint();
}
