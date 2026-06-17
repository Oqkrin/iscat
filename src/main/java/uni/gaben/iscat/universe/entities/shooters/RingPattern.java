package uni.gaben.iscat.universe.entities.shooters;


import uni.gaben.iscat.universe.entities.hardcoded.projectiles.ProjectileModel;
import uni.gaben.iscat.universe.entities.hardcoded.projectiles.ProjectileType;

import java.util.function.Consumer;

/** Spara n proiettili in ogni direzione a farma d'anello */
public class RingPattern implements Pattern {

    private final int count;

    /**
     * @param count Numero totale di proiettili distribuiti nell'anello a 360°.
     */
    public RingPattern(int count) {
        this.count = count;
    }

    @Override
    public void execute(Shooter<?> shooter, ProjectileType type, double angle, Consumer<ProjectileModel> customizer) {
        if (count <= 0) return;

        double angleStep = (2.0 * Math.PI) / count;

        for (int i = 0; i < count; i++) {
            double currentAngle = angle + (i * angleStep);
            shooter.shoot(type, currentAngle, customizer);
        }
    }
}