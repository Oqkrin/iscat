package uni.gaben.iscat.universe.entities.hardcoded.asteroid;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.geometry.Polygon;
import uni.gaben.iscat.universe.entities.AbstractPhysicalEntityModel;
import uni.gaben.iscat.universe.entities.parsed.EntityRecordBuilder;
import uni.gaben.iscat.universe.entities.hardcoded.projectiles.AbstractPhysicalProjectileModel;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseCollisionLayers;
import uni.gaben.iscat.universe.spawn.UniverseSpawner;
import uni.gaben.iscat.universe.UniverseVelocitySettings;

import java.util.*;

public class AsteroidModel extends AbstractPhysicalEntityModel {
    private final double size;
    private final Vector2[] displayVertices;

    // Cache vertici trasformati in pixel — aggiornata solo quando posizione/rotazione cambia
    private final double[] cachedXPoints;
    private final double[] cachedYPoints;
    private double lastCachedX = Double.NaN;
    private double lastCachedY = Double.NaN;
    private double lastCachedAngle = Double.NaN;

    // Durability Mechanics
    private final double maxDurability;
    private double currentDurability;
    private final double splitAngle;

    public AsteroidModel(double x, double y) {
        this(x, y, 0);
    }

    public AsteroidModel(double x, double y, double radius) {
        this(x, y, radius, AsteroidSettings.MIN_VERTICES, AsteroidSettings.VERTICE_VARIATION,
                AsteroidSettings.RADIUS_VARIATION_MIN, AsteroidSettings.RADIUS_VARIATION_RANGE);
    }

    public AsteroidModel(double x, double y, double radiusPixel,
                         int minVertices, int variation,
                         double radiusMin, double radiusRange) {
        super(x, y, new EntityRecordBuilder().build());
        Random rand = new Random();
        this.splitAngle = rand.nextDouble() * Math.PI * 2;
        this.size = radiusPixel != 0 ? radiusPixel * 2 : (16 + rand.nextInt(AsteroidSettings.MAXPXSIZE));
        double radiusMeters = UU.pxToM(size / 2.0);

        this.displayVertices = AsteroidShapeFactory.getScaledShape(radiusMeters);
        Polygon polygon = new Polygon(this.displayVertices);

        BodyFixture fixture = addFixture(polygon);
        fixture.setFilter(UniverseCollisionLayers.ASTEROID_FILTER);

        double density = 1.0 + Math.pow(size / 45.0, 2.0);
        fixture.setDensity(density);
        setMass(MassType.NORMAL);

        double damping = 0.5 + (size / 100.0);
        setLinearDamping(damping);
        setAngularDamping(damping);

        this.maxDurability = AsteroidSettings.BASE_DURABILITY * density;
        this.currentDurability = this.maxDurability;

        // Alloca la cache una sola volta con la dimensione giusta
        this.cachedXPoints = new double[displayVertices.length];
        this.cachedYPoints = new double[displayVertices.length];

        this.addOnCollision("projectile", other -> {
            if (this.shouldRemove()) return;
            if (other instanceof AbstractPhysicalProjectileModel projectile) {
                takeDamage(AsteroidSettings.PROJECTILE_DAMAGE_FACTOR);
                projectile.setShouldRemove(true);
            }
        });
    }

    /**
     * Restituisce i vertici trasformati in pixel, aggiornando la cache
     * solo se posizione o rotazione sono cambiate dall'ultimo frame.
     */
    public double[] getCachedXPoints() {
        refreshCacheIfNeeded();
        return cachedXPoints;
    }

    public double[] getCachedYPoints() {
        refreshCacheIfNeeded();
        return cachedYPoints;
    }

