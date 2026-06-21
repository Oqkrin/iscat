package uni.gaben.iscat.universe.entities.shooters;

import uni.gaben.iscat.universe.entities.projectiles.ProjectileModel;
import uni.gaben.iscat.universe.entities.projectiles.ProjectileType;
import uni.gaben.iscat.universe.entities.projectiles.Shooter;
import java.util.function.Consumer;

/**
 * Un attacco spread
 */
public class SpreadPattern implements Pattern {

    /** Il numero totale di proiettili che compongono il ventaglio. */
    private final int count;

    /** L'ampiezza totale del cono di svasatura espressa in radianti nel mondo fisico. */
    private final double spreadAngleRad;

    /**
     * Costruisce un generatore di sventagliata conica.
     *
     * @param count          Numero complessivo di colpi da distribuire nell'arco di fuoco.
     * @param spreadAngleDeg Ampiezza angolare complessiva del ventaglio espressa in gradi sessagesimali (es. **30°**).
     */
    public SpreadPattern(int count, double spreadAngleDeg) {
        this.count = count;
        this.spreadAngleRad = Math.toRadians(spreadAngleDeg); // Conversione immediata nell'unità radiometrica standard
    }

    /**
     * Calcola la scomposizione angolare del settore e innesca la salva a raggiera.
     * <p>
     * Gestisce i casi limite (quantità nulle o colpo singolo atomico) ed esegue l'interpolazione
     * lineare dei vettori di traiettoria per ciascun proiettile dell'ondata.
     * </p>
     */
    @Override
    public void execute(Shooter<?> shooter, ProjectileType type, double angle, Consumer<ProjectileModel> customizer) {
        // Guardie di corto circuito per la gestione dei casi limite e l'ottimizzazione delle risorse
        if (count <= 0) return;
        if (count == 1) {
            // Se vi è un solo proiettile, degrada automaticamente a un colpo singolo centrato sulla mira
            shooter.shoot(type, angle, customizer);
            return;
        }

        // Calcola l'angolo di partenza sul margine sinistro del cono rispetto all'asse centrale
        double startAngle = angle - (spreadAngleRad / 2.0);

        // Calcola l'ampiezza di ogni singolo spicchio angolare intercorrente tra due proiettili adiacenti
        double angleStep = spreadAngleRad / (count - 1);

        // Iterazione per la proiezione e lo spawn della raggiera di colpi
        for (int i = 0; i < count; i++) {
            double currentAngle = startAngle + (i * angleStep);

            // Invocazione dello spawn balistico tramite il modulo Shooter delegato
            shooter.shoot(type, currentAngle, customizer);
        }
    }
}