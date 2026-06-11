package uni.gaben.iscat.universe.entity.projectiles.shooters;

import uni.gaben.iscat.universe.entity.projectiles.ProjectileModel;
import uni.gaben.iscat.universe.entity.projectiles.ProjectileType;

import java.util.function.Consumer;

public class SpreadPatternShooter implements PatternShooter {
    private final int count;
    private final double spreadAngleRad;

    /**
     * @param count           Numero di proiettili nel ventaglio.
     * @param spreadAngleDeg  Ampiezza totale del ventaglio in gradi (es. 30°).
     */
    public SpreadPatternShooter(int count, double spreadAngleDeg) {
        this.count = count;
        this.spreadAngleRad = Math.toRadians(spreadAngleDeg);
    }

    @Override
    public void execute(Shooter<?> shooter, ProjectileType type, double angle, Consumer<ProjectileModel> customizer) {
        if (count <= 0) return;
        if (count == 1) {
            shooter.shoot(type, angle, customizer);
            return;
        }

        // Calcola l'angolo di partenza (metà a sinistra dell'asse di puntamento)
        double startAngle = angle - (spreadAngleRad / 2.0);
        double angleStep = spreadAngleRad / (count - 1);

        for (int i = 0; i < count; i++) {
            double currentAngle = startAngle + (i * angleStep);
            shooter.shoot(type, currentAngle, customizer);
        }
    }
}