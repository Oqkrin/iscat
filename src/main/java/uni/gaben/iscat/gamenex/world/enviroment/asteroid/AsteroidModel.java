package uni.gaben.iscat.gamenex.world.enviroment.asteroid;

import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.gamenex.interfaces.model.AbstractEntityModel;
import uni.gaben.iscat.gamenex.world.PhysicsSettings;

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
                        
        this.size = radiusPixel * 2;
        double radiusMeters = radiusPixel / PhysicsSettings.SCALE;
        
        int numVertices = minVertices + (int)(Math.random() * variation);
        displayVertices = new Vector2[numVertices];
        double angleStep = Math.PI * 2 / numVertices;
        
        for(int i=0; i<numVertices; i++) {
            double angle = i * angleStep;
            double r = radiusMeters * (radiusMin + Math.random() * radiusRange);
            displayVertices[i] = new Vector2(Math.cos(angle) * r, Math.sin(angle) * r);
        }
        
        // Use a circle for physics collision
        addFixture(Geometry.createCircle(radiusMeters));
        setMass(MassType.NORMAL);
        translate(x / PhysicsSettings.SCALE, y / PhysicsSettings.SCALE);
    }

    /**
     * Helper constructor using default settings.
     */
    public AsteroidModel(double x, double y, double radiusPixel) {
        this(x, y, radiusPixel, 
             AsteroidSettings.MIN_VERTICES, AsteroidSettings.VERTICE_VARIATION, 
             AsteroidSettings.RADIUS_VARIATION_MIN, AsteroidSettings.RADIUS_VARIATION_RANGE);
    }
    
    public double getSize() { return size; }
    public Vector2[] getDisplayVertices() { return displayVertices; }
}
