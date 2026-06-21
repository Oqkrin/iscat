package uni.gaben.iscat.universe.entities.shooters;

import org.dyn4j.geometry.Vector2;
import uni.gaben.iscat.universe.entities.AbstractPhysicalEntityModel;
import uni.gaben.iscat.universe.entities.projectiles.ProjectileModel;
import uni.gaben.iscat.universe.entities.projectiles.ProjectileType;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.entities.projectiles.Shooter;
import java.util.function.Consumer;

/**
 * Spara n proiettili in una direzione allineati paralelamente
 */
public class ParallelLinePattern implements Pattern {

    /** Il numero totale di proiettili paralleli da generare per ogni singola salva. */
    private final int count;

    /** Distanza di spaziatura laterale tra due proiettili consecutivi convertita in metri del mondo fisico. */
    private final double spacingMeters;

    /**
     * Costruisce un generatore di colpi paralleli.
     *
     * @param count     Numero di proiettili che compongono la linea di sbarramento.
     * @param spacingPx Intervallo lineare di spaziatura intercorrente tra i colpi, espresso in pixel.
     */
    public ParallelLinePattern(int count, double spacingPx) {
        this.count = count;
        this.spacingMeters = UU.pxToM(spacingPx); // Conversione immediata nel sistema metrico di dyn4j
    }

    /**
     * Calcola le coordinate di spawn e avvia la sfilza di proiettili paralleli.
     * <p>
     * Trova il vettore frontale di sicurezza, distribuisce simmetricamente i punti di origine
     * traslandoli lungo l'asse ortogonale passante per il centro e invoca il metodo di sparo posizionale.
     * </p>
     */
    @Override
    public void execute(Shooter<?> shooter, ProjectileType type, double angle, Consumer<ProjectileModel> customizer) {
        if (count <= 0) return;

        Vector2 origin = shooter.getModel().getTransform().getTranslation();

        // Calcolo dell'angolo ortogonale (+90°) per ricavare la retta di sfasamento laterale (Perpendicular Axis)
        double perpAngle = angle + (Math.PI / 2.0);

        // Determina la distanza di proiezione frontale per evitare l'autocollisione con la propria fixture
        double forwardDistance = 0.1;
        if (shooter.getModel() instanceof AbstractPhysicalEntityModel aem) {
            forwardDistance = aem.getHeightMeters() / 2.0;
        }

        // Calcola l'estensione totale del fronte di fuoco e il punto di ancoraggio iniziale sinistro (startOffset)
        double totalWidth = (count - 1) * spacingMeters;
        double startOffset = -totalWidth / 2.0;

        // Iterazione per la composizione e lo spawn della retta di proiettili
        for (int i = 0; i < count; i++) {
            double currentOffset = startOffset + (i * spacingMeters);

            // Trigonometria vettoriale accoppiata: combina il vettore normale avanti con il componente laterale ortogonale
            Vector2 spawnPos = new Vector2(
                    origin.x + Math.cos(angle) * forwardDistance + Math.cos(perpAngle) * currentOffset,
                    origin.y + Math.sin(angle) * forwardDistance + Math.sin(perpAngle) * currentOffset
            );

            // Genera il proiettile nella coordinata esatta mantenendo l'angolo di traiettoria parallelo all'asse centrale
            shooter.shoot(type, spawnPos, angle, customizer);
        }
    }
}