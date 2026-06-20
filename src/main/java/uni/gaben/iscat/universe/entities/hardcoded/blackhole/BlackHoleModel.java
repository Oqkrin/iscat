package uni.gaben.iscat.universe.entities.hardcoded.blackhole;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.entities.AbstractPhysicalEntityModel;
import uni.gaben.iscat.universe.entities.AbstractLivingEntityModel;
import uni.gaben.iscat.universe.entities.EntityRecordBuilder;
import uni.gaben.iscat.universe.entities.hardcoded.projectiles.AbstractPhysicalProjectileModel;
import uni.gaben.iscat.universe.entities.interfaces.HasShockwave;
import uni.gaben.iscat.universe.entities.player.PlayerModel;
import uni.gaben.iscat.universe.effects.Shockwave;
import uni.gaben.iscat.utils.Updatable;

public class BlackHoleModel extends AbstractPhysicalEntityModel implements Updatable, HasShockwave {

    private UU radius;
    private BodyFixture fixture;
    private final Shockwave shockwave = new Shockwave();

    private final double maxRadiusM;
    private final double initialRadiusM;

    // ---- Absorption strength (increased) ----
    private static final double MAX_DENSITY = 40.0;          // soft cap for density
    private static final double GROWTH_FACTOR = 0.3;         // was 0.1 → 3x faster density gain per kg
    private static final double RADIUS_GROWTH_BASE = 0.005;  // was 0.002 → 2.5x faster radius gain per kg

    // Hawking radiation parameters
    private static final double RADIATION_RADIUS_DECAY = 0.02;
    private static final double RADIATION_DENSITY_DECAY = 0.01;
    private static final double RADIATION_IDLE_TIME = 2.0;
    private double timeSinceLastAbsorption = 0.0;

    // ---- Deferred update flags ----
    private boolean needsFixtureUpdate = false;
    private double pendingRadiusM = -1;
    private double pendingDensity = -1;

    public BlackHoleModel(double x, double y, double initialRadiusM) {
        super(x, y, new EntityRecordBuilder().build());
        this.initialRadiusM = initialRadiusM;
        this.radius = new UU(initialRadiusM, UU.units.METERS);
        this.maxRadiusM = initialRadiusM * 7.5;

        createFixture();
        setMass(MassType.NORMAL);
        addOnCollision("blackhole", this::absorbEntity);
    }

    public BlackHoleModel(double x, double y) {
        this(x, y, Math.random() * 2.0 + 0.5);
    }

    private void createFixture() {
        if (fixture != null) removeFixture(fixture);
        fixture = addFixture(Geometry.createCircle(radius.m().get()));
        setMass(MassType.NORMAL);
        shockwave.trigger(1, radius.px().get(), 15);
        shockwave.update(0.85);
    }

    private void absorbEntity(AbstractPhysicalEntityModel other) {
        if (other == null || other.shouldRemove()) return;
        if (other instanceof AbstractPhysicalProjectileModel appm) {
            appm.extinguish();
            return;
        }

        // Player: repulse instead of absorb
        if (other instanceof PlayerModel p) {
            Vector2 pushDir = p.getTransform().getTranslation().subtract(this.getTransform().getTranslation());
            double dist = pushDir.getMagnitude();
            if (dist > 0.01) {
                pushDir.normalize();
                // Impulse scales with current density (capped)
                double impulseMag = 800.0 * Math.min(fixture.getDensity(), MAX_DENSITY);
                p.applyImpulse(pushDir.setMagnitude(impulseMag));
            }
            return;
        }

        double absorbedMass = other.getMass().getMass();

        // 1. Compute new radius (logarithmic, asymptotic toward maxRadiusM)
        double newRadius = radius.m().get();
        if (newRadius < maxRadiusM) {
            double progress = (newRadius - initialRadiusM) / (maxRadiusM - initialRadiusM);
            double growth = absorbedMass * RADIUS_GROWTH_BASE * (1.0 - progress);
            newRadius = Math.min(newRadius + growth, maxRadiusM);
        }

        // 2. Compute new density (capped)
        double currentDensity = fixture.getDensity();
        double newDensity = Math.min(currentDensity + absorbedMass * GROWTH_FACTOR, MAX_DENSITY);

        // 3. Defer application to avoid ConcurrentModificationException
        pendingRadiusM = newRadius;
        pendingDensity = newDensity;
        needsFixtureUpdate = true;

        // Kill the absorbed entity (safe, doesn't modify dyn4j structures)
        if (other instanceof AbstractLivingEntityModel l) l.extinguish();
        else other.setShouldRemove(true);

        // Reset radiation timer
        timeSinceLastAbsorption = 0.0;
    }

    @Override
    public void update(double dt) {
        super.update(dt);

        // ---- Apply pending fixture changes (safe, outside collision loop) ----
        if (needsFixtureUpdate) {
            if (pendingRadiusM > 0) {
                this.radius = new UU(pendingRadiusM, UU.units.METERS);
                createFixture();                // removes old fixture, adds new one
            }
            if (pendingDensity > 0) {
                fixture.setDensity(pendingDensity);
                this.setMass(MassType.NORMAL); // recalc mass with new density
            }
            needsFixtureUpdate = false;
            pendingRadiusM = -1;
            pendingDensity = -1;
        }

        // ---- Hawking radiation (shrink when idle) ----
        timeSinceLastAbsorption += dt;
        if (timeSinceLastAbsorption > RADIATION_IDLE_TIME) {
            // Decay radius toward initial
            if (radius.m().get() > initialRadiusM) {
                double decay = RADIATION_RADIUS_DECAY * radius.m().get() * dt;
                this.radius = new UU(Math.max(initialRadiusM, radius.m().get() - decay), UU.units.METERS);
                // We can update fixture immediately because we are in a safe context
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