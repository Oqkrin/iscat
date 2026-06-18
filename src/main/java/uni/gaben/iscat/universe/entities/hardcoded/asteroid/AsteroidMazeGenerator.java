package uni.gaben.iscat.universe.entities.hardcoded.asteroid;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.UniverseSettings;
import uni.gaben.iscat.universe.spawn.UniverseSpawner;

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
            UniverseModel universe = UniverseSpawner.getInstance().getUniverseModel();
            double boundaryRadius = universe != null
                    ? universe.getUniverseRadius() * 12
                    : UniverseSettings.DEFAULT_WIDTH / 2.0 * 0.9;

            double wallThickness = 1000.0;
            double circumference = 2 * Math.PI * boundaryRadius;
            double angularStepSize = 250.0;
            int totalSteps = (int) (circumference / angularStepSize);

            for (int step = 0; step < totalSteps; step++) {
                double angle = (step / (double) totalSteps) * Math.PI * 2.0;
                int density = random.nextInt(5, 12);

                for (int i = 0; i < density; i++) {
                    double radialJitter = (random.nextDouble() - 0.5) * wallThickness;
                    double finalRadius = boundaryRadius + radialJitter;
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