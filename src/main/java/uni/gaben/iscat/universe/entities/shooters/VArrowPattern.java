package uni.gaben.iscat.universe.entities.shooters;

import org.dyn4j.geometry.Vector2;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import uni.gaben.iscat.universe.entities.AbstractPhysicalEntityModel;
import uni.gaben.iscat.universe.entities.projectiles.ProjectileModel;
import uni.gaben.iscat.universe.entities.projectiles.ProjectileType;
import uni.gaben.iscat.universe.UU;
import uni.gaben.iscat.universe.entities.projectiles.Shooter;

import java.util.function.Consumer;

/**
 * Gestisce lo sparo a forma di V capovolta (freccia ^).
 * Il proiettile al centro parte subito, quelli ai lati partono dopo.
 */
public class VArrowPattern implements Pattern {

    private final int count;
    private final double spacingMeters;
    private final double intervalSeconds;

    /**
     * Crea il pattern a V capovolta.
     *
     * @param count           Numero di proiettili totali.
     * @param spacingPx       Spazio orizzontale tra un proiettile e l'altro (in pixel).
     * @param intervalSeconds Tempo di attesa prima di sparare le coppie laterali.
     */
    public VArrowPattern(int count, double spacingPx, double intervalSeconds) {
        this.count = (count % 2 == 0) ? count + 1 : count;
        this.spacingMeters = UU.pxToM(spacingPx);
        this.intervalSeconds = intervalSeconds;
    }

    /**
     * Esegue l'attacco calcolando i tempi e i punti di spawn dei proiettili.
     */
    @Override
    public void execute(Shooter<?> shooter, ProjectileType type, double angle, Consumer<ProjectileModel> customizer) {
        if (count <= 0) return;

        Vector2 origin = shooter.getModel().getTransform().getTranslation();
        double perpAngle = angle + (Math.PI / 2.0);

        double forwardDistance = 0.1;
        if (shooter.getModel() instanceof AbstractPhysicalEntityModel aem) {
            forwardDistance = aem.getHeightMeters() / 2.0;
        }

        // Sparo immediato del proiettile centrale
        Vector2 centerSpawn = new Vector2(
                origin.x + Math.cos(angle) * forwardDistance,
                origin.y + Math.sin(angle) * forwardDistance
        );
        shooter.shoot(type, centerSpawn, angle, customizer);

        int totalPairs = count / 2;

        // Sparo temporizzato per le coppie laterali
        for (int step = 1; step <= totalPairs; step++) {
            double currentDelay = step * intervalSeconds;
            double lateralOffset = step * spacingMeters;

            Vector2 leftSpawn = new Vector2(
                    origin.x + Math.cos(angle) * forwardDistance + Math.cos(perpAngle) * (-lateralOffset),
                    origin.y + Math.sin(angle) * forwardDistance + Math.sin(perpAngle) * (-lateralOffset)
            );

            Vector2 rightSpawn = new Vector2(
                    origin.x + Math.cos(angle) * forwardDistance + Math.cos(perpAngle) * lateralOffset,
                    origin.y + Math.sin(angle) * forwardDistance + Math.sin(perpAngle) * lateralOffset
            );

            PauseTransition pause = new PauseTransition(Duration.seconds(currentDelay));
            pause.setOnFinished(event -> {
                shooter.shoot(type, leftSpawn, angle, customizer);
                shooter.shoot(type, rightSpawn, angle, customizer);
            });
            pause.play();
        }
    }
}