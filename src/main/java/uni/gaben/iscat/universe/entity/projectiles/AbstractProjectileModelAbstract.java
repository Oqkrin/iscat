package uni.gaben.iscat.universe.entity.projectiles;

import org.dyn4j.geometry.MassType;
import uni.gaben.iscat.universe.entity.AbstractLivingModel;

/**
 * Base class for all projectile entities.
 * Concrete subclasses (e.g. {@link Projectile}) are responsible for adding their own
 * fixture via {@code setType()} — no default fixture is added here to avoid double-allocation.
 */
public abstract class AbstractProjectileModelAbstract extends AbstractLivingModel {
    protected double terminalVelocity;
    protected double baseAccelerationPerTick = 20.0;

    protected AbstractProjectileModelAbstract(double maxLife) {
        this(0, 0, maxLife);
    }

    protected AbstractProjectileModelAbstract(double x, double y, double maxLife) {
        super(x, y, maxLife, maxLife);
        setBullet(true);
        setMass(MassType.NORMAL);
    }

    @Override
    public double getTerminalVelocity() { return terminalVelocity; }
    public void setTerminalVelocity(double v) { this.terminalVelocity = v; }
}
