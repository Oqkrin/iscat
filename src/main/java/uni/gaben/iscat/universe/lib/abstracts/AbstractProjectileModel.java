package uni.gaben.iscat.universe.lib.abstracts;

import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import uni.gaben.iscat.universe.lib.implementations.LivingEntityModel;
import uni.gaben.iscat.universe.UniverseModel;

public abstract class AbstractProjectileModel extends LivingEntityModel {
    protected double terminalVelocity;
    protected double baseAccelerationPerTick = 20.0;
    protected double size = 16;

    protected  AbstractProjectileModel(double maxPierceCount) {
        this(0, 0, maxPierceCount);
    }
    protected  AbstractProjectileModel(double x, double y, double maxPierceCount) {
        super(x, y, maxPierceCount, maxPierceCount);
        setBullet(true);
        addFixture(Geometry.createCircle(UniverseModel.getUniverseScaled(size)));
        setMass(MassType.NORMAL);
    }

    @Override
    public double getTerminalVelocity() {
        return terminalVelocity;
    }
    public void setTerminalVelocity(double v) { this.terminalVelocity = v; }
    public abstract AbstractProjectileModel blueprint();
}
