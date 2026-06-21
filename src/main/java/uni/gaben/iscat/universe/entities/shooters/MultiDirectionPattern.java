package uni.gaben.iscat.universe.entities.shooters;

import uni.gaben.iscat.universe.entities.projectiles.ProjectileModel;
import uni.gaben.iscat.universe.entities.projectiles.ProjectileType;
import uni.gaben.iscat.universe.entities.projectiles.Shooter;
import java.util.function.Consumer;

/**
 * Spara un tipo di attacco in n direzioni
 */
public class MultiDirectionPattern implements Pattern {

    /** Il numero totale di direzioni d'espansione in cui suddividere l'angolo giro. */
    private final int directions;

    /** Offset angolare statico (in radianti) da sommare all'orientamento di puntamento iniziale. */
    private final double angleOffset;

    /** Il pattern logico interno da rieseguire e orientare per ogni asse calcolato. */
    private final Pattern inner;

    /**
     * Costruisce un replicatore di pattern multi-direzionale.
     *
     * @param directions  Numero di assi radiali equidistanti in cui clonare l'attacco (es. 4 genera una disposizione a croce, 2 un attacco fronte/retro).
     * @param angleOffset Spostamento angolare iniziale in radianti per ruotare l'intera raggiera rispetto alla linea di mira.
     * @param inner       L'algoritmo di sventagliata o figura interna da proiettare lungo le macro-direzioni calcolate.
     */
    public MultiDirectionPattern(int directions, double angleOffset, Pattern inner) {
        this.directions = directions;
        this.angleOffset = angleOffset;
        this.inner = inner;
    }

    /**
     * Scompone e distribuisce la traiettoria di sparo delegandola al pattern interno.
     * <p>
     * Calcola la costante di passo angolare $\Delta\theta = \frac{2\pi}{\text{directions}}$ e avvia un ciclo iterativo.
     * Per ogni iterazione, mappa l'angolo risultante combinando l'orientamento della sorgente, l'offset locale
     * e lo spicchio radiale corrente, invocando infine la pipeline dell'oggetto {@code inner}.
     * </p>
     */
    @Override
    public void execute(Shooter<?> shooter, ProjectileType type, double angle, Consumer<ProjectileModel> customizer) {
        // Guardia di corto circuito: previene loop nulli o configurazioni prive di logica interna
        if (directions <= 0 || inner == null) return;

        double angleStep = (2.0 * Math.PI) / directions;

        for (int i = 0; i < directions; i++) {
            // Calcolo della traiettoria assoluta per la direzione corrente
            double currentAngle = angle + angleOffset + (i * angleStep);

            // Delega l'esecuzione della logica di sparo interna, iniettando il nuovo vettore di mira
            inner.execute(shooter, type, currentAngle, customizer);
        }
    }
}