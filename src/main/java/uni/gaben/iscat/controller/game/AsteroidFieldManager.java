package uni.gaben.iscat.controller.game;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.UniverseSpawner;
import uni.gaben.iscat.universe.entity.enviroment.asteroid.AsteroidModel;

import java.util.Random;

/**
 * Manages the generation and logic of asteroid fields for the game.
 */
public class AsteroidFieldManager {

    private final Random random = new Random();

    public AsteroidFieldManager() {}

    /**
     * Spawns the initial asteroid belts around the specified center point.
     *
     * @param centerX The X coordinate for the center of the belt.
     * @param centerY The Y coordinate for the center of the belt.
     */
    public void spawnInitialAsteroidBelts(double centerX, double centerY) {
        // Safety check: prevent NaN or invalid spawn positions
        if (Double.isNaN(centerX) || Double.isNaN(centerY) || centerX == 0.0 || centerY == 0.0) {
            System.err.println("!!! AsteroidFieldManager called with invalid center: " + centerX + ", " + centerY);
            System.err.println("!!! Using default spawn center instead");
            centerX = UniverseModel.DEFAULT_SPAWN_CENTER;
            centerY = UniverseModel.DEFAULT_SPAWN_CENTER;
        }

        for (int clump = 0; clump < 6; clump++) {
            double angle = (clump * (Math.PI * 2.0 / 6.0)) + (Math.random() * 0.5);
            double dist = 600.0 + Math.random() * 1200.0;

            double cx = centerX + Math.cos(angle) * dist;
            double cy = centerY + Math.sin(angle) * dist;

            int count = random.nextInt(3, 18);
            for (int i = 0; i < count; i++) {
                double offsetAngle = Math.random() * Math.PI * 2.0;
                double offsetDist = Math.random() * 180.0;

                double ax = cx + Math.cos(offsetAngle) * offsetDist;
                double ay = cy + Math.sin(offsetAngle) * offsetDist;

                double radius = 20.0 + Math.random() * 100.0;
                AsteroidModel ast = new AsteroidModel(ax, ay, radius);

                double driftAngle = Math.random() * Math.PI * 2.0;
                double speed = 0.5 + Math.random() * 2.0;
                ast.setLinearVelocity(new Vector2(
                        Math.cos(driftAngle) * speed,
                        Math.sin(driftAngle) * speed
                ));

                UniverseSpawner.getInstance().spawnEntity(ast);
            }
        }
    }
}
