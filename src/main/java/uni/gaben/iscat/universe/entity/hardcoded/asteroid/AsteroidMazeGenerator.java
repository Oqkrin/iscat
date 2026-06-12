package uni.gaben.iscat.universe.entity.hardcoded.asteroid;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.UniverseSpawner;

import java.util.Random;

/**
 * AsteroidMazeGenerator
 * * Manages procedural generation of interconnected asteroid fields for the game world.
 * Features rectangular boundary generation along screen edges and localized, high-density
 * geometric cluster (clump) injections.
 * * Optimized to use a reusable workspace vector to minimize heap allocations and GC spikes.
 */
    public class AsteroidMazeGenerator {

        private final Random random = new Random();
        private final Vector2 workspaceVelocity = new Vector2();

        public AsteroidMazeGenerator() {}

        /**
         * Generates a circular boundary of asteroids around the world center.
         */
        public void generate(double centerX, double centerY) {
            // 1. Sanity Checks
            if (Double.isNaN(centerX) || Double.isNaN(centerY) || centerX == 0.0 || centerY == 0.0) {
                centerX = UniverseModel.DEFAULT_SPAWN_WIDTHCENTER;
                centerY = UniverseModel.DEFAULT_SPAWN_HEIGHTCENTER;
            }

            // 2. Metrics (Basing scale off UniverseModel defaults)
            // We set the radius to be large enough to contain the play area
            double boundaryRadius = centerX * 2.5;
            double wallThickness = 500.0; // Depth of the asteroid ring

            // Calculate circumference to determine how many steps we need for a "solid" wall
            double circumference = 2 * Math.PI * boundaryRadius;
            double angularStepSize = 250.0; // Pixels between clusters
            int totalSteps = (int) (circumference / angularStepSize);

            // 3. Radial Generation Loop
            for (int step = 0; step < totalSteps; step++) {
                // Current angle in radians
                double angle = (step / (double) totalSteps) * Math.PI * 2.0;

                // Number of asteroids to spawn at this specific angular slice
                int density = random.nextInt(5, 12);

                for (int i = 0; i < density; i++) {
                    // Add jitter to the radius so it's a thick band, not a thin line
                    double radialJitter = (random.nextDouble() - 0.5) * wallThickness;
                    double finalRadius = boundaryRadius + radialJitter;

                    // Polar to Cartesian conversion
                    double ax = centerX + Math.cos(angle) * finalRadius;
                    double ay = centerY + Math.sin(angle) * finalRadius;

                    // Slightly randomize the angle for each rock in the slice to prevent "spoke" patterns
                    double angleJitter = (random.nextDouble() - 0.5) * 0.05;
                    double finalX = centerX + Math.cos(angle + angleJitter) * finalRadius;
                    double finalY = centerY + Math.sin(angle + angleJitter) * finalRadius;

                    spawnAsteroidAt(finalX, finalY);
                }
            }
        }

        private void spawnAsteroidAt(double x, double y) {
            double radius = AsteroidSettings.MIN_SPLIT_SIZE + random.nextInt(AsteroidSettings.MAXPXSIZE);
            spawnAsteroidAtCustomSize(x, y, radius);
        }

        private void spawnAsteroidAtCustomSize(double x, double y, double radius) {
            AsteroidModel ast = new AsteroidModel(x, y, radius);
            ast.setLinearVelocity(workspaceVelocity);
            UniverseSpawner.getInstance().spawnEntity(ast);
        }
        
    /**
     * PROCEDURAL HELPER: spawnClump
     * * Spawns a high-density, localized cluster of diverse asteroids packed inside a
     * specific geometric boundary profile (Circle or Regular Hexagon) of a fixed radius.
     * Use this to create point-of-interest anomalies, dense maze nodes, or clearing obstructions.
     *
     * @param cx                 The absolute world X center coordinate of the cluster.
     * @param cy                 The absolute world Y center coordinate of the cluster.
     * @param clumpRadius        The outer radius boundary constraint of the cluster (typically 128.0).
     * @param useHexagonalLayout If true, shapes the outer boundary into a 6-sided polygon;
     * if false, defaults to a smooth radial distribution.
     */
    public void spawnClump(double cx, double cy, double clumpRadius, boolean useHexagonalLayout) {
        // Determine density profile: How many rocks can we fit inside this space?
        int rockCount = random.nextInt(6, 15);

        for (int i = 0; i < rockCount; i++) {
            // 1. Calculate a random direction angle sweeping across the full 360-degree radial span.
            double angle = Math.random() * Math.PI * 2.0;

            // 2. Establish a baseline distribution factor.
            double distributionFactor = Math.sqrt(Math.random());
            double targetRadius = clumpRadius * distributionFactor;

            // 3. Hexagonal Boundary Correction Math
            if (useHexagonalLayout) {
                // To clip a circle into a flat-topped hexagon, we must calculate the maximum allowable
                // radial distance at this specific angle based on 60-degree (PI / 3.0) rotational symmetries.
                double sectorAngle = angle % (Math.PI / 3.0);
                double localDeviation = sectorAngle - (Math.PI / 6.0);

                // Polar equation for a regular hexagon side boundary
                double maxHexRadius = (clumpRadius * Math.cos(Math.PI / 6.0)) / Math.cos(localDeviation);

                // Adjust our target radius so no asteroid exceeds the flat boundary edge of the hexagon
                targetRadius = Math.min(targetRadius, maxHexRadius);
            }

            // 4. Polar-to-Cartesian Mapping relative to our custom cluster center parameters.
            double ax = cx + Math.cos(angle) * targetRadius;
            double ay = cy + Math.sin(angle) * targetRadius;

            // 5. Scaled Sizing Logic
            double baseMinSize = AsteroidSettings.MIN_SPLIT_SIZE * 0.6;
            double variableSizeRange = (AsteroidSettings.MAXPXSIZE * 0.2);
            double calculatedRadius = baseMinSize + Math.random() * variableSizeRange;

            // 6. Kinematic Linkage
            spawnAsteroidAtCustomSize(ax, ay, calculatedRadius);
        }
    }
}