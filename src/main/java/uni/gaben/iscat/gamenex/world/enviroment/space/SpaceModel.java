package uni.gaben.iscat.gamenex.world.enviroment.space;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.dyn4j.dynamics.Body;
import org.dyn4j.world.World;

import java.util.Random;
import uni.gaben.iscat.gamenex.player.PlayerModel;
import uni.gaben.iscat.gamenex.world.enviroment.space.starfield.StarfieldModel;
import uni.gaben.iscat.gamenex.world.enviroment.asteroid.AsteroidModel;
import uni.gaben.iscat.gamenex.world.enviroment.EnvironmentSettings;

public class SpaceModel extends World<Body> {

    private Random rand = new Random();
    private PlayerModel player;
    private final StarfieldModel starfieldModel = new StarfieldModel();

    private DoubleProperty width = new SimpleDoubleProperty(EnvironmentSettings.DEFAULT_WIDTH);
    private DoubleProperty height = new SimpleDoubleProperty(EnvironmentSettings.DEFAULT_HEIGHT);

    public SpaceModel() {
        setGravity(World.ZERO_GRAVITY);
        
        // Init player
        player = new PlayerModel(width.get() / 2.0, height.get() / 2.0);
        addBody(player);
        
        // Init an asteroid to test collision/physics
        AsteroidModel asteroid = new AsteroidModel(
            EnvironmentSettings.TEST_ASTEROID_X, 
            EnvironmentSettings.TEST_ASTEROID_Y, 
            EnvironmentSettings.TEST_ASTEROID_RADIUS
        );
        asteroid.setLinearVelocity(EnvironmentSettings.TEST_ASTEROID_VEL_X, 0); 
        addBody(asteroid);
    }
    
    public void setDimensions(double w, double h) {
        if (w > 0 && h > 0) {
            this.width.set(w);
            this.height.set(h);
        }
    }

    public double getWidth() { return width.get(); }
    public double getHeight() { return height.get(); }

    public DoubleProperty widthProperty() { return width; }
    public DoubleProperty heightProperty() { return height; }

    
    @Override
    public boolean update(double dt) {
        player.update(dt);
        return super.update(dt);
    }
    
    public PlayerModel getPlayer() { return player; }
    public StarfieldModel getStarfieldModel() { return starfieldModel; }
}
