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
        // Passiamo un valore di fallback temporaneo alla classe base astratta
        super(1.0);
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

        if (type == ProjectileType.PLAYER_BULLET) {
            setTerminalVelocity(type.terminalVelocity * 1.5);
        } else {
            setTerminalVelocity(type.terminalVelocity);
        }

        // Valore iniziale di default (sarà sovrascritto dinamicamente da chi spara)
        this.endurance.set(1.0);
        setMaxEndurance(1.0);
    }

    /**
     * Imposta l'energia (danno/durata) del proiettile in modo dinamico.
     */
    public void setEnergyDirect(double energy) {
        this.endurance.set(energy);
        setMaxEndurance(energy);
    }

    public ProjectileType getType() { return type; }

    @Override
    public boolean isInalterable() {
        return false;
    }
}