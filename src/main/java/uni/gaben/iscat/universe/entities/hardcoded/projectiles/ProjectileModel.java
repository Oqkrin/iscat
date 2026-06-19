package uni.gaben.iscat.universe.entities.hardcoded.projectiles;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseModel;

public class ProjectileModel extends AbstractPhysicalProjectileModel {
    private ProjectileType type;
    private boolean inPool = false;
    private UniverseModel universeModel;

    public boolean isInPool() { return inPool; }
    public void setInPool(boolean inPool) { this.inPool = inPool; }

    public ProjectileModel(ProjectileType type) {
        super(1.0);
        setType(type);
    }

    /**
     * Resets the projectile state for pool recycling.
     */
    public void reset(ProjectileType type) {
        this.inPool = false;
        this.clearOnCollisions();
        this.setKilledByProjectile(false);
        this.setShouldRemove(false);
        this.setEnabled(true);
        this.setAtRest(false);
        this.getTransform().setTranslation(0, 0);
        this.getTransform().setRotation(0);
        this.setLinearVelocity(0, 0);
        this.setAngularVelocity(0);
        this.clearAccumulatedForce();
        this.clearAccumulatedTorque();
        this.setType(type);
    }

    public void setUniverseModel(UniverseModel universeModel) {
        this.universeModel = universeModel;
    }

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

        this.endurance.set(1.0);
        setMaxEndurance(1.0);
    }

    public void setEnergyDirect(double energy) {
        this.endurance.set(energy);
        setMaxEndurance(energy);
    }

    public ProjectileType getType() { return type; }

    @Override
    public boolean isInalterable() { return false; }

    @Override
    public boolean shouldRemove() {
        if (super.shouldRemove()) {
            return true;
        }
        if (universeModel != null) {
            Vector2 pos = this.getTransform().getTranslation();
            double distanceSquared = pos.getMagnitudeSquared();
            double radius = universeModel.getUniverseRadius();
            return distanceSquared > radius * radius;
        }
        return false;
    }
}