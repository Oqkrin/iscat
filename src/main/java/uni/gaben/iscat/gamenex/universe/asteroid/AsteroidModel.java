package uni.gaben.iscat.gamenex.universe.asteroid;

import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.gamenex.lib.abstracts.AbstractEntityModel;
import uni.gaben.iscat.gamenex.universe.GamenexCollisionLayers;
import uni.gaben.iscat.gamenex.universe.UniverseSettings;

import java.util.Random;

/**
 * Model for an Asteroid entity.
 */
public class AsteroidModel extends AbstractEntityModel {
    private final double size;
    private final Vector2[] displayVertices;
    
    /**
     * @param x              initial x position in pixels
     * @param y              initial y position in pixels
     * @param radiusPixel    collision radius in pixels
     * @param minVertices    minimum number of vertices for rendering
     * @param variation      random variation for vertex count
     * @param radiusMin      minimum radius multiplier for jagged edges
     * @param radiusRange    random radius range multiplier for jagged edges
     */
    public AsteroidModel(double x, double y, double radiusPixel, 
                        int minVertices, int variation, 
                        double radiusMin, double radiusRange) {
        Random rand = new Random();
        this.size = radiusPixel != 0 ? radiusPixel * 2 : (2+rand.nextInt(AsteroidSettings.MAXPXSIZE));
        double radiusMeters = size/2 / UniverseSettings.SCALE;

        int numVertices = minVertices + (rand.nextInt(variation));
        displayVertices = new Vector2[numVertices];
        double angleStep = Math.PI * 2 / numVertices;
        
        for(int i=0; i<numVertices; i++) {
            double angle = i * angleStep;
            double r = radiusMeters * (radiusMin + Math.random() * radiusRange);
            displayVertices[i] = new Vector2(Math.cos(angle) * r, Math.sin(angle) * r);
        }
        
        // Use a circle for physics collision
        BodyFixture fixture = addFixture(Geometry.createCircle(radiusMeters));
        fixture.setFilter(GamenexCollisionLayers.ASTEROID_FILTER);
        setMass(MassType.NORMAL);
        translate(x / UniverseSettings.SCALE, y / UniverseSettings.SCALE);
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
    
    public double getSize() { return size; }
    public Vector2[] getDisplayVertices() { return displayVertices; }
}
