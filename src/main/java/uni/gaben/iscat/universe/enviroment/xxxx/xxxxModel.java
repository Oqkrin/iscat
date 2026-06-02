package uni.gaben.iscat.universe.enviroment.xxxx;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import uni.gaben.iscat.universe.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.lib.abstracts.AbstractProjectileModel;
import uni.gaben.iscat.universe.lib.implementations.LivingEntityModel;
import uni.gaben.iscat.universe.lib.interfaces.model.HasShockwave;
import uni.gaben.iscat.universe.player.PlayerModel;
import uni.gaben.iscat.universe.rendering.vfx.ShockwaveModel;
import uni.gaben.iscat.utils.Updatable;

/**
 * A black hole that grows when absorbing entities and exerts gravitational pull
 * on all nearby objects. Gravity strength is proportional to the black hole's
 * mass (which increases with radius) and inversely proportional to distance².
 */
public class xxxxModel extends AbstractEntityModel implements Updatable, HasShockwave {

    private UU radius;           // current radius in meters
    private BodyFixture fixture;// reference to current fixture

    private ShockwaveModel shockwaveModel = new ShockwaveModel();

    /**
     * Creates a black hole at (x, y) with the given initial radius (pixels).
     *
     * @param x               world X coordinate (pixels)
     * @param y               world Y coordinate (pixels)
     * @param initialRadiusM initial radius in pixels
     */
    public xxxxModel(double x, double y, double initialRadiusM) {
        super(x, y);
        this.radius = new UU(initialRadiusM, UU.units.METERS);
        createFixture();
        setMass(MassType.NORMAL);
        setOnCollision(this::absorbEntity);
    }

    /**
     * Creates a black hole with a default attraction range (e.g., 5× its initial radius).
     */
    public xxxxModel(double x, double y) {
        this(x, y, Math.random());
    }

    private void createFixture() {
        // Remove old fixture if present
        if (fixture != null) {
            removeFixture(fixture);
        }
        // Create new circular fixture with current radius
        fixture = addFixture(Geometry.createCircle(radius.m().get()));
        setMass(MassType.NORMAL);
    }

    private void absorbEntity(AbstractEntityModel other) {
        if (other == null || other.shouldRemove() && !(other instanceof AbstractProjectileModel)) return;
        if (other instanceof PlayerModel p) {
            p.applyImpulse(1000);
            return;
        }

        // Increase radius based on the size of the absorbed entity
        double absorbedRadius = other.getWidthMeters() / 2.0;
        double newMeters = radius.m().get() + (absorbedRadius * 0.5);

        radius.m().set(newMeters);

        if (other instanceof LivingEntityModel l) l.kill();
        else other.setShouldRemove(true);

        // Replace fixture with larger circle
        createFixture();
        shockwaveModel.trigger(1, radius.px().get(), 15);
        shockwaveModel.update(.97);
    }

    // ------------------------------------------------------------------------
    // Gravity (applied every frame)
    // ------------------------------------------------------------------------

    @Override
    public void update(double dt) {
        super.update(dt);
        //shockwaveModel.update(dt);
    }

    public UU getRadius() {
        return radius;
    }

    @Override
    public ShockwaveModel shockwave() {
        return shockwaveModel;
    }
}