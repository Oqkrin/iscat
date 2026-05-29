package uni.gaben.iscat.universe.lib.implementations.attacks;

import uni.gaben.iscat.universe.lib.interfaces.model.AttackPattern;
import uni.gaben.iscat.universe.projectiles.Projectile;
import uni.gaben.iscat.universe.projectiles.ProjectileType;
import uni.gaben.iscat.universe.projectiles.Shooter;

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
    public void execute(Shooter<?> shooter, ProjectileType type, double angle, Consumer<Projectile> customizer) {
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