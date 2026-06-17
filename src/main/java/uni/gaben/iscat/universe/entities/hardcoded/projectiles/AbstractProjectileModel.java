package uni.gaben.iscat.universe.entities.hardcoded.projectiles;

import org.dyn4j.geometry.MassType;
import uni.gaben.iscat.universe.entities.AbstractLivingEntityModel;
import uni.gaben.iscat.universe.entities.EntityRecord;
import uni.gaben.iscat.universe.entities.EntityRecordBuilder;

/**
 * Base class for all projectile entities.
 * Concrete subclasses (e.g. {@link ProjectileModel}) are responsible for adding their own
 * fixture via {@code setType()} — no default fixture is added here to avoid double-allocation.
 */
public abstract class AbstractProjectileModel extends AbstractLivingEntityModel {
    protected double terminalVelocity;
    protected double baseAccelerationPerTick = 20.0;

    protected AbstractProjectileModel(double maxLife) {
        this(0, 0, new EntityRecordBuilder().initLife(maxLife).build());
    }

    protected AbstractProjectileModel(double x, double y, EntityRecord projectileRecord) {
        super(x, y, projectileRecord);
        setBullet(true);
        setMass(MassType.NORMAL);
    }

    @Override
    public double getTerminalVelocity() { return terminalVelocity; }
    public void setTerminalVelocity(double v) { this.terminalVelocity = v; }
}
