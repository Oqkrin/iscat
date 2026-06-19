package uni.gaben.iscat.universe.entities.hardcoded.projectiles;

import org.dyn4j.geometry.MassType;
import uni.gaben.iscat.universe.entities.AbstractLivingEntityModel;
import uni.gaben.iscat.universe.entities.EntityRecord;
import uni.gaben.iscat.universe.entities.EntityRecordBuilder;

public abstract class AbstractPhysicalProjectileModel extends AbstractLivingEntityModel {
    protected double terminalVelocity;
    protected double baseAccelerationPerTick = 20.0;

    protected AbstractPhysicalProjectileModel(double maxLife) {
        this(0, 0, new EntityRecordBuilder().initLife(maxLife).build());
    }

    protected AbstractPhysicalProjectileModel(double x, double y, EntityRecord projectileRecord) {
        super(x, y, projectileRecord);
        setBullet(true);
        setMass(MassType.NORMAL);
    }

    @Override
    public double getTerminalVelocity() { return terminalVelocity; }
    public void setTerminalVelocity(double v) { this.terminalVelocity = v; }

    /**
     * Override extinguish to prevent any side effects (audio, heart drops, etc.)
     * Projectiles simply disappear.
     */
    @Override
    public void extinguish(boolean silent) {
        if (shouldRemove()) return;
        setShouldRemove(true);
        onDeath();
    }
}