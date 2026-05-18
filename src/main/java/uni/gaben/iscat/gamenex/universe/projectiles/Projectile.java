package uni.gaben.iscat.gamenex.universe.projectiles;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import uni.gaben.iscat.gamenex.lib.abstracts.AbstractProjectileModel;
import uni.gaben.iscat.gamenex.universe.UniverseSettings;

public class Projectile extends AbstractProjectileModel {

    private ProjectileType type = ProjectileType.PLAYER_BULLET;

    /** Imposta il tipo e ricostruisce fixture + parametri fisici di conseguenza. */
    public void setType(ProjectileType type) {
        this.type = type;

        // Rimuove eventuali fixture precedenti e ne crea una nuova
        removeAllFixtures();
        double radiusMeters = type.radiusPx / UniverseSettings.SCALE;
        BodyFixture fixture = addFixture(Geometry.createCircle(radiusMeters));
        fixture.setFilter(type.filter);
        setMass(MassType.NORMAL);

        // Propaga i parametri balistici all'abstract base
        setTerminalVelocity(type.terminalVelocity);
        setDamage(type.damage);
        setLifespan(type.lifespan);
    }

    public ProjectileType getType() { return type; }

    @Override
    public AbstractProjectileModel blueprint() {
        Projectile p = new Projectile();
        p.setType(this.type); // il clone eredita il tipo del proiettile originale
        return p;
    }
}