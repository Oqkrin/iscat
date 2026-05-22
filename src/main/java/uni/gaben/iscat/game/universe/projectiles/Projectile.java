package uni.gaben.iscat.game.universe.projectiles;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import uni.gaben.iscat.game.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.game.lib.abstracts.AbstractProjectileModel;
import uni.gaben.iscat.game.lib.utils.UU;

import java.util.function.Consumer;

public class Projectile extends AbstractProjectileModel {
    private ProjectileType type;

    public Projectile(ProjectileType type) {
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
        setMaxLife(type.energy);
        setLife(type.energy);
    }

    public ProjectileType getType() { return type; }

    @Override
    public AbstractProjectileModel blueprint() {
        Projectile p = new Projectile(type);
        p.setTerminalVelocity(this.getTerminalVelocity());
        p.setMaxLife(this.getMaxLife());
        p.setLife(this.getLife());
        p.baseAccelerationPerTick = this.baseAccelerationPerTick;
        p.size = this.size;
        return p;
    }
}