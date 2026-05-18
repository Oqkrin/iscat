package uni.gaben.iscat.gamenex.lib.abstracts;

import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import uni.gaben.iscat.gamenex.lib.implementations.LivingEntityModel;
import uni.gaben.iscat.gamenex.universe.UniverseModel;

public abstract class AbstractProjectileModel extends LivingEntityModel {
    protected double terminalVelocity;
    protected double damage;
    protected double lifespan;
    protected double baseAccelerationPerTick = 20.0;
    protected double size = 16;
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
    public void setTerminalVelocity(double v) { this.terminalVelocity = v; }

    public double getDamage() { return damage; }
    public void setDamage(double d) { this.damage = d; }

    public double getLifespan() { return lifespan; }
    public void setLifespan(double l) { this.lifespan = l; }

    public abstract AbstractProjectileModel blueprint();
}
