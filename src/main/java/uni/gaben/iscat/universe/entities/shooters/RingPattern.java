package uni.gaben.iscat.universe.entities.shooters;

import uni.gaben.iscat.universe.entities.projectiles.ProjectileModel;
import uni.gaben.iscat.universe.entities.projectiles.ProjectileType;
import uni.gaben.iscat.universe.entities.projectiles.Shooter;
import java.util.function.Consumer;

/**
 * Spara in n direzioni
 */
public class RingPattern implements Pattern {

    /** Il numero totale di proiettili da distribuire uniformemente lungo la circonferenza dell'anello. */
    private final int count;

    /**
     * Costruisce un generatore di pattern ad anello.
     *
     * @param count Numero complessivo di colpi che compongono l'anello radiale.
     */
    public RingPattern(int count) {
        this.count = count;
    }

    /**
     * Calcola la suddivisione angolare e innesca la pioggia radiale di proiettili.
     * <p>
     * Calcola il delta angolare costante $\Delta\theta = \frac{2\pi}{\text{count}}$ e, partendo dall'angolo
     * di orientamento nominale dell'emettitore, proietta e istanzia i singoli colpi nel mondo.
     * </p>
     */
    @Override
    public void execute(Shooter<?> shooter, ProjectileType type, double angle, Consumer<ProjectileModel> customizer) {
        // Guardia di corto circuito: previene l'avvio di cicli iterativi con quantità nulle o negative
        if (count <= 0) return;

        double angleStep = (2.0 * Math.PI) / count;

        for (int i = 0; i < count; i++) {
            // Calcolo dell'asse di traiettoria assoluto per il proiettile corrente dell'anello
            double currentAngle = angle + (i * angleStep);

            // Invocazione dello spawn balistico tramite il modulo Shooter delegato
            shooter.shoot(type, currentAngle, customizer);
        }
    }
}