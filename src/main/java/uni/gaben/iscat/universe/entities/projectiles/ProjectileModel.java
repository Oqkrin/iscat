package uni.gaben.iscat.universe.entities.projectiles;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseModel;

/**
 * Rappresenta un'istanza concreta di un proiettile riutilizzabile nell'universo di gioco.
 * Ottimizzato per il riciclo tramite pool di memoria e per controlli rapidi sui confini.
 */
public class ProjectileModel extends AbstractPhysicalProjectileModel {

    private ProjectileType type;
    private boolean inPool = false;
    private UniverseModel universeModel;

    /**
     * Costruisce un nuovo proiettile configurandone il tipo iniziale.
     */
    public ProjectileModel(ProjectileType type) {
        super(1.0);
        setType(type);
    }

    public boolean isInPool() {
        return inPool;
    }

    public void setInPool(boolean inPool) {
        this.inPool = inPool;
    }

    /**
     * Ripristina completamente lo stato del proiettile per il riutilizzo della pool.
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
        this.setDannoDinamico(0.0);
        this.setType(type);
    }

    public void setUniverseModel(UniverseModel universeModel) {
        this.universeModel = universeModel;
    }

    /**
     * Configura le proprietà fisiche, le maschere di collisione e i limiti di velocità del proiettile.
     */
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

    public ProjectileType getType() {
        return type;
    }

    @Override
    public boolean isInalterable() {
        return false;
    }

    /**
     * Verifica se il proiettile deve essere rimosso.
     * Ottimizzato calcolando manualmente la magnitudo quadra per evitare allocazioni ad ogni frame.
     */
    @Override
    public boolean shouldRemove() {
        if (super.shouldRemove()) {
            return true;
        }
        if (universeModel != null) {
            Vector2 pos = this.getTransform().getTranslation();
            double distanceSquared = pos.x * pos.x + pos.y * pos.y;
            double radius = universeModel.getUniverseRadius();
            return distanceSquared > radius * radius;
        }
        return false;
    }
}