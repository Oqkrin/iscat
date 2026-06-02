package uni.gaben.iscat.universe.enviroment.blackhole;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.lib.abstracts.AbstractProjectileModel;
import uni.gaben.iscat.universe.lib.implementations.LivingEntityModel;
import uni.gaben.iscat.universe.lib.interfaces.model.HasShockwave;
import uni.gaben.iscat.universe.player.PlayerModel;
import uni.gaben.iscat.universe.rendering.vfx.ShockwaveModel;
import uni.gaben.iscat.utils.Updatable;

public class BlackHoleModel extends AbstractEntityModel implements Updatable, HasShockwave {

    private UU radius;
    private BodyFixture fixture;
    private final ShockwaveModel shockwaveModel = new ShockwaveModel();

    private final double maxRadiusM;
    private final double initialRadiusM;

    public BlackHoleModel(double x, double y, double initialRadiusM) {
        super(x, y);
        this.initialRadiusM = initialRadiusM;
        this.radius = new UU(initialRadiusM, UU.units.METERS);
        this.maxRadiusM = initialRadiusM * 7.5;

        createFixture();
        setMass(MassType.NORMAL);
        setOnCollision(this::absorbEntity);
    }

    public BlackHoleModel(double x, double y) {
        this(x, y, Math.random());
    }

    private void createFixture() {
        if (fixture != null) removeFixture(fixture);
        fixture = addFixture(Geometry.createCircle(radius.m().get()));
        setMass(MassType.NORMAL);
    }

    private void absorbEntity(AbstractEntityModel other) {
        if (other == null || other.shouldRemove() || other instanceof AbstractProjectileModel) return;

        if (other instanceof PlayerModel p) {
            Vector2 pushDir = p.getTransform().getTranslation().subtract(this.getTransform().getTranslation());
            if (pushDir.getMagnitudeSquared() > 0) {
                p.applyImpulse(pushDir.getNormalized());
            }
            return;
        }

        double absorbedMass = other.getMass().getMass();

        // 1. CRESCITA LOGARITMICA DEL RAGGIO (Asintotica verso maxRadiusM)
        if (radius.m().get() < maxRadiusM) {
            double progress = (radius.m().get() - initialRadiusM) / (maxRadiusM - initialRadiusM);
            // La crescita rallenta man mano che ci si avvicina al maxRadiusM
            double logGrowth = absorbedMass * 0.1 * (1.0 - progress);
            this.radius = new UU(Math.min(radius.m().get() + logGrowth, maxRadiusM), UU.units.METERS);
            createFixture();
        }

        // 2. CRESCITA ESPONENZIALE/LINEARE DELLA DENSITÀ (Attrazione)
        // Anche se il raggio è fermo, la densità aumenta esponenzialmente per una gravità brutale
        double currentDensity = fixture.getDensity();
        double growthFactor = 1.2; // Esponezione o moltiplicatore di crescita
        fixture.setDensity(currentDensity + (absorbedMass * growthFactor));

        // Forza il ricalcolo della massa totale basata sulla nuova densità (Area * Densità)
        this.setMass(MassType.NORMAL);

        if (other instanceof LivingEntityModel l) l.kill();
        else other.setShouldRemove(true);

        shockwaveModel.trigger(1, radius.px().get(), 15);
        shockwaveModel.update(.9);
    }

    @Override
    public void update(double dt) {
        super.update(dt);
        shockwaveModel.update(dt);
    }

    public UU getRadius() { return radius; }
    @Override
    public ShockwaveModel shockwave() { return shockwaveModel; }
}