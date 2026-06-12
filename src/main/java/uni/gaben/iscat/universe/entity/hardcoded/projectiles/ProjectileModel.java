package uni.gaben.iscat.universe.entity.hardcoded.projectiles;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import uni.gaben.iscat.universe.UU;

public class ProjectileModel extends AbstractProjectileModel {
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

        // Rimuove eventuali fixture precedenti e ne crea una nuova
        removeAllFixtures();
        double radiusMeters = UU.pxToM(type.radiusPx);
        BodyFixture fixture = addFixture(Geometry.createCircle(radiusMeters));
        fixture.setFilter(type.filter);
        setMass(MassType.NORMAL);

        // Propaga i parametri balistici all'abstract base
        setTerminalVelocity(type.terminalVelocity);
        // Set life directly on the property BEFORE setMaxLife to prevent the kill-trigger
        // in setLife(). When recycled, life == 0, so setMaxLife calls setLife(0) which
        // re-triggers kill() — bypassing this by writing the field directly first.
        this.endurance.set(type.energy);
        setMaxEndurance(type.energy);
    }

    public ProjectileType getType() { return type; }
}