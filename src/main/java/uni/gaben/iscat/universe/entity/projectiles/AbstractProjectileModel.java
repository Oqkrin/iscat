package uni.gaben.iscat.universe.entity.projectiles;

import org.dyn4j.geometry.MassType;
import uni.gaben.iscat.universe.entity.AbstractLivingModel;

/**
 * Base class for all projectile entities.
 * Concrete subclasses (e.g. {@link ProjectileProjectileModel}) are responsible for adding their own
 * fixture via {@code setType()} — no default fixture is added here to avoid double-allocation.
 */
public abstract class AbstractProjectileModel extends AbstractLivingModel {
    protected double terminalVelocity;
    protected double baseAccelerationPerTick = 20.0;

    protected AbstractProjectileModel(double maxLife) {
        this(0, 0, maxLife);
    }

    protected AbstractProjectileModel(double x, double y, double maxLife) {
        super(x, y, maxLife, maxLife);
        setBullet(true);
        setMass(MassType.NORMAL);
    }

    @Override
    public double getTerminalVelocity() { return terminalVelocity; }
    public void setTerminalVelocity(double v) { this.terminalVelocity = v; }
}
