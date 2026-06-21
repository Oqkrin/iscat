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

    /**
     * Costruisce un'istanza del generatore procedurale inizializzando i vettori di workspace.
     */
    public AsteroidMazeGenerator() {}

    /**
     * Genera una barriera circolare perimetrale di asteroidi attorno al centro del mondo di gioco.
     * <p>
     * Calcola la circonferenza teorica del confine e la suddivide in passi angolari costanti.
     * In ogni passo applica un fattore di jitter casuale (radiale e angolare) all'interno dello spessore
     * della parete per spezzare l'artificiosità geometrica e distribuire la densità dei detriti.
     * </p>
     *
     * @param centerX Coordinata X assoluta del centro dell'universo.
     * @param centerY Coordinata Y assoluta del centro dell'universo.
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
            // Distribuzione della frazione angolare sull'intero arco di 360 gradi
            double angle = (step / (double) totalSteps) * Math.PI * 2.0;
            int density = random.nextInt(5, 12);

            for (int i = 0; i < density; i++) {
                // Applicazione del Jitter per creare imperfezioni naturali lungo la faglia del perimetro
                double radialJitter = (random.nextDouble() - 0.5) * wallThickness;
                double finalRadius = boundaryRadius + radialJitter;
                double angleJitter = (random.nextDouble() - 0.5) * 0.05;

                // Trasformazione da coordinate polari a coordinate cartesiane locali
                double finalX = centerX + Math.cos(angle + angleJitter) * finalRadius;
                double finalY = centerY + Math.sin(angle + angleJitter) * finalRadius;

                spawnAsteroidAt(finalX, finalY);
            }
        }
    }

    /**
     * Calcola una dimensione casuale compresa nei limiti di configurazione e istanzia l'asteroide.
     */
    private void spawnAsteroidAt(double x, double y) {
        double radius = AsteroidSettings.MIN_SPLIT_SIZE + random.nextInt(AsteroidSettings.MAXPXSIZE);
        spawnAsteroidAtCustomSize(x, y, radius);
    }

    /**
     * Alloca, configura cinematicamente e inserisce l'asteroide nel sistema di tracciamento dello spawner.
     */
    private void spawnAsteroidAtCustomSize(double x, double y, double radius) {
        AsteroidModel ast = new AsteroidModel(x, y, radius);
        ast.setLinearVelocity(workspaceVelocity); // Utilizza il vettore riciclato statico a velocità zero
        UniverseSpawner.getInstance().spawnEntity(ast);
    }

    /**
     * Genera un cluster (clump) localizzato ad alta densità con profilo geometrico circolare o esagonale.
     * <p>
     * Utilizza una distribuzione basata sulla radice quadrata per garantire l'uniformità radiale dell'area.
     * Se è richiesto il layout esagonale, l'algoritmo applica la formula polare del poligono regolare a 6 facce,
     * effettuando il clipping del raggio massimo per appiattire i bordi sferici entro simmetrie di 60° ($\frac{\pi}{3}$).
     * </p>
     *
     * @param cx                 Coordinata X del baricentro del cluster.
     * @param cy                 Coordinata Y del baricentro del cluster.
     * @param clumpRadius        Il raggio di contenimento esterno massimo del cluster.
     * @param useHexagonalLayout Se {@code true}, forza il perimetro esterno a collassare in un esagono regolare.
     */
    public void spawnClump(double cx, double cy, double clumpRadius, boolean useHexagonalLayout) {
        int rockCount = random.nextInt(6, 15);

        for (int i = 0; i < rockCount; i++) {
            double angle = Math.random() * Math.PI * 2.0;

            // La radice quadrata corregge l'addensamento innaturale verso il centro tipico del random polare puro
            double distributionFactor = Math.sqrt(Math.random());
            double targetRadius = clumpRadius * distributionFactor;

            // --- Calcolo della Correzione di Confine Esagonale ---
            if (useHexagonalLayout) {
                // Isola l'angolo del settore corrente (0-60 gradi) e calcola la deviazione dall'apotema (30 gradi)
                double sectorAngle = angle % (Math.PI / 3.0);
                double localDeviation = sectorAngle - (Math.PI / 6.0);

                // Equazione polare del confine lineare esagonale: r = (R * cos(30°)) / cos(deviazione)
                double maxHexRadius = (clumpRadius * Math.cos(Math.PI / 6.0)) / Math.cos(localDeviation);

                // Esegue il clipping restrittivo per non far debordare l'asteroide dai lati piatti dell'esagono
                targetRadius = Math.min(targetRadius, maxHexRadius);
            }

            // Mappatura polare-cartesiana proiettata dal centro del cluster
            double ax = cx + Math.cos(angle) * targetRadius;
            double ay = cy + Math.sin(angle) * targetRadius;

            // Calcolo del dimensionamento scalato per i detriti del cluster
            double baseMinSize = AsteroidSettings.MIN_SPLIT_SIZE * 0.6;
            double variableSizeRange = (AsteroidSettings.MAXPXSIZE * 0.2);
            double calculatedRadius = baseMinSize + Math.random() * variableSizeRange;

            spawnAsteroidAtCustomSize(ax, ay, calculatedRadius);
        }
    }
}