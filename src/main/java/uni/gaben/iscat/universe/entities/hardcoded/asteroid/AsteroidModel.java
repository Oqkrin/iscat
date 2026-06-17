package uni.gaben.iscat.universe.entities.hardcoded.asteroid;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.geometry.Polygon;
import uni.gaben.iscat.universe.entities.AbstractPhysicalEntityModel;
import uni.gaben.iscat.universe.entities.EntityRecordBuilder;
import uni.gaben.iscat.universe.entities.hardcoded.projectiles.AbstractPhysicalProjectileModel;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseCollisionLayers;
import uni.gaben.iscat.universe.spawn.UniverseSpawner;
import uni.gaben.iscat.universe.UniverseVelocitySettings;

import java.util.*;

public class AsteroidModel extends AbstractPhysicalEntityModel {
    private final double size; // Acts as diameter in pixels
    private final Vector2[] displayVertices;

    // Durability Mechanics
    private final double maxDurability;
    private double currentDurability;
    private final double splitAngle;

    // Helper Constructor
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
        this.splitAngle = rand.nextDouble() * Math.PI * 2; // Pre-determine the fracture plane
        // Normalize size definition: if radiusPixel is provided, diameter is radiusPixel * 2
        this.size = radiusPixel != 0 ? radiusPixel * 2 : (16 + rand.nextInt(AsteroidSettings.MAXPXSIZE));
        double radiusMeters = UU.pxToM(size / 2.0);

        // Use pre-computed shapes from AsteroidShapeFactory
        this.displayVertices = AsteroidShapeFactory.getScaledShape(radiusMeters);
        Polygon polygon = new Polygon(this.displayVertices);

        BodyFixture fixture = addFixture(polygon);
        fixture.setFilter(UniverseCollisionLayers.ASTEROID_FILTER);

        // Density scales quadratically with size
        double density = 1.0 + Math.pow(size / 45.0, 2.0);
        fixture.setDensity(density);

        // Set standard dyn4j mass tracking directly
        setMass(MassType.NORMAL);

        double damping = 0.5 + (size / 100.0);
        setLinearDamping(damping);
        setAngularDamping(damping);

        // Calculate density-based durability
        this.maxDurability = AsteroidSettings.BASE_DURABILITY * density;
        this.currentDurability = this.maxDurability;

        // COLLISION MECHANISM
        this.addOnCollision("projectile",other -> {
            if (this.shouldRemove()) return;

            if (other instanceof AbstractPhysicalProjectileModel projectile) {
                double damage = AsteroidSettings.PROJECTILE_DAMAGE_FACTOR;
                takeDamage(damage);
                projectile.setShouldRemove(true);
            }
        });
    }

    public void takeDamage(double amount) {
        this.currentDurability = Math.max(0, this.currentDurability - amount);
        if (this.currentDurability <= 0) {
            handleBreakup();
        }
    }

    private void handleBreakup() {
        this.setShouldRemove(true);

        // Only split if the size criteria is met
        if (this.size >= AsteroidSettings.MIN_SPLIT_SIZE) {
            // Read positions directly in METERS to match physics space coordinate rules
            double asteroidXMeters = this.getTransform().getTranslationX();
            double asteroidYMeters = this.getTransform().getTranslationY();

            // Target child radius in pixels
            double smallerAsteroidARadius = (this.size / 8.0) * (0.8 + Math.random() * 0.4);
            double smallerAsteroidBRadius = (this.size / 8.0) * (0.8 + Math.random() * 0.4);
            double smallerAsteroidCRadius = (this.size / 8.0) * (0.8 + Math.random() * 0.4);
            double smallerAsteroidDRadius = (this.size / 8.0) * (0.8 + Math.random() * 0.4);

            double angle = this.splitAngle;
            double offsetMeters = UU.pxToM(this.size / 4.0); // Convert pixel offset distance to meters Safely

            // Compute separation positions cleanly in METERS
            double mX1 = asteroidXMeters + Math.cos(angle) * offsetMeters;
            double mY1 = asteroidYMeters + Math.sin(angle) * offsetMeters;
            double mX2 = asteroidXMeters - Math.cos(angle) * offsetMeters;
            double mY2 = asteroidYMeters - Math.sin(angle) * offsetMeters;
            double mX3 = mX1;
            double mY3 = mY2;
            double mX4 = mX2;
            double mY4 = mY1;

            // Convert back to pixels before passing to constructor (since super() maps parameters)
            AsteroidModel smallerAsteroidA = new AsteroidModel(UU.mToPx(mX1), UU.mToPx(mY1), smallerAsteroidARadius);
            AsteroidModel smallerAsteroidB = new AsteroidModel(UU.mToPx(mX2), UU.mToPx(mY2), smallerAsteroidBRadius);
            AsteroidModel smallerAsteroidC = new AsteroidModel(UU.mToPx(mX3), UU.mToPx(mY3), smallerAsteroidCRadius);
            AsteroidModel smallerAsteroidD = new AsteroidModel(UU.mToPx(mX4), UU.mToPx(mY4), smallerAsteroidDRadius);
            // Handle outward velocity tracking forces
            Vector2 asteroidLinearVelocity = this.getLinearVelocity();
            double pushForce = 2.0;

            Vector2 smallerAsteroidAVelocity = asteroidLinearVelocity.copy()
                    .add(new Vector2(Math.cos(angle) * pushForce, Math.sin(angle) * pushForce));
            Vector2 smallerAsteroidBVelocity = asteroidLinearVelocity.copy()
                    .subtract(new Vector2(Math.cos(angle) * pushForce, Math.sin(angle) * pushForce));
            Vector2 smallerAsteroidCVelocity = asteroidLinearVelocity.copy()
                    .add(new Vector2(Math.cos(angle) * pushForce, Math.sin(angle) * pushForce)).getRightHandOrthogonalVector();
            Vector2 smallerAsteroidDVelocity = asteroidLinearVelocity.copy()
                    .subtract(new Vector2(Math.cos(angle) * pushForce, Math.sin(angle) * pushForce)).getRightHandOrthogonalVector();

            smallerAsteroidA.setLinearVelocity(smallerAsteroidAVelocity);
            smallerAsteroidB.setLinearVelocity(smallerAsteroidBVelocity);
            smallerAsteroidC.setLinearVelocity(smallerAsteroidCVelocity);
            smallerAsteroidD.setLinearVelocity(smallerAsteroidDVelocity);

            UniverseSpawner.getInstance().spawnEntity(smallerAsteroidA);
            UniverseSpawner.getInstance().spawnEntity(smallerAsteroidB);
            UniverseSpawner.getInstance().spawnEntity(smallerAsteroidC);
            UniverseSpawner.getInstance().spawnEntity(smallerAsteroidD);
        }
    }

    public double getSize() { return size; }
    public Vector2[] getDisplayVertices() { return displayVertices; }

    public double getDurabilityHealthRatio() {
        return currentDurability / maxDurability;
    }

    @Override
    public double getTerminalVelocity() {
        return UniverseVelocitySettings.asteroidTerminalVelocity(size);
    }

    public double getSplitAngle() { return splitAngle; }
}