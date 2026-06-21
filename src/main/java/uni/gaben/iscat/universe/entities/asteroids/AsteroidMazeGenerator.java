package uni.gaben.iscat.universe.entities.asteroids;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.UniverseModel;
import uni.gaben.iscat.universe.UniverseSettings;
import uni.gaben.iscat.universe.spawn.UniverseSpawner;
import java.util.Random;

/**
 * Generatore procedurale di campi di asteroidi (Asteroid Maze Generator).
 * <p>
 * Gestisce la creazione di barriere perimetrali esterne e l'iniezione locale di agglomerati
 * geometrici ad alta densità (clump) per strutturare la mappa di gioco o creare nodi di navigazione.
 * </p>
 * <p>
 * <b>Ottimizzazione Memory-Footprint:</b> Utilizza un vettore di workspace riutilizzabile
 * ({@link Vector2}) per i calcoli cinematici, riducendo a zero le allocazioni sullo heap e i picchi del Garbage Collector.
 * </p>
 */
public class AsteroidMazeGenerator {

    private final Random random = new Random();

    /** Vettore di supporto pre-allocato per azzerare l'instanziazione di oggetti cinetici nel ciclo di spawn. */
    private final Vector2 workspaceVelocity = new Vector2();

    public AsteroidMazeGenerator() {}

    public void generate(double centerX, double centerY) {
        UniverseModel universe = UniverseSpawner.getInstance().getUniverseModel();
        double boundaryRadius = universe != null
                ? universe.getUniverseRadius() * 12
                : UniverseSettings.DEFAULT_WIDTH / 2.0 * 0.9;

        double wallThickness = 300.0;
        double circumference = 2 * Math.PI * boundaryRadius;

        double angularStepSize = 250.0;
        int totalSteps = (int) (circumference / angularStepSize);

        // Genera la barriera perimetrale
        for (int step = 0; step < totalSteps; step++) {
            double angle = (step / (double) totalSteps) * Math.PI * 2.0;

            int density = random.nextInt(3, 7);

            for (int i = 0; i < density; i++) {
                double radialJitter = (random.nextDouble() - 0.5) * wallThickness;
                double finalRadius = boundaryRadius + radialJitter;
                double angleJitter = (random.nextDouble() - 0.5) * 0.05;

                double finalX = centerX + Math.cos(angle + angleJitter) * finalRadius;
                double finalY = centerY + Math.sin(angle + angleJitter) * finalRadius;

                spawnAsteroidAt(finalX, finalY);
            }
        }

        int interiorAsteroidsCount = (int)(boundaryRadius * boundaryRadius * Math.PI / 2500000.0);

        for (int i = 0; i < interiorAsteroidsCount; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0;
            double radius = Math.sqrt(random.nextDouble()) * (boundaryRadius - wallThickness / 2.0);

            double finalX = centerX + Math.cos(angle) * radius;
            double finalY = centerY + Math.sin(angle) * radius;

            spawnAsteroidAt(finalX, finalY);
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

    public void spawnClump(double cx, double cy, double clumpRadius, boolean useHexagonalLayout) {
        int rockCount = random.nextInt(6, 15);

        for (int i = 0; i < rockCount; i++) {
            double angle = Math.random() * Math.PI * 2.0;
            double distributionFactor = Math.sqrt(Math.random());
            double targetRadius = clumpRadius * distributionFactor;

            if (useHexagonalLayout) {
                double sectorAngle = angle % (Math.PI / 3.0);
                double localDeviation = sectorAngle - (Math.PI / 6.0);
                double maxHexRadius = (clumpRadius * Math.cos(Math.PI / 6.0)) / Math.cos(localDeviation);
                targetRadius = Math.min(targetRadius, maxHexRadius);
            }

            double ax = cx + Math.cos(angle) * targetRadius;
            double ay = cy + Math.sin(angle) * targetRadius;

            double baseMinSize = AsteroidSettings.MIN_SPLIT_SIZE * 0.6;
            double variableSizeRange = (AsteroidSettings.MAXPXSIZE * 0.2);
            double calculatedRadius = baseMinSize + Math.random() * variableSizeRange;

            spawnAsteroidAtCustomSize(ax, ay, calculatedRadius);
        }
    }
}