    private void refreshCacheIfNeeded() {
        double tx    = getTransform().getTranslationX();
        double ty    = getTransform().getTranslationY();
        double angle = getTransform().getRotationAngle();

        if (tx == lastCachedX && ty == lastCachedY && angle == lastCachedAngle) return;

        for (int i = 0; i < displayVertices.length; i++) {
            Vector2 w = getTransform().getTransformed(displayVertices[i]);
            cachedXPoints[i] = UU.mToPx(w.x);
            cachedYPoints[i] = UU.mToPx(w.y);
        }

        lastCachedX     = tx;
        lastCachedY     = ty;
        lastCachedAngle = angle;
    }

    public void takeDamage(double amount) {
        this.currentDurability = Math.max(0, this.currentDurability - amount);
        if (this.currentDurability <= 0) handleBreakup();
    }

    private void handleBreakup() {
        this.setShouldRemove(true);

        if (this.size >= AsteroidSettings.MIN_SPLIT_SIZE) {
            double asteroidXMeters = this.getTransform().getTranslationX();
            double asteroidYMeters = this.getTransform().getTranslationY();

            double smallerAsteroidARadius = (this.size / 8.0) * (0.8 + Math.random() * 0.4);
            double smallerAsteroidBRadius = (this.size / 8.0) * (0.8 + Math.random() * 0.4);
            double smallerAsteroidCRadius = (this.size / 8.0) * (0.8 + Math.random() * 0.4);
            double smallerAsteroidDRadius = (this.size / 8.0) * (0.8 + Math.random() * 0.4);

            double angle = this.splitAngle;
            double offsetMeters = UU.pxToM(this.size / 4.0);

            double mX1 = asteroidXMeters + Math.cos(angle) * offsetMeters;
            double mY1 = asteroidYMeters + Math.sin(angle) * offsetMeters;
            double mX2 = asteroidXMeters - Math.cos(angle) * offsetMeters;
            double mY2 = asteroidYMeters - Math.sin(angle) * offsetMeters;
            double mX3 = mX1;
            double mY3 = mY2;
            double mX4 = mX2;
            double mY4 = mY1;

            AsteroidModel a = new AsteroidModel(UU.mToPx(mX1), UU.mToPx(mY1), smallerAsteroidARadius);
            AsteroidModel b = new AsteroidModel(UU.mToPx(mX2), UU.mToPx(mY2), smallerAsteroidBRadius);
            AsteroidModel c = new AsteroidModel(UU.mToPx(mX3), UU.mToPx(mY3), smallerAsteroidCRadius);
            AsteroidModel d = new AsteroidModel(UU.mToPx(mX4), UU.mToPx(mY4), smallerAsteroidDRadius);

            Vector2 vel = this.getLinearVelocity();
            double pushForce = 2.0;

            a.setLinearVelocity(vel.copy().add(new Vector2(Math.cos(angle) * pushForce, Math.sin(angle) * pushForce)));
            b.setLinearVelocity(vel.copy().subtract(new Vector2(Math.cos(angle) * pushForce, Math.sin(angle) * pushForce)));
            c.setLinearVelocity(vel.copy().add(new Vector2(Math.cos(angle) * pushForce, Math.sin(angle) * pushForce)).getRightHandOrthogonalVector());
            d.setLinearVelocity(vel.copy().subtract(new Vector2(Math.cos(angle) * pushForce, Math.sin(angle) * pushForce)).getRightHandOrthogonalVector());

            UniverseSpawner.getInstance().spawnEntity(a);
            UniverseSpawner.getInstance().spawnEntity(b);
            UniverseSpawner.getInstance().spawnEntity(c);
            UniverseSpawner.getInstance().spawnEntity(d);
        }
    }

    public double getSize()                  { return size; }
    public Vector2[] getDisplayVertices()    { return displayVertices; }
    public double getDurabilityHealthRatio() { return currentDurability / maxDurability; }
    public double getSplitAngle()            { return splitAngle; }

    @Override
    public double getTerminalVelocity() {
        return UniverseVelocitySettings.asteroidTerminalVelocity(size);
    }
}