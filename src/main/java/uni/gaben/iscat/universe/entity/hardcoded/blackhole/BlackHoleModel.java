package uni.gaben.iscat.universe.entity.hardcoded.blackhole;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.entity.AbstractEntityModel;
import uni.gaben.iscat.universe.entity.AbstractLivingEntityModel;
import uni.gaben.iscat.universe.entity.EntityRecordBuilder;
import uni.gaben.iscat.universe.entity.hardcoded.projectiles.AbstractProjectileModel;
import uni.gaben.iscat.universe.entity.interfaces.HasShockwave;
import uni.gaben.iscat.universe.entity.hardcoded.player.PlayerModel;
import uni.gaben.iscat.universe.Shockwave;
import uni.gaben.iscat.utils.Updatable;

public class BlackHoleModel extends AbstractEntityModel implements Updatable, HasShockwave {

    private UU radius;
    private BodyFixture fixture;
    private final Shockwave shockwave = new Shockwave();

    private final double maxRadiusM;
    private final double initialRadiusM;

    // Growth limits
    private static final double MAX_DENSITY = 40.0;          // soft cap for density
    private static final double GROWTH_FACTOR = .1;         // density growth multiplier per kg absorbed
    private static final double RADIUS_GROWTH_BASE = 0.002;   // radius growth per kg (before asymptotic damping)

    // Hawking radiation parameters
    private static final double RADIATION_RADIUS_DECAY = 0.02;   // fraction of current radius lost per second
    private static final double RADIATION_DENSITY_DECAY = 0.01;  // density points lost per second
    private static final double RADIATION_IDLE_TIME = 2.0;        // seconds without absorption before decay starts
    private double timeSinceLastAbsorption = 0.0;

    public BlackHoleModel(double x, double y, double initialRadiusM) {
        super(x, y, new EntityRecordBuilder().build());
        this.initialRadiusM = initialRadiusM;
        this.radius = new UU(initialRadiusM, UU.units.METERS);
        this.maxRadiusM = initialRadiusM * 7.5;

        createFixture();
        setMass(MassType.NORMAL);
        setOnCollision(this::absorbEntity);
    }

    public BlackHoleModel(double x, double y) {
        this(x, y, Math.random() * 2.0 + 0.5); // random initial radius between 0.5 and 2.5 meters
    }

    private void createFixture() {
        if (fixture != null) removeFixture(fixture);
        fixture = addFixture(Geometry.createCircle(radius.m().get()));
        setMass(MassType.NORMAL);
        shockwave.trigger(1, radius.px().get(), 15);
        shockwave.update(0.85);
    }

    private void absorbEntity(AbstractEntityModel other) {
        if (other == null || other.shouldRemove()) return;
        // Don't absorb other black holes or projectiles (projectiles just die)
        if (other instanceof BlackHoleModel) return;
        if (other instanceof AbstractProjectileModel) {
            other.setShouldRemove(true);
            return;
        }

        // Player collision: violent repulsion instead of instant death
        if (other instanceof PlayerModel p) {
            Vector2 pushDir = p.getTransform().getTranslation().subtract(this.getTransform().getTranslation());
            double dist = pushDir.getMagnitude();
            if (dist > 0.01) {
                pushDir.normalize();
                // Impulse scales with current density (capped) and a fixed constant
                double impulseMag = 800.0 * Math.min(fixture.getDensity(), MAX_DENSITY);
                p.applyImpulse(pushDir.multiply(impulseMag));
            }
            return;
        }

        double absorbedMass = other.getMass().getMass();

        // 1. Radius growth (logarithmic, asymptotic toward maxRadiusM)
        if (radius.m().get() < maxRadiusM) {
            double progress = (radius.m().get() - initialRadiusM) / (maxRadiusM - initialRadiusM);
            double growth = absorbedMass * RADIUS_GROWTH_BASE * (1.0 - progress);
            this.radius = new UU(Math.min(radius.m().get() + growth, maxRadiusM), UU.units.METERS);
            createFixture();
        }

        // 2. Density growth (capped)
        double currentDensity = fixture.getDensity();
        double newDensity = Math.min(currentDensity + absorbedMass * GROWTH_FACTOR, MAX_DENSITY);
        fixture.setDensity(newDensity);

        // Recalculate mass based on new density
        this.setMass(MassType.NORMAL);

        // Kill the absorbed entity
        if (other instanceof AbstractLivingEntityModel l) l.extinguish();
        else other.setShouldRemove(true);

        // Reset radiation timer
        timeSinceLastAbsorption = 0.0;
    }

    @Override
    public void update(double dt) {
        super.update(dt);
        timeSinceLastAbsorption += dt;

        // Hawking radiation: slowly shrink if idle
        if (timeSinceLastAbsorption > RADIATION_IDLE_TIME) {
            // Decay radius toward initial
            if (radius.m().get() > initialRadiusM) {
                double decay = RADIATION_RADIUS_DECAY * radius.m().get() * dt;
                this.radius = new UU(Math.max(initialRadiusM, radius.m().get() - decay), UU.units.METERS);
                createFixture();
            }
            // Decay density toward a minimal value (1.0)
            double currentDensity = fixture.getDensity();
            if (currentDensity > 1.0) {
                double newDensity = Math.max(1.0, currentDensity - RADIATION_DENSITY_DECAY * dt);
                fixture.setDensity(newDensity);
                this.setMass(MassType.NORMAL);
            }
        }
    }

    public UU getRadius() { return radius; }

    @Override
    public Shockwave shockwave() { return shockwave; }
}