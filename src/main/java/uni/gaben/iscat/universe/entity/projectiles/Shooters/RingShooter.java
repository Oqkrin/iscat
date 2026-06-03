package uni.gaben.iscat.universe.entity.projectiles.Shooters;


import uni.gaben.iscat.universe.entity.projectiles.Projectile;
import uni.gaben.iscat.universe.entity.projectiles.ProjectileType;
import uni.gaben.iscat.universe.entity.projectiles.Shooter;

import java.util.function.Consumer;

/** Spara n proiettili in ogni direzione a farma d'anello */
public class RingShooter implements ShooterPattern {

    private final int count;

    /**
     * @param count Numero totale di proiettili distribuiti nell'anello a 360°.
     */
    public RingShooter(int count) {
        this.count = count;
    }

    @Override
    public void execute(Shooter<?> shooter, ProjectileType type, double angle, Consumer<Projectile> customizer) {
        if (count <= 0) return;

        double angleStep = (2.0 * Math.PI) / count;

        for (int i = 0; i < count; i++) {
            double currentAngle = angle + (i * angleStep);
            shooter.shoot(type, currentAngle, customizer);
        }
    }
}