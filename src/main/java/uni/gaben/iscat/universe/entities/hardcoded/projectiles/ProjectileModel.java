package uni.gaben.iscat.universe.entities.hardcoded.projectiles;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import uni.gaben.iscat.universe.UU;

public class ProjectileModel extends AbstractPhysicalProjectileModel {
    private ProjectileType type;
    private boolean inPool = false;

    public boolean isInPool() { return inPool; }
    public void setInPool(boolean inPool) { this.inPool = inPool; }

    public ProjectileModel(ProjectileType type) {
        super(type.energy);
        setType(type);
    }

    /** Imposta il tipo e ricostruisce fixture + parametri fisici di conseguenza. */
    public void setType(ProjectileType type) {
        this.type = type;
        removeAllFixtures();
        double radiusMeters = UU.pxToM(type.radiusPx);
        BodyFixture fixture = addFixture(Geometry.createCircle(radiusMeters));
        fixture.setFilter(type.filter);
        setMass(MassType.NORMAL);
        setTerminalVelocity(type.terminalVelocity);
        this.endurance.set(type.energy);
        setMaxEndurance(type.energy);
    }

    public ProjectileType getType() { return type; }

    @Override
    public boolean isInalterable() {
        return false;
    }
}