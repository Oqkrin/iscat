package uni.gaben.iscat.game.universe.asteroid;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.game.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.game.lib.abstracts.AbstractProjectileModel;
import uni.gaben.iscat.game.lib.utils.UU;
import uni.gaben.iscat.game.universe.UniverseCollisionLayers;
import uni.gaben.iscat.game.universe.UniverseSpawner;
import uni.gaben.iscat.game.universe.VelocitySettings;

import java.util.Random;

/**
 * Model for an Asteroid entity.
 */
public class AsteroidModel extends AbstractEntityModel {
    private final double size;
    private final Vector2[] displayVertices;

    /**
     * @param x           initial x position in pixels
     * @param y           initial y position in pixels
     * @param radiusPixel collision radius in pixels
     * @param minVertices minimum number of vertices for rendering
     * @param variation   random variation for vertex count
     * @param radiusMin   minimum radius multiplier for jagged edges
     * @param radiusRange random radius range multiplier for jagged edges
     */
    public AsteroidModel(double x, double y, double radiusPixel,
            int minVertices, int variation,
            double radiusMin, double radiusRange) {
        super(x, y);
        Random rand = new Random();
        this.size = radiusPixel != 0 ? radiusPixel * 2 : (2 + rand.nextInt(AsteroidSettings.MAXPXSIZE));
        double radiusMeters = UU.pxToM(size / 2);

        int numVertices = minVertices + (rand.nextInt(variation));
        displayVertices = new Vector2[numVertices];
        double angleStep = Math.PI * 2 / numVertices;

        for (int i = 0; i < numVertices; i++) {
            double angle = i * angleStep;
            double r = radiusMeters * (radiusMin + Math.random() * radiusRange);
            displayVertices[i] = new Vector2(Math.cos(angle) * r, Math.sin(angle) * r);
        }

        // Use a circle for physics collision
        BodyFixture fixture = addFixture(Geometry.createCircle(radiusMeters));
        fixture.setFilter(UniverseCollisionLayers.ASTEROID_FILTER);

        // Density scales quadratically with size to make bigger ones feel extremely heavy and immovable
        double density = 1.0 + Math.pow(size / 45.0, 2.0);
        fixture.setDensity(density);

        setMass(MassType.NORMAL);

        // Linear and angular damping scale with size so larger asteroids are sluggish and slow down fast
        double damping = 0.5 + (size / 100.0);
        setLinearDamping(damping);
        setAngularDamping(damping);

        // LOGICA DI SPLITTING: Quando colpito da un proiettile, si divide in due
        // asteroidi minori
        this.setOnCollision(other -> {
            if (other instanceof AbstractProjectileModel) {
                if (this.shouldRemove())
                    return;
                this.setShouldRemove(true);

                // Solo se la dimensione dell'asteroide è sufficiente, lo dividiamo in due
                if (this.size >= 24.0) {
                    double asteroidX = UU.mToPx(this.getTransform().getTranslationX());
                    double asteroidY = UU.mToPx(this.getTransform().getTranslationY());

                    double smallerAsteroidARadius = (this.size / 4.0) * (0.8 + Math.random() * 0.4);
                    double smallerAsteroidBRadius = (this.size / 4.0) * (0.8 + Math.random() * 0.4);

                    // Angolo casuale per spingere i frammenti in direzioni opposte
                    double angle = Math.random() * Math.PI * 2;
                    double offsetPx = this.size / 3.0;

                    double x1 = asteroidX + Math.cos(angle) * offsetPx;
                    double y1 = asteroidY + Math.sin(angle) * offsetPx;
                    double x2 = asteroidX - Math.cos(angle) * offsetPx;
                    double y2 = asteroidY - Math.sin(angle) * offsetPx;

                    AsteroidModel smallerAsteroidA = new AsteroidModel(x1, y1, smallerAsteroidARadius);
                    AsteroidModel smallerAsteroidB = new AsteroidModel(x2, y2, smallerAsteroidBRadius);

                    // Copia la velocità lineare del genitore e applica un impulso laterale per
                    // farli dividere
                    Vector2 asteroidLinearVelocity = this.getLinearVelocity();
                    double pushForce = 3.0; // m/s di impulso di separazione

                    Vector2 smallerAsteroidAVelocity = asteroidLinearVelocity.copy()
                            .add(new Vector2(Math.cos(angle) * pushForce, Math.sin(angle) * pushForce));
                    Vector2 smallerAsteroidBVelocity = asteroidLinearVelocity.copy()
                            .subtract(new Vector2(Math.cos(angle) * pushForce, Math.sin(angle) * pushForce));

                    smallerAsteroidA.setLinearVelocity(smallerAsteroidAVelocity);
                    smallerAsteroidB.setLinearVelocity(smallerAsteroidBVelocity);

                    // Spawna i figli nel mondo di gioco
                    UniverseSpawner.getInstance().spawnEntity(smallerAsteroidA);
                    UniverseSpawner.getInstance().spawnEntity(smallerAsteroidB);
                }
            }
        });
    }

    /**
     * Helper constructor using default settings.
     */
    public AsteroidModel(double x, double y, double radiusPixel) {
        this(x, y, radiusPixel,
                AsteroidSettings.MIN_VERTICES, AsteroidSettings.VERTICE_VARIATION,
                AsteroidSettings.RADIUS_VARIATION_MIN, AsteroidSettings.RADIUS_VARIATION_RANGE);
    }

    public AsteroidModel(double x, double y) {
        this(x, y, 0,
                AsteroidSettings.MIN_VERTICES, AsteroidSettings.VERTICE_VARIATION,
                AsteroidSettings.RADIUS_VARIATION_MIN, AsteroidSettings.RADIUS_VARIATION_RANGE);
    }

    public double getSize() {
        return size;
    }

    public Vector2[] getDisplayVertices() {
        return displayVertices;
    }

    @Override
    public double getTerminalVelocity() {
        return VelocitySettings.asteroidTerminalVelocity(size);
    }

}
