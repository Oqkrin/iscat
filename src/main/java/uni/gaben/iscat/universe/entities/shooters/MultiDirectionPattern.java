package uni.gaben.iscat.universe.entities.shooters;

import uni.gaben.iscat.universe.entities.hardcoded.projectiles.ProjectileModel;
import uni.gaben.iscat.universe.entities.hardcoded.projectiles.ProjectileType;
import uni.gaben.iscat.universe.entities.hardcoded.projectiles.Shooter;

import java.util.function.Consumer;

/** spara un tipo di attacco in n direzioni  */
public class MultiDirectionPattern implements Pattern {

    private final int directions;
    private final double angleOffset;
    private final Pattern inner;

    /**
     * @param directions  Numero di direzioni in cui replicare l'attacco (es. 4 -> Croce).
     * @param angleOffset Offset angolare iniziale in radianti rispetto alla mira.
     * @param inner       Il pattern di attacco da eseguire in ogni singola direzione.
     */
    public MultiDirectionPattern(int directions, double angleOffset, Pattern inner) {
        this.directions = directions;
        this.angleOffset = angleOffset;
        this.inner = inner;
    }

    @Override
    public void execute(Shooter<?> shooter, ProjectileType type, double angle, Consumer<ProjectileModel> customizer) {
        if (directions <= 0 || inner == null) return;

        double angleStep = (2.0 * Math.PI) / directions;

        for (int i = 0; i < directions; i++) {
            // Calcola l'angolo per questa specifica direzione basandosi sull'asse di puntamento attuale
            double currentAngle = angle + angleOffset + (i * angleStep);

            // Inietta l'angolo calcolato nell'attacco interno
            inner.execute(shooter, type, currentAngle, customizer);
        }
    }
}