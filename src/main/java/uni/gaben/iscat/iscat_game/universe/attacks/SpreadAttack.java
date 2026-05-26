package uni.gaben.iscat.iscat_game.universe.attacks;

import uni.gaben.iscat.iscat_game.universe.projectiles.Projectile;
import uni.gaben.iscat.iscat_game.universe.projectiles.Shooter;

import java.util.function.Consumer;

public class SpreadAttack implements AttackPattern {
    private final int count;
    private final double spreadAngleRad;

    /**
     * @param count           Numero di proiettili nel ventaglio.
     * @param spreadAngleDeg  Ampiezza totale del ventaglio in gradi (es. 30°).
     */
    public SpreadAttack(int count, double spreadAngleDeg) {
        this.count = count;
        this.spreadAngleRad = Math.toRadians(spreadAngleDeg);
    }

    @Override
    public void execute(Shooter<?> shooter, Projectile template, double angle, Consumer<Projectile> customizer) {
        if (count <= 0) return;
        if (count == 1) {
            shooter.shoot(template, angle, customizer);
            return;
        }

        // Calcola l'angolo di partenza (metà a sinistra dell'asse di puntamento)
        double startAngle = angle - (spreadAngleRad / 2.0);
        double angleStep = spreadAngleRad / (count - 1);

        for (int i = 0; i < count; i++) {
            double currentAngle = startAngle + (i * angleStep);
            shooter.shoot(template, currentAngle, customizer);
        }
    }
}