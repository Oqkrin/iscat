package uni.gaben.iscat.universe.entities.shooters;

import uni.gaben.iscat.universe.entities.projectiles.ProjectileModel;
import uni.gaben.iscat.universe.entities.projectiles.ProjectileType;
import uni.gaben.iscat.universe.entities.projectiles.Shooter;
import java.util.function.Consumer;

/**
 * Spara una figura fatta da n proiettili come attacco ad area
 */
public class FigurePattern implements Pattern {

    private final int count;

    /**
     * Tipi di geometrie procedurali disponibili per l'espansione dell'ondata.
     */
    public enum FigureType {
        NONE,
        CIRCLE,
        SQUARE,
        TRIANGLE,
        STAR
    }
    private FigureType type;

    /**
     * Costruisce un generatore di pattern geometrico.
     *
     * @param count Numero totale di proiettili che compongono la figura (maggiore è il numero, più definita sarà la forma).
     * @param type  La tipologia di figura geometrica da proiettare nello spazio.
     */
    public FigurePattern(int count, FigureType type) {
        this.count = count;
        this.type = type;
    }

    /**
     * Esegue l'algoritmo di sparo radiale distribuitivo.
     * <p>
     * Calcola il delta angolare regolare $\Delta\theta = \frac{2\pi}{\text{count}}$, interroga la funzione
     * di calcolo polare per ottenere il moltiplicatore di velocità e istanzia i proiettili tramite il modulo {@link Shooter}.
     * </p>
     */
    @Override
    public void execute(Shooter<?> shooter, ProjectileType bulletType, double angle, Consumer<ProjectileModel> customizer) {
        if (count <= 0) return;

        double angleStep = (2.0 * Math.PI) / count;

        for (int i = 0; i < count; i++) {
            // Calcolo dell'angolo assoluto del proiettile corrente (incluso l'offset di orientamento dell'emettitore)
            double currentAngle = angle + (i * angleStep);

            // Estrazione del coefficiente moltiplicativo posizionale dalla funzione polare
            double geometricFactor = calculateGeometricFactor(currentAngle);

            // Decorazione funzionale del proiettile: incapsula il customizer utente e applica la deformazione cinetica
            Consumer<ProjectileModel> figureCustomizer = bullet -> {
                if (customizer != null) {
                    customizer.accept(bullet);
                }
                // Altera la velocità per distorcere la sfericità dell'onda di espansione
                bullet.setTerminalVelocity(bullet.getTerminalVelocity() * geometricFactor);
            };

            // Iniezione del colpo nel motore di gioco
            shooter.shoot(bulletType, currentAngle, figureCustomizer);
        }
    }

    /**
     * Risolutore matematico polare per il calcolo del raggio locale della figura ($\rho(\theta)$).
     * <p>
     * Determina la coordinata radiale necessaria a mantenere i bordi della figura allineati o modellati:
     * </p>
     * <ul>
     * <li><b>SQUARE:</b> Sfrutta la parametrizzazione dell'equazione del quadrato in coordinate polari:
     * $\rho(\theta) = \frac{1}{\max(|\cos\theta|, |\sin\theta|)}$.</li>
     * <li><b>TRIANGLE:</b> Riduce l'angolo in un intervallo periodico di $\frac{2\pi}{3}$ radianti (120°) per calcolare l'ipotenusa locale dei tre lati dritti.</li>
     * <li><b>STAR:</b> Genera una sinusoide armonica con frequenza a 5 punte, modulando la profondità delle rientranze.</li>
     * <li><b>CIRCLE:</b> Restituisce un fattore unitario costante ($1.0$), mantenendo l'espansione perfettamente isotropa.</li>
     * </ul>
     *
     * @param angle L'angolo radiante corrente ($\theta$) da analizzare.
     * @return Il fattore di scala geometrico adimensionale applicabile alla velocità cinetica.
     */
    private double calculateGeometricFactor(double angle) {
        switch (type) {
            case NONE:
                System.err.println("[FigurePattern] Devi selezionare una figura geometrica!");
                return 1.0;

            case SQUARE:
                return 1.0 / Math.max(Math.abs(Math.cos(angle)), Math.abs(Math.sin(angle)));

            case TRIANGLE:
                // Normalizzazione dell'angolo nell'intervallo [0, 2pi) per prevenire anomalie con angoli negativi
                double normalizedAngle = ((angle % (2.0 * Math.PI)) + (2.0 * Math.PI)) % (2.0 * Math.PI);
                double triStep = 2.0 * Math.PI / 3.0; // Angolo interno tra i vertici di un triangolo equilatero
                double localAngle = (normalizedAngle % triStep) - (triStep / 2.0);
                return 0.5 / Math.cos(localAngle);

            case STAR:
                double starFrequency = 5.0; // Numero di punte della stella
                double depth = 0.4;          // Coefficiente di rientranza interna dei vertici minimi
                return 1.0 - depth * Math.abs(Math.sin((starFrequency * angle) / 2.0));

            case CIRCLE:
            default:
                return 1.0;
        }
    }
